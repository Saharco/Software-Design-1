package il.ac.technion.cs.softwaredesign

import com.google.inject.Inject
import il.ac.technion.cs.softwaredesign.utils.DatabaseMapper

/**
 * Implementation of CourseApp querying functionality
 * @see CourseAppStatistics
 */
class CourseAppStatisticsImpl @Inject constructor(dbMapper: DatabaseMapper) : CourseAppStatistics {

    private val dbUsers = dbMapper("users")
    private val dbChannels = dbMapper("channels")
    private val auth = AuthenticationManager(dbUsers, dbChannels)

    override fun totalUsers(): Long {
        return auth.getTotalUsers()
    }

    override fun loggedInUsers(): Long {
        return auth.getLoggedInUsers()
    }

    override fun top10ChannelsByUsers(): List<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun top10ActiveChannelsByUsers(): List<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun top10UsersByChannels(): List<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}