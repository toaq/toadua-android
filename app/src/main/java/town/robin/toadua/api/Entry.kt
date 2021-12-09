package town.robin.toadua.api

data class Entry(
    val id: String,
    val head: String,
    val body: String,
    val user: String,
    val date: String, // TODO: change type
    val scope: String,
    var score: Int,
    var vote: Int?, // TODO: change type
    val notes: MutableList<Note>,
)