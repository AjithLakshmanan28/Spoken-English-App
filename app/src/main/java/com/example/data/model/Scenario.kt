package com.example.data.model

data class Scenario(
    val id: String,
    val title: String,
    val tagline: String,
    val category: String,
    val level: String,
    val aiRole: String,
    val userRole: String,
    val contextPrompt: String,
    val initialAiGreeting: String,
    val starterPhrases: List<String>,
    val emoji: String,
    val accentColorHex: Long = 0xFF4338CA
)

object ScenarioRepository {
    val scenarios: List<Scenario> = listOf(
        Scenario(
            id = "coffee_shop",
            title = "Coffee Shop & Cafe",
            tagline = "Order your favorite drink, customize milk & size, and pay.",
            category = "Daily Life",
            level = "Beginner",
            aiRole = "Barista (Alex)",
            userRole = "Customer",
            contextPrompt = "You are Alex, a friendly barista at 'The Daily Grind' coffee shop. Speak in concise, warm, natural conversational English. Ask questions one at a time, suggest pastries, and clarify drink sizes or milk preferences.",
            initialAiGreeting = "Hi there! Welcome to The Daily Grind. What can I get started for you today?",
            starterPhrases = listOf(
                "Could I get a medium iced caramel latte, please?",
                "Do you have any oat milk options?",
                "What kind of pastries do you recommend today?"
            ),
            emoji = "☕",
            accentColorHex = 0xFFD97706
        ),
        Scenario(
            id = "job_interview",
            title = "Job Interview",
            tagline = "Introduce yourself, explain strengths, and answer behavioral questions.",
            category = "Career",
            level = "Advanced",
            aiRole = "Interview Panelist (Sarah)",
            userRole = "Job Candidate",
            contextPrompt = "You are Sarah, a professional and encouraging hiring manager interviewing a candidate. Ask thoughtful interview questions, comment naturally on the candidate's answers, and ask follow-up questions about teamwork or problem solving.",
            initialAiGreeting = "Hello! Thanks for meeting with us today. To start off, could you tell me a little bit about yourself and your background?",
            starterPhrases = listOf(
                "Sure! I have a background in software development and love solving tricky user problems.",
                "Thank you for having me. I've spent the past few years focusing on mobile design.",
                "Glad to be here. My greatest strength is collaborating across diverse teams."
            ),
            emoji = "💼",
            accentColorHex = 0xFF4338CA
        ),
        Scenario(
            id = "travel_hotel",
            title = "Hotel Check-In & Travel",
            tagline = "Check into your room, ask for amenities, and request local travel tips.",
            category = "Travel",
            level = "Beginner",
            aiRole = "Front Desk Concierge (Liam)",
            userRole = "Hotel Guest",
            contextPrompt = "You are Liam, a polite front desk clerk at a boutique hotel. Greet the guest, confirm their reservation details, answer questions about breakfast and Wi-Fi, and offer local recommendations.",
            initialAiGreeting = "Good afternoon! Welcome to the Grand Horizon Hotel. How may I assist you today?",
            starterPhrases = listOf(
                "Hi, I have a reservation under the name John Smith for three nights.",
                "Is complimentary breakfast included in the stay?",
                "Could you recommend a nice dinner spot within walking distance?"
            ),
            emoji = "🏨",
            accentColorHex = 0xFF0D9488
        ),
        Scenario(
            id = "casual_networking",
            title = "Networking & Casual Chat",
            tagline = "Break the ice, discuss weekend plans, hobbies, and make new connections.",
            category = "Social",
            level = "Intermediate",
            aiRole = "Friendly Attendee (Emma)",
            userRole = "Event Attendee",
            contextPrompt = "You are Emma, an outgoing fellow attendee at an international community mixer. Chat casually about hobbies, city life, movies, or weekend plans. Use natural spoken idioms and keep the conversation balanced.",
            initialAiGreeting = "Hey there! Mind if I join you? Quite a lively turnout today, isn't it?",
            starterPhrases = listOf(
                "Not at all! Have you been to one of these events before?",
                "It's great! I just moved to the city a few months ago.",
                "Yeah, the crowd is vibrant. Are you working nearby?"
            ),
            emoji = "🤝",
            accentColorHex = 0xFFE11D48
        ),
        Scenario(
            id = "doctor_visit",
            title = "Doctor's Appointment",
            tagline = "Describe symptoms, ask about medication instructions, and schedule a checkup.",
            category = "Everyday",
            level = "Intermediate",
            aiRole = "Dr. Miller",
            userRole = "Patient",
            contextPrompt = "You are Dr. Miller, an empathetic physician. Listen to the patient's symptoms, ask clarifying questions (duration, severity), and provide clear, reassuring medical advice in plain English.",
            initialAiGreeting = "Hello! Come on in and take a seat. What brings you into the clinic today?",
            starterPhrases = listOf(
                "Hi doctor, I've had a persistent sore throat and a slight fever since Tuesday.",
                "I feel dizzy whenever I stand up too quickly.",
                "Should I take this medication before or after meals?"
            ),
            emoji = "🩺",
            accentColorHex = 0xFF0284C7
        ),
        Scenario(
            id = "airport_flight",
            title = "Airport & Boarding",
            tagline = "Navigate luggage check-in, gate changes, and board your flight smoothly.",
            category = "Travel",
            level = "Beginner",
            aiRole = "Airline Agent (Marcus)",
            userRole = "Traveler",
            contextPrompt = "You are Marcus, an airline check-in agent at Gate B12. Help the passenger check bags, confirm passport/ticket info, and give gate departure times.",
            initialAiGreeting = "Good morning, airline tickets and passports, please! Where are you flying to today?",
            starterPhrases = listOf(
                "Here's my passport. I'm flying to London Heathrow with one checked bag.",
                "Is the flight running on time, or is there any gate delay?",
                "Could I request a window seat if there's one available?"
            ),
            emoji = "✈️",
            accentColorHex = 0xFF7C3AED
        ),
        Scenario(
            id = "free_conversation",
            title = "Open Practice (Any Topic)",
            tagline = "Talk about anything you like! Get instant pronunciation & grammar corrections.",
            category = "General",
            level = "All Levels",
            aiRole = "English Coach (Chloe)",
            userRole = "Learner",
            contextPrompt = "You are Chloe, an expert, enthusiastic English speaking coach. Chat about whatever topic the user brings up. Gently point out grammar nuances and celebrate great phrasing.",
            initialAiGreeting = "Hi there! I'm Chloe, your speaking coach. What's on your mind today? We can chat about movies, sports, tech, or your daily life!",
            starterPhrases = listOf(
                "I want to practice talking about my favorite movie.",
                "Can we discuss how to explain complex ideas simply?",
                "Tell me about a fun cultural tradition in English-speaking countries."
            ),
            emoji = "✨",
            accentColorHex = 0xFFEC4899
        )
    )

    fun getScenarioById(id: String): Scenario {
        return scenarios.find { it.id == id } ?: scenarios.first()
    }
}
