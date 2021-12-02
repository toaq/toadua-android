package town.robin.toadua

data class SearchResponse(
    override val version: String,
    override val success: Boolean,
    override val error: String?,
    val results: Array<Entry>?,
) : ToaduaResponse