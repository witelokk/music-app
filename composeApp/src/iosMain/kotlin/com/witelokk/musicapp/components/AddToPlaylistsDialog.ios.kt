package com.witelokk.musicapp.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.witelokk.musicapp.api.models.PlaylistSummary
import com.witelokk.musicapp.logd
import kotlinx.coroutines.suspendCancellableCoroutine
import musicapp.composeapp.generated.resources.Res
import musicapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.getString
import platform.Foundation.NSOperationQueue
import platform.UIKit.*
import kotlin.coroutines.resume

@Composable
actual fun AddToPlaylistsDialog(
    showDialog: Boolean,
    playlists: List<PlaylistSummary>,
    onDismissRequest: () -> Unit,
    onAddRequest: (List<String>) -> Unit,
) {
    LaunchedEffect(showDialog, playlists) {
        if (!showDialog || playlists.isEmpty()) return@LaunchedEffect

        val title = getString(Res.string.add_to_playlists_dialog_title)
        val cancelTitle = getString(Res.string.cancel)

        val selected = showAddToPlaylistsAlert(
            playlists = playlists,
            title = title,
            cancelTitle = cancelTitle,
        )
        if (selected.isNotEmpty()) {
            onAddRequest(selected)
        } else {
            onDismissRequest()
        }
    }
}

private suspend fun showAddToPlaylistsAlert(
    playlists: List<PlaylistSummary>,
    title: String,
    cancelTitle: String,
): List<String> = suspendCancellableCoroutine { continuation ->
    val controller = topViewController()
    if (controller == null) {
        continuation.resume(emptyList())
        return@suspendCancellableCoroutine
    }

    NSOperationQueue.mainQueue.addOperationWithBlock {
        val alert = UIAlertController.alertControllerWithTitle(
            title = title,
            message = null,
            preferredStyle = UIAlertControllerStyleActionSheet
        )

        playlists.forEach { playlist ->
            val action = UIAlertAction.actionWithTitle(
                title = playlist.name,
                style = UIAlertActionStyleDefault
            ) { _ ->
                if (continuation.isActive) {
                    continuation.resume(listOf(playlist.id))
                }
            }
            alert.addAction(action)
        }

        val cancel = UIAlertAction.actionWithTitle(
            title = cancelTitle,
            style = UIAlertActionStyleCancel
        ) { _ ->
            if (continuation.isActive) continuation.resume(emptyList())
        }
        alert.addAction(cancel)

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
