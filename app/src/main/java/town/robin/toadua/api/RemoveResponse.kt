package town.robin.toadua.api

data class RemoveResponse(
    override val version: String,
    override val success: Boolean,
    override val error: String?,
) : ToaduaResponse