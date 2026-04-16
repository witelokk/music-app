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
actual fun DeletePlaylistDialog(
    showDialog: Boolean,
    playlistName: String,
    onDismissRequest: () -> Unit,
    onConfirmDelete: () -> Unit,
) {
    LaunchedEffect(showDialog, playlistName) {
        if (!showDialog) return@LaunchedEffect

        val title = getString(Res.string.delete_playlists_dialog_title)
        val message = getString(Res.string.delete_playlists_dialog_text, playlistName)
        val cancelTitle = getString(Res.string.cancel)
        val deleteTitle = getString(Res.string.delete)

        val confirmed = showDeletePlaylistAlert(
            title = title,
            message = message,
            cancelTitle = cancelTitle,
            deleteTitle = deleteTitle,
        )
        if (confirmed) {
            onConfirmDelete()
        } else {
            onDismissRequest()
        }
    }
}

private suspend fun showDeletePlaylistAlert(
    title: String,
    message: String,
    cancelTitle: String,
    deleteTitle: String,
): Boolean =
    suspendCancellableCoroutine { continuation ->
        val controller = topViewController()
        if (controller == null) {
            continuation.resume(false)
            return@suspendCancellableCoroutine
        }
        
        NSOperationQueue.mainQueue.addOperationWithBlock {
            val alert = UIAlertController.alertControllerWithTitle(
                title = title,
                message = message,
                preferredStyle = UIAlertControllerStyleAlert
            )

            val cancel = UIAlertAction.actionWithTitle(
                title = cancelTitle,
                style = UIAlertActionStyleCancel
            ) { _ ->
                if (continuation.isActive) continuation.resume(false)
            }

            val delete = UIAlertAction.actionWithTitle(
                title = deleteTitle,
                style = UIAlertActionStyleDestructive
            ) { _ ->
                if (continuation.isActive) continuation.resume(true)
            }

            alert.addAction(cancel)
            alert.addAction(delete)

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
