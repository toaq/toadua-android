package town.robin.toadua.api

class SearchRequest(val token: String?, val query: List<Any>, sortOrder: SortOrder?) : ToaduaRequest {
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
        fun search(token: String?, language: String, terms: List<String>, userFilter: String?, sortOrder: SortOrder?): SearchRequest {
            val conjuncts = terms.map { listOf("term", it) }.toMutableList()
            if (userFilter != null) conjuncts.add(listOf("user", userFilter))
            val query = listOf("and", listOf("scope", language), *conjuncts.toTypedArray())

            return SearchRequest(token, query, sortOrder)
        }

        fun gloss(token: String?, language: String, terms: List<String>): SearchRequest {
            val conjuncts = terms.map { listOf("term", it) }
            val query = listOf(
                "and",
                listOf("scope", language),
                if (conjuncts.size == 1) conjuncts.first() else listOf("or", *conjuncts.toTypedArray())
            )

            return SearchRequest(token, query, null)
        }
    }
}