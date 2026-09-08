package com.example.data.model

data class PronunciationDrill(
    val id: String,
    val title: String,
    val targetSound: String,
    val category: String,
    val targetPhrase: String,
    val phoneticIpa: String,
    val tips: String,
    val syllables: List<String>,
    val difficulty: String = "Medium"
)

object PronunciationDrillRepository {
    val drills: List<PronunciationDrill> = listOf(
        PronunciationDrill(
            id = "p1",
            title = "Voiceless /θ/ vs /s/",
            targetSound = "/θ/ vs /s/",
            category = "Consonants",
            targetPhrase = "I think three thick thorns fell into the sea.",
            phoneticIpa = "/aɪ θɪŋk θriː θɪk θɔːrnz fɛl ˈɪntuː ðə siː/",
            tips = "Place your tongue lightly between your top and bottom teeth for /θ/ ('think'). Pull it back inside behind your teeth for /s/ ('sink').",
            syllables = listOf("I", "think", "three", "thick", "thorns", "fell", "in-to", "the", "sea"),
            difficulty = "Challenging"
        ),
        PronunciationDrill(
            id = "p2",
            title = "Liquid /r/ vs /l/",
            targetSound = "/r/ vs /l/",
            category = "Consonants",
            targetPhrase = "The real red lorry rolled along the long road.",
            phoneticIpa = "/ðə rɪəl rɛd ˈlɔːri roʊld əˈlɔːŋ ðə lɔːŋ roʊd/",
            tips = "For /l/, the tip of your tongue touches the roof of your mouth. For /r/, curl the sides of the tongue back without touching the roof.",
            syllables = listOf("The", "real", "red", "lor-ry", "rolled", "a-long", "the", "long", "road"),
            difficulty = "Challenging"
        ),
        PronunciationDrill(
            id = "p3",
            title = "Fricative /v/ vs /w/",
            targetSound = "/v/ vs /w/",
            category = "Consonants",
            targetPhrase = "We went viewing vintage white wine vineyards.",
            phoneticIpa = "/wi wɛnt ˈvjuːɪŋ ˈvɪntɪdʒ waɪt waɪn ˈvɪnjərdz/",
            tips = "For /v/, press your upper teeth against your lower lip. For /w/, round your lips in an 'O' shape without biting.",
            syllables = listOf("We", "went", "view-ing", "vin-tage", "white", "wine", "vine-yards"),
            difficulty = "Medium"
        ),
        PronunciationDrill(
            id = "p4",
            title = "Connected Speech & Flap T",
            targetSound = "American Flap [ɾ]",
            category = "Connected Speech",
            targetPhrase = "Get a bottle of water later at the party.",
            phoneticIpa = "/ɡɛt ə ˈbɑː.t̬əl əv ˈwɑː.t̬ɚ ˈleɪ.t̬ɚ æt ðə ˈpɑːr.t̬i/",
            tips = "In North American English, a 't' between two vowel sounds becomes a quick tap/flap, sounding like a soft 'd'. Don't pop an explosive 'T'.",
            syllables = listOf("Get a", "bot-tle", "of", "wa-ter", "la-ter", "at the", "par-ty"),
            difficulty = "Easy"
        ),
        PronunciationDrill(
            id = "p5",
            title = "Silent Letters in English",
            targetSound = "Silent Letters",
            category = "Spelling Nuances",
            targetPhrase = "The subtle subtle doubt of the handsome knight.",
            phoneticIpa = "/ðə ˈsʌt.əl daʊt əv ðə ˈhæn.səm naɪt/",
            tips = "Notice that the 'b' in 'subtle' and 'doubt', the 'd' in 'handsome', and the 'k' in 'knight' are completely silent!",
            syllables = listOf("The", "sub-tle", "doubt", "of the", "hand-some", "knight"),
            difficulty = "Medium"
        ),
        PronunciationDrill(
            id = "p6",
            title = "Contractions & Reductions",
            targetSound = "Reductions",
            category = "Connected Speech",
            targetPhrase = "I would have gone if you had asked me.",
            phoneticIpa = "/aɪ wʊdəv ɡɔːn ɪf juːd æskt miː/",
            tips = "Native speakers contract 'would have' into 'would've' (/wʊdəv/) or 'woulda'. Practice blending the words smoothly together.",
            syllables = listOf("I would've", "gone", "if you'd", "asked", "me"),
            difficulty = "Medium"
        )
    )
}
