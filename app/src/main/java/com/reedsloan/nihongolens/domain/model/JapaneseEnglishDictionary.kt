package com.reedsloan.nihongolens.domain.model

data class JapaneseEnglishDictionary(
    val entries: List<JapaneseEnglishEntry>
)

data class JapaneseEnglishEntry(
    val word: List<JapaneseWord>,
    val wordKanaOnly: List<JapaneseWord>,
    // This is a list of lists because there are basically categories of meanings
    // for example 走る can mean "to run" or "to travel" etc.
    // but also to run (of a vehicle) or to drive
    val englishDefinitions: List<EnglishDefinition>,
)

// Example of a word entry from JMdict
// "kanji":[{"common":true,"text":"食べる","tags":[]},{"common":false,"text":"喰べる","tags":["iK"]}],"kana":[{"common":true,"text":"たべる","tags":[],"appliesToKanji":["*"]}])
data class JapaneseWord(
    val text: String,
    val tags: List<String>,
    val common: Boolean
)

data class EnglishDefinition(
    val text: List<String>,
    val partOfSpeech: List<String>,
    val info: List<String>,
    // Contains information such as "usually written using kana alone" or "as 〜に走る; occ. 趨る"
    val misc: List<String>
)