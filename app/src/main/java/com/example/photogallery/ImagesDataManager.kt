package com.example.photogallery

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore

/**
 * Class to fetch all images from the device
 */
class ImagesDataManager {
    /**
     * Data class to hold the image data
     * @param uri: Uri of the image
     * @param size: Size of the image
     * @param unit: Unit of the size
     */
    data class ImageData(val uri: Uri, val size: String, val unit: String)

    companion object {
        /**
         * Function to fetch all images from the device
         * @param contentResolver: ContentResolver
         * @return List<ImageData>: List of image data
         */
        fun fetchAllImages(contentResolver: ContentResolver): List<ImageData> {
            val imageDataList = mutableListOf<ImageData>()
            val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.SIZE)
            val cursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                "${MediaStore.Images.Media.DATE_ADDED} ASC"
            )
//            if (cursor != null && cursor.count > 10) {
//                cursor.moveToPosition(9)
//                cursor.setNotificationUri(
//                    contentResolver,
//                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//                )
//            }

            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val sizeInBytes = it.getLong(sizeColumn)
                    var sizeInMB = sizeInBytes / (1024.0f * 1024.0f)
                    var size: String
                    var unit: String
                    if (sizeInMB < 1) {
                        size = String.format("%.1f", sizeInBytes / 1024.0f)
                        unit = "KB"
                    } else {
                        size = String.format("%.1f", sizeInMB)
                        unit = "MB"
                    }
                    val uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())
                    imageDataList.add(ImageData(uri, size, unit))
                }
            }
            return imageDataList
        }
    }
}