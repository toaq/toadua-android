package town.robin.toadua.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

fun listSection(
    scope: GroupedLazyListScope,
    key: Any,
    title: @Composable () -> Unit,
    controls: (@Composable RowScope.() -> Unit)? = null,
    content: GroupedLazyListScope.() -> Unit,
) {
    scope.run {
        group(key) {
            item("title") {
                Row(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 4.dp)
                        .height(48.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ProvideTextStyle(MaterialTheme.typography.labelLarge, content = title)
                    if (controls != null) Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        content = controls,
                    )
                }
            }
            group("entries", spacedBy = 16.dp, content = content)
        }
    }
}