package town.robin.toadua.api

data class LoginResponse(
    override val version: String,
    override val success: Boolean,
    override val error: String?,
    val token: String?,
) : ToaduaResponse