package town.robin.toadua.api

data class RegisterResponse(
    override val version: String,
    override val success: Boolean,
    override val error: String?,
    val token: String?,
) : ToaduaResponse