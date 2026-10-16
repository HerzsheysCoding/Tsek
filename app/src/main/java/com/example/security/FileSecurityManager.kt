package com.example.security

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.InputStream

/**
 * Enforces Point 16: Restrict file uploads
 * - Enforces allowed image MIME types (JPEG, PNG, WEBP)
 * - Restricts file size (max 8MB to prevent DoS / OOM crashes)
 * - Strips EXIF metadata & downsizes oversized dimensions (max 2048x2048)
 */
object FileSecurityManager {

    private const val TAG = "FileSecurityManager"
    private const val MAX_FILE_SIZE_BYTES = 8 * 1024 * 1024 // 8 MB
    private const val MAX_DIMENSION = 2048

    private val ALLOWED_MIME_TYPES = setOf(
        "image/jpeg",
        "image/jpg",
        "image/png",
        "image/webp"
    )

    data class FileValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null,
        val sanitizedBitmap: Bitmap? = null
    )

    /**
     * Point 16: Validates and securely processes a selected image screenshot.
     */
    fun validateAndProcessImage(context: Context, uri: Uri): FileValidationResult {
        try {
            val contentResolver = context.contentResolver

            // 1. MIME Type check
            val mimeType = contentResolver.getType(uri)?.lowercase()
            if (mimeType != null && mimeType !in ALLOWED_MIME_TYPES) {
                return FileValidationResult(
                    isValid = false,
                    errorMessage = "Unsupported file format. Only PNG, JPEG, and WebP images are allowed."
                )
            }

            // 2. File size check
            var stream: InputStream? = contentResolver.openInputStream(uri)
            val byteSize = stream?.available() ?: 0
            stream?.close()

            if (byteSize > MAX_FILE_SIZE_BYTES) {
                return FileValidationResult(
                    isValid = false,
                    errorMessage = "Image file is too large (max 8 MB allowed)."
                )
            }

            // 3. Decode dimensions first to prevent OOM
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            stream = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(stream, null, options)
            stream?.close()

            val width = options.outWidth
            val height = options.outHeight

            if (width <= 0 || height <= 0) {
                return FileValidationResult(
                    isValid = false,
                    errorMessage = "Unable to decode image dimensions or corrupted image file."
                )
            }

            // 4. Downscale if dimensions exceed safe boundaries
            var sampleSize = 1
            while (width / sampleSize > MAX_DIMENSION || height / sampleSize > MAX_DIMENSION) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565 // Memory-safe color depth
            }

            stream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(stream, null, decodeOptions)
            stream?.close()

            if (bitmap == null) {
                return FileValidationResult(
                    isValid = false,
                    errorMessage = "Failed to load image data securely."
                )
            }

            // Successfully processed with EXIF stripped by in-memory decoding
            return FileValidationResult(
                isValid = true,
                sanitizedBitmap = bitmap
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error in validateAndProcessImage", e)
            return FileValidationResult(
                isValid = false,
                errorMessage = "Security error validating file: ${e.message}"
            )
        }
    }
}
