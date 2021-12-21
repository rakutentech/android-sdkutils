package com.rakuten.tech.mobile.sdkutils.json

import android.content.Context
import com.google.gson.Gson
import java.io.*
import java.io.FileReader


fun Gson.fileToJson(context: Context, path: String) {

    try {
        val file = File(context.filesDir.appendText(path))
        val inputAsString = FileInputStream(file).bufferedReader().use { it.readText() }
    } catch (e: FileNotFoundException) {

    }
}


private class FileReader(path: String) {
    private val content: String
    init {
        val reader = InputStreamReader(this.javaClass.classLoader!!.getResourceAsStream(path))
        content = reader.readText()
        reader.close()
    }
}