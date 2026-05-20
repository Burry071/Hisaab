package com.hisaab.parser.bank

import com.hisaab.parser.model.TransactionType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class MeezanBankParserTest {

    private lateinit var parser: MeezanBankParser
    private val ts = 1_700_000_000_000L

    @Before fun setUp() { parser = MeezanBankParser() }

    @Test fun `canParse returns true for MEEZANBANK sender`() =
        assertTrue(parser.canParse("MEEZANBANK", ""))

    @Test fun `canParse returns true for MEEZAN sender`() =
        assertTrue(parser.canParse("MEEZAN", ""))

    @Test fun `canParse returns true for MBL sender`() =
        assertTrue(parser.canParse("MBL", ""))

    @Test fun `canParse returns false for non-Meezan sender`() =
        assertFalse(parser.canParse("UBL", ""))

    @Test fun `parse debit SMS returns DEBIT type`() {
        val body = "PKR 6,500 debited from your Meezan A/C XXXX7788. Bal: PKR 43,500. Stan: 112345"
        val result = parser.parse("MEEZAN", body, ts)
        assertEquals(TransactionType.DEBIT, result.type)
        assertEquals(BigDecimal("6500"), result.amount)
        assertEquals("Meezan Bank", result.institution)
    }

    @Test fun `parse debit extracts balance`() {
        val body = "PKR 4,000 debited from your Meezan A/C. Available Balance: PKR 36,000."
        assertEquals(BigDecimal("36000"), parser.parse("MEEZAN", body, ts).balanceAfter)
    }

    @Test fun `parse credit SMS returns CREDIT type`() {
        val body = "PKR 30,000 credited to your Meezan A/C XXXX5566. Bal: PKR 80,000."
        val result = parser.parse("MEEZAN", body, ts)
        assertEquals(TransactionType.CREDIT, result.type)
        assertEquals(BigDecimal("30000"), result.amount)
    }

    @Test fun `parse Ibft returns TRANSFER type`() {
        val body = "Ibft of PKR 15,000 to Hamza Iqbal. Available Balance: PKR 35,000. Stan: 998877"
        val result = parser.parse("MEEZAN", body, ts)
        assertEquals(TransactionType.TRANSFER, result.type)
        assertEquals(BigDecimal("15000"), result.amount)
    }

    @Test fun `parse Ibft extracts counterparty`() {
        val body = "Ibft of PKR 8,000 to Fatima Malik. Bal: PKR 22,000."
        assertEquals("Fatima Malik", parser.parse("MEEZAN", body, ts).counterparty)
    }

    @Test fun `parse bill payment returns BILL_PAYMENT type`() {
        val body = "Bill payment of PKR 2,500 processed. Bal: PKR 47,500."
        val result = parser.parse("MEEZAN", body, ts)
        assertEquals(TransactionType.BILL_PAYMENT, result.type)
        assertEquals(BigDecimal("2500"), result.amount)
    }

    @Test fun `parse extracts Stan reference number`() {
        val body = "PKR 6,500 debited from your Meezan A/C. Bal: PKR 43,500. Stan: 556677"
        assertEquals("556677", parser.parse("MEEZAN", body, ts).referenceNumber)
    }

    @Test fun `parse unrecognised returns UNKNOWN with low confidence`() {
        val body = "Meezan Bank: Your profit rate has been updated."
        val result = parser.parse("MEEZAN", body, ts)
        assertEquals(TransactionType.UNKNOWN, result.type)
        assertTrue(result.confidenceScore < 0.5f)
    }

    @Test fun `idempotency - same SMS same ID`() {
        val body = "PKR 6,500 debited from your Meezan A/C. Bal: PKR 43,500."
        assertEquals(parser.parse("MEEZAN", body, ts).id, parser.parse("MEEZAN", body, ts).id)
    }

    @Test fun `Ibft with lakh amount parsed correctly`() {
        val body = "Ibft of PKR 1,00,000 to Ahmed Khan. Bal: PKR 4,00,000."
        assertEquals(BigDecimal("100000"), parser.parse("MEEZAN", body, ts).amount)
    }
}
