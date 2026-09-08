package com.example.data.model

data class DailyPhrase(
    val id: String,
    val phrase: String,
    val phonetic: String,
    val meaning: String,
    val contextType: String, // e.g. "Workplace", "Casual", "Social"
    val dialogSpeakerA: String,
    val dialogSpeakerB: String,
    val speakingTip: String
)

object DailyPhraseRepository {
    val phrases: List<DailyPhrase> = listOf(
        DailyPhrase(
            id = "dp1",
            phrase = "Hit the nail on the head",
            phonetic = "/hɪt ðə neɪl ɒn ðə hɛd/",
            meaning = "To describe exactly what is causing a situation or problem; to be entirely accurate.",
            contextType = "Workplace & Debate",
            dialogSpeakerA = "I think the project is delayed simply because we didn't define the scope clearly.",
            dialogSpeakerB = "You hit the nail on the head! That's exactly where we lost track of time.",
            speakingTip = "Emphasize 'nail' and 'head'. Link 'hit the' smoothly as 'hit-the'."
        ),
        DailyPhrase(
            id = "dp2",
            phrase = "Play it by ear",
            phonetic = "/pleɪ ɪt baɪ ɪər/",
            meaning = "To proceed without a strict plan and deal with the situation as it develops.",
            contextType = "Social Plans",
            dialogSpeakerA = "What time are we meeting up for dinner tonight?",
            dialogSpeakerB = "I have a late meeting, so let's play it by ear and text around 6 PM.",
            speakingTip = "Notice how 'play it' blends together as 'play-it' (/pleɪ.ɪt/)."
        ),
        DailyPhrase(
            id = "dp3",
            phrase = "Call it a day",
            phonetic = "/kɔːl ɪt ə deɪ/",
            meaning = "To decide or agree to stop doing something (often work for the day).",
            contextType = "Workplace",
            dialogSpeakerA = "We've been reviewing these slides for four hours straight.",
            dialogSpeakerB = "Good point. Let's call it a day and finish the rest with fresh eyes tomorrow.",
            speakingTip = "Connected speech: 'call it a' sounds like 'call-i-tuh' (/kɔː.lɪ.tə/)."
        ),
        DailyPhrase(
            id = "dp4",
            phrase = "Break the ice",
            phonetic = "/breɪk ði aɪs/",
            meaning = "To make people feel more relaxed and comfortable in a social or meeting setting.",
            contextType = "Networking",
            dialogSpeakerA = "Everyone was so quiet at the beginning of the workshop.",
            dialogSpeakerB = "Until Maya broke the ice with that hilarious travel story!",
            speakingTip = "Because 'ice' starts with a vowel, 'the' is pronounced 'thee' (/ði/)."
        ),
        DailyPhrase(
            id = "dp5",
            phrase = "Touch base",
            phonetic = "/tʌtʃ beɪs/",
            meaning = "To briefly make contact or communicate with someone to catch up or share updates.",
            contextType = "Business & Professional",
            dialogSpeakerA = "Could we touch base next Monday regarding the new client proposal?",
            dialogSpeakerB = "Definitely! Send a quick calendar invite for ten minutes.",
            speakingTip = "Keep the transition between 'touch' (/tʃ/) and 'base' (/b/) crisp."
        ),
        DailyPhrase(
            id = "dp6",
            phrase = "Cut corners",
            phonetic = "/kʌt ˈkɔːrnərz/",
            meaning = "To do something in the easiest, cheapest, or fastest way, often sacrificing quality.",
            contextType = "Quality & Craft",
            dialogSpeakerA = "Why did the app crash right after the update?",
            dialogSpeakerB = "They cut corners on automated testing to hit the arbitrary deadline.",
            speakingTip = "Strong stress on the first syllable of 'COR-ners'."
        )
    )
}
