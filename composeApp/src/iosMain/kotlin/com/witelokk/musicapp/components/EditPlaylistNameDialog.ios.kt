package com.witelokk.musicapp.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.suspendCancellableCoroutine
import musicapp.composeapp.generated.resources.Res
import musicapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.getString
import platform.Foundation.NSOperationQueue
import platform.UIKit.*
import kotlin.coroutines.resume

@Composable
actual fun EditPlaylistNameDialog(
    showDialog: Boolean,
    currentName: String,
    onNameChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    LaunchedEffect(showDialog, currentName) {
        if (!showDialog) return@LaunchedEffect

        val title = getString(Res.string.edit_playlist_name_dialog_title)
        val cancelTitle = getString(Res.string.cancel)
        val yesTitle = getString(Res.string.yes)

        val newName = showEditPlaylistAlert(
            currentName = currentName,
            title = title,
            cancelTitle = cancelTitle,
            yesTitle = yesTitle,
        )
        if (!newName.isNullOrBlank() && newName != currentName) {
            onNameChange(newName)
            onConfirm()
        } else {
            onDismissRequest()
        }
    }
}

private suspend fun showEditPlaylistAlert(
    currentName: String,
    title: String,
    cancelTitle: String,
    yesTitle: String,
): String? =
    suspendCancellableCoroutine { continuation ->
        val controller = topViewController()
        if (controller == null) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        NSOperationQueue.mainQueue.addOperationWithBlock {
            val alert = UIAlertController.alertControllerWithTitle(
                title = title,
                message = null,
                preferredStyle = UIAlertControllerStyleAlert
            )

            alert.addTextFieldWithConfigurationHandler { textField ->
                textField?.text = currentName
            }

            val cancel = UIAlertAction.actionWithTitle(
                title = cancelTitle,
                style = UIAlertActionStyleCancel
            ) { _ ->
                if (continuation.isActive) continuation.resume(null)
            }

            val save = UIAlertAction.actionWithTitle(
                title = yesTitle,
                style = UIAlertActionStyleDefault
            ) { _ ->
                val textField = alert.textFields?.first() as? UITextField
                val value = textField?.text?.trim()
                if (continuation.isActive) {
                    continuation.resume(value?.takeIf { it.isNotEmpty() })
                }
            }

            alert.addAction(cancel)
            alert.addAction(save)

            controller.presentViewController(alert, true, completion = null)
        }
    }

private fun topViewController(): UIViewController? {
    val app = UIApplication.sharedApplication
    val keyWindow = app.keyWindow ?: app.windows.firstOrNull() as? UIWindow
    var root = keyWindow?.rootViewController
    while (root?.presentedViewController != null) {
        root = root.presentedViewController
    }
    return root
}
