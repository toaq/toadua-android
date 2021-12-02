package town.robin.toadua.api

data class VoteResponse(
    override val version: String,
    override val success: Boolean,
    override val error: String?,
    val entry: Entry?,
) : ToaduaResponse
