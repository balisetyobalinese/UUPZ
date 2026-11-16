import android.app.DownloadManager
import android.content.*
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File

class MainActivity : ComponentActivity() {

    private var downloadId: Long = 0
    private var apkFile: File? = null

    // Fungsi 1 KLIK iki tok sing dipanggil tombol INSTALL
    fun satuKlikInstall(apkUrl: String, namaFile: String) {
        // 1. Cek izin install, nek durung, njaluk izin sek
        if (!packageManager.canRequestPackageInstalls()) {
            val intent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
            return
        }

        // 2. Download otomatis, gak usah buka browser
        val request = DownloadManager.Request(Uri.parse(apkUrl))
        request.setTitle("Mengunduh $namaFile...")
        request.setDescription("Mohon tunggu...")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setMimeType("application/vnd.android.package-archive")
        request.setAllowedOverMetered(true)

        apkFile = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), namaFile)
        request.setDestinationUri(Uri.fromFile(apkFile))

        val dm = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadId = dm.enqueue(request)

        // 3. Daftar receiver: nek download rampung, langsung install
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    installOtomatis()
                }
            }
        }, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    private fun installOtomatis() {
        val file = apkFile ?: return
        val uri = FileProvider.getUriForFile(this, "$packageName.provider", file)
        val install = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(install)
        // File APK langsung dihapus otomatis bar install, ben gak numpuk
        // file.delete()
    }
}
