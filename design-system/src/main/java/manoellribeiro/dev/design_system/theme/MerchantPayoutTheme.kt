package manoellribeiro.dev.design_system.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/** Shape tokens shared by buttons, fields and dialogs. */
object MerchantShapes {
    val Control = RoundedCornerShape(MerchantDimens.CornerRadiusSmall)
    val Dialog = RoundedCornerShape(MerchantDimens.CornerRadiusLarge)
}

private val MerchantColorScheme = lightColorScheme(
    primary = MerchantColors.Primary,
    onPrimary = MerchantColors.OnPrimary,
    background = MerchantColors.ScreenBackground,
    onBackground = MerchantColors.TextPrimary,
    surface = MerchantColors.Surface,
    onSurface = MerchantColors.TextPrimary,
    surfaceContainer = MerchantColors.Surface,
    error = MerchantColors.Error,
    outline = MerchantColors.FieldBorder,
)

private val MerchantTypography = Typography(
    headlineLarge = MerchantTextStyles.ScreenTitle,
    headlineSmall = MerchantTextStyles.ModalTitle,
    titleLarge = MerchantTextStyles.SectionTitle,
    bodyLarge = MerchantTextStyles.BodyLarge,
    bodyMedium = MerchantTextStyles.BodyMedium,
    bodySmall = MerchantTextStyles.Caption,
    labelLarge = MerchantTextStyles.Button,
    labelMedium = MerchantTextStyles.NavigationLabel,
)

/** Configures the Material components the design system builds on. Dynamic colour is off to keep the brand palette. */
@Composable
fun MerchantPayoutTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MerchantColorScheme,
        typography = MerchantTypography,
        content = content,
    )
}
