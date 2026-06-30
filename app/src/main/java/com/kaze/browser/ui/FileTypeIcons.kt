package com.kaze.browser.ui

import androidx.annotation.DrawableRes
import com.kaze.browser.R

/**
 * Maps a downloaded file to a Material-style file-type icon (not a coloured letter
 * badge). Falls back to the MIME type when the name has no useful extension.
 */
@DrawableRes
fun fileTypeIcon(fileName: String, mimeType: String?): Int {
    val ext = fileName.substringAfterLast('.', "").lowercase()
    return when (ext) {
        "pdf" -> R.drawable.ic_file_pdf
        "doc", "docx", "txt", "rtf", "odt", "md", "pages" -> R.drawable.ic_file_doc
        "zip", "rar", "7z", "tar", "gz", "apk" -> R.drawable.ic_file_archive
        "mp3", "wav", "aac", "flac", "ogg", "m4a" -> R.drawable.ic_file_audio
        "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg", "heic" -> R.drawable.ic_file_image
        "mp4", "mkv", "mov", "webm", "avi", "m4v" -> R.drawable.ic_file_video
        else -> when (mimeType?.substringBefore('/')) {
            "image" -> R.drawable.ic_file_image
            "audio" -> R.drawable.ic_file_audio
            "video" -> R.drawable.ic_file_video
            else -> if (mimeType == "application/pdf") R.drawable.ic_file_pdf else R.drawable.ic_file_doc
        }
    }
}
