package town.robin.toadua

interface ToaduaResponse {
    val version: String
    val success: Boolean
    val error: String?
}