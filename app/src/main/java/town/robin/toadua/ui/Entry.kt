package town.robin.toadua.ui

import town.robin.toadua.api.Entry

class InvalidVoteException : Exception()

data class Entry(
    val id: String,
    val user: String,
    val term: String,
    val definition: String,
    val score: Int,
    val vote: Vote,
    val comments: List<Comment>,
    val pendingEdit: EntryComposition? = null,
    val pendingComment: CommentComposition? = null,
) {
    constructor(entry: Entry) : this(
        id = entry.id,
        user = entry.user,
        term = entry.head,
        definition = entry.body.replace(modelBlank, uiBlank),
        score = entry.score,
        vote = when (entry.vote) {
            -1 -> Vote.DISLIKE
            null, 0 -> Vote.NO_VOTE
            1 -> Vote.LIKE
            else -> throw InvalidVoteException()
        },
        comments = entry.notes.map { Comment(it) },
    )
}