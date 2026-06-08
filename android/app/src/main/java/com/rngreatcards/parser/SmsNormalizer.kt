package com.rngreatcards.parser

class SmsNormalizer {
    fun normalize(rawSms: String): NormalizedSms {
        val lower = rawSms.lowercase()
        val compact = lower
            .replace(Regex("""[\r\n\t]+"""), " ")
            // Preserve commas and decimal points so amount extraction still sees
            // values like 1,250.00 instead of splitting them into separate tokens.
            .replace(Regex("""[:;()]+"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()

        return NormalizedSms(
            raw = rawSms.trim(),
            lower = lower,
            compact = compact,
        )
    }
}
