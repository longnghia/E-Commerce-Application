package com.ln.simplechat.utils

import com.ln.simplechat.model.ChatMedia
import com.luck.picture.lib.entity.LocalMedia

fun LocalMedia.toChatMedia() = ChatMedia(availablePath, duration, mimeType, fileName)
