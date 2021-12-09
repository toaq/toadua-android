package town.robin.toadua

enum class UpdateAction { ADD, REMOVE, MODIFY }

class LiveList<T>(val list: MutableList<T>, val updateIndex: Int?, val updateAction: UpdateAction)