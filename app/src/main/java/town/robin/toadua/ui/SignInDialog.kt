package town.robin.toadua.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import town.robin.toadua.R

@Composable
fun SignInDialog(
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    username: String,
    onUsernameChange: (username: String) -> Unit,
    password: String,
    onPasswordChange: (password: String) -> Unit,
    state: SignInState,
) {
    val busy = state == SignInState.BUSY

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = !busy && username.isNotBlank() && password.isNotBlank()
            ) { Text(stringResource(R.string.sign_in)) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
        icon = { Icon(Icons.Outlined.Login, null) },
        title = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment =  Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.sign_in), textAlign = TextAlign.Center)
                Text(
                    stringResource(R.string.join_to_collaborate),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment =  Alignment.CenterHorizontally,
            ) {
                OutlinedTextField(
                    value = username,
                    onValueChange = onUsernameChange,
                    label = { Text(stringResource(R.string.username)) },
                    singleLine = true,
                    enabled = !busy,
                    keyboardOptions = KeyboardOptions(autoCorrect = false, keyboardType = KeyboardType.Ascii),
                    isError = state == SignInState.BAD_USERNAME,
                    supportingText = (
                        if (state == SignInState.BAD_USERNAME)
                            { -> Text(
                                stringResource(R.string.cant_find_account),
                                color = MaterialTheme.colorScheme.error
                            ) }
                        else null
                    ),
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text(stringResource(R.string.password)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    enabled = !busy,
                    keyboardOptions = KeyboardOptions(autoCorrect = false, keyboardType = KeyboardType.Password),
                    isError = state == SignInState.BAD_PASSWORD,
                    supportingText = (
                        if (state == SignInState.BAD_PASSWORD)
                            { -> Text(
                                stringResource(R.string.incorrect_password),
                                color = MaterialTheme.colorScheme.error
                            ) }
                        else null
                    ),
                )
                if (state == SignInState.UNKNOWN_ERROR) {
                    Text(
                        stringResource(R.string.unknown_error),
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    )
}