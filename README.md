# Atmos Player
Android video player (Media3/ExoPlayer, Dolby Atmos / E-AC-3 JOC passthrough) with a built-in torrent downloader (libtorrent4j).

## Get the APK (no Android Studio needed)
1. Create a free GitHub repo and upload this folder's contents (keep the .github folder).
2. Open the repo's Actions tab -> "Build APK" -> run it (~5 min).
3. Download "AtmosPlayer-apk" from the run, unzip, install app-debug.apk on your phone (allow "install unknown apps").

## Or build locally
Android Studio -> Open this folder -> Build > Build APK(s).

## Atmos notes
Atmos plays as true Atmos only when the file has an E-AC-3 JOC track AND the output supports it
(Atmos soundbar/TV via HDMI, or Dolby-enabled phone speakers/headphones). Otherwise Android downmixes it.
Use torrents only for content you have the right to download.
