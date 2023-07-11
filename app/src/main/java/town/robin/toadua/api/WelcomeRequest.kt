package town.robin.toadua.api

class WelcomeRequest(val token: String?) : ToaduaRequest {
    override val action = "welcome"
}