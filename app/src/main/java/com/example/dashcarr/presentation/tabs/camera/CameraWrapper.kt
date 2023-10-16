package com.example.dashcarr.presentation.tabs.camera

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.LifecycleOwner
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraWrapper(private var activity: Activity) {
    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var onFinished: (() -> Unit?)? = null

    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private lateinit var preview: Preview

    private fun askForPermission(): Boolean = ContextCompat.checkSelfPermission(
        activity.applicationContext,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    fun isRecording(): Boolean {
        return recording != null
    }

    fun isCameraStarted(): Boolean {
        return this.videoCapture != null
    }

    fun startRecording(onStarted: () -> Unit, path: String) {
        if (isRecording() || !isCameraStarted()) {
            return
        }
        onFinished = null
        // To start recording, we create a new recording session.
        // First we create our intended MediaStore video content object,
        // with system timestamp as the display name(so we could capture multiple videos).
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, path)
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(activity.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        recording = videoCapture!!.output
            .prepareRecording(activity.applicationContext, mediaStoreOutputOptions)
            .apply {
                // Enable Audio for recording
                if (
                    PermissionChecker.checkSelfPermission(
                        activity.applicationContext,
                        Manifest.permission.RECORD_AUDIO
                    ) ==
                    PermissionChecker.PERMISSION_GRANTED
                ) {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(activity.applicationContext)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        onStarted()
                    }

                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val msg = "Video capture succeeded"
                            Log.d(this::class.simpleName, msg)
                            Toast.makeText(activity, "File saved", Toast.LENGTH_SHORT).show()
                        } else {
                            recording?.close()
                            recording = null
                            Log.e(this::class.simpleName, "Video capture ends with error: ${recordEvent.error}")
                        }
                        onFinished?.let { it() }
                    }
                }
            }
    }

    fun stopRecording(onFinished: () -> Unit) {
        if (isRecording()) {
            this.onFinished = onFinished
            recording?.stop()
            recording = null
        }
    }

    fun startCamera(
        videoPreviewView: PreviewView?,
        lifecycleOwner: LifecycleOwner,
        cameraSelector: CameraSelector,
        started: () -> Unit
    ) {
        if (!askForPermission()) {
            return
        }
        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity.applicationContext)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            preview = Preview.Builder()
                .build().also {
                    it.setSurfaceProvider(videoPreviewView?.surfaceProvider)
                }

            // Video
            val recorder = Recorder.Builder()
                .setQualitySelector(
                    QualitySelector.from(
                        Quality.HIGHEST,
                        FallbackStrategy.higherQualityOrLowerThan(Quality.SD)
                    )
                )
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    videoCapture
                )
            } catch (exc: Exception) {
                Log.e(this::class.simpleName, "Use case binding failed", exc)
            }
            started()
        }, ContextCompat.getMainExecutor(activity.applicationContext))

    }

    fun destroy() {
        cameraExecutor.shutdown()
        ProcessCameraProvider.getInstance(activity.applicationContext).get().unbindAll()
        videoCapture = null
    }
}