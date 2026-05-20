package com.hisaab.parser.bank

import com.hisaab.parser.model.TransactionType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class BankAlfalahParserTest {

    private lateinit var parser: BankAlfalahParser
    private val ts = 1_700_000_000_000L

    @Before fun setUp() { parser = BankAlfalahParser() }

    @Test fun `canParse returns true for ALFALAH sender`() =
        assertTrue(parser.canParse("ALFALAH", ""))

    @Test fun `canParse returns true for BANKALFALAH sender`() =
        assertTrue(parser.canParse("BANKALFALAH", ""))

    @Test fun `canParse returns true for BAL sender`() =
        assertTrue(parser.canParse("BAL", ""))

    @Test fun `canParse returns false for non-Alfalah sender`() =
        assertFalse(parser.canParse("HBL", ""))

    @Test fun `parse debit returns DEBIT type with correct amount`() {
        val body = "PKR 9,000 has been debited from your Alfalah A/C XXXX8899. Available Balance: PKR 41,000. Ref: BAL778899"
        val result = parser.parse("ALFALAH", body, ts)
        assertEquals(TransactionType.DEBIT, result.type)
        assertEquals(BigDecimal("9000"), result.amount)
        assertEquals("Bank Alfalah", result.institution)
    }

    @Test fun `parse debit extracts available balance`() {
        val body = "PKR 5,000 has been debited from your Alfalah account. Available Balance: PKR 25,000."
        assertEquals(BigDecimal("25000"), parser.parse("ALFALAH", body, ts).balanceAfter)
    }

    @Test fun `parse credit returns CREDIT type`() {
        val body = "PKR 45,000 has been credited to your Alfalah A/C XXXX1122. Balance: PKR 95,000."
        val result = parser.parse("ALFALAH", body, ts)
        assertEquals(TransactionType.CREDIT, result.type)
        assertEquals(BigDecimal("45000"), result.amount)
    }

    @Test fun `parse transfer returns TRANSFER type`() {
        val body = "You have transferred PKR 18,000 to Omar Sheikh. Balance: PKR 32,000. Ref: BAL334455"
        val result = parser.parse("ALFALAH", body, ts)
        assertEquals(TransactionType.TRANSFER, result.type)
        assertEquals(BigDecimal("18000"), result.amount)
    }

    @Test fun `parse transfer extracts counterparty`() {
        val body = "You have transferred PKR 7,500 to Aisha Siddiqui. Balance: PKR 12,500."
        assertEquals("Aisha Siddiqui", parser.parse("ALFALAH", body, ts).counterparty)
    }

    @Test fun `parse bill payment returns BILL_PAYMENT type`() {
        val body = "Your bill payment of PKR 3,200 has been processed. Balance: PKR 16,800."
        val result = parser.parse("ALFALAH", body, ts)
        assertEquals(TransactionType.BILL_PAYMENT, result.type)
        assertEquals(BigDecimal("3200"), result.amount)
    }

    @Test fun `parse extracts reference number`() {
        val body = "PKR 9,000 has been debited. Bal: PKR 41,000. Ref: BAL778899"
        assertEquals("BAL778899", parser.parse("ALFALAH", body, ts).referenceNumber)
    }

    @Test fun `parse unrecognised returns UNKNOWN with low confidence`() {
        val body = "Bank Alfalah: Your debit card has been activated."
        val result = parser.parse("ALFALAH", body, ts)
        assertEquals(TransactionType.UNKNOWN, result.type)
        assertTrue(result.confidenceScore < 0.5f)
    }

    @Test fun `idempotency - same SMS same ID`() {
        val body = "PKR 9,000 has been debited from your Alfalah account. Balance: PKR 41,000."
        assertEquals(parser.parse("ALFALAH", body, ts).id, parser.parse("ALFALAH", body, ts).id)
    }

    @Test fun `handles lakh-style comma amounts`() {
        val body = "PKR 2,50,000 has been credited to your Alfalah A/C. Balance: PKR 3,50,000."
        val result = parser.parse("ALFALAH", body, ts)
        assertEquals(BigDecimal("250000"), result.amount)
        assertEquals(BigDecimal("350000"), result.balanceAfter)
    }

    @Test fun `currency is always PKR`() {
        val body = "PKR 5,000 has been debited from your account. Balance: PKR 45,000."
        assertEquals("PKR", parser.parse("ALFALAH", body, ts).currency)
    }
}
