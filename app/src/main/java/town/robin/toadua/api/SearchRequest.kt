@file:Suppress("unused")

package town.robin.toadua.api

// Usernames of users we want to exclude when picking a random word
private val randomWordExcludedUsers = setOf(
    "examples", "oldexamples", "oldofficial", "oldcountries", "spreadsheet",
)

class SearchRequest(
    val token: String?,
    val query: List<Any>,
    sortOrder: SortOrder?,
    val limit: Int?,
) : ToaduaRequest {
    override val action = "search"

    val ordering = when (sortOrder) {
        null -> null
        SortOrder.HIGHEST -> "highest"
        SortOrder.LOWEST -> "lowest"
        SortOrder.NEWEST -> "newest"
        SortOrder.OLDEST -> "oldest"
        SortOrder.RANDOM -> "random"
    }

    companion object {
        fun search(
            token: String?,
            language: String?,
            terms: List<String> = listOf(),
            userFilter: String? = null,
            idFilter: String? = null,
            arityFilter: Arity? = null,
            sortOrder: SortOrder? = null,
            limit: Int? = null,
        ): SearchRequest {
            val conjuncts = terms.map { listOf("term", it) }.toMutableList<Any>()
            if (language != null) conjuncts.add(listOf("scope", language))
            if (userFilter != null) conjuncts.add(listOf("user", userFilter))
            if (idFilter != null) conjuncts.add(listOf("id", idFilter))
            if (arityFilter != null) conjuncts.add(listOf("arity", arityFilter.number))

            val query = listOf("and", *conjuncts.toTypedArray())
            return SearchRequest(token, query, sortOrder, limit)
        }

        fun randomWord(
            token: String?,
            language: String,
        ): SearchRequest {
            val excludedUserFilters = randomWordExcludedUsers.map { listOf("user", it) }
            val query = listOf(
                "and",
                listOf("scope", language),
                listOf("not", listOf("or", *excludedUserFilters.toTypedArray()))
            )
            return SearchRequest(token, query, SortOrder.RANDOM, 1)
        }
    }
}