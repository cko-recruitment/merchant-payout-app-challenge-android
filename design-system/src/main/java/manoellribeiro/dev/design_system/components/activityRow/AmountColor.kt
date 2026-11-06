package manoellribeiro.dev.design_system.components.activityRow

import androidx.compose.ui.graphics.Color
import manoellribeiro.dev.design_system.theme.MerchantColors

internal fun amountColor(isOutflow: Boolean): Color =
    if (isOutflow) MerchantColors.Error else MerchantColors.Success
