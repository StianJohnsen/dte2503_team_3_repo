package com.example.dashcarr.presentation.tabs.camera

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.Runnable
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraWrapper(activity: Activity, button: Button) {
    companion object {
        private const val TAG = "CameraWrapper"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    private var videoCaptureButton: Button = button
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var onFinished: Runnable? = null

    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var activity: Activity = activity
    private lateinit var preview: Preview

    public fun askForPermission(): Boolean {
        val hasPermission = ContextCompat.checkSelfPermission(
            activity.applicationContext,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            ActivityCompat.requestPermissions(
                activity, arrayOf(android.Manifest.permission.CAMERA), 1
            )
        }
        return hasPermission
    }

    public fun isRecording(): Boolean {
        return recording != null
    }

    public fun isCameraStarted(): Boolean {
        return this.videoCapture != null
    }

    public fun startRecording(onStarted: Runnable, path: Path) {
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
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, path.toString())
            }
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
                        onStarted.run()
                    }

                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val msg = "Video capture succeeded: ${recordEvent.outputResults.outputUri}"
                            Log.d(TAG, msg)
                        } else {
                            recording?.close()
                            recording = null
                            Log.e(TAG, "Video capture ends with error: ${recordEvent.error}")
                        }
                        onFinished?.run()
                    }
                }
            }
    }

    public fun stopRecording(onFinished: Runnable) {
        if (isRecording()) {
            this.onFinished = onFinished
            recording?.stop()
            recording = null
        }
    }

    public fun startCamera(videoPreviewView: PreviewView?) {
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

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    activity.applicationContext as LifecycleOwner,
                    cameraSelector,
                    preview,
                    videoCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(activity.applicationContext))
    }

    public fun destroy() {
        cameraExecutor.shutdown()
    }
}