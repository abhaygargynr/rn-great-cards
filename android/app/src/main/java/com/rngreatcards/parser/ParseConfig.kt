package com.rngreatcards.parser

object ParseConfig {
    val bankRules = listOf(
        BankRule("HDFC Bank", listOf("hdfc bank", "hdfc credit card", "hdfc card")),
        BankRule("ICICI Bank", listOf("icici bank", "icici credit card", "icici card")),
        BankRule("Axis Bank", listOf("axis bank", "axis bank card", "axis credit card", "axis card")),
        BankRule("YES BANK", listOf("yes bank", "yes bank credit card")),
        BankRule(
            "Federal Bank",
            listOf("edge federal bank credit card", "edge federal bank", "federal bank", "jupiter edge"),
        ),
        BankRule(
            "BOBCARD / Bank of Baroda",
            listOf("bobcard one", "bobcard", "bank of baroda"),
        ),
        BankRule("SBI Card", listOf("sbi card", "sbi credit card")),
        BankRule("Kotak Mahindra Bank", listOf("kotak mahindra", "kotak credit card", "kotak")),
        BankRule("IndusInd Bank", listOf("indusind credit card", "indusind bank", "indusind")),
        BankRule("IDFC FIRST Bank", listOf("idfc first bank", "idfc first", "idfc credit card")),
        BankRule("AU Small Finance Bank", listOf("au small finance bank", "au credit card", "au bank")),
        BankRule("RBL Bank", listOf("rbl credit card", "rbl bank")),
    )

    val currencyAliases = mapOf(
        "rs" to "INR",
        "rs." to "INR",
        "inr" to "INR",
        "₹" to "INR",
        "usd" to "USD",
        "eur" to "EUR",
        "aed" to "AED",
    )

    val dateRegexes = listOf(
        Regex("""\b\d{2}/\d{2}/\d{2,4}\b""", RegexOption.IGNORE_CASE),
        Regex("""\b\d{2}-\d{2}-\d{2,4}\b""", RegexOption.IGNORE_CASE),
        Regex("""\b\d{2}-[a-z]{3}-\d{2,4}\b""", RegexOption.IGNORE_CASE),
        Regex("""\b\d{2}\s+[a-z]{3}\s+\d{2,4}\b""", RegexOption.IGNORE_CASE),
        Regex("""\b[a-z]{3}\s+\d{1,2},\s+\d{4}\b""", RegexOption.IGNORE_CASE),
    )

    val exclusionConfidence = mapOf(
        ExcludeReason.OTP to 0.98,
        ExcludeReason.OFFER to 0.95,
        ExcludeReason.SERVICE_MESSAGE to 0.92,
        ExcludeReason.LIMIT_UPDATE to 0.92,
        ExcludeReason.DECLINED to 0.96,
        ExcludeReason.AUTH_HOLD to 0.90,
        ExcludeReason.FUTURE_AUTO_DEBIT to 0.94,
        ExcludeReason.BILL_DUE to 0.95,
        ExcludeReason.CARD_PAYMENT to 0.94,
        ExcludeReason.EMI_CONVERSION to 0.94,
        ExcludeReason.FEE_OR_CHARGE to 0.94,
        ExcludeReason.DEBIT_CARD to 0.95,
        ExcludeReason.UPI_BANK_ACCOUNT to 0.94,
        ExcludeReason.BALANCE_ALERT to 0.95,
        ExcludeReason.SAVINGS_ACCOUNT to 0.93,
        ExcludeReason.INVESTMENT to 0.93,
        ExcludeReason.INSURANCE to 0.93,
        ExcludeReason.REWARD_OR_CASHBACK to 0.90,
        ExcludeReason.LOW_CONFIDENCE to 0.4,
        ExcludeReason.MALFORMED_SMS to 0.1,
    )

    val stopTokens = listOf(
        " on ",
        ". avl limit",
        ". available limit",
        ". avl lmt",
        " with your",
        " using your",
        " from your",
        " against original txn",
        " foreign currency markup",
        " inr equivalent",
        ". tap to view",
    )
}
