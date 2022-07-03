package town.robin.toadua.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableChipBorder
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.SelectableChipElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun recessedFilterChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = MaterialTheme.colorScheme.background,
    selectedContainerColor = MaterialTheme.colorScheme.secondary,
    selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
    selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondary,
    selectedTrailingIconColor = MaterialTheme.colorScheme.onSecondary,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecessedFilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    shape: Shape = FilterChipDefaults.shape,
    colors: SelectableChipColors = recessedFilterChipColors(),
    elevation: SelectableChipElevation? = FilterChipDefaults.filterChipElevation(),
    border: SelectableChipBorder? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) = FilterChip(
    selected,
    onClick,
    label,
    modifier,
    enabled,
    leadingIcon,
    trailingIcon,
    shape,
    colors,
    elevation,
    border,
    interactionSource,
)