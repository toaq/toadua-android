package town.robin.toadua.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun FieldCard(
    value: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    onFocusChanged: (FocusState) -> Unit = { },
    label: (@Composable () -> Unit)? = null,
    placeholder: (@Composable () -> Unit)? = null,
    icon: ImageVector,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    expanded: Boolean,
    expandedContent: @Composable () -> Unit,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var focused by remember { mutableStateOf(false) }

    Card(modifier.bringIntoViewRequester(bringIntoViewRequester)) {
        Column {
            TextField(
                value = value,
                onValueChange = onValueChange,
                label = label,
                placeholder = placeholder,
                leadingIcon = leadingIcon,
                trailingIcon = {
                    if (focused || expanded) trailingIcon?.invoke()
                    else Icon(imageVector = icon, contentDescription = null)
                },
                singleLine = true,
                colors = bareTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged {
                        focused = it.isFocused
                        onFocusChanged(it)
                    },
                enabled = enabled,
            )
            AnimatedVisibility(visible = expanded) {
                // Bring into view once the animation is settled
                LaunchedEffect(transition.currentState) {
                    if (transition.currentState == EnterExitState.Visible)
                        bringIntoViewRequester.bringIntoView()
                }
                expandedContent()
            }
        }
    }
}