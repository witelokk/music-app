package com.witelokk.musicapp.components

import androidx.annotation.IntRange
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun CodeField(
    @IntRange(from = 1) length: Int,
    onCodeChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester,
    isCodeInvalid: Boolean = false,
    enabled: Boolean = true,
) {
    val fieldTexts = List(length) { rememberSaveable { mutableStateOf("") } }
    val fieldFocuses = listOf(focusRequester) + List(length-1) { remember { FocusRequester() } }

    fun update() {
        val textBuilder = StringBuilder()
        for (fieldText in fieldTexts) {
            textBuilder.append(fieldText.value)
        }
        onCodeChanged(textBuilder.toString())
    }

    fun onValueChanged(i: Int, text: String) {
        if (text.isNotEmpty() && !text.last().isDigit()) {
            return update()
        }

        if (text.isEmpty()) {
            fieldTexts[i].value = ""
            if (i >= 1) {
                fieldFocuses[i - 1].requestFocus()
            }
            return update()
        }

        fieldTexts[i].value = text.last().toString()
        if (i != length - 1) {
            fieldFocuses[i + 1].requestFocus()
        }

        update()
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (i in 0..<length) {
            OutlinedTextField(
                fieldTexts[i].value,
                enabled = enabled,
                isError = isCodeInvalid,
                onValueChange = { onValueChanged(i, it) },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .width(40.dp)
                    .focusRequester(fieldFocuses[i])
                    .onKeyEvent {
                        if (fieldTexts[i].value.isEmpty() && it.key == Key.Backspace) {
                            if (i != 0) fieldFocuses[i - 1].requestFocus()
                            true
                        } else {
                            false
                        }
                    }
            )
        }
    }
}
