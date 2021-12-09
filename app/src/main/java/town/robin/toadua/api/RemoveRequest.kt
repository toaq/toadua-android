package town.robin.toadua.api

class RemoveRequest(val token: String, entryId: String) : ToaduaRequest {
    override val action = "remove"

    val id = entryId
}