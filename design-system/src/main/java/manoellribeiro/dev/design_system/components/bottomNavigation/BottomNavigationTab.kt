package manoellribeiro.dev.design_system.components.bottomNavigation

import androidx.annotation.DrawableRes
import manoellribeiro.dev.design_system.R

/** One tab of [MerchantBottomNavigationBar]. Use R.drawable.ic_home or R.drawable.ic_payouts as the icon. */
data class BottomNavigationTab(
    val label: String,
    @param:DrawableRes val iconRes: Int,
)
