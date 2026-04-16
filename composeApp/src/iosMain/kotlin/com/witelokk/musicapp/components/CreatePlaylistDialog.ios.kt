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
actual fun CreatePlaylistDialog(
    showDialog: Boolean,
    playlistName: String,
    onNameChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onCreate: () -> Unit,
) {
    LaunchedEffect(showDialog) {
        if (!showDialog) return@LaunchedEffect

        val title = getString(Res.string.create_playlist)
        val placeholder = getString(Res.string.name)
        val cancelTitle = getString(Res.string.cancel)
        val createTitle = getString(Res.string.create)

        val name = showCreatePlaylistAlert(
            title = title,
            placeholder = placeholder,
            cancelTitle = cancelTitle,
            createTitle = createTitle,
        )
        if (!name.isNullOrBlank()) {
            onNameChange(name)
            onCreate()
        } else {
            onDismissRequest()
        }
    }
}

private suspend fun showCreatePlaylistAlert(
    title: String,
    placeholder: String,
    cancelTitle: String,
    createTitle: String,
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
                textField?.placeholder = placeholder
            }

            val cancel = UIAlertAction.actionWithTitle(
                title = cancelTitle,
                style = UIAlertActionStyleCancel
            ) { _ ->
                if (continuation.isActive) continuation.resume(null)
            }

            val create = UIAlertAction.actionWithTitle(
                title = createTitle,
                style = UIAlertActionStyleDefault
            ) { _ ->
                val textField = alert.textFields?.first() as? UITextField
                val value = textField?.text?.trim()
                if (continuation.isActive) {
                    continuation.resume(value?.takeIf { it.isNotEmpty() })
                }
            }

            alert.addAction(cancel)
            alert.addAction(create)

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
