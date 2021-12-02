package town.robin.toadua.api

class LoginRequest(username: String, password: String) : ToaduaRequest {
    override val action = "login"

    val name = username
    val pass = password
}