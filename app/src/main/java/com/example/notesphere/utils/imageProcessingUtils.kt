package com.example.notesphere.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream

suspend fun extractInfoFromIdCard(uri: Uri, context: Context): Triple<String, String, Bitmap> {
    val bitmap = android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    val faceDetector = FaceDetection.getClient(
        FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .build()
    )
    val image = InputImage.fromBitmap(bitmap, 0)
    val faces = faceDetector.process(image).await()

    if (faces.isNotEmpty()) {
        val face = faces[0]
        val bounds = face.boundingBox
        val faceWidth = bounds.width()
        val faceHeight = bounds.height()

        // Crop face bitmap with 20% padding (unchanged)
        val paddingPercent = 20f
        val paddingWidthPx = (paddingPercent / 100f * faceWidth).toInt()
        val paddingHeightPx = (paddingPercent / 100f * faceHeight).toInt()
        val faceLeft = maxOf(0, bounds.left - paddingWidthPx)
        val faceTop = maxOf(0, bounds.top - paddingHeightPx)
        val faceRight = minOf(bitmap.width, bounds.right + paddingWidthPx)
        val faceBottom = minOf(bitmap.height, bounds.bottom + paddingHeightPx)
        val faceWidthCropped = faceRight - faceLeft
        val faceHeightCropped = faceBottom - faceTop

        if (faceWidthCropped <= 0 || faceHeightCropped <= 0) {
            throw Exception("Invalid face crop dimensions")
        }

        val faceBitmap = Bitmap.createBitmap(bitmap, faceLeft, faceTop, faceWidthCropped, faceHeightCropped)

        // Crop custom-padded bitmap for text extraction
        val topPaddingPercent = 26f
        val bottomPaddingPercent = 151f
        val leftPaddingPercent = 185f
        val rightPaddingPercent = 185f

        val topPaddingPx = (topPaddingPercent / 100f * faceHeight).toInt()
        val bottomPaddingPx = (bottomPaddingPercent / 100f * faceHeight).toInt()
        val leftPaddingPx = (leftPaddingPercent / 100f * faceWidth).toInt()
        val rightPaddingPx = (rightPaddingPercent / 100f * faceWidth).toInt()

        val customLeft = maxOf(0, bounds.left - leftPaddingPx)
        val customTop = maxOf(0, bounds.top - topPaddingPx)
        val customRight = minOf(bitmap.width, bounds.right + rightPaddingPx)
        val customBottom = minOf(bitmap.height, bounds.bottom + bottomPaddingPx)
        val customWidth = customRight - customLeft
        val customHeight = customBottom - customTop

        if (customWidth <= 0 || customHeight <= 0) {
            throw Exception("Invalid custom padding crop dimensions")
        }

        val customBitmap = Bitmap.createBitmap(bitmap, customLeft, customTop, customWidth, customHeight)
        val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val customImage = InputImage.fromBitmap(customBitmap, 0)
        val textResult = textRecognizer.process(customImage).await()
        val lines = textResult.text.lines().filter { it.isNotBlank() }

        val role = when (lines.getOrNull(0)?.uppercase()) {
            "STUDENT" -> "student"
            "STAFF" -> "teacher"
            else -> "other"
        }
        val name = lines.getOrNull(1) ?: "Name not found"

        return Triple(role, name, faceBitmap)
    } else {
        throw Exception("No face detected")
    }
}

fun saveBitmapToFile(bitmap: Bitmap, context: Context): Uri {
    val file = File(context.cacheDir, "profile_face_${System.currentTimeMillis()}.png")
    val outputStream = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    outputStream.flush()
    outputStream.close()
    return Uri.fromFile(file)
}