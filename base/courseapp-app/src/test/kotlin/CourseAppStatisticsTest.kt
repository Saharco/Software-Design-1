import com.authzee.kotlinguice4.getInstance
import com.google.inject.Guice
import il.ac.technion.cs.softwaredesign.CourseApp
import il.ac.technion.cs.softwaredesign.CourseAppInitializer
import il.ac.technion.cs.softwaredesign.CourseAppModule
import il.ac.technion.cs.softwaredesign.CourseAppStatistics
import il.ac.technion.cs.softwaredesign.storage.SecureStorageModule
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class CourseAppStatisticsTest {
    private val injector = Guice.createInjector(CourseAppModule(), SecureStorageModule())
    private val courseAppInitializer = injector.getInstance<CourseAppInitializer>()

    init {
        courseAppInitializer.setup()
    }

    private val app = injector.getInstance<CourseApp>()
    private val statistics = injector.getInstance<CourseAppStatistics>()

    @Test
    internal fun `can query total users when no users exist yet`() {
        assertEquals(0, statistics.totalUsers())
    }

    @Test
    internal fun `can query logged in users when no users are logged in`() {
        assertEquals(0, statistics.loggedInUsers())
    }

    @Test
    internal fun `adding distinct users to the system properly increases total number of users`() {
        val token = app.login("sahar", "a very strong password")
        assertEquals(1, statistics.totalUsers())

        app.login("yuval", "a weak password")
        assertEquals(2, statistics.totalUsers())

        app.logout(token)

        assertEquals(2, statistics.totalUsers())
    }

    @Test
    internal fun `logging in & out changes the amount of logged in users accordingly`() {
        val token1 = app.login("sahar", "a very strong password")
        val token2 = app.login("yuval", "a weak password")

        assertEquals(2, statistics.loggedInUsers())

        app.logout(token1)
        assertEquals(1, statistics.loggedInUsers())

        app.logout(token2)
        assertEquals(0, statistics.loggedInUsers())

        app.login("sahar", "a very strong password")
        assertEquals(1, statistics.loggedInUsers())
    }
}