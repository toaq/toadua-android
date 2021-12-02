package town.robin.toadua.api

class LogoutRequest(val token: String) : ToaduaRequest {
    override val action = "logout"
}