package manoellribeiro.dev.design_system.components.bottomNavigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import manoellribeiro.dev.design_system.R
import manoellribeiro.dev.design_system.components.topHairline
import manoellribeiro.dev.design_system.theme.MerchantColors
import manoellribeiro.dev.design_system.theme.MerchantPayoutTheme
import manoellribeiro.dev.design_system.theme.MerchantTextStyles

/** App-wide tab bar. Keep [tabs] in a stable instance so the bar can skip recomposition. */
@Composable
fun MerchantBottomNavigationBar(
    tabs: List<BottomNavigationTab>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier.topHairline(MerchantColors.BarDivider),
        containerColor = MerchantColors.Surface,
        tonalElevation = 0.dp,
    ) {
        val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = MerchantColors.NavigationSelected,
            selectedTextColor = MerchantColors.NavigationSelected,
            unselectedIconColor = MerchantColors.NavigationUnselected,
            unselectedTextColor = MerchantColors.NavigationUnselected,
            indicatorColor = Color.Transparent,
        )
        tabs.forEachIndexed { index, tab ->
            NavigationBarItem(
                selected = index == selectedTabIndex,
                onClick = { onTabSelected(index) },
                icon = {
                    // The visible label already names the tab for TalkBack.
                    Icon(painter = painterResource(tab.iconRes), contentDescription = null)
                },
                label = { Text(text = tab.label, style = MerchantTextStyles.NavigationLabel) },
                colors = itemColors,
            )
        }
    }
}

private val PreviewTabs = listOf(
    BottomNavigationTab(label = "Home", iconRes = R.drawable.ic_home),
    BottomNavigationTab(label = "Payouts", iconRes = R.drawable.ic_payouts),
)

@Preview(showBackground = true)
@Composable
private fun MerchantBottomNavigationBarHomeSelectedPreview() {
    MerchantPayoutTheme {
        MerchantBottomNavigationBar(tabs = PreviewTabs, selectedTabIndex = 0, onTabSelected = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun MerchantBottomNavigationBarPayoutsSelectedPreview() {
    MerchantPayoutTheme {
        MerchantBottomNavigationBar(tabs = PreviewTabs, selectedTabIndex = 1, onTabSelected = {})
    }
}
