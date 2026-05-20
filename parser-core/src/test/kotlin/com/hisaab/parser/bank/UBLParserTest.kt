package com.hisaab.parser.bank

import com.hisaab.parser.model.TransactionType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class UBLParserTest {

    private lateinit var parser: UBLParser
    private val ts = 1_700_000_000_000L

    @Before fun setUp() { parser = UBLParser() }

    @Test fun `canParse returns true for UBL sender`() =
        assertTrue(parser.canParse("UBL", ""))

    @Test fun `canParse returns true for UBLBANK sender`() =
        assertTrue(parser.canParse("UBLBANK", ""))

    @Test fun `canParse returns false for non-UBL sender`() =
        assertFalse(parser.canParse("MCB", ""))

    @Test fun `parse debit SMS returns DEBIT with correct amount`() {
        val body = "PKR 8,000 debited from A/C XXXX3344. Available Bal: PKR 42,000. TRN: 778899"
        val result = parser.parse("UBL", body, ts)
        assertEquals(TransactionType.DEBIT, result.type)
        assertEquals(BigDecimal("8000"), result.amount)
        assertEquals("UBL", result.institution)
    }

    @Test fun `parse debit extracts balance`() {
        val body = "PKR 3,000 debited from A/C XXXX1122. Available Bal: PKR 17,000."
        assertEquals(BigDecimal("17000"), parser.parse("UBL", body, ts).balanceAfter)
    }

    @Test fun `parse credit SMS returns CREDIT type`() {
        val body = "PKR 25,000 credited to your A/C XXXX5566. Available Bal: PKR 75,000."
        val result = parser.parse("UBL", body, ts)
        assertEquals(TransactionType.CREDIT, result.type)
        assertEquals(BigDecimal("25000"), result.amount)
    }

    @Test fun `parse fund transfer returns TRANSFER type`() {
        val body = "Fund Transfer of PKR 12,000 to Zara Ali. Bal: PKR 38,000. TRN: 556677"
        val result = parser.parse("UBL", body, ts)
        assertEquals(TransactionType.TRANSFER, result.type)
        assertEquals(BigDecimal("12000"), result.amount)
    }

    @Test fun `parse fund transfer extracts counterparty`() {
        val body = "Fund Transfer of PKR 5,000 to Ahmad Khan A/C. Bal: PKR 20,000."
        val result = parser.parse("UBL", body, ts)
        assertEquals("Ahmad Khan", result.counterparty)
    }

    @Test fun `parse bill payment returns BILL_PAYMENT type`() {
        val body = "Utility bill payment of PKR 4,500 processed. Bal: PKR 55,500."
        val result = parser.parse("UBL", body, ts)
        assertEquals(TransactionType.BILL_PAYMENT, result.type)
        assertEquals(BigDecimal("4500"), result.amount)
    }

    @Test fun `parse extracts reference number`() {
        val body = "PKR 6,000 debited from A/C XXXX7788. Bal: PKR 34,000. TRN: 123ABC"
        assertEquals("123ABC", parser.parse("UBL", body, ts).referenceNumber)
    }

    @Test fun `parse unrecognised returns UNKNOWN with low confidence`() {
        val body = "Dear UBL customer, your account statement is ready."
        val result = parser.parse("UBL", body, ts)
        assertEquals(TransactionType.UNKNOWN, result.type)
        assertTrue(result.confidenceScore < 0.5f)
    }

    @Test fun `idempotency - same SMS same ID`() {
        val body = "PKR 8,000 debited from A/C XXXX3344. Available Bal: PKR 42,000."
        assertEquals(parser.parse("UBL", body, ts).id, parser.parse("UBL", body, ts).id)
    }

    @Test fun `handles Indian-style lakh comma formatting`() {
        val body = "PKR 1,50,000 credited to your A/C XXXX9900. Bal: PKR 2,50,000."
        val result = parser.parse("UBL", body, ts)
        assertEquals(BigDecimal("150000"), result.amount)
    }

    @Test fun `currency is always PKR`() {
        val body = "PKR 5,000 debited from A/C XXXX1234. Bal: PKR 45,000."
        assertEquals("PKR", parser.parse("UBL", body, ts).currency)
    }
}
