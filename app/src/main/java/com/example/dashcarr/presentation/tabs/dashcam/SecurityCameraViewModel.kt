package com.example.dashcarr.presentation.tabs.dashcam

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModel
import com.example.dashcarr.presentation.MainActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class SecurityCameraViewModel @Inject constructor(
) : ViewModel() {

    private lateinit var videoIntent: Intent
    private lateinit var uri: Uri
    private lateinit var launcher: ActivityResultLauncher<Uri>
    private lateinit var permLauncher: ActivityResultLauncher<Intent>

    fun initViewModel(activity: MainActivity) {

        uri = Uri.Builder().path(MediaStore.Video.Media.EXTERNAL_CONTENT_URI.path).build()
////            .appendPath("Dashcarr")
////            .appendPath("security_camera").build()
//        val dir = File(uri.path!!)
//        if (!dir.exists()) {
//            if (!dir.mkdirs()) {
//                Log.e("test", "Dirs not created: $dir")
//            }
//        }

//        val file = File(MediaStore.Video.Media.EXTERNAL_CONTENT_URI.path.toString(), "video.mp4")
//        uri = Uri.Builder().path(Uri.fromFile(dir).path).appendPath("video.mp4").build()
//        Log.e("test", uri.toString())
//        uri = FileProvider.getUriForFile(
//            activity.applicationContext,
//            activity.applicationContext.packageName + ".cameraRecordings",
//            file
//        )
        videoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        // content://media/external_primary/video/media/1000000025
//        val path = Path("content://media/external_primary/video/media/1000000028")
//        val fileName = "captureTemp.mp4"
//        val values = ContentValues()
//        values.put(MediaStore.Video.Media.TITLE, "video.mp4")
//        values.put(MediaStore.Video.VideoColumns.MIME_TYPE, "video/mp4")

//        videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
//        videoIntent.putExtra(MediaStore.Video.Media.TITLE, "video.mp4")
//        videoIntent.putExtra(MediaStore.Video.VideoColumns.MIME_TYPE, "video/mp4")
//        videoIntent.putExtra(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Dashcarr/SecurityCamera")

//        val contentResolver = activity.baseContext.contentResolver
//        activity.externalCacheDir?.toUri()?.let { contentResolver.insert(it, values) };

        permLauncher =
            activity.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) {
                if (it.resultCode == Activity.RESULT_OK) {
                    Toast.makeText(
                        activity.baseContext, "Video saved to:\n"
                                + it.data?.data, Toast.LENGTH_LONG
                    ).show()
                    Log.e("test", it.data?.data.toString())
                } else {
                    Toast.makeText(
                        activity.baseContext, "Error while recording video: ${it.resultCode}", Toast.LENGTH_LONG
                    ).show()
                }
//                fileDescriptor?.close()
            }

//        shareContentIntent.clipData = ClipData.newRawUri("", uri)
//        shareContentIntent.addFlags(
//            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//        )
//        permLauncher.launch(shareContentIntent)
        launcher =
            activity.registerForActivityResult(
                ActivityResultContracts.CaptureVideo()
            ) { result ->
                Log.e("test", "result is $result")
            }
    }

    /**
     * Starts the camera intent if the permissions are granted.
     * If permissions are missing, it will request the permission without starting the intent.
     */
    fun startRecording(activity: MainActivity) {
        val hasPermission = activity.checkSelfPermission(
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
//            launcher.launch(uri)
        } else {
            activity.requestPermissions(
                arrayOf(android.Manifest.permission.CAMERA), 1
            )
        }
    }
}