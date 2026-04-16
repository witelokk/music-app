package com.witelokk.musicapp.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.witelokk.musicapp.api.models.Song
import kotlinx.coroutines.suspendCancellableCoroutine
import musicapp.composeapp.generated.resources.Res
import musicapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.getString
import platform.Foundation.NSOperationQueue
import platform.UIKit.*
import kotlin.coroutines.resume

@Composable
actual fun ArtistsDialog(
    showDialog: Boolean,
    song: Song,
    onArtistSelected: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    LaunchedEffect(showDialog, song) {
        if (!showDialog) return@LaunchedEffect

        val title = getString(Res.string.artists)
        val closeTitle = getString(Res.string.close)

        showArtistsAlert(
            song = song,
            onArtistSelected = onArtistSelected,
            title = title,
            closeTitle = closeTitle,
        )
        onDismissRequest()
    }
}

private suspend fun showArtistsAlert(
    song: Song,
    onArtistSelected: (String) -> Unit,
    title: String,
    closeTitle: String,
) = suspendCancellableCoroutine<Unit> { continuation ->
    val controller = topViewController()
    if (controller == null) {
        continuation.resume(Unit)
        return@suspendCancellableCoroutine
    }

    NSOperationQueue.mainQueue.addOperationWithBlock {
        val alert = UIAlertController.alertControllerWithTitle(
            title = title,
            message = null,
            preferredStyle = UIAlertControllerStyleActionSheet
        )

        song.artists.forEach { artist ->
            val action = UIAlertAction.actionWithTitle(
                title = artist.name,
                style = UIAlertActionStyleDefault
            ) { _ ->
                onArtistSelected(artist.id)
                if (continuation.isActive) continuation.resume(Unit)
            }
            alert.addAction(action)
        }

        val close = UIAlertAction.actionWithTitle(
            title = closeTitle,
            style = UIAlertActionStyleCancel
        ) { _ ->
            if (continuation.isActive) continuation.resume(Unit)
        }
        alert.addAction(close)

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
