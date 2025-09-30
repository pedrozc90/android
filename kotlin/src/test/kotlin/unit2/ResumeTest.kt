package unit2

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import kotlin.test.assertEquals

class ResumeTest {

    // TASK 1
    @Test
    fun test_Task1() {
        data class Event(val title: String, val description: String? = null, val daypart: String, val duration: Int)

        val event1 = Event(
            title = "Study Kotlin",
            description = "Commit to studying Kotlin at least 15 minutes per day.",
            daypart = "Evening",
            duration = 15
        )
        val event2 = Event(title = "Study Kotlin", daypart = "Morning", duration = 15)
    }

    // TASK 2
    enum class DayPart {
        MORNING,
        AFTERNOON,
        EVENING
    }

    data class Event(val title: String, val description: String? = null, val daypart: DayPart, val duration: Int)

    @Test
    fun test_Task2() {
        val event1 = Event(
            title = "Study Kotlin",
            description = "Commit to studying Kotlin at least 15 minutes per day.",
            daypart = DayPart.EVENING,
            duration = 15
        )
        assertAll(
            { assertEquals("Study Kotlin", event1.title) },
            { assertEquals("Commit to studying Kotlin at least 15 minutes per day.", event1.description) },
            { assertEquals(DayPart.EVENING, event1.daypart) },
            { assertEquals(15, event1.duration) }
        )
    }

    // TASK 3
    @Test
    fun test_Task3() {
        val event1 = Event(title = "Wake up", description = "Time to get up", daypart = DayPart.MORNING, duration = 0)
        val event2 = Event(title = "Eat breakfast", daypart = DayPart.MORNING, duration = 15)
        val event3 = Event(title = "Learn about Kotlin", daypart = DayPart.AFTERNOON, duration = 30)
        val event4 = Event(title = "Practice Compose", daypart = DayPart.AFTERNOON, duration = 60)
        val event5 = Event(title = "Watch latest DevBytes video", daypart = DayPart.AFTERNOON, duration = 10)
        val event6 = Event(title = "Check out latest Android Jetpack library", daypart = DayPart.EVENING, duration = 45)

        val events = listOf<Event>(event1, event2, event3, event4, event5, event6)
        assertEquals(6, events.size)
    }

    // TASK 4
    @Test
    fun test_Task4() {
        val event1 = Event(title = "Wake up", description = "Time to get up", daypart = DayPart.MORNING, duration = 0)
        val event2 = Event(title = "Eat breakfast", daypart = DayPart.MORNING, duration = 15)
        val event3 = Event(title = "Learn about Kotlin", daypart = DayPart.AFTERNOON, duration = 30)
        val event4 = Event(title = "Practice Compose", daypart = DayPart.AFTERNOON, duration = 60)
        val event5 = Event(title = "Watch latest DevBytes video", daypart = DayPart.AFTERNOON, duration = 10)
        val event6 = Event(title = "Check out latest Android Jetpack library", daypart = DayPart.EVENING, duration = 45)

        val events = listOf<Event>(event1, event2, event3, event4, event5, event6)
        assertEquals(6, events.size)

        val shortEvents = events.filter { it.duration < 60 }
            .sortedBy { it.duration }
        assertEquals(5, shortEvents.size, "You have ${ shortEvents.size } short events")
    }

    // TASK 5
    @Test
    fun test_Task5() {
        val event1 = Event(title = "Wake up", description = "Time to get up", daypart = DayPart.MORNING, duration = 0)
        val event2 = Event(title = "Eat breakfast", daypart = DayPart.MORNING, duration = 15)
        val event3 = Event(title = "Learn about Kotlin", daypart = DayPart.AFTERNOON, duration = 30)
        val event4 = Event(title = "Practice Compose", daypart = DayPart.AFTERNOON, duration = 60)
        val event5 = Event(title = "Watch latest DevBytes video", daypart = DayPart.AFTERNOON, duration = 10)
        val event6 = Event(title = "Check out latest Android Jetpack library", daypart = DayPart.EVENING, duration = 45)

        val events = listOf<Event>(event1, event2, event3, event4, event5, event6)
        assertEquals(6, events.size)

        val groupedEvents = events.groupBy { it.daypart }
        assertEquals(2, groupedEvents[DayPart.MORNING]?.size)
        assertEquals(3, groupedEvents[DayPart.AFTERNOON]?.size)
        assertEquals(1, groupedEvents[DayPart.EVENING]?.size)
    }

    // TASK 6
    @Test
    fun test_Task6() {
        val event1 = Event(title = "Wake up", description = "Time to get up", daypart = DayPart.MORNING, duration = 0)
        val event2 = Event(title = "Eat breakfast", daypart = DayPart.MORNING, duration = 15)
        val event3 = Event(title = "Learn about Kotlin", daypart = DayPart.AFTERNOON, duration = 30)
        val event4 = Event(title = "Practice Compose", daypart = DayPart.AFTERNOON, duration = 60)
        val event5 = Event(title = "Watch latest DevBytes video", daypart = DayPart.AFTERNOON, duration = 10)
        val event6 = Event(title = "Check out latest Android Jetpack library", daypart = DayPart.EVENING, duration = 45)

        val events = listOf<Event>(event1, event2, event3, event4, event5, event6)
            .sortedWith(compareBy<Event> { it.daypart.ordinal }.thenBy { it.title })

        val lastEvent = events.last()
        assertEquals(DayPart.EVENING, lastEvent.daypart)
    }

}