package manoellribeiro.dev.design_system.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import manoellribeiro.dev.design_system.theme.MerchantDimens

/** Draws the divider on the existing node, so list rows don't pay for an extra layout node each. */
internal fun Modifier.bottomHairline(color: Color): Modifier = drawBehind {
    val strokeWidth = MerchantDimens.Hairline.toPx()
    val y = size.height - strokeWidth / 2
    drawLine(color, Offset(0f, y), Offset(size.width, y), strokeWidth)
}

internal fun Modifier.topHairline(color: Color): Modifier = drawBehind {
    val strokeWidth = MerchantDimens.Hairline.toPx()
    val y = strokeWidth / 2
    drawLine(color, Offset(0f, y), Offset(size.width, y), strokeWidth)
}
