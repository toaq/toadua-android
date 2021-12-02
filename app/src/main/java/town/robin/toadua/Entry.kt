package town.robin.toadua

import java.time.Instant

data class Entry(
    val id: String,
    val head: String,
    val body: String,
    val user: String,
    val date: String, // TODO: change type
    val scope: String,
    val score: Int,
    val vote: Int?, // TODO: change type
    val notes: Array<Note>,
)