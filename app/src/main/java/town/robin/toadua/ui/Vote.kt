package town.robin.toadua.ui

enum class Vote {
    NO_VOTE, LIKE, DISLIKE;

    val score: Int
        get() = when (this) {
            NO_VOTE -> 0
            LIKE -> 1
            DISLIKE -> -1
        }
}