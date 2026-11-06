package manoellribeiro.dev.design_system.components.formField

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import manoellribeiro.dev.design_system.theme.MerchantColors
import manoellribeiro.dev.design_system.theme.MerchantDimens
import manoellribeiro.dev.design_system.theme.MerchantPayoutTheme
import manoellribeiro.dev.design_system.theme.MerchantShapes
import manoellribeiro.dev.design_system.theme.MerchantTextStyles

/**
 * Text input with a bold label above it. [errorMessage] replaces [helperText] when present, and
 * the field stays stateless so the ViewModel can own the value through SavedStateHandle.
 */
@Composable
fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    helperText: String? = null,
    errorMessage: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MerchantDimens.SpacingSmall),
    ) {
        FieldLabel(text = label)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = label },
            textStyle = MerchantTextStyles.FieldInput,
            placeholder = placeholder?.let { text ->
                {
                    Text(
                        text = text,
                        maxLines = 1,
                        color = MerchantColors.TextPlaceholder,
                        style = MerchantTextStyles.FieldInput,
                    )
                }
            },
            supportingText = (errorMessage ?: helperText)?.let { text ->
                { Text(text = text, style = MerchantTextStyles.Caption) }
            },
            isError = errorMessage != null,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            shape = MerchantShapes.Control,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MerchantColors.Surface,
                unfocusedContainerColor = MerchantColors.Surface,
                errorContainerColor = MerchantColors.Surface,
                focusedTextColor = MerchantColors.TextPrimary,
                unfocusedTextColor = MerchantColors.TextPrimary,
                focusedBorderColor = MerchantColors.Primary,
                unfocusedBorderColor = MerchantColors.FieldBorder,
                focusedSupportingTextColor = MerchantColors.TextHelper,
                unfocusedSupportingTextColor = MerchantColors.TextHelper,
            ),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2)
@Composable
private fun LabeledTextFieldEmptyPreview() {
    MerchantPayoutTheme {
        LabeledTextField(
            label = "IBAN",
            value = "",
            onValueChange = {},
            placeholder = "GB29NWBK60161331926819",
            helperText = "Enter the destination bank account IBAN",
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2)
@Composable
private fun LabeledTextFieldFilledPreview() {
    MerchantPayoutTheme {
        LabeledTextField(
            label = "Amount",
            value = "400",
            onValueChange = {},
            keyboardType = KeyboardType.Decimal,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2)
@Composable
private fun LabeledTextFieldErrorPreview() {
    MerchantPayoutTheme {
        LabeledTextField(
            label = "IBAN",
            value = "12",
            onValueChange = {},
            helperText = "Enter the destination bank account IBAN",
            errorMessage = "Enter a valid IBAN",
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF2F2F2)
@Composable
private fun AmountAndCurrencyRowPreview() {
    MerchantPayoutTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(MerchantDimens.SpacingMedium)) {
            LabeledTextField(
                label = "Amount",
                value = "",
                onValueChange = {},
                modifier = Modifier.weight(1f),
                placeholder = "0.00",
                keyboardType = KeyboardType.Decimal,
            )
            LabeledDropdownField(
                label = "Currency",
                selectedOption = "GBP",
                options = listOf("GBP", "EUR"),
                onOptionSelected = {},
                modifier = Modifier.width(100.dp),
            )
        }
    }
}
