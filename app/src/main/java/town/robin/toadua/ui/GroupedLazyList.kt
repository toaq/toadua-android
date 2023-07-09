package town.robin.toadua.ui

import android.os.Parcelable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

interface GroupedLazyListScope : LazyListScope {
    fun group(key: Any, spacedBy: Dp = 0.dp, content: GroupedLazyListScope.() -> Unit)
}

@Parcelize
private data class ScopedKey(val key: @RawValue Any, val scopeKey: @RawValue Any) : Parcelable

private fun withGroups(
    scope: LazyListScope,
    spacedBy: Dp,
    content: GroupedLazyListScope.() -> Unit,
) {
    content.invoke(object : GroupedLazyListScope {
        var scopeKey: Any? = null
        var index = 0
        var spaceBefore = 0.dp
        var spaceAfter = spacedBy

        override fun item(
            key: Any?,
            contentType: Any?,
            content: @Composable LazyItemScope.() -> Unit,
        ) {
            val scopedKey = scopeKey?.let {
                ScopedKey(key ?: index, it)
            } ?: key ?: index
            spaceBefore.let { spaceBefore ->
                scope.item(scopedKey, contentType) {
                    if (spaceBefore != 0.dp) Spacer(Modifier.height(spaceBefore))
                    content()
                }
            }
            index++
            spaceBefore = spaceAfter
        }

        override fun items(
            count: Int,
            key: ((index: Int) -> Any)?,
            contentType: (index: Int) -> Any?,
            itemContent: @Composable LazyItemScope.(index: Int) -> Unit,
        ) {
            val scopedKey = index.let { baseIndex ->
                (key ?: { index -> baseIndex + index }).let { key ->
                    scopeKey?.let { scopeKey ->
                        key.let {
                            { index: Int ->
                                ScopedKey(it(index), scopeKey)
                            }
                        }
                    } ?: key
                }
            }
            spaceBefore.let { spaceBefore ->
                spaceAfter.let { spaceAfter ->
                    scope.items(count, scopedKey, contentType) { index ->
                        val currentSpacing = if (index == 0) spaceBefore else spaceAfter
                        if (currentSpacing != 0.dp) Spacer(Modifier.height(currentSpacing))
                        itemContent(index)
                    }
                }
            }
            index += count
            spaceBefore = spaceAfter
        }

        override fun group(key: Any, spacedBy: Dp, content: GroupedLazyListScope.() -> Unit) {
            val prevScopeKey = scopeKey
            val prevSpaceAfter = spaceAfter
            scopeKey = prevScopeKey?.let { ScopedKey(key, it) } ?: key
            spaceAfter = spacedBy
            content()
            scopeKey = prevScopeKey
            spaceBefore = prevSpaceAfter
        }

        @ExperimentalFoundationApi
        override fun stickyHeader(
            key: Any?,
            contentType: Any?,
            content: @Composable LazyItemScope.() -> Unit,
        ) {
            val scopedKey = scopeKey?.let {
                ScopedKey(key ?: index, it)
            } ?: key ?: index
            spaceBefore.let { spaceBefore ->
                scope.stickyHeader(scopedKey, contentType) {
                    if (spaceBefore != 0.dp) Spacer(Modifier.height(spaceBefore))
                    content()
                }
            }
            index++
            spaceBefore = spaceAfter
        }
    })
}

@Composable
fun GroupedLazyColumn(
    modifier: Modifier = Modifier,
    spacedBy: Dp = 0.dp,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    content: @ExtensionFunctionType GroupedLazyListScope.() -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled,
        content = { withGroups(this, spacedBy, content) },
    )
}