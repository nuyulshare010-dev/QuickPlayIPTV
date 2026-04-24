# Quick Play DRM

An advanced, open-source Android IPTV and Media Player built with Kotlin, Jetpack Compose, and AndroidX Media3 (ExoPlayer).

## Features
- **M3U Playlist Parsing:** Add your own custom playlists.
- **DRM Support:** Natively supports Widevine, ClearKey, and PlayReady DRM streams.
- **Custom Headers:** Supports custom `User-Agent` and `Cookie` headers per channel or playlist.
- **External Player Integration:** Acts as a universal media player for other apps (like 1DM, Telegram, WhatsApp).
- **Immersive UI:** Fullscreen landscape playback with quality selection.

## How DRM and Custom Headers Work in M3U
To play DRM-protected streams or streams that require specific HTTP headers, the app parses Kodi/VLC specific tags from your M3U file.

### Example M3U Entry:
```m3u
#KODIPROP:inputstream.adaptive.license_type=clearkey
#KODIPROP:inputstream.adaptive.license_key=https://game.denver69.fun/Jtv/key.php?id=762&token=ENIqtk
#EXTINF:-1 tvg-id="762" tvg-logo="https://jiotvimages.cdn.jio.com/dare_images/images/SonyPIX.png" group-title="English",Sony Pix HD
#EXTVLCOPT:http-user-agent=Denver1769
#EXTVLCOPT:http-cookie=__hdnea__=st=1776643237~exp=1776664837~acl=/*~hmac=d5dbc48621c9672c2d5de1988d57287f82df9f8accfd8b41027c48072a577be7
#EXTHTTP:{"cookie":"__hdnea__=st=1776643237~exp=1776664837~acl=/*~hmac=d5dbc48621c9672c2d5de1988d57287f82df9f8accfd8b41027c48072a577be7"}
https://jiotvmblive.cdn.jio.com/bpk-tv/SonyPIX_MOB/WDVLive/index.mpd|cookie=__hdnea__=st=1776643237~exp=1776664837~acl=/*~hmac=d5dbc48621c9672c2d5de1988d57287f82df9f8accfd8b41027c48072a577be7&User-Agent=Denver1769
```

### Supported Tags:
- **DRM Type:** `#KODIPROP:inputstream.adaptive.license_type=` (Options: `clearkey`, `widevine`, `playready`)
- **DRM License URL:** `#KODIPROP:inputstream.adaptive.license_key=`
- **User-Agent:** `#EXTVLCOPT:http-user-agent=`
- **Cookies:** `#EXTVLCOPT:http-cookie=`
- **Inline Headers:** Appended to the stream URL using a pipe `|` (e.g., `http://stream.url|cookie=...&User-Agent=...`)

## Building the App
1. Clone the repository.
2. Open the project in Android Studio.
3. Sync Gradle and build the APK.
