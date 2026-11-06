package manoellribeiro.dev.design_system.components.activityRow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import manoellribeiro.dev.design_system.theme.MerchantColors
import manoellribeiro.dev.design_system.theme.MerchantDimens
import manoellribeiro.dev.design_system.theme.MerchantPayoutTheme
import manoellribeiro.dev.design_system.theme.MerchantTextStyles

/** Footer shown at the end of the list while the next page loads. */
@Composable
fun LoadingMoreIndicator(label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {}
            .padding(vertical = MerchantDimens.SpacingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MerchantDimens.SpacingSmall),
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(MerchantDimens.LoadingIndicatorSize),
            color = MerchantColors.Accent,
            strokeWidth = MerchantDimens.LoadingIndicatorStroke,
        )
        Text(
            text = label,
            color = MerchantColors.TextSecondary,
            style = MerchantTextStyles.BodyMedium,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingMoreIndicatorPreview() {
    MerchantPayoutTheme { LoadingMoreIndicator(label = "Loading more...") }
}
