package town.robin.toadua.ui

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun bareTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    errorIndicatorColor = Color.Transparent,
)

@Composable
fun errorMenuItemColors() = MenuDefaults.itemColors(
    textColor = MaterialTheme.colorScheme.error,
    leadingIconColor = MaterialTheme.colorScheme.error,
    trailingIconColor = MaterialTheme.colorScheme.error,
)

const val uiBlank = '◯'
const val modelBlank = '▯'
val cardPadding = 16.dp

val crossfade = fadeIn() togetherWith fadeOut()
val slideFadeAndReplace = fun(slideDistance: Int): ContentTransform {
    val inSpec = slideInVertically(tween(400, 400)) { -slideDistance } +
            fadeIn(tween(400, 400))
    val outSpec = slideOutVertically(tween(400)) { -slideDistance } +
            fadeOut(tween(400))
    return inSpec togetherWith outSpec
}

infix fun <T> List<T>.prepend(e: T): List<T> {
    return buildList(this.size + 1) {
        add(e)
        addAll(this@prepend)
    }
}