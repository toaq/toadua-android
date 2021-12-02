package town.robin.toadua.api

class RegisterRequest(username: String, password: String) : ToaduaRequest {
    override val action = "register"

    val name = username
    val pass = password
}