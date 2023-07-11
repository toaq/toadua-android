@file:Suppress("unused")

package town.robin.toadua.api

class CreateRequest(
    val token: String, val head: String, val body: String, val scope: String,
) : ToaduaRequest {
    override val action = "create"
}