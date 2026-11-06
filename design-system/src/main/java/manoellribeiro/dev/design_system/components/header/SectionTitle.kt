package manoellribeiro.dev.design_system.components.header

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import manoellribeiro.dev.design_system.theme.MerchantColors
import manoellribeiro.dev.design_system.theme.MerchantPayoutTheme
import manoellribeiro.dev.design_system.theme.MerchantTextStyles

/** Title of a block within a screen, e.g. "Recent Activity". */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.semantics { heading() },
        color = MerchantColors.TextPrimary,
        style = MerchantTextStyles.SectionTitle,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2)
@Composable
private fun SectionTitlePreview() {
    MerchantPayoutTheme { SectionTitle(text = "Recent Activity") }
}
