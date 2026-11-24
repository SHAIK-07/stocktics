package com.stocktics

import com.stocktics.data.stocks.SupabaseStockClient
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SupabaseStockClientTest {

    @Test
    fun testGetRealStocks_returnsPopulatedList() = runBlocking {
        val stocks = SupabaseStockClient.getRealStocks()
        assertNotNull(stocks)
        assertFalse("Stocks list should not be empty", stocks.isEmpty())
        assertTrue("Should contain at least 5 stocks", stocks.size >= 5)

        // Verify key symbols exist
        val symbols = stocks.map { it.symbol }
        assertTrue("Should contain RELIANCE", symbols.contains("RELIANCE"))
        assertTrue("Should contain TCS", symbols.contains("TCS"))

        val reliance = stocks.first { it.symbol == "RELIANCE" }
        assertTrue("Price should have currency prefix", reliance.price.startsWith("₹") || reliance.price.startsWith("$"))
        assertFalse("Company name should not be blank", reliance.name.isBlank())
        assertFalse("Initials should not be blank", reliance.initials.isBlank())
    }
}
