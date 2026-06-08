package com.rngreatcards.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsParserTest {
    private val parser = SmsParser()

    @Test
    fun parsesClearCreditCardSpend() {
        val result = parser.parseOne(
            "INR 1,250.00 spent on HDFC Bank Credit Card xx5678 at SWIGGY on 03-04-2026. Avl Limit: INR 1,45,300.00.",
        )

        assertEquals(ParseDecision.INCLUDE, result.decision)
        assertEquals("HDFC Bank", result.transaction?.bank)
        assertEquals(TransactionType.DEBIT, result.transaction?.type)
        assertEquals("INR", result.transaction?.currency)
        assertEquals("5678", result.transaction?.cardLastFour)
        assertEquals("SWIGGY", result.transaction?.merchant)
        assertEquals("2026-04-03", result.transaction?.date)
    }

    @Test
    fun excludesDebitCardMessage() {
        val result = parser.parseOne(
            "Transaction Alert: Rs. 500.00 debited from your HDFC Bank Debit Card ending 1234 at SWIGGY on 06-04-26.",
        )

        assertEquals(ParseDecision.EXCLUDE, result.decision)
        assertEquals(ExcludeReason.DEBIT_CARD, result.excludeReason)
        assertNull(result.transaction)
    }

    @Test
    fun excludesOtpMessage() {
        val result = parser.parseOne(
            "Use 458219 as your OTP for HDFC Bank Net Banking login. Valid for 5 mins. Do NOT share with anyone.",
        )

        assertEquals(ParseDecision.EXCLUDE, result.decision)
        assertEquals(ExcludeReason.OTP, result.excludeReason)
    }

    @Test
    fun excludesUpiAccountMessage() {
        val result = parser.parseOne(
            "Rs 1,200 debited from A/c XX4521 via UPI on 11-04-26. UPI/P2A/MOHAN-SHARMA@OKAXIS/Personal. UPI Ref: 240411887211.",
        )

        assertEquals(ParseDecision.EXCLUDE, result.decision)
        assertEquals(ExcludeReason.UPI_BANK_ACCOUNT, result.excludeReason)
    }

    @Test
    fun resolvesCoBrandedFederalBankIssuer() {
        val result = parser.parseOne(
            "Hey there, you've spent Rs 1836.00 to HOSPITALITY PVT DELHI IN on your Edge Federal Bank Credit Card ending 4422 on 07-04-2026. Tap to view your transactions in the Jupiter app.",
        )

        assertEquals(ParseDecision.INCLUDE, result.decision)
        assertEquals("Federal Bank", result.transaction?.bank)
        assertEquals("4422", result.transaction?.cardLastFour)
    }

    @Test
    fun resolvesBobcardIssuer() {
        val result = parser.parseOne(
            "You've spent Rs. 849.00 at Blackwater Coffee, Gurgaon with your BOBCARD One Credit Card ending in XX9907 on 08-04-2026.",
        )

        assertEquals(ParseDecision.INCLUDE, result.decision)
        assertEquals("BOBCARD / Bank of Baroda", result.transaction?.bank)
    }

    @Test
    fun includesRefund() {
        val result = parser.parseOne(
            "Refund of Rs 450.00 has been credited to your HDFC Card xx5678 from BIGBASKET on 12-04-26 against original txn dated 02-04-26.",
        )

        assertEquals(ParseDecision.INCLUDE, result.decision)
        assertEquals(TransactionType.REFUND, result.transaction?.type)
        assertEquals("BIGBASKET", result.transaction?.merchant)
    }

    @Test
    fun keepsForeignCurrency() {
        val result = parser.parseOne(
            "USD 49.99 spent on your Axis Bank Card XX9876 at NETFLIX.COM/US on 13-APR-26. Foreign currency markup of 3.5% will be applied. INR equivalent will appear in statement.",
        )

        assertEquals(ParseDecision.INCLUDE, result.decision)
        assertEquals("USD", result.transaction?.currency)
        assertEquals(49.99, result.transaction?.amount ?: 0.0, 0.001)
    }

    @Test
    fun excludesBillDue() {
        val result = parser.parseOne(
            "Your HDFC Bank Credit Card xx5678 bill of Rs 23,450.00 is due on 15-04-26. View your bill at hdfcbank.com/billview.",
        )

        assertEquals(ParseDecision.EXCLUDE, result.decision)
        assertEquals(ExcludeReason.BILL_DUE, result.excludeReason)
    }

    @Test
    fun excludesCardPayment() {
        val result = parser.parseOne(
            "Payment of Rs 23,450.00 received towards your HDFC Bank Credit Card xx5678 on 11-04-26. Thank you.",
        )

        assertEquals(ParseDecision.EXCLUDE, result.decision)
        assertEquals(ExcludeReason.CARD_PAYMENT, result.excludeReason)
    }

    @Test
    fun excludesDeclinedTransaction() {
        val result = parser.parseOne(
            "Transaction Declined: Attempt to spend Rs. 9,999 on your ICICI Credit Card XX1122 at FOREIGN MERCHANT was declined due to insufficient credit limit.",
        )

        assertEquals(ParseDecision.EXCLUDE, result.decision)
        assertEquals(ExcludeReason.DECLINED, result.excludeReason)
    }

    @Test
    fun excludesMalformedSms() {
        val result = parser.parseOne("Spent Rs. 2,4")

        assertEquals(ParseDecision.EXCLUDE, result.decision)
        assertEquals(ExcludeReason.MALFORMED_SMS, result.excludeReason)
        assertTrue(result.confidence <= 0.1)
    }

    @Test
    fun matchesVisibleSampleCounts() {
        val results = parser.parseSms(VISIBLE_SAMPLES)

        val includeCount = results.count { it.decision == ParseDecision.INCLUDE }
        val excludeCount = results.count { it.decision == ParseDecision.EXCLUDE }
        val includedIndexes = results.mapIndexedNotNull { index, result ->
            if (result.decision == ParseDecision.INCLUDE) index + 1 else null
        }

        assertEquals(7, includeCount)
        assertEquals(18, excludeCount)
        assertEquals(listOf(2, 5, 7, 8, 9, 21, 22), includedIndexes)
    }

    companion object {
        private val VISIBLE_SAMPLES = listOf(
            "Sent Rs.450.00 From HDFC Bank A/C *4521 To BIGBASKET on 02/04/26. Ref 405617287211. Not You? Call 18002586161/SMS BLOCK CC to 7308080808 to block CC.",
            "INR 1,250.00 spent on HDFC Bank Credit Card xx5678 at SWIGGY on 03-04-2026. Avl Limit: INR 1,45,300.00.",
            "ICICI Bank Acct XX123 debited Rs 2,500.00 on 04-Apr-26 & credited to UPI/swiggy@hdfc/Payment. UPI Ref:240412345678. Call 18002662 if not you.",
            "Dear Customer, Rs 50000 credited to your A/c XX4521 on 05-04-2026 by SALARY-ACMECORP. Avl Bal: Rs 1,52,300.45.",
            "INR 320.00 spent using Axis Bank Card no. XX9876 on 06-APR-26 at AMAZON. Available Limit: INR 87,500.00.",
            "Transaction Alert: Rs. 500.00 debited from your HDFC Bank Debit Card ending 1234 at SWIGGY on 06-04-26.",
            "Spent Rs. 1200.00 on YES BANK Credit Card XX8888 at AMAZON on 07-04-26. Avl Lmt: Rs 78,500.",
            "Hey there, you've spent Rs 1836.00 to HOSPITALITY PVT DELHI IN on your Edge Federal Bank Credit Card ending 4422 on 07-04-2026. Tap to view your transactions in the Jupiter app.",
            "You've spent Rs. 849.00 at Blackwater Coffee, Gurgaon with your BOBCARD One Credit Card ending in XX9907 on 08-04-2026.",
            "Use 458219 as your OTP for HDFC Bank Net Banking login. Valid for 5 mins. Do NOT share with anyone.",
            "Avl Bal in your A/C XX4521 as on 08-04-26 is INR 1,02,450.30. Call 18002586161 for details.",
            "Your HDFC Bank Credit Card xx5678 bill of Rs 23,450.00 is due on 15-04-26. View your bill at hdfcbank.com/billview.",
            "Get flat 50% off + extra 10% cashback on travel bookings with HDFC Credit Cards this weekend. T&C apply. Visit hdfcbank.com/offers.",
            "Dear Customer, Rs 2,500 will be auto debited via E-Mandate from your HDFC Card XX5678 on 12-04-26 for NETFLIX_SUBSCRIPTION. Please maintain sufficient limit.",
            "Transaction Declined: Attempt to spend Rs. 9,999 on your ICICI Credit Card XX1122 at FOREIGN MERCHANT was declined due to insufficient credit limit.",
            "Your SIP of Rs 5,000 in Mirae Asset Large Cap Fund folio 12345678 has been debited from A/c XX4521 on 10-04-26.",
            "Your Rs 75,000.00 spend on HDFC Card xx5678 at CROMA-ELECTRONICS has been converted to EMI of Rs 6,847/month for 12 months at 13% interest.",
            "Finance charge of Rs 1,250.45 + GST Rs 225.08 has been debited from your HDFC Credit Card xx5678 for late payment on bill dated 31-03-2026.",
            "Payment of Rs 23,450.00 received towards your HDFC Bank Credit Card xx5678 on 11-04-26. Thank you.",
            "Rs 1,200 debited from A/c XX4521 via UPI on 11-04-26. UPI/P2A/MOHAN-SHARMA@OKAXIS/Personal. UPI Ref: 240411887211.",
            "Refund of Rs 450.00 has been credited to your HDFC Card xx5678 from BIGBASKET on 12-04-26 against original txn dated 02-04-26.",
            "USD 49.99 spent on your Axis Bank Card XX9876 at NETFLIX.COM/US on 13-APR-26. Foreign currency markup of 3.5% will be applied. INR equivalent will appear in statement.",
            "Premium of Rs 12,500 debited from A/c XX4521 on 13-04-26 for HDFC Life Insurance Policy XYZ-2026. Renewal complete.",
            "Rs.99 debited from A/c XX4521 via UPI on 14-04-26. UPI Ref: 240478234511 to NETFLIX-MONTHLY. Avl Bal: Rs 1,02,351.30.",
            "Spent Rs. 2,4",
        )
    }
}
