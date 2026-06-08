package com.rngreatcards.parser

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField
import java.util.Locale

class BankResolver(
    private val bankRules: List<BankRule> = ParseConfig.bankRules,
) {
    fun resolve(normalizedSms: NormalizedSms): String? {
        val compact = normalizedSms.compact
        return bankRules.firstOrNull { rule -> rule.aliases.any(compact::contains) }?.bank
    }
}

class CurrencyExtractor {
    private val currencyAmountRegex = Regex(
        """(?i)(₹|rs\.?|inr|usd|eur|aed)\s*([0-9]+(?:,[0-9]{2,3})*(?:\.\d{1,2})?)|([0-9]+(?:,[0-9]{2,3})*(?:\.\d{1,2})?)\s*(inr|usd|eur|aed)\b""",
    )

    fun extractCandidates(normalizedSms: NormalizedSms, type: TransactionType): List<AmountMatch> {
        val compact = normalizedSms.compact
        return currencyAmountRegex.findAll(compact).map { match ->
            val currencyToken = match.groups[1]?.value ?: match.groups[4]?.value
            val amountToken = match.groups[2]?.value ?: match.groups[3]?.value
            val amount = amountToken?.replace(",", "")?.toDoubleOrNull()
            if (amount == null) {
                null
            } else {
                val score = contextualScore(compact, match.range.first, type)
                AmountMatch(amount = amount, currency = normalizeCurrency(currencyToken), score = score)
            }
        }.filterNotNull().toList()
    }

    private fun normalizeCurrency(value: String?): String? {
        if (value == null) {
            return null
        }
        return ParseConfig.currencyAliases[value.lowercase()]
    }

    private fun contextualScore(text: String, index: Int, type: TransactionType): Int {
        val from = (index - 50).coerceAtLeast(0)
        val to = (index + 90).coerceAtMost(text.length)
        val window = text.substring(from, to)
        var score = 0

        val positiveTokens = if (type == TransactionType.REFUND) {
            listOf("refund of", "credited to your", "reversal")
        } else {
            listOf("spent", "spent using", "you've spent", "debited", "transaction alert", "used")
        }
        val negativeTokens = listOf(
            "available limit",
            "avl limit",
            "avl lmt",
            "available balance",
            "avl bal",
            "bill",
            "due",
            "gst",
            "fee",
            "interest",
            "month",
            "equivalent",
            "markup",
        )

        positiveTokens.forEach { token ->
            if (window.contains(token)) {
                score += 3
            }
        }
        negativeTokens.forEach { token ->
            if (window.contains(token)) {
                score -= 3
            }
        }

        return score
    }
}

class CardExtractor {
    private val cardPatterns = listOf(
        Regex("""(?i)\b(?:credit\s+card|card)\s+(?:xx|xxxx)?(\d{4})\b"""),
        Regex("""(?i)\bcard\s+no\.?\s+(?:xx|xxxx)?(\d{4})\b"""),
        Regex("""(?i)\bending(?:\s+in)?\s+(?:xx|xxxx)?(\d{4})\b"""),
        Regex("""(?i)\*(\d{4})\b"""),
    )

    fun extract(normalizedSms: NormalizedSms): String? {
        val compact = normalizedSms.compact
        if (Regex("""(?i)(a/c|acct|account)\s+\*?x{0,4}\d{4}""").containsMatchIn(compact) &&
            !compact.contains("card")
        ) {
            return null
        }

        return cardPatterns.firstNotNullOfOrNull { regex ->
            regex.find(normalizedSms.raw)?.groupValues?.getOrNull(1)
        }
    }
}

class MerchantExtractor {
    private val refundPatterns = listOf(
        Regex("""(?i)\bfrom\s+(.+?)(?:\s+on\s+\d|\s+against original txn|[.]\s*|$)"""),
    )
    private val spendPatterns = listOf(
        Regex("""(?i)\bat\s+(.+?)(?:\s+on\s+\d|\s+with your|\s+using your|\.\s*avl|\.\s*available limit|[.]\s*|$)"""),
        Regex("""(?i)\bto\s+(.+?)(?:\s+on your|\s+on\s+\d|\.\s*|$)"""),
    )

    fun extract(normalizedSms: NormalizedSms, type: TransactionType): String? {
        val patterns = if (type == TransactionType.REFUND) refundPatterns else spendPatterns
        return patterns.firstNotNullOfOrNull { regex ->
            regex.find(normalizedSms.raw)?.groupValues?.getOrNull(1)?.trim()?.trimEnd('.')
        }?.let { rawMerchant ->
            ParseConfig.stopTokens.fold(rawMerchant) { current, stopToken ->
                val stopIndex = current.lowercase().indexOf(stopToken.lowercase())
                if (stopIndex >= 0) current.substring(0, stopIndex) else current
            }.trim().takeIf { it.isNotEmpty() }
        }
    }
}

class DateExtractor {
    private val formatters = listOf(
        formatter("dd/MM/uuuu"),
        formatter("dd/MM/uu"),
        formatter("dd-MM-uuuu"),
        formatter("dd-MM-uu"),
        formatter("dd-MMM-uuuu"),
        formatter("dd-MMM-uu"),
        formatter("dd MMM uuuu"),
        formatter("MMM d, uuuu"),
    )

    fun extract(normalizedSms: NormalizedSms): String? {
        val raw = normalizedSms.raw
        for (regex in ParseConfig.dateRegexes) {
            val match = regex.find(raw) ?: continue
            val value = match.value
            val parsed = parseDate(value)
            if (parsed != null) {
                return parsed.toString()
            }
        }
        return null
    }

    private fun parseDate(value: String): LocalDate? {
        for (formatter in formatters) {
            try {
                return LocalDate.parse(value.uppercase(Locale.ENGLISH), formatter)
            } catch (_: DateTimeParseException) {
            }
        }
        return null
    }

    private fun formatter(pattern: String): DateTimeFormatter =
        DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern(pattern)
            .parseDefaulting(ChronoField.YEAR_OF_ERA, 2026)
            .toFormatter(Locale.ENGLISH)
}
