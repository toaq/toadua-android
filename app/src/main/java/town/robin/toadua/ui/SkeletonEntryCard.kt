package town.robin.toadua.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import town.robin.toadua.R

@Composable
fun SkeletonEntryCard(modifier: Modifier = Modifier) {
    val normalRadius = 12.dp
    val smallRadius = 4.dp
    val normalPadding = cardPadding
    val smallPadding = cardPadding / 2
    val smallNotePadding = cardPadding * 3 / 4
    val recessOutset = 4.dp

    val blankIcon = ImageVector.vectorResource(R.drawable.blank)

    Column(modifier) {
        Card(
            shape = RoundedCornerShape(
                topStart = normalRadius,
                topEnd = normalRadius,
                bottomStart = smallRadius,
                bottomEnd = smallRadius,
            ),
        ) {
            Column(
                modifier = Modifier.padding(
                    start = normalPadding - recessOutset,
                    top = normalPadding,
                    end = normalPadding - recessOutset,
                    bottom = smallPadding
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    Modifier.padding(horizontal = recessOutset),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SkeletonText(4, style = MaterialTheme.typography.titleLarge)
                    SkeletonText(20, style = MaterialTheme.typography.bodyMedium)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ProvideTextStyle(MaterialTheme.typography.titleSmall) {
                        RecessedSurface {
                            Box(
                                Modifier
                                    .padding(horizontal = 16.dp)
                                    .height(40.dp)
                                    .defaultMinSize(minWidth = 40.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                SkeletonText(4, Modifier.align(Alignment.Center))
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            repeat(3) {
                                RecessedIconButton(
                                    icon = blankIcon,
                                    onClick = {},
                                    contentDescription = null,
                                )
                            }
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(
                topStart = smallRadius,
                topEnd = smallRadius,
                bottomStart = normalRadius,
                bottomEnd = normalRadius,
            ),
        ) {
            Row(
                modifier = Modifier.padding(
                    start = normalPadding,
                    top = smallNotePadding,
                    end = normalPadding,
                    bottom = smallNotePadding,
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SkeletonText(4, style = MaterialTheme.typography.titleSmall)
                SkeletonText(16, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}