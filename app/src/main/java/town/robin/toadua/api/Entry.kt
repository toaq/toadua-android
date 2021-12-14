package town.robin.toadua.api

data class Entry(
    val id: String,
    val head: String,
    val body: String,
    val user: String,
    val date: String,
    val scope: String,
    var score: Int,
    var vote: Int?,
    val notes: MutableList<Note>,
)