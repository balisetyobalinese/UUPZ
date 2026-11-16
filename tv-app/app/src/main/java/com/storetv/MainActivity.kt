package com.store.tv

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.core.content.FileProvider
import java.io.File

class MainActivity : ComponentActivity() {

    private var downloadId: Long = 0
    private var apkFile: File? = null

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == downloadId) {
                apkFile?.let { installApk(it) }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        // ... kode UI mu sing lawas, ojo dihapus, taruh neng kene ...
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(downloadReceiver)
    }

    // Fungsi 1 KLIK iki tok sing dipanggil tombol INSTALL
    private fun satuKlikInstall(apkUrl: String, namaFile: String) {
        if (!packageManager.canRequestPackageInstalls()) {
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            intent.data = Uri.parse("package:" + packageName)
            startActivity(intent)
            return
        }

        val request = DownloadManager.Request(Uri.parse(apkUrl))
        request.setTitle("Mengunduh $namaFile...")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setMimeType("application/vnd.android.package-archive")
        request.setAllowedOverMetered(true)

        // FIX OS 10: Ojo nganggo Uri.fromFile maneh!
        apkFile = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), namaFile)
        if (apkFile?.exists() == true) apkFile?.delete()
        request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, namaFile)

        val dm = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadId = dm.enqueue(request)
    }

    private fun installApk(file: File) {
        val uri = FileProvider.getUriForFile(this, "$packageName.provider", file)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
