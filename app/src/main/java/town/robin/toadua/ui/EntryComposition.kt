package town.robin.toadua.ui

data class EntryComposition(
    val term: String,
    val definition: String,
    val busy: Boolean,
    val initialTerm: String = term,
    val initialDefinition: String = definition,
)

val blankEntryComposition = EntryComposition("", "", false)