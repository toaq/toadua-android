package town.robin.toadua.api

import java.time.Instant

data class Note(
    val date: String,
    val user: String,
    val content: String,
)