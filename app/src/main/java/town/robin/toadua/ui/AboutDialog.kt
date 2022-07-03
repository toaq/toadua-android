package town.robin.toadua.ui

import androidx.compose.material.icons.Icons
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
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            OutlinedButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        },
        icon = { Icon(Icons.Outlined.Info, null) },
        title = { Text(stringResource(R.string.about_toadua)) },
        text = {
            Text(stringResource(R.string.about_toadua_description, BuildConfig.VERSION_NAME))
        }
    )
}