package com.rngreatcards.parser

enum class ParseDecision {
    INCLUDE,
    EXCLUDE,
}

enum class TransactionType {
    DEBIT,
    CREDIT,
    REFUND,
}

enum class ExcludeReason {
    OTP,
    OFFER,
    SERVICE_MESSAGE,
    LIMIT_UPDATE,
    DECLINED,
    AUTH_HOLD,
    FUTURE_AUTO_DEBIT,
    BILL_DUE,
    CARD_PAYMENT,
    EMI_CONVERSION,
    FEE_OR_CHARGE,
    DEBIT_CARD,
    UPI_BANK_ACCOUNT,
    BALANCE_ALERT,
    SAVINGS_ACCOUNT,
    INVESTMENT,
    INSURANCE,
    REWARD_OR_CASHBACK,
    LOW_CONFIDENCE,
    MALFORMED_SMS,
}

data class ParsedResult(
    val rawSms: String,
    val decision: ParseDecision,
    val excludeReason: ExcludeReason?,
    val transaction: TransactionData?,
    val confidence: Double,
)

data class TransactionData(
    val amount: Double,
    val currency: String,
    val bank: String,
    val cardLastFour: String?,
    val merchant: String?,
    val type: TransactionType,
    val date: String?,
)

data class NormalizedSms(
    val raw: String,
    val lower: String,
    val compact: String,
)

data class BankRule(
    val bank: String,
    val aliases: List<String>,
)

data class AmountMatch(
    val amount: Double,
    val currency: String?,
    val score: Int,
)

data class IncludeSignals(
    val explicitCreditCard: Boolean,
    val genericCardWithLimit: Boolean,
    val bankResolved: Boolean,
    val amountExtracted: Boolean,
    val currencyExtracted: Boolean,
    val merchantExtracted: Boolean,
    val cardExtracted: Boolean,
    val dateExtracted: Boolean,
    val multipleAmounts: Boolean,
    val accountAndCardSignalsMixed: Boolean,
    val refundAmbiguous: Boolean,
)
