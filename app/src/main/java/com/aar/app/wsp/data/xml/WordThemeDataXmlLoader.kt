package com.aar.app.wsp.data.xml

import android.content.Context
import android.content.res.AssetManager
import com.aar.app.wsp.model.GameTheme
import com.aar.app.wsp.model.Word
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader

// Class to load only JSON word/theme files from assets
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
        _words = mutableListOf()
        _gameThemes = mutableListOf()

        var themeId = 1
        var wordId = 1

        try {
            val files = assetManager.list(BASE_FOLDER)

            if (files.isNullOrEmpty()) {
                android.util.Log.e("Loader", "No files found in assets/$BASE_FOLDER")
                return
            }

            files.forEach { fileName ->
                if (fileName.endsWith(".json", ignoreCase = true)) {
                    android.util.Log.d("Loader", "Loading JSON: $fileName")
                    val ids = loadJson("$BASE_FOLDER/$fileName", themeId, wordId)
                    themeId = ids.first
                    wordId = ids.second
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

        // Add theme
        val theme = GameTheme(themeId, themeResponse.name)
        _gameThemes!!.add(theme)

        // Add words
        themeResponse.words.forEach { str ->
            _words!!.add(Word(wordId, themeId, str))
            wordId++
        }

        themeId++
        return Pair(themeId, wordId)
    }

    companion object {
        private const val BASE_FOLDER = "words"
    }
}
