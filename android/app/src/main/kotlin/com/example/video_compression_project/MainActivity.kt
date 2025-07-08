package com.example.video_compression_project


import android.os.Handler
import android.os.Looper
import androidx.annotation.NonNull
import com.arthenica.ffmpegkit.FFmpegKit
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.io.File

class MainActivity: FlutterActivity() {
    private val CHANNEL = "video_compression"

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
            call, result ->
            if (call.method == "compressVideo") {
                val filePath = call.argument<String>("filePath")
                if (filePath != null) {
                    compressVideo(filePath) { compressedPath ->
                        if (compressedPath != null) {
                            result.success(compressedPath)
                        } else {
                            result.error("COMPRESSION_FAILED", "Video compression failed", null)
                        }
                    }
                } else {
                    result.error("INVALID_PATH", "File path is null", null)
                }
            }
        }
    }

    private fun compressVideo(inputPath: String, callback: (String?) -> Unit) {
        val outputFile = File(cacheDir, "compressed_${System.currentTimeMillis()}.mp4")
        val outputPath = outputFile.absolutePath

        val command = "-i $inputPath -vcodec libx264 -crf 28 -preset ultrafast $outputPath"

        FFmpegKit.executeAsync(command) { session ->
            val returnCode = session.returnCode
            Handler(Looper.getMainLooper()).post {
                if (returnCode.isValueSuccess) {
                    callback(outputPath)
                } else {
                    callback(null)
                }
            }
        }
    }
}

