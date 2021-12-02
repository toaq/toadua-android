package town.robin.toadua.api

data class LogoutResponse(
    override val version: String,
    override val success: Boolean,
    override val error: String?,
) : ToaduaResponse