package com.rngreatcards.parser

class SmsParser(
    private val normalizer: SmsNormalizer = SmsNormalizer(),
    private val bankResolver: BankResolver = BankResolver(),
    private val currencyExtractor: CurrencyExtractor = CurrencyExtractor(),
    private val merchantExtractor: MerchantExtractor = MerchantExtractor(),
    private val dateExtractor: DateExtractor = DateExtractor(),
    private val cardExtractor: CardExtractor = CardExtractor(),
) {
    fun parseSms(samples: List<String>): List<ParsedResult> = samples.map(::parseOne)

    fun parseOne(rawSms: String): ParsedResult {
        val normalized = normalizer.normalize(rawSms)

        detectEarlyExclusion(normalized)?.let { reason ->
            return exclude(rawSms, reason)
        }

        val includeType = detectIncludeCandidate(normalized)
        if (includeType == null) {
            return if (isMalformed(normalized)) {
                exclude(rawSms, ExcludeReason.MALFORMED_SMS)
            } else {
                exclude(rawSms, ExcludeReason.LOW_CONFIDENCE)
            }
        }

        val bank = bankResolver.resolve(normalized)
        val amountCandidates = currencyExtractor.extractCandidates(normalized, includeType)
        val amountMatch = amountCandidates.maxByOrNull { it.score }
        val cardLastFour = cardExtractor.extract(normalized)
        val merchant = merchantExtractor.extract(normalized, includeType)
        val date = dateExtractor.extract(normalized)

        if (amountMatch == null || amountMatch.currency == null) {
            return if (isMalformed(normalized)) {
                exclude(rawSms, ExcludeReason.MALFORMED_SMS)
            } else {
                exclude(rawSms, ExcludeReason.LOW_CONFIDENCE)
            }
        }

        val signals = IncludeSignals(
            explicitCreditCard = normalized.compact.contains("credit card"),
            genericCardWithLimit = hasGenericCardWithCreditSignals(normalized),
            bankResolved = bank != null,
            amountExtracted = true,
            currencyExtracted = true,
            merchantExtracted = merchant != null,
            cardExtracted = cardLastFour != null,
            dateExtracted = date != null,
            multipleAmounts = amountCandidates.size > 1,
            accountAndCardSignalsMixed = hasAccountSignals(normalized) && hasCardSignals(normalized),
            refundAmbiguous = includeType == TransactionType.REFUND && !normalized.compact.contains("refund"),
        )

        val transaction = TransactionData(
            amount = amountMatch.amount,
            currency = amountMatch.currency,
            bank = bank ?: "Unknown Issuer",
            cardLastFour = cardLastFour,
            merchant = merchant,
            type = includeType,
            date = date,
        )

        return ParsedResult(
            rawSms = rawSms,
            decision = ParseDecision.INCLUDE,
            excludeReason = null,
            transaction = transaction,
            confidence = scoreInclude(signals),
        )
    }

    private fun detectEarlyExclusion(normalizedSms: NormalizedSms): ExcludeReason? {
        val text = normalizedSms.compact

        if (containsAny(text, listOf("otp", "one time password")) ||
            (text.contains("valid for") && text.contains("do not share")) ||
            text.contains("net banking login")
        ) {
            return ExcludeReason.OTP
        }

        if (containsAny(text, listOf("offer", "cashback", "flat 50% off", "discount", "t&c apply", "this weekend", "visit ")) &&
            !text.contains("refund of")
        ) {
            return ExcludeReason.OFFER
        }

        if (containsAny(text, listOf("activated", "pin generated", "set pin", "card dispatched", "card delivered", "kyc", "profile updated", "mobile number updated", "email updated", "address updated"))) {
            return ExcludeReason.SERVICE_MESSAGE
        }

        if (containsAny(text, listOf("credit limit enhanced", "credit limit increased", "limit increased", "limit decreased", "temporary limit", "cash limit", "available credit limit updated"))) {
            return ExcludeReason.LIMIT_UPDATE
        }

        if (containsAny(text, listOf("declined", "failed", "unsuccessful", "insufficient credit limit", "insufficient limit"))) {
            return ExcludeReason.DECLINED
        }

        if (containsAny(text, listOf("pre-auth", "pre authorization", "pre-authorisation", "pre-authorization", "authorization hold", "amount blocked", "temporary hold", "hold placed"))) {
            return ExcludeReason.AUTH_HOLD
        }

        if (containsAny(text, listOf("will be auto debited", "will be debited", "e-mandate", "scheduled", "upcoming")) ||
            (text.contains("please maintain sufficient limit") && !text.contains("spent"))
        ) {
            return ExcludeReason.FUTURE_AUTO_DEBIT
        }

        if (containsAny(text, listOf("bill of", "bill is due", "payment due", "minimum amount due", "total amount due", "statement generated", "statement is ready")) ||
            (text.contains("due on") && text.contains("credit card"))
        ) {
            return ExcludeReason.BILL_DUE
        }

        if (containsAny(text, listOf("payment received towards your", "payment of", "thank you")) &&
            containsAny(text, listOf("credit card", "towards your", "towards card"))
        ) {
            return ExcludeReason.CARD_PAYMENT
        }

        if (containsAny(text, listOf("converted to emi", "emi of rs", "emi of inr", "/month")) && text.contains("months")) {
            return ExcludeReason.EMI_CONVERSION
        }

        if (containsAny(text, listOf("finance charge", "late payment fee", "annual fee", "joining fee", "interest charged")) ||
            (text.contains("charge of") && text.contains("gst")) ||
            (text.contains("debited from your") && text.contains("late payment"))
        ) {
            return ExcludeReason.FEE_OR_CHARGE
        }

        if (text.contains("debit card") || text.contains("atm card")) {
            return ExcludeReason.DEBIT_CARD
        }

        if (containsUpiBankAccountSignals(normalizedSms)) {
            return ExcludeReason.UPI_BANK_ACCOUNT
        }

        if (containsAny(text, listOf("avl bal", "available balance", "balance in your a/c"))) {
            return ExcludeReason.BALANCE_ALERT
        }

        if (containsSavingsAccountSignals(normalizedSms)) {
            return ExcludeReason.SAVINGS_ACCOUNT
        }

        if (containsAny(text, listOf("sip", "mutual fund", "folio", "asset fund"))) {
            return ExcludeReason.INVESTMENT
        }

        if (containsAny(text, listOf("premium", "insurance", "policy", "renewal"))) {
            return ExcludeReason.INSURANCE
        }

        if (containsAny(text, listOf("reward points earned", "cashback earned", "cashback credited", "voucher", "coupon", "miles earned"))) {
            return ExcludeReason.REWARD_OR_CASHBACK
        }

        return null
    }

    private fun detectIncludeCandidate(normalizedSms: NormalizedSms): TransactionType? {
        val text = normalizedSms.compact

        val isRefund = (text.contains("refund of") || text.contains("reversal")) &&
            containsAny(text, listOf("credited", "credited to your")) &&
            hasCardSignals(normalizedSms)
        if (isRefund) {
            return TransactionType.REFUND
        }

        val spentLike = containsAny(text, listOf("spent", "spent using", "you've spent"))
        val cardLike = text.contains("credit card") ||
            hasGenericCardWithCreditSignals(normalizedSms) ||
            hasNamedIssuerCardSignal(normalizedSms)
        if (spentLike && cardLike && !hasAccountSignals(normalizedSms)) {
            return TransactionType.DEBIT
        }

        return null
    }

    private fun hasGenericCardWithCreditSignals(normalizedSms: NormalizedSms): Boolean {
        val text = normalizedSms.compact
        val hasGenericCard = text.contains(" card ") || text.contains(" card no") || text.contains("card ending")
        val hasCreditHints = containsAny(text, listOf("available limit", "avl limit", "avl lmt", "credit card"))
        return hasGenericCard && hasCreditHints && !text.contains("debit card")
    }

    private fun hasNamedIssuerCardSignal(normalizedSms: NormalizedSms): Boolean {
        val text = normalizedSms.compact
        val bankResolved = bankResolver.resolve(normalizedSms) != null
        val genericCardMention = text.contains(" card ") || text.contains("card xx") || text.contains("card ending")
        val creditHints = containsAny(
            text,
            listOf(
                "spent on your",
                "to your",
                "credited to your",
                "foreign currency markup",
                "will appear in statement",
                "against original txn",
            ),
        )
        return bankResolved && genericCardMention && creditHints && !text.contains("debit card")
    }

    private fun hasCardSignals(normalizedSms: NormalizedSms): Boolean {
        val text = normalizedSms.compact
        return text.contains("credit card") ||
            hasGenericCardWithCreditSignals(normalizedSms) ||
            hasNamedIssuerCardSignal(normalizedSms)
    }

    private fun hasAccountSignals(normalizedSms: NormalizedSms): Boolean = containsSavingsAccountSignals(normalizedSms)

    private fun containsSavingsAccountSignals(normalizedSms: NormalizedSms): Boolean {
        val text = normalizedSms.compact
        val accountWords = containsAny(text, listOf(" a/c ", " acct ", " account ", "from hdfc bank a/c", "from a/c", "to your a/c"))
        val accountActions = containsAny(text, listOf("debited from", "credited to", "sent rs", "salary", "avl bal", "available balance"))
        return accountWords && accountActions && !text.contains("credit card")
    }

    private fun containsUpiBankAccountSignals(normalizedSms: NormalizedSms): Boolean {
        val text = normalizedSms.compact
        val hasUpi = containsAny(text, listOf("upi", "upi ref", "via upi", "p2a", "p2p", "@okaxis", "@hdfc"))
        val accountContext = containsAny(text, listOf("a/c", "acct", "account", "debited", "credited to upi", "payment"))
        return hasUpi && (accountContext || !text.contains("credit card"))
    }

    private fun isMalformed(normalizedSms: NormalizedSms): Boolean {
        val raw = normalizedSms.raw.trim()
        if (raw.length < 12) {
            return true
        }
        if (raw.endsWith(",")) {
            return true
        }
        if (Regex("""(?i)\brs\.?\s*\d{1,3},\d{0,2}$""").containsMatchIn(raw)) {
            return true
        }
        return false
    }

    private fun exclude(rawSms: String, reason: ExcludeReason): ParsedResult = ParsedResult(
        rawSms = rawSms,
        decision = ParseDecision.EXCLUDE,
        excludeReason = reason,
        transaction = null,
        confidence = ParseConfig.exclusionConfidence[reason] ?: 0.4,
    )

    private fun scoreInclude(signals: IncludeSignals): Double {
        var score = 0.75

        if (signals.explicitCreditCard) score += 0.08
        if (signals.genericCardWithLimit) score += 0.03
        if (signals.bankResolved) score += 0.05
        if (signals.amountExtracted) score += 0.05
        if (signals.currencyExtracted) score += 0.04
        if (signals.merchantExtracted) score += 0.04
        if (signals.cardExtracted) score += 0.03
        if (signals.dateExtracted) score += 0.03

        if (!signals.explicitCreditCard && signals.genericCardWithLimit) score -= 0.08
        if (!signals.bankResolved) score -= 0.08
        if (!signals.merchantExtracted) score -= 0.07
        if (!signals.dateExtracted) score -= 0.05
        if (!signals.cardExtracted) score -= 0.05
        if (signals.multipleAmounts) score -= 0.10
        if (signals.accountAndCardSignalsMixed) score -= 0.12
        if (signals.refundAmbiguous) score -= 0.10

        return score.coerceIn(0.0, 0.95)
    }

    private fun containsAny(text: String, tokens: List<String>): Boolean = tokens.any(text::contains)
}
