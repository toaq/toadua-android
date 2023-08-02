package town.robin.toadua.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonAdd
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
fun CreateAccountDialog(
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    username: String,
    onUsernameChange: (username: String) -> Unit,
    password: String,
    onPasswordChange: (password: String) -> Unit,
    antiSpamAnswer: String,
    onAntiSpamAnswerChange: (antiSpamAnswer: String) -> Unit,
    state: CreateAccountState,
) {
    val busy = state == CreateAccountState.BUSY

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = username.isNotBlank() && password.isNotEmpty() && antiSpamAnswer.isNotBlank() && !busy
            ) { Text(stringResource(R.string.sign_up)) }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
        icon = { Icon(Icons.Outlined.PersonAdd, null) },
        title = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.create_account), textAlign = TextAlign.Center)
                Text(
                    stringResource(R.string.join_to_collaborate),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                OutlinedTextField(
                    value = username,
                    onValueChange = onUsernameChange,
                    label = { Text(stringResource(R.string.username)) },
                    singleLine = true,
                    enabled = !busy,
                    keyboardOptions = KeyboardOptions(
                        autoCorrect = false,
                        keyboardType = KeyboardType.Ascii
                    ),
                    isError = state == CreateAccountState.USERNAME_TAKEN,
                    supportingText = (
                        if (state == CreateAccountState.USERNAME_TAKEN)
                            { ->
                                Text(
                                    stringResource(R.string.username_taken),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
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
                    keyboardOptions = KeyboardOptions(
                        autoCorrect = false,
                        keyboardType = KeyboardType.Password
                    ),
                )
                OutlinedTextField(
                    value = antiSpamAnswer,
                    onValueChange = onAntiSpamAnswerChange,
                    label = { Text("Raq ní toakue hí zu?") },
                    singleLine = true,
                    enabled = !busy,
                    isError = state == CreateAccountState.BAD_ANTI_SPAM_ANSWER,
                )
                if (state == CreateAccountState.UNKNOWN_ERROR) {
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