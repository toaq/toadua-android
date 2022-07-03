package town.robin.toadua.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun MenuButton(
    icon: ImageVector,
    label: String,
    expanded: Boolean,
    onClick: () -> Unit,
    onDismissRequest: () -> Unit,
    menuContent: @Composable ColumnScope.() -> Unit,
) {
    Box {
        FilledTonalButton(onClick = onClick, contentPadding = ButtonDefaults.ButtonWithIconContentPadding) {
            Icon(icon, null, modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(label)
        }
        DropdownMenu(
            modifier = Modifier.defaultMinSize(minWidth = 150.dp),
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            content = menuContent,
        )
    }
}