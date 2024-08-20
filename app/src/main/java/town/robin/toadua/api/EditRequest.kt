@file:Suppress("unused")

package town.robin.toadua.api

class EditRequest(
    val token: String, entryId: String, val body: String, val scope: String,
) : ToaduaRequest {
    override val action = "edit"

    val id = entryId
}