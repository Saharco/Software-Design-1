import com.authzee.kotlinguice4.getInstance
import com.google.inject.Guice
import il.ac.technion.cs.softwaredesign.CourseApp
import il.ac.technion.cs.softwaredesign.CourseAppInitializer
import il.ac.technion.cs.softwaredesign.CourseAppModule
import il.ac.technion.cs.softwaredesign.exceptions.*
import il.ac.technion.cs.softwaredesign.storage.SecureStorageModule
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException


class CourseAppTest {

    private val injector = Guice.createInjector(CourseAppModule(), SecureStorageModule())
    private val courseAppInitializer = injector.getInstance<CourseAppInitializer>()

    init {
        courseAppInitializer.setup()
    }

    private val app = injector.getInstance<CourseApp>()
//    private val courseAppStatistics = injector.getInstance<CourseAppStatistics>()


    @Test
    internal fun `user successfully logged in after login`() {
        val token = app.login("sahar", "a very strong password")

        assertEquals(app.isUserLoggedIn(token, "sahar"), true)
    }

    @Test
    internal fun `attempting login twice without logout should throw UserAlreadyLoggedInException`() {
        app.login("sahar", "a very strong password")

        assertThrows<UserAlreadyLoggedInException> {
            app.login("sahar", "a very strong password")
        }
    }

    @Test
    internal fun `creating two users with same username should throw NoSuchEntityException`() {
        app.login("sahar", "a very strong password")
        assertThrows<NoSuchEntityException> {
            app.login("sahar", "weak password")
        }
    }

    @Test
    internal fun `using token to check login session after self's login session expires should throw InvalidTokenException`() {
        val token = app.login("sahar", "a very strong password")
        app.login("yuval", "popcorn")
        app.logout(token)
        assertThrows<InvalidTokenException> {
            app.isUserLoggedIn(token, "yuval")
        }
    }

    @Test
    internal fun `logging out with an invalid token should throw InvalidTokenException`() {
        var token = "invalid token"
        assertThrows<InvalidTokenException> {
            app.logout(token)
        }

        token = app.login("sahar", "a very strong password")
        app.logout(token)

        assertThrows<InvalidTokenException> {
            app.logout(token)
        }
    }

    @Test
    internal fun `two different users should have different tokens`() {
        val token1 = app.login("sahar", "a very strong password")
        val token2 = app.login("yuval", "popcorn")
        assertTrue(token1 != token2)
    }

    @Test
    internal fun `checking if user is logged in when they are not should return false`() {
        val token = app.login("sahar", "a very strong password")
        val otherToken = app.login("yuval", "popcorn")
        app.logout(otherToken)
        assertEquals(app.isUserLoggedIn(token, "yuval"), false)
    }

    @Test
    internal fun `checking if user is logged in when they dont exist should return null`() {
        val token = app.login("sahar", "a very strong password")
        assertNull(app.isUserLoggedIn(token, "yuval"))
    }

    @Test
    internal fun `system can hold lots of distinct users and tokens`() {
        val strings = ArrayList<String>()
        populateWithRandomStrings(strings)
        val users = strings.distinct()
        val systemSize = users.size
        val tokens = HashSet<String>()

        for (i in 0 until systemSize) {
            // Dont care about exact values here: username & password are the same for each user
            val token = app.login(users[i], users[i])
            tokens.add(token)
        }

        assertEquals(tokens.size, users.size)

        for (token in tokens) {
            app.logout(token)
        }
    }

    private fun populateWithRandomStrings(list: ArrayList<String>, amount: Int = 100,
                                          maxSize: Int = 30, charPool: List<Char>? = null) {
        val pool = charPool ?: ('a'..'z') + ('A'..'Z') + ('0'..'9') + '/'
        for (i in 0 until amount) {
            val randomString = (1..maxSize)
                    .map { kotlin.random.Random.nextInt(0, pool.size) }
                    .map(pool::get)
                    .joinToString("")
            list.add(randomString)
        }
    }

    @Test
    internal fun `first logged in user is an administrator by default`() {
        val token = app.login("sahar", "a very strong password")
        app.login("yuval", "weak password")

        app.makeAdministrator(token, "yuval")
    }

    @Test
    internal fun `administrator can appoint other users to be administrators`() {
        val token1 = app.login("sahar", "a very strong password")
        val token2 = app.login("yuval", "weak password")
        app.login("victor", "anak")

        app.makeAdministrator(token1, "yuval")
        app.makeAdministrator(token2, "victor")
    }

    @Test
    internal fun `trying to appoint others to be administrators without a valid token should throw InvalidTokenException`() {
        app.login("sahar", "a very strong password")
        app.login("yuval", "weak password")

        assertThrows<InvalidTokenException> {
            app.makeAdministrator("badToken", "yuval")
        }
    }

    @Test
    internal fun `trying to appoint others to be administrators without authorization should throw UserNotAuthorizedException`() {
        app.login("sahar", "a very strong password")
        val nonAdminToken = app.login("yuval", "weak password")
        app.login("victor", "anak")

        assertThrows<UserNotAuthorizedException> {
            app.makeAdministrator(nonAdminToken, "victor")
        }
    }

    @Test
    internal fun `trying to appoint a non-existing user to be an administrator should NoSuchEntityException`() {
        val adminToken = app.login("sahar", "a very strong password")

        assertThrows<NoSuchEntityException> {
            app.makeAdministrator(adminToken, "yuval")
        }
    }

    @Test
    internal fun `joining a channel with an invalid name should throw NameFormatException`() {
        val adminToken = app.login("sahar", "a very strong password")

        assertThrows<NameFormatException> {
            app.channelJoin(adminToken, "badName")
        }
    }

    @Test
    internal fun `creating a new channel without administrator authorization should throw UserNotAuthorizedException`() {
        app.login("sahar", "a very strong password")
        val notAdminToken = app.login("yuval", "weak password")

        assertThrows<UserNotAuthorizedException> {
            app.channelJoin(notAdminToken, "#TakeCare")
        }
    }

    @Test
    internal fun `administrator can successfully create new channels`() {
        val adminToken = app.login("sahar", "a very strong password")
        app.channelJoin(adminToken, "#TakeCare")
    }

    @Test
    internal fun `users can successfully join an existing channel`() {
        val adminToken = app.login("sahar", "a very strong password")
        val notAdminToken = app.login("yuval", "weak password")
        app.channelJoin(adminToken, "#TakeCare")
        app.channelJoin(notAdminToken, "#TakeCare")
    }
}