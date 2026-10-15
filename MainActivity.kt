package com.atmosplayer.app

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    private lateinit var list: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private val ui = Handler(Looper.getMainLooper())
    private val saveDir by lazy { File(getExternalFilesDir(null), "Movies") }
    private val PICK = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 64, 32, 32) }
        val title = TextView(this).apply { text = "Atmos Player"; textSize = 26f; setTextColor(Color.WHITE) }
        val input = EditText(this).apply {
            hint = "Paste magnet link"; setTextColor(Color.WHITE); setHintTextColor(Color.GRAY)
        }
        val addBtn = Button(this).apply { text = "Download torrent" }
        val openBtn = Button(this).apply { text = "Open video from device" }
        list = ListView(this)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf<String>())
        list.adapter = adapter
        listOf(title, input, addBtn, openBtn).forEach { root.addView(it) }
        root.addView(list, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        setContentView(root)

        addBtn.setOnClickListener { addMagnet(input.text.toString().trim()); input.setText("") }
        openBtn.setOnClickListener {
            startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE); type = "video/*"
            }, PICK)
        }
        list.setOnItemClickListener { _, _, pos, _ ->
            val item = synchronized(TorrentManager.items) { TorrentManager.items.getOrNull(pos) } ?: return@setOnItemClickListener
            val f = item.videoFile(saveDir)
            if (f != null && f.exists()) play(Uri.fromFile(f))
            else Toast.makeText(this, "Still downloading…", Toast.LENGTH_SHORT).show()
        }
        intent?.data?.takeIf { it.scheme == "magnet" }?.let { addMagnet(it.toString()) }
        refresh()
    }

    private fun addMagnet(m: String) {
        if (!m.startsWith("magnet:")) { toast("Enter a valid magnet link"); return }
        toast("Fetching torrent info…")
        thread { val err = TorrentManager.addMagnet(m, saveDir); err?.let { ui.post { toast(it) } } }
    }

    private fun refresh() {
        val rows = synchronized(TorrentManager.items) {
            TorrentManager.items.map {
                "${it.name}\n${if (it.done) "Done — tap to play" else "${it.progress}%  ${it.speedKb} KB/s"}"
            }
        }
        adapter.clear(); adapter.addAll(rows)
        ui.postDelayed({ refresh() }, 1000)
    }

    private fun play(uri: Uri) = startActivity(Intent(this, PlayerActivity::class.java).setData(uri))
    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()

    @Deprecated("simple picker")
    override fun onActivityResult(req: Int, res: Int, data: Intent?) {
        super.onActivityResult(req, res, data)
        if (req == PICK && res == RESULT_OK) data?.data?.let { play(it) }
    }
}
