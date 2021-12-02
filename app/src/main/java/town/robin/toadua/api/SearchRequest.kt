package town.robin.toadua.api

class SearchRequest(val token: String?, terms: List<String>) : ToaduaRequest {
    override val action = "search"

    val query =
        if (terms.size == 1) arrayOf("term", terms.first())
        else arrayOf("and", *terms.map { arrayOf("term", it) }.toTypedArray())
}