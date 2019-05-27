package il.ac.technion.cs.softwaredesign

import com.google.inject.Inject
import il.ac.technion.cs.softwaredesign.database.DatabaseMap

/**
 * Implementation of CourseApp functionality
 * @see CourseApp
 */
class CourseAppImpl @Inject constructor(map: DatabaseMap) : CourseApp {

    private val dbUsers = map().getValue("users")
    private val auth = AuthenticationManager(dbUsers)


    override fun login(username: String, password: String): String {
        return auth.performLogin(username, password)
    }

    override fun logout(token: String) {
        return auth.performLogout(token)
    }

    override fun isUserLoggedIn(token: String, username: String): Boolean? {
        return auth.isUserLoggedIn(token, username)
    }

    override fun makeAdministrator(token: String, username: String) {
        return auth.makeAdministrator(token, username)
    }

    override fun channelJoin(token: String, channel: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun channelPart(token: String, channel: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun channelMakeOperator(token: String, channel: String, username: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun channelKick(token: String, channel: String, username: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isUserInChannel(token: String, channel: String, username: String): Boolean? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun numberOfActiveUsersInChannel(token: String, channel: String): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun numberOfTotalUsersInChannel(token: String, channel: String): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}