package manoellribeiro.dev.design_system.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Text style tokens. Components read these directly rather than through
 * MaterialTheme.typography, so rows in a LazyColumn skip a CompositionLocal lookup per item.
 */
object MerchantTextStyles {
    val ScreenTitle = TextStyle(fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold)
    val ModalTitle = TextStyle(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold)
    val SectionTitle = TextStyle(fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold)
    val BalanceAmount = TextStyle(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold)
    val DialogTitle = TextStyle(fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.Medium)

    val BodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp)
    val BodyLargeBold = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold)
    val BodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp)
    val Caption = TextStyle(fontSize = 12.sp, lineHeight = 16.sp)

    val FieldInput = TextStyle(fontSize = 18.sp, lineHeight = 24.sp)
    val FieldSelection = TextStyle(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold)
    val Button = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold)
    val NavigationLabel = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium)
}
