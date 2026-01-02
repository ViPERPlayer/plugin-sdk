package com.viperplayer.plugin

/**
 * Represents a paginated result with cursor-based pagination.
 * 
 * @param T The type of items in the result
 * @property items The items in this page
 * @property nextCursor Cursor for fetching the next page (null if no more pages)
 * @property totalCount Total number of items (if known)
 */
data class PagedResult<T>(
    val items: List<T>,
    val nextCursor: String? = null,
    val totalCount: Int? = null
) {
    /** Whether there are more pages to fetch */
    val hasMore: Boolean
        get() = nextCursor != null
    
    /** Whether this result is empty */
    val isEmpty: Boolean
        get() = items.isEmpty()
    
    /** Number of items in this page */
    val size: Int
        get() = items.size
    
    /**
     * Map items to a different type.
     */
    fun <R> map(transform: (T) -> R): PagedResult<R> {
        return PagedResult(
            items = items.map(transform),
            nextCursor = nextCursor,
            totalCount = totalCount
        )
    }
    
    companion object {
        /**
         * Create an empty result.
         */
        fun <T> empty(): PagedResult<T> = PagedResult(emptyList())
    }
}

