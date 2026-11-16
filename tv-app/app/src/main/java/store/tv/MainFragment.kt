package store.tv

import android.app.DownloadManager
import android.content.*
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.net.URL

class MainFragment : BrowseSupportFragment() {

    private val GITHUB_URL = "https://raw.githubusercontent.com/balisetyobalinese/UUPZ/main/apps.json"
    private var downloadId: Long = -1
    private var namaFileAktif = ""
    private lateinit var downloadReceiver: BroadcastReceiver

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        title = "STORE.TV"
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
        brandColor = resources.getColor(android.R.color.holo_blue_dark)

        // Search + Header
        setOnSearchClickedListener { Toast.makeText(context, "Cari...", Toast.LENGTH_SHORT).show() }

        // Klik card -> Auto Install
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            val app = item as AppModel
            autoInstall(app.url, "${app.nama}.apk")
        }

        loadDariGithub()

        downloadReceiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    val file = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), namaFileAktif)
                    installApk(file)
                }
            }
        }
        requireContext().registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    private fun loadDariGithub() {
        Thread {
            try {
                val json = URL(GITHUB_URL).readText()
                val type = object : TypeToken<List<AppModel>>() {}.type
                val list: List<AppModel> = Gson().fromJson(json, type)
                activity?.runOnUiThread { tampilkan(list) }
            } catch (e: Exception) {
                activity?.runOnUiThread {
                    Toast.makeText(context, "Gagal load: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun tampilkan(list: List<AppModel>) {
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        val cardPresenter = CardPresenter()

        // Kelompokno per kategori
        val kategoriList = list.map { it.kategori }.distinct()
        for (kategori in kategoriList) {
            val listRowAdapter = ArrayObjectAdapter(cardPresenter)
            list.filter { it.kategori == kategori }.forEach { listRowAdapter.add(it) }
            val header = HeaderItem(0, kategori)
            rowsAdapter.add(ListRow(header, listRowAdapter))
        }
        adapter = rowsAdapter
    }

    private fun autoInstall(urlApk: String, namaFile: String) {
        val ctx = requireContext()
        if (!ctx.packageManager.canRequestPackageInstalls()) {
            val i = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            i.data = Uri.parse("package:${ctx.packageName}")
            startActivity(i)
            Toast.makeText(ctx, "Izinkan Install Unknown Apps sek, terus klik lagi", Toast.LENGTH_LONG).show()
            return
        }
        namaFileAktif = namaFile
        val req = DownloadManager.Request(Uri.parse(urlApk))
        req.setTitle("Mengunduh $namaFile")
        req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        req.setMimeType("application/vnd.android.package-archive")
        req.setDestinationInExternalFilesDir(ctx, Environment.DIRECTORY_DOWNLOADS, namaFile)
        val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadId = dm.enqueue(req)
        Toast.makeText(ctx, "Mengunduh $namaFile... nanti auto install", Toast.LENGTH_SHORT).show()
    }

    private fun installApk(file: File) {
        val ctx = requireContext()
        val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.provider", file)
        val i = Intent(Intent.ACTION_VIEW)
        i.setDataAndType(uri, "application/vnd.android.package-archive")
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(i)
    }

    override fun onDestroy() {
        super.onDestroy()
        try { requireContext().unregisterReceiver(downloadReceiver) } catch (e: Exception) {}
    }
}
