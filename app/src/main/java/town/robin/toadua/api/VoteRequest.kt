package town.robin.toadua.api

class VoteRequest(val token: String, entryId: String, val vote: Int) : ToaduaRequest {
    override val action = "vote"

    val id = entryId
}