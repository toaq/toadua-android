package town.robin.toadua.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import town.robin.toadua.R

@Composable
fun UserMenu(
    username: String?,
    onShowCreateAccount: () -> Unit,
    onShowSignIn: () -> Unit,
    onSignOut: () -> Unit,
) {
    val loggedIn = username != null

    var expanded by rememberSaveable { mutableStateOf(false) }
    var showAbout by rememberSaveable { mutableStateOf(false) }

    if (showAbout) AboutDialog(onDismiss = { showAbout = false })

    MenuButton(
        icon = Icons.Outlined.Face,
        label = username ?: stringResource(R.string.guest),
        expanded = expanded,
        onClick = { expanded = true },
        onDismissRequest = { expanded = false },
    ) {
        if (loggedIn) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.sign_out)) },
                onClick = { onSignOut(); expanded = false },
                leadingIcon = { Icon(Icons.Outlined.Logout, null) },
            )
        } else {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.create_account)) },
                onClick = { onShowCreateAccount(); expanded = false },
                leadingIcon = { Icon(Icons.Outlined.PersonAdd, null) },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.sign_in)) },
                onClick = { onShowSignIn(); expanded = false },
                leadingIcon = { Icon(Icons.Outlined.Login, null) },
            )
        }
        DropdownMenuItem(
            text = { Text(stringResource(R.string.about)) },
            onClick = { showAbout = true; expanded = false },
            leadingIcon = { Icon(Icons.Outlined.Info, null) },
        )
    }
}