package town.robin.toadua.ui

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.times

@Composable
fun SkeletonText(
    width: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
) {
    with(LocalDensity.current) {
        val fontSize = style.fontSize.toDp()
        val lineHeight = style.lineHeight.toDp()

        Surface(
            modifier = modifier
                .padding(vertical = (lineHeight - fontSize) / 2)
                .alpha(0.3f)
                .height(fontSize)
                .width(width * fontSize),
            color = style.color.takeOrElse { LocalContentColor.current },
            shape = RoundedCornerShape(fontSize / 2),
            content = {},
        )
    }
}