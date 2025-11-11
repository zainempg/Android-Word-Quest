package com.aar.app.wsp.data.xml

import android.content.Context
import android.content.res.AssetManager
import com.aar.app.wsp.model.GameTheme
import com.aar.app.wsp.model.Word
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.xml.sax.InputSource
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import javax.xml.parsers.SAXParserFactory

// Class to load both XML and JSON word/theme files from assets
class WordThemeDataXmlLoader(context: Context) {

    private val assetManager: AssetManager = context.assets
    private var _words: MutableList<Word>? = null
    private var _gameThemes: MutableList<GameTheme>? = null

    val words: List<Word>
        get() {
            if (_words == null) loadData()
            return _words ?: emptyList()
        }

    val gameThemes: List<GameTheme>
        get() {
            if (_gameThemes == null) loadData()
            return _gameThemes ?: emptyList()
        }

    fun release() {
        _words = null
        _gameThemes = null
    }

    private fun loadData() {
        _words = ArrayList()
        _gameThemes = ArrayList()

        var themeId = 1
        var wordId = 1

        try {
            val files = assetManager.list(BASE_FOLDER)

            if (files.isNullOrEmpty()) {
                android.util.Log.e("Loader", "No files found in assets/$BASE_FOLDER")
                return
            }

            files?.forEach { fileName ->
                val path = "$BASE_FOLDER/$fileName"

                when {
                    fileName.endsWith(".json", ignoreCase = true) -> {
                        android.util.Log.d("Loader", "Loading JSON: $path")
                        loadJson(path, themeId, wordId).apply {
                            themeId = this.first
                            wordId = this.second
                        }
                    }

                    fileName.endsWith(".xml", ignoreCase = true) -> {
                        android.util.Log.d("Loader", "Loading XML: $path")
                        loadXml(path, themeId, wordId).apply {
                            themeId = this.first
                            wordId = this.second
                        }
                    }

                    else -> {
                        android.util.Log.d("Loader", "Skipping unsupported file: $fileName")
                    }
                }
            }

        } catch (e: Exception) {
            android.util.Log.e("Loader", "Error loading data: ${e.message}")
        }
    }

    // Load JSON file
    private fun loadJson(path: String, startThemeId: Int, startWordId: Int): Pair<Int, Int> {
        var themeId = startThemeId
        var wordId = startWordId

        val gson = Gson()
        val reader = BufferedReader(InputStreamReader(assetManager.open(path)))
        val type = object : TypeToken<JsonThemeResponse>() {}.type

        val themeResponse: JsonThemeResponse = gson.fromJson(reader, type)

        val theme = GameTheme(themeId, themeResponse.name)
        _gameThemes!!.add(theme)

        themeResponse.words.forEach { str ->
            _words!!.add(Word(wordId, themeId, str))
            wordId++
        }

        themeId++
        return Pair(themeId, wordId)
    }

    // Load XML file
    private fun loadXml(path: String, startThemeId: Int, startWordId: Int): Pair<Int, Int> {
        var themeId = startThemeId
        var wordId = startWordId

        val parser = SAXParserFactory.newInstance().newSAXParser().xmlReader
        val handler = SaxWordThemeHandler(startThemeId, startWordId)
        // Make sure SaxWordThemeHandler supports default constructor

        parser.contentHandler = handler
        parser.parse(InputSource(assetManager.open(path)))

        handler.gameThemes?.forEach {
            _gameThemes!!.add(it)
            themeId++
        }

        handler.words?.forEach {
            _words!!.add(it)
            wordId++
        }

        return Pair(themeId, wordId)
    }

    companion object {
        private const val BASE_FOLDER = "words"
    }
}
