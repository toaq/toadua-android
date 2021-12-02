package town.robin.toadua.api

interface ToaduaResponse {
    val version: String
    val success: Boolean
    val error: String?
}