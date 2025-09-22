package questions

import org.example.questions.Difficulty
import org.example.questions.Question
import org.example.questions.Quiz
import kotlin.test.Test
import kotlin.test.assertEquals

class QuestionTest {

    @Test
    fun testGenericClass() {
        val question1 = Question<String>("Quoth the raven ___", "nevermore", Difficulty.MEDIUM)
        val question2 = Question<Boolean>("The sky is green. True or false", false, Difficulty.EASY)
        val question3 = Question<Int>("How many days are there between full moons?", 28, Difficulty.HARD)
        println(question1)
    }

    @Test
    fun testObject() {
        val result = "${Quiz.answered} of ${Quiz.total} answered"
        assertEquals("3 of 10 answered", result)
        assertEquals("3 of 10 answered", Quiz().progressText)
        Quiz().printProgressBar()
    }

    @Test
    fun testApply() {
        Quiz().apply {
            printQuiz()
        }
    }

}