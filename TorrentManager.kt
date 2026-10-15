package com.atmosplayer.app

import org.libtorrent4j.SessionManager
import org.libtorrent4j.TorrentHandle
import org.libtorrent4j.TorrentInfo
import java.io.File

class TorrentItem(val handle: TorrentHandle, val info: TorrentInfo) {
    val name: String get() = info.name()
    val progress: Int get() = (handle.status().progress() * 100).toInt()
    val speedKb: Int get() = handle.status().downloadRate() / 1024
    val done: Boolean get() = handle.status().progress() >= 1f
    fun videoFile(saveDir: File): File? {
        val files = info.files()
        var best = -1; var size = 0L
        for (i in 0 until files.numFiles()) {
            val n = files.filePath(i).lowercase()
            if (listOf(".mp4", ".mkv", ".avi", ".mov", ".webm", ".m4v", ".ts").any { n.endsWith(it) } &&
                files.fileSize(i) > size) { best = i; size = files.fileSize(i) }
        }
        return if (best >= 0) File(saveDir, files.filePath(best)) else null
    }
}

object TorrentManager {
    private val session = SessionManager()
    val items = mutableListOf<TorrentItem>()

    fun start() { if (!session.isRunning) session.start() }

    /** Blocking: call off the main thread. Returns null on success or an error message. */
    fun addMagnet(magnet: String, saveDir: File): String? {
        start()
        saveDir.mkdirs()
        val data = session.fetchMagnet(magnet, 60, saveDir) ?: return "Could not fetch torrent metadata (no peers?)"
        val ti = TorrentInfo.bdecode(data)
        session.download(ti, saveDir)
        val handle = session.find(ti.infoHash()) ?: return "Failed to start download"
        synchronized(items) { items.add(TorrentItem(handle, ti)) }
        return null
    }

    fun stop() { if (session.isRunning) session.stop() }
}
