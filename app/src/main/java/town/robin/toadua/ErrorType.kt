package town.robin.toadua

enum class ErrorType(val string: Int) {
    SIGN_IN(R.string.sign_in_error),
    CREATE_ACCOUNT(R.string.create_account_error),
    SEARCH(R.string.search_error),
    CREATE(R.string.create_error),
    DELETE(R.string.delete_error),
    VOTE(R.string.vote_error),
    COMMENT(R.string.comment_error),
}