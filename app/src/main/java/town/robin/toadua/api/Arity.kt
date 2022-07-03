package town.robin.toadua.api

enum class Arity {
    NULLARY, UNARY, BINARY, TERNARY;

    val number: Int
        get() = when (this) {
            NULLARY -> 0
            UNARY -> 1
            BINARY -> 2
            TERNARY -> 3
        }
}