package town.robin.toadua.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import town.robin.toadua.BuildConfig
import town.robin.toadua.R

@Composable
fun IncompatibleVersionsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            OutlinedButton(onClick = onDismiss) { Text(stringResource(R.string.ok)) }
        },
        icon = { Icon(Icons.Outlined.ErrorOutline, null) },
        title = { Text(stringResource(R.string.out_of_date)) },
        text = {
            Text(stringResource(R.string.out_of_date_description))
        }
    )
}