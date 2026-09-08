package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.ConversationMessage
import com.example.data.model.Scenario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class AiSpeakingResponse(
    val reply: String,
    val feedback: String?,
    val nativeAlternative: String?,
    val pronunciationTip: String?,
    val suggestedReplies: List<String>
)

class GeminiService {
    private val TAG = "GeminiService"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun getConversationTurn(
        scenario: Scenario,
        history: List<ConversationMessage>,
        userUtterance: String
    ): AiSpeakingResponse = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isNullOrEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d(TAG, "No valid Gemini API key configured; using smart contextual fallback.")
            return@withContext getContextualFallback(scenario, history, userUtterance)
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val systemInstruction = """
You are a friendly, encouraging native English speaking coach and roleplay partner.
SCENARIO: ${scenario.title} (${scenario.category}, Level: ${scenario.level})
YOUR ROLE: ${scenario.aiRole}
USER ROLE: ${scenario.userRole}
SCENARIO CONTEXT: ${scenario.contextPrompt}

Your goals:
1. Stay in character and reply conversationally in 1-3 spoken sentences. Keep it natural and engaging.
2. Analyze the user's spoken sentence:
   - If there is any grammatical error, awkward phrasing, or non-native phrasing, give 1 concise, gentle coaching tip.
   - Provide a natural native English alternative phrase that sounds idiomatic.
   - Provide a quick pronunciation or connected speech tip (IPA or linking tip) for a key word.
   - Provide 2-3 short, natural suggested replies the user could say next.

Return ONLY a valid JSON object matching this schema:
{
  "reply": "Your in-character spoken reply to the user (1-3 sentences)",
  "feedback": "Concise coaching note on their sentence (or null if their English was already natural)",
  "nativeAlternative": "More natural or idiomatic phrasing (or null if already great)",
  "pronunciationTip": "Quick tip e.g. how to link sounds or pronounce tricky words (or null)",
  "suggestedReplies": ["Suggested user reply 1", "Suggested user reply 2", "Suggested user reply 3"]
}
""".trimIndent()

            // Build conversation history contents
            val contentsArray = JSONArray()

            // Include past 4 turns for context
            val recentHistory = history.takeLast(4)
            for (msg in recentHistory) {
                val role = if (msg.isUser) "user" else "model"
                val partObj = JSONObject().put("text", msg.text)
                val contentObj = JSONObject()
                    .put("role", role)
                    .put("parts", JSONArray().put(partObj))
                contentsArray.put(contentObj)
            }

            // Current user message
            val currentPartObj = JSONObject().put("text", userUtterance)
            contentsArray.put(
                JSONObject()
                    .put("role", "user")
                    .put("parts", JSONArray().put(currentPartObj))
            )

            val systemInstructionObj = JSONObject().put(
                "parts",
                JSONArray().put(JSONObject().put("text", systemInstruction))
            )

            val generationConfig = JSONObject()
                .put("temperature", 0.7)
                .put("responseMimeType", "application/json")

            val requestBodyJson = JSONObject()
                .put("contents", contentsArray)
                .put("systemInstruction", systemInstructionObj)
                .put("generationConfig", generationConfig)

            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w(TAG, "Gemini API error code: ${response.code}, falling back to local simulation")
                return@withContext getContextualFallback(scenario, history, userUtterance)
            }

            val bodyString = response.body?.string() ?: ""
            val root = JSONObject(bodyString)
            val candidates = root.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawJsonText = parts?.optJSONObject(0)?.optString("text", "") ?: ""

            if (rawJsonText.isNotEmpty()) {
                val parsedJson = JSONObject(rawJsonText)
                val reply = parsedJson.optString("reply", "That sounds good! Could you tell me more?")
                val feedback = parsedJson.optString("feedback").takeIf { it.isNotBlank() && it != "null" }
                val nativeAlternative = parsedJson.optString("nativeAlternative").takeIf { it.isNotBlank() && it != "null" }
                val pronunciationTip = parsedJson.optString("pronunciationTip").takeIf { it.isNotBlank() && it != "null" }

                val suggestionsList = mutableListOf<String>()
                val suggestionsArray = parsedJson.optJSONArray("suggestedReplies")
                if (suggestionsArray != null) {
                    for (i in 0 until suggestionsArray.length()) {
                        val s = suggestionsArray.optString(i)
                        if (s.isNotBlank()) suggestionsList.add(s)
                    }
                }

                return@withContext AiSpeakingResponse(
                    reply = reply,
                    feedback = feedback,
                    nativeAlternative = nativeAlternative,
                    pronunciationTip = pronunciationTip,
                    suggestedReplies = if (suggestionsList.isNotEmpty()) suggestionsList else getDefaultSuggestions(scenario)
                )
            } else {
                return@withContext getContextualFallback(scenario, history, userUtterance)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Gemini API request: ${e.message}", e)
            return@withContext getContextualFallback(scenario, history, userUtterance)
        }
    }

    private fun getContextualFallback(
        scenario: Scenario,
        history: List<ConversationMessage>,
        userUtterance: String
    ): AiSpeakingResponse {
        val lower = userUtterance.lowercase()

        val (reply, feedback, nativeAlternative, pronunciationTip, suggestions) = when (scenario.id) {
            "coffee_shop" -> {
                when {
                    lower.contains("latte") || lower.contains("cappuccino") || lower.contains("coffee") -> {
                        Tuple5(
                            "Great choice! What size would you prefer: small, medium, or large? And would you like dairy or oat milk?",
                            "Polite request! Adding 'Could I get...' makes it sound friendly and natural in modern cafes.",
                            "Could I please get a medium latte with oat milk?",
                            "Pronounce 'latte' as /ˈlɑːteɪ/ with stress on the first syllable.",
                            listOf("Medium with oat milk, please.", "Large with whole milk, to go.", "Could you make that decaf?")
                        )
                    }
                    lower.contains("milk") || lower.contains("oat") || lower.contains("almond") || lower.contains("size") -> {
                        Tuple5(
                            "Got it. Medium with oat milk! Would you like anything to eat today? Our fresh blueberry scones just came out of the oven.",
                            "Great pronunciation! Using 'Got it' is very common when confirming details.",
                            "I'll have that to stay, and no pastry today thanks.",
                            "Link 'blueberry scones' smoothly without pausing in between.",
                            listOf("Just the coffee, thanks!", "I'll take a blueberry scone too.", "How much does that come to?")
                        )
                    }
                    lower.contains("how much") || lower.contains("pay") || lower.contains("card") || lower.contains("cash") -> {
                        Tuple5(
                            "That will be $4.75. You can tap your card or phone right on the reader whenever you're ready!",
                            "Well phrased. 'How much does that come to?' is a very common idiom.",
                            "Can I pay with contactless card or Apple Pay?",
                            "Connected speech: 'tap your' blends into /tæp jər/.",
                            listOf("Here's my card, thank you!", "Could I get a receipt, please?", "Have a wonderful day!")
                        )
                    }
                    else -> {
                        Tuple5(
                            "Sure thing! Would you like that hot or iced, and what size can I get for you?",
                            "Clear and understandable speech. Keep working on full sentence structures.",
                            "I'd like an iced drink, medium size please.",
                            "Stress the first syllable in 'medium' (/ˈmiː.di.əm/).",
                            listOf("Make it an iced version, please.", "Hot is great, medium size.", "What syrups do you have?")
                        )
                    }
                }
            }

            "job_interview" -> {
                when {
                    lower.contains("background") || lower.contains("experience") || lower.contains("worked") || lower.contains("years") -> {
                        Tuple5(
                            "That's impressive background. Could you give me an example of a challenging situation you encountered on a team and how you resolved it?",
                            "Strong vocabulary! Notice how using action verbs like 'led', 'designed', or 'collaborated' strengthens interview answers.",
                            "Over the past three years, I've specialized in building scalable user features.",
                            "Pronounce 'resolved' as /rɪˈzɒlvd/ with a voiced /z/ sound.",
                            listOf(
                                "Once we faced a tight deadline, so I organized daily standups.",
                                "A key challenge was prioritizing feature requests among stakeholders.",
                                "I resolved a technical disagreement by setting up a prototype test."
                            )
                        )
                    }
                    lower.contains("strength") || lower.contains("weakness") || lower.contains("skill") -> {
                        Tuple5(
                            "Thank you for sharing that candidly. How do you usually handle feedback from colleagues or management during a project?",
                            "Good composure and articulation. Try using the STAR method (Situation, Task, Action, Result).",
                            "I view constructive feedback as a fast-track way to elevate project quality.",
                            "Stress 'con-STRUC-tive' on the middle syllable.",
                            listOf(
                                "I welcome constructive criticism and use it to iterate quickly.",
                                "I actively seek feedback during weekly one-on-one check-ins.",
                                "Do you encourage peer code reviews on your team?"
                            )
                        )
                    }
                    else -> {
                        Tuple5(
                            "I appreciate that insight. Why are you interested in joining our company specifically at this stage of your career?",
                            "Good pace and clear tone. Remember to pause naturally between thoughts.",
                            "I'm eager to join because your team's mission aligns with my passion for accessibility.",
                            "Pronounce 'specifically' as /spəˈsɪf.ɪ.kli/ (4 syllables).",
                            listOf(
                                "Your emphasis on user experience strongly resonates with me.",
                                "I want to work with a team that values continuous learning.",
                                "What does a typical day look like for someone in this role?"
                            )
                        )
                    }
                }
            }

            "travel_hotel" -> {
                when {
                    lower.contains("reservation") || lower.contains("booking") || lower.contains("name") || lower.contains("check in") -> {
                        Tuple5(
                            "Ah yes, I found your booking right here! You're in room 408 with a King bed. Here are your keycards. Breakfast is served from 7 to 10 AM on the ground floor.",
                            "Perfect check-in greeting! 'I have a reservation under the name...' is the most standard hotel phrase.",
                            "Hi, I have a reservation under Smith for three nights.",
                            "Smooth link: 'keycards' /ˈkiː.kɑːrdz/.",
                            listOf("Where are the elevators located?", "Is Wi-Fi password protected?", "What time is checkout on Sunday?")
                        )
                    }
                    lower.contains("wifi") || lower.contains("wi-fi") || lower.contains("internet") -> {
                        Tuple5(
                            "The Wi-Fi network is 'GrandGuest' and the passcode is your room number plus your last name. High speed connection is complimentary throughout the hotel.",
                            "Clear query. 'Complimentary' is the common hotel word for 'free of charge'.",
                            "Could you please share the Wi-Fi credentials?",
                            "Pronounce 'complimentary' as /ˌkɑːm.pləˈmɛn.tər.i/.",
                            listOf("Thank you! Is room service available 24/7?", "Where can I find an iron and ironing board?", "Could I request extra towels?")
                        )
                    }
                    else -> {
                        Tuple5(
                            "Certainly! Let me assist you with that. Is there anything else you need to make your stay comfortable?",
                            "Natural spoken rhythm. Using polite markers like 'Could you...' or 'Would it be possible...' is very effective.",
                            "Could you recommend a good local restaurant nearby?",
                            "Notice the flap 't' in 'comfortable' /ˈkʌm.fər.t̬ə.bəl/.",
                            listOf("Could you call a taxi for tomorrow morning?", "Can I leave my bags here after checkout?", "Everything looks wonderful, thank you!")
                        )
                    }
                }
            }

            else -> {
                Tuple5(
                    "That's very interesting! I completely agree with your point. What made you think about that particular aspect?",
                    "Great fluency and vocabulary! Your pronunciation is clear and easy to follow.",
                    "From my perspective, that has a huge impact on daily life.",
                    "Practice linking vowel to vowel smoothly: 'to agree' /tuː‿əˈɡriː/.",
                    listOf(
                        "I noticed it happening frequently in my neighborhood.",
                        "It's something I've been reflecting on recently.",
                        "What do you think is the best way forward?"
                    )
                )
            }
        }

        return AiSpeakingResponse(
            reply = reply,
            feedback = feedback,
            nativeAlternative = nativeAlternative,
            pronunciationTip = pronunciationTip,
            suggestedReplies = suggestions
        )
    }

    private fun getDefaultSuggestions(scenario: Scenario): List<String> {
        return scenario.starterPhrases
    }

    private data class Tuple5(
        val reply: String,
        val feedback: String?,
        val nativeAlternative: String?,
        val pronunciationTip: String?,
        val suggestions: List<String>
    )
}
