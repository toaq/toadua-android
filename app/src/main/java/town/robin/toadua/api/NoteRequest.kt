package town.robin.toadua.api

class NoteRequest(val token: String, entryId: String, val content: String) : ToaduaRequest {
    override val action = "note"

    val id = entryId
}