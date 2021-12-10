package town.robin.toadua.api

class SearchRequest(val token: String?, terms: List<String>, userFilter: String?, sortOrder: SortOrder?) : ToaduaRequest {
    override val action = "search"

    val query: Array<Any>
    init {
        val conjuncts = terms.map { arrayOf("term", it) }.toMutableList()
        if (userFilter != null) conjuncts.add(arrayOf("user", userFilter))
        query =
            if (conjuncts.size == 1) conjuncts.first() as Array<Any>
            else arrayOf("and", *conjuncts.toTypedArray())
    }
    val ordering = when (sortOrder) {
        null -> null
        SortOrder.HIGHEST -> "highest"
        SortOrder.LOWEST -> "lowest"
        SortOrder.NEWEST -> "newest"
        SortOrder.OLDEST -> "oldest"
        SortOrder.RANDOM -> "random"
    }
}