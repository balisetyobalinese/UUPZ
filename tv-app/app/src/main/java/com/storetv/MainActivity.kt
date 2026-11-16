package com.storetv

import android.app.DownloadManager
import android.content.*
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import androidx.activity.ComponentActivity
import android.os.Bundle

class MainActivity : ComponentActivity() {

    private var downloadId: Long = 0
    private var apkFile: File? = null

    // Fungsi 1 KLIK iki tok sing dipanggil tombol INSTALL
    private fun satuKlikInstall(context: Context, apkUrl: String, namaFile: String) {
        if (!context.packageManager.canRequestPackageInstalls()) {
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            intent.data = Uri.parse("package:" + context.packageName)
            context.startActivity(intent)
            return
        }

        val request = DownloadManager.Request(Uri.parse(apkUrl))
        request.setTitle("Mengunduh $namaFile...")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setMimeType("application/vnd.android.package-archive")
        request.setAllowedOverMetered(true)

        apkFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), namaFile)
        request.setDestinationUri(Uri.fromFile(apkFile))

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = dm.enqueue(request)

        // Nek download rampung, langsung muncul BATAL / INSTAL
        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    installOtomatis(context)
                }
            }
        }, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    private fun installOtomatis(context: Context) {
        val file = apkFile ?: return
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val install = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(install)
    }
}
