package town.robin.toadua.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import town.robin.toadua.R

@Composable
fun LanguageMenu(
    language: String,
    languages: Map<String, Language>,
    onSelectLanguage: (language: String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var showNewLanguageDialog by remember { mutableStateOf(false) }

    if (showNewLanguageDialog) NewLanguageDialog(
        onDismiss = { showNewLanguageDialog = false },
        onSubmit = { onSelectLanguage(it); showNewLanguageDialog = false; expanded = false },
    )

    MenuButton(
        icon = Icons.Outlined.Language,
        label = languages[language]!!.name,
        expanded = expanded,
        onClick = { expanded = true },
        onDismissRequest = { expanded = false }
    ) {
        languages.forEach {
            DropdownMenuItem(
                text = { Text(it.value.name) },
                onClick = { onSelectLanguage(it.key); expanded = false },
                trailingIcon = { if (language == it.key) Icon(Icons.Outlined.Check, stringResource(R.string.selected)) }
            )
        }
        DropdownMenuItem(
            text = { Text(stringResource(R.string.new_language)) },
            onClick = { showNewLanguageDialog = true },
            trailingIcon = { Icon(Icons.Outlined.Add, null) },
        )
    }
}