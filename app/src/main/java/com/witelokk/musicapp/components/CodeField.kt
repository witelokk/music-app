package com.witelokk.musicapp.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun CodeField(
    length: Int,
    codeText: MutableState<String>,
    isCodeWrong: MutableState<Boolean>,
    focusOnDisplay: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val fieldTexts = List(length) { rememberSaveable { mutableStateOf("") } }
    val fieldFocus = List(length) { FocusRequester() }

    fun onValueChanged(i: Int, text: String) {
        if (text.isNotEmpty() && !text.last().isDigit()) {
            return
        }

        isCodeWrong.value = false

        if (text.isEmpty()) {
            fieldTexts[i].value = ""
            if (i >= 1) {
                fieldFocus[i - 1].requestFocus()
            }
            return
        }

        fieldTexts[i].value = text.last().toString()
        if (i != length - 1) {
            fieldFocus[i + 1].requestFocus()
        }

        val textBuilder = StringBuilder()
        for (fieldText in fieldTexts) {
            textBuilder.append(fieldText.value)
        }
        codeText.value = textBuilder.toString()
    }

    LaunchedEffect(Unit) {
        if (focusOnDisplay) {
            fieldFocus[0].requestFocus()
        }
    }

    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (i in 0..<length) {
            OutlinedTextField(fieldTexts[i].value,
                enabled = enabled,
                isError = isCodeWrong.value,
                onValueChange = { onValueChanged(i, it) },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.width(40.dp).focusRequester(fieldFocus[i]).onKeyEvent {
                    if (fieldTexts[i].value.isEmpty() && it.key == Key.Backspace) {
                        isCodeWrong.value = false
                        if (i != 0) fieldFocus[i - 1].requestFocus()
                        true
                    } else {
                        false
                    }
                })
        }
    }
}
