package com.example.photogallery

import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startIntentSenderForResult
import androidx.core.content.ContextCompat
import com.example.photogallery.ImagesDataManager.Companion.fetchAllImages

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED-> {
                    // You can use the API that requires the permission.
                    setContent {
                        PhotoGalleryApp(this)
                    }
                }

                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.READ_MEDIA_IMAGES
                )  -> {
                    // In an educational UI, explain to the user why your app requires these permissions for a specific feature to behave as expected.
                    // In this UI, include a "cancel" or "no thanks" button that lets the user continue using your app without granting the permissions.
                    setContent {
                        GrantPermissionUI()
                    }
                }

                else -> {
                    requestPermissions(
                        arrayOf(
                            android.Manifest.permission.READ_MEDIA_IMAGES
                        ),
                        1
                    )

                }
            }
        }
}

@Composable
fun GrantPermissionUI(){
    Text(
        text = "Please grant permission to access photos",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        textAlign = TextAlign.Center
    )

}
@Composable
fun PhotoGalleryApp(activity: Context) {
        val context = LocalContext.current
        var photoUris = remember { fetchAllImages(context.contentResolver) }
        PhotoList(
            activity,
            photoUris,
            onDelete = {uri ->
            deleteImage(activity, context.contentResolver, uri)
            photoUris = photoUris.filterNot { it.uri == uri }
        })

}

/**
    * This composable function displays a list of photos in a stack of cards.
    * The top card can be swiped left or right to delete or save the photo.
    * The top card is draggable horizontally.
    * The background color changes to red or green based on the direction of the swipe.
    * The top card is draggable horizontally.
    * @param activity The context of the activity
    * @param photoUris The list of photo URIs, and image size and unit
    * @param onDelete The callback function to delete the photo
 */
@Composable
fun PhotoList(activity: Context, photoUris: List<ImagesDataManager.ImageData>, onDelete: (Uri) -> Unit) {
    PhotoCardStack(activity, photoUris, onDelete)
}


/**
 * This function deletes an image from the gallery.
 * @param activity The context of the activity
 * @param contentResolver The content resolver
 * @param uri The URI of the image to be deleted
 */
fun deleteImage(activity: Context, contentResolver: ContentResolver, uri: Uri) {
    try {
        contentResolver.delete(uri, null, null)
    } catch (securityException: SecurityException) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val recoverableSecurityException = securityException as?
                    RecoverableSecurityException ?:
            throw RuntimeException(securityException.message, securityException)

            val intentSender =
                recoverableSecurityException.userAction.actionIntent.intentSender

            intentSender?.let {
                startIntentSenderForResult(
                    activity as Activity, intentSender , 1,
                    null, 0, 0, 0, null)
            }
        } else {
            throw RuntimeException(securityException.message, securityException)
        }
    } catch (runtimeException: RuntimeException) {
        // Handle the RuntimeException here
        Toast.makeText(activity, "Failed to delete image. The image may no longer exist.", Toast.LENGTH_SHORT).show()
    }
}



