package org.example.unit2.questions

class Quiz : ProgressPrintable {

    val question1 = Question<String>("Quoth the raven ___", "nevermore", Difficulty.MEDIUM)
    val question2 = Question<Boolean>("The sky is green. True or false", false, Difficulty.EASY)
    val question3 = Question<Int>("How many days are there between full moons?", 28, Difficulty.HARD)

    override val progressText: String
        get() = "${answered} of ${total} answered"

    override fun printProgressBar() {
        repeat(answered) { print("#") }
        repeat(total - answered) { print("#") }
        println()
        println(progressText)
    }

    fun printQuiz() {
        question1.let {
            println(it.questionText)
            println(it.answer)
            println(it.difficulty)
        }
        println()
        question2.let {
            println(it.questionText)
            println(it.answer)
            println(it.difficulty)
        }
        println()
        question3.let {
            println(it.questionText)
            println(it.answer)
            println(it.difficulty)
        }
        println()
    }

    companion object StudentProgress {

        val total: Int = 10
        val answered: Int = 3

    }

}

val Quiz.StudentProgress._progressText: String
    get() = "$answered of $total answered"

fun Quiz.StudentProgress._printProgressBar() {
    repeat(Quiz.answered) {
        print("#")
    }
    println()
    println(Quiz._progressText)
}
