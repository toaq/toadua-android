package town.robin.toadua.ui

import town.robin.toadua.api.Note

data class Comment(val user: String, val content: String) {
    constructor(note: Note) : this(note.user, note.content.replace(modelBlank, uiBlank))
}