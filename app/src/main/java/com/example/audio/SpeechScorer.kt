package com.example.audio

import java.util.Locale
import kotlin.math.max

data class PronunciationScoreResult(
    val accuracyPercentage: Int,
    val wordResults: List<WordMatchResult>,
    val feedbackMessage: String
)

data class WordMatchResult(
    val targetWord: String,
    val isMatched: Boolean
)

object SpeechScorer {
    /**
     * Compares recognized user speech with target phrase.
     * Computes word-level matching and similarity score.
     */
    fun evaluate(targetPhrase: String, spokenTranscript: String): PronunciationScoreResult {
        val cleanTargetWords = cleanWords(targetPhrase)
        val cleanSpokenWords = cleanWords(spokenTranscript)

        if (cleanTargetWords.isEmpty()) {
            return PronunciationScoreResult(100, emptyList(), "Great!")
        }

        if (cleanSpokenWords.isEmpty()) {
            val emptyList = cleanTargetWords.map { WordMatchResult(it, false) }
            return PronunciationScoreResult(0, emptyList, "No speech detected. Tap the mic and try again!")
        }

        val wordResults = mutableListOf<WordMatchResult>()
        var matchCount = 0

        // Look for each target word in spoken transcript with fuzzy tolerance
        var spokenIndex = 0
        for (target in cleanTargetWords) {
            var matched = false
            for (i in spokenIndex until cleanSpokenWords.size) {
                val spoken = cleanSpokenWords[i]
                if (isWordSimilar(target, spoken)) {
                    matched = true
                    spokenIndex = i + 1
                    break
                }
            }
            if (!matched) {
                // Check anywhere in spoken words if not in strict order
                if (cleanSpokenWords.any { isWordSimilar(target, it) }) {
                    matched = true
                }
            }

            if (matched) matchCount++
            wordResults.add(WordMatchResult(target, matched))
        }

        val accuracy = ((matchCount.toFloat() / cleanTargetWords.size.toFloat()) * 100).toInt()
            .coerceIn(0, 100)

        val feedback = when {
            accuracy >= 90 -> "Outstanding articulation! Native-level clarity."
            accuracy >= 75 -> "Great job! Very clear and understandable."
            accuracy >= 55 -> "Good attempt! Pay close attention to the highlighted words."
            else -> "Keep practicing! Listen to the audio model and try speaking at a relaxed pace."
        }

        return PronunciationScoreResult(
            accuracyPercentage = accuracy,
            wordResults = wordResults,
            feedbackMessage = feedback
        )
    }

    private fun cleanWords(text: String): List<String> {
        return text.lowercase(Locale.US)
            .replace(Regex("[^a-z0-9\\s']"), "")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
    }

    private fun isWordSimilar(w1: String, w2: String): Boolean {
        if (w1 == w2) return true
        if (w1.replace("'", "") == w2.replace("'", "")) return true
        // Allow 1 char edit distance for words > 4 chars
        if (w1.length > 4 && w2.length > 4 && kotlin.math.abs(w1.length - w2.length) <= 1) {
            return levenshtein(w1, w2) <= 1
        }
        return false
    }

    private fun levenshtein(lhs: CharSequence, rhs: CharSequence): Int {
        val lhsLen = lhs.length
        val rhsLen = rhs.length
        var cost = Array(lhsLen + 1) { it }
        var newCost = Array(lhsLen + 1) { 0 }

        for (i in 1..rhsLen) {
            newCost[0] = i
            for (j in 1..lhsLen) {
                val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1
                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1
                newCost[j] = minOf(costInsert, costDelete, costReplace)
            }
            val swap = cost
            cost = newCost
            newCost = swap
        }
        return cost[lhsLen]
    }
}
