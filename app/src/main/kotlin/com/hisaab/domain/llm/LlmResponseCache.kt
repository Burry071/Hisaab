package com.hisaab.domain.llm

import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory LLM response cache with TTL-based expiry.
 *
 * Prevents duplicate API calls for:
 *  - Same SMS body parsed twice (idempotent Tier 3 fallback)
 *  - Same insight category with same deviation % (insight regeneration)
 *  - Repeat forecast requests within a session
 *
 * TTL: 1 hour by default (financial data is time-sensitive)
 * Max entries: 200 (LRU eviction when full)
 *
 * This is an in-process cache — it clears on app restart.
 * Room DB is the persistent source of truth for parsed transactions.
 */
class LlmResponseCache(
    private val ttlMs: Long = 60 * 60 * 1000,   // 1 hour
    private val maxEntries: Int = 200,
) {
    private data class Entry(
        val response: LlmResponse,
        val expiresAtMs: Long,
    )

    private val store = ConcurrentHashMap<String, Entry>()

    /** Returns a cached response if present and not expired. */
    fun get(key: String): LlmResponse? {
        val entry = store[key] ?: return null
        if (System.currentTimeMillis() > entry.expiresAtMs) {
            store.remove(key)
            return null
        }
        return entry.response.copy(fromCache = true)
    }

    /** Stores a response. Evicts oldest 20% if full. */
    fun put(key: String, response: LlmResponse) {
        if (store.size >= maxEntries) evictOldest()
        store[key] = Entry(
            response    = response,
            expiresAtMs = System.currentTimeMillis() + ttlMs,
        )
    }

    /** Removes all expired entries. Can be called periodically. */
    fun evictExpired() {
        val now = System.currentTimeMillis()
        store.entries.removeIf { it.value.expiresAtMs < now }
    }

    /** Clears the entire cache (e.g., on provider change in Settings). */
    fun clear() = store.clear()

    /** Current cache size for telemetry / AgentTrace display. */
    val size: Int get() = store.size

    private fun evictOldest() {
        val toRemove = store.entries
            .sortedBy { it.value.expiresAtMs }
            .take(maxEntries / 5)  // evict 20%
        toRemove.forEach { store.remove(it.key) }
    }
}
