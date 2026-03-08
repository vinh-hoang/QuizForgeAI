package ai.quiz.forge.service.model

import ai.quiz.forge.shared.Option

data class Question(
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val hint: String,
    var correctOption: Option?=null,
    var selectedOption: Option? = null,
    var explanation: String? = null,
)
