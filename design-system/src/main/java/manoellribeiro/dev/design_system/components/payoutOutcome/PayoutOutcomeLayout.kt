package manoellribeiro.dev.design_system.components.payoutOutcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import manoellribeiro.dev.design_system.components.button.PrimaryActionButton
import manoellribeiro.dev.design_system.theme.MerchantDimens
import manoellribeiro.dev.design_system.theme.MerchantTextStyles

@Composable
internal fun PayoutOutcomeLayout(
    title: String,
    titleColor: Color,
    message: String,
    messageColor: Color,
    actionLabel: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(MerchantDimens.SpacingHuge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MerchantDimens.SpacingExtraLarge),
    ) {
        icon()
        Text(
            text = title,
            modifier = Modifier.semantics { heading() },
            color = titleColor,
            textAlign = TextAlign.Center,
            style = MerchantTextStyles.ModalTitle,
        )
        Text(
            text = message,
            color = messageColor,
            textAlign = TextAlign.Center,
            style = MerchantTextStyles.BodyLarge,
        )
        PrimaryActionButton(label = actionLabel, onClick = onActionClick)
    }
}
