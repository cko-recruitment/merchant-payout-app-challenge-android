package manoellribeiro.dev.design_system.components.formField

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import manoellribeiro.dev.design_system.R
import manoellribeiro.dev.design_system.theme.MerchantColors
import manoellribeiro.dev.design_system.theme.MerchantDimens
import manoellribeiro.dev.design_system.theme.MerchantPayoutTheme
import manoellribeiro.dev.design_system.theme.MerchantShapes
import manoellribeiro.dev.design_system.theme.MerchantTextStyles

/** Dropdown with a bold label above it, used to pick the payout currency. */
@Composable
fun LabeledDropdownField(
    label: String,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MerchantDimens.SpacingSmall),
    ) {
        FieldLabel(text = label)
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = MerchantDimens.FieldMinHeight)
                    .background(MerchantColors.Surface, MerchantShapes.Control)
                    .border(FieldBorderStroke, MerchantShapes.Control)
                    .clickable(onClickLabel = label, role = Role.DropdownList) { isExpanded = true }
                    .padding(horizontal = MerchantDimens.SpacingLarge),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = selectedOption,
                    color = MerchantColors.TextPrimary,
                    style = MerchantTextStyles.FieldSelection,
                )
                Icon(
                    painter = painterResource(R.drawable.ic_dropdown_arrow),
                    contentDescription = null,
                    modifier = Modifier.size(MerchantDimens.DropdownArrowSize),
                    tint = MerchantColors.TextTertiary,
                )
            }
            DropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false },
                containerColor = MerchantColors.Surface,
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                color = MerchantColors.TextPrimary,
                                style = MerchantTextStyles.BodyLarge,
                            )
                        },
                        onClick = {
                            isExpanded = false
                            onOptionSelected(option)
                        },
                    )
                }
            }
        }
    }
}

private val FieldBorderStroke = BorderStroke(MerchantDimens.Hairline, MerchantColors.FieldBorder)

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2)
@Composable
private fun LabeledDropdownFieldPreview() {
    MerchantPayoutTheme {
        LabeledDropdownField(
            label = "Currency",
            selectedOption = "GBP",
            options = listOf("GBP", "EUR"),
            onOptionSelected = {},
            modifier = Modifier.width(100.dp),
        )
    }
}
