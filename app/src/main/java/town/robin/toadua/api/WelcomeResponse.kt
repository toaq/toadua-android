package town.robin.toadua.api

data class WelcomeResponse(
    override val version: String,
    override val success: Boolean,
    override val error: String?,
    val name: String?,
) : ToaduaResponse