package town.robin.toadua.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowRow

@Composable
fun ChipGroup(
    modifier: Modifier = Modifier,
    label: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        label?.let { key("label") { it() } }
        key("chips") {
            FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = (-6).dp, content = content)
        }
    }
}