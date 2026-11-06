package manoellribeiro.dev.design_system.components.button

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import manoellribeiro.dev.design_system.theme.MerchantColors
import manoellribeiro.dev.design_system.theme.MerchantShapes
import manoellribeiro.dev.design_system.theme.MerchantTextStyles

@Composable
internal fun MerchantButton(
    label: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    minHeight: Dp,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = minHeight),
        enabled = enabled,
        shape = MerchantShapes.Control,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = MerchantColors.PrimaryDisabled,
            disabledContentColor = MerchantColors.OnPrimaryDisabled,
        ),
    ) {
        Text(text = label, style = MerchantTextStyles.Button)
    }
}
