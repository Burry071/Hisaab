package com.hisaab.parser.bank

import com.hisaab.parser.model.TransactionType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class MCBParserTest {

    private lateinit var parser: MCBParser
    private val ts = 1_700_000_000_000L

    @Before fun setUp() { parser = MCBParser() }

    @Test fun `canParse returns true for MCB sender`() =
        assertTrue(parser.canParse("MCB", ""))

    @Test fun `canParse returns true for MCBBANK sender`() =
        assertTrue(parser.canParse("MCBBANK", ""))

    @Test fun `canParse returns false for non-MCB sender`() =
        assertFalse(parser.canParse("HBL", ""))

    @Test fun `parse debit returns DEBIT type with correct amount`() {
        val body = "Your A/C XXXX4321 debited with Rs.7,500. Available Balance: Rs.32,500. Ref: MCB112233"
        val result = parser.parse("MCB", body, ts)
        assertEquals(TransactionType.DEBIT, result.type)
        assertEquals(BigDecimal("7500"), result.amount)
        assertEquals("MCB", result.institution)
    }

    @Test fun `parse debit extracts balance`() {
        val body = "Your A/C XXXX1111 debited with Rs.2,000. Available Balance: Rs.48,000."
        assertEquals(BigDecimal("48000"), parser.parse("MCB", body, ts).balanceAfter)
    }

    @Test fun `parse credit returns CREDIT type`() {
        val body = "Your A/C XXXX2222 credited with Rs.20,000. Available Balance: Rs.70,000."
        val result = parser.parse("MCB", body, ts)
        assertEquals(TransactionType.CREDIT, result.type)
        assertEquals(BigDecimal("20000"), result.amount)
    }

    @Test fun `parse transfer returns TRANSFER type`() {
        val body = "Transfer of Rs.10,000 made to Bilal Ahmed (MCB A/C). Bal: Rs.50,000. Ref: MCB556677"
        val result = parser.parse("MCB", body, ts)
        assertEquals(TransactionType.TRANSFER, result.type)
        assertEquals(BigDecimal("10000"), result.amount)
    }

    @Test fun `parse transfer extracts counterparty name`() {
        val body = "Transfer of Rs.8,000 made to Nadia Hussain (MCB A/C). Bal: Rs.22,000."
        assertEquals("Nadia Hussain", parser.parse("MCB", body, ts).counterparty)
    }

    @Test fun `parse bill payment returns BILL_PAYMENT type`() {
        val body = "MCB bill payment Rs.5,000 processed. Available Bal: Rs.15,000."
        val result = parser.parse("MCB", body, ts)
        assertEquals(TransactionType.BILL_PAYMENT, result.type)
        assertEquals(BigDecimal("5000"), result.amount)
    }

    @Test fun `parse ATM withdrawal returns DEBIT with ATM counterparty`() {
        val body = "ATM Cash Withdrawal Rs.20,000 from XXXX3333. Balance: Rs.30,000."
        val result = parser.parse("MCB", body, ts)
        assertEquals(TransactionType.DEBIT, result.type)
        assertEquals(BigDecimal("20000"), result.amount)
        assertEquals("ATM", result.counterparty)
    }

    @Test fun `parse extracts reference number`() {
        val body = "Your A/C XXXX4444 debited with Rs.3,000. Bal: Rs.17,000. Ref: MCB998877"
        assertEquals("MCB998877", parser.parse("MCB", body, ts).referenceNumber)
    }

    @Test fun `parse unrecognised returns UNKNOWN`() {
        val body = "MCB wishes you Eid Mubarak!"
        val result = parser.parse("MCB", body, ts)
        assertEquals(TransactionType.UNKNOWN, result.type)
        assertTrue(result.confidenceScore < 0.5f)
    }

    @Test fun `idempotency - same SMS same ID`() {
        val body = "Your A/C XXXX4321 debited with Rs.7,500. Bal: Rs.32,500."
        assertEquals(parser.parse("MCB", body, ts).id, parser.parse("MCB", body, ts).id)
    }

    @Test fun `parse handles empty body`() {
        val result = parser.parse("MCB", "", ts)
        assertEquals(TransactionType.UNKNOWN, result.type)
    }
}
