package town.robin.toadua.api

data class EditResponse(
    override val version: String,
    override val success: Boolean,
    override val error: String?,
    val entry: Entry?,
) : ToaduaResponse
