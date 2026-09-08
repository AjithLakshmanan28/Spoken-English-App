package com.example.data.model

data class TongueTwister(
    val id: String,
    val title: String,
    val text: String,
    val focusSound: String,
    val difficulty: String,
    val speedTip: String
)

object TongueTwisterRepository {
    val twisters: List<TongueTwister> = listOf(
        TongueTwister(
            id = "tt1",
            title = "Peter Piper's Peppers",
            text = "Peter Piper picked a peck of pickled peppers. A peck of pickled peppers Peter Piper picked.",
            focusSound = "Bilabial plosive /p/",
            difficulty = "Easy",
            speedTip = "Focus on crisp lip closure and quick puff of air on each /p/."
        ),
        TongueTwister(
            id = "tt2",
            title = "Seashells on the Seashore",
            text = "She sells seashells by the seashore. The shells she sells are surely seashells.",
            focusSound = "Sibilant /s/ vs /ʃ/",
            difficulty = "Medium",
            speedTip = "Switch tongue positions cleanly between /s/ ('sells') and /ʃ/ ('she', 'shells')."
        ),
        TongueTwister(
            id = "tt3",
            title = "The Woodchuck Question",
            text = "How much wood would a woodchuck chuck if a woodchuck could chuck wood?",
            focusSound = "Glides /w/ and affricates /tʃ/",
            difficulty = "Medium",
            speedTip = "Keep the vowel sound in 'wood' and 'would' short (/ʊ/), not long like 'food'."
        ),
        TongueTwister(
            id = "tt4",
            title = "Betty Botter's Batter",
            text = "Betty Botter bought some butter, but she said the butter's bitter. If I put it in my batter, it will make my batter bitter.",
            focusSound = "Voiced /b/ and flap /t/",
            difficulty = "Challenging",
            speedTip = "Keep your mouth relaxed and bounce quickly across the flap 't's."
        ),
        TongueTwister(
            id = "tt5",
            title = "Unique New York",
            text = "You know New York, you need New York, you know you need unique New York.",
            focusSound = "Nasal /n/ and glide /j/",
            difficulty = "Challenging",
            speedTip = "Emphasize the second syllable of 'u-NIQUE' (/juːˈniːk/)."
        )
    )
}
