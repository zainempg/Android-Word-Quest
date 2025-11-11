package com.aar.app.wsp.data.xml

import com.aar.app.wsp.model.GameTheme
import com.aar.app.wsp.model.Word
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler

// SAX handler for parsing XML WordBank files
class SaxWordThemeHandler(
    private var themeStartId: Int = 1,
    private var wordStartId: Int = 1
) : DefaultHandler() {

    private val wordList = mutableListOf<Word>()
    private val themeList = mutableListOf<GameTheme>()
    private var currentThemeId = themeStartId

    override fun startElement(uri: String?, localName: String?, qName: String, attributes: Attributes) {
        when (qName.lowercase()) {

            "wordbank" -> {
                val themeName = attributes.getValue("theme") ?: "Unknown"
                val theme = GameTheme(currentThemeId, themeName)
                themeList.add(theme)
                currentThemeId++
            }

            "item" -> {
                val wordStr = attributes.getValue("str") ?: ""
                val word = Word(wordStartId, currentThemeId - 1, wordStr)
                wordList.add(word)
                wordStartId++
            }
        }
    }

    val words: List<Word>
        get() = wordList

    val gameThemes: List<GameTheme>
        get() = themeList
}
