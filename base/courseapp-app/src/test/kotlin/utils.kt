import il.ac.technion.cs.softwaredesign.CourseApp
import il.ac.technion.cs.softwaredesign.CourseAppStatistics
import org.junit.jupiter.api.Assertions
import java.util.*

data class User(val username: String, val password: String,
                var token: String? = null,
                val channels: MutableList<String> = mutableListOf(),
                var isAdmin: Boolean = false)

data class Channel(val name: String, var totalUsersCount: Int = 0, var onlineUsersCount: Int = 0)

fun CourseApp.leaveRandomChannels(users: ArrayList<User>,
                                  channels: ArrayList<Channel>, min: Int = 10, max: Int = 100) {
    var leaveAmount = kotlin.random.Random.nextInt(min, max)
    while (leaveAmount > 0) {
        val userIndex = kotlin.random.Random.nextInt(users.size)
        if (users[userIndex].token == null || users[userIndex].channels.isEmpty()) continue
        val channelIndex = kotlin.random.Random.nextInt(users[userIndex].channels.size)

        channelPart(users[userIndex].token!!, channels[channelIndex].name)

        users[userIndex].channels.remove(channels[channelIndex].name)
        channels[channelIndex].totalUsersCount--
        channels[channelIndex].onlineUsersCount--
        leaveAmount--
    }
}

fun CourseApp.performRandomRelog(loggedOutUsers: ArrayList<Int>, users: ArrayList<User>,
                                 channels: ArrayList<Channel>, min: Int = 15, max: Int = 40) {
    val relogAmount = kotlin.random.Random.nextInt(min, max)
    for (i in 0..relogAmount) {
        val loggedOutUserIndex = kotlin.random.Random.nextInt(loggedOutUsers.size)
        val user = users[loggedOutUserIndex]

        user.token = login(user.username, user.password)

        for (channel in channels)
            if (user.channels.contains(channel.name))
                channel.onlineUsersCount++

        loggedOutUsers.removeAt(loggedOutUserIndex)
    }
}

fun CourseApp.performRandomLogout(users: ArrayList<User>,
                                  channels: ArrayList<Channel>, min: Int = 20,
                                  max: Int = 50): ArrayList<Int> {
    val loggedOutUsersIndices = ArrayList<Int>()
    var logoutAmount = kotlin.random.Random.nextInt(min, max)
    while (logoutAmount > 0) {
        val chosenUserIndex = kotlin.random.Random.nextInt(0, users.size)
        if (users[chosenUserIndex].token == null) continue

        logout(users[chosenUserIndex].token!!)

        users[chosenUserIndex].token = null
        for (channel in channels)
            if (users[chosenUserIndex].channels.contains(channel.name))
                channel.onlineUsersCount--

        loggedOutUsersIndices.add(chosenUserIndex)
        logoutAmount--
    }
    return loggedOutUsersIndices
}

fun <T> createMaxHeap(list: List<T>, cmp: Comparator<T>, limit: Int = 10):
        PriorityQueue<T> {
    val bigHeap = PriorityQueue<T>(cmp)
    bigHeap.addAll(list)
    val heap = PriorityQueue<T>(cmp)
    var i = limit
    while (i > 0 && bigHeap.isNotEmpty()) {
        heap.add(bigHeap.poll())
        i--
    }
    return heap
}

fun CourseApp.joinRandomChannels(users: ArrayList<User>,
                                 channels: ArrayList<Channel>, min: Int = 200, max: Int = 600) {
    var joinCount = kotlin.random.Random.nextInt(min, max)
    while (joinCount > 0) {
        val chosenUserIndex = kotlin.random.Random.nextInt(0, users.size)
        val chosenChannelIndex = kotlin.random.Random.nextInt(0, channels.size)
        val chosenChannelName = channels[chosenChannelIndex].name
        if (users[chosenUserIndex].channels.contains(chosenChannelName) ||
                users[chosenUserIndex].token == null) continue

        channelJoin(users[chosenUserIndex].token!!, chosenChannelName)

        channels[chosenChannelIndex].totalUsersCount++
        channels[chosenChannelIndex].onlineUsersCount++
        users[chosenUserIndex].channels.add(chosenChannelName)
        joinCount--
    }
}

fun CourseApp.createRandomChannels(admin: User, channelsAmount: Int = 50):
        ArrayList<Channel> {
    val adminToken = admin.token!!
    val channels = ArrayList<Channel>()
    for (i in 0..channelsAmount) {
        val name = UUID.randomUUID().toString()
        channelJoin(adminToken, name)
        channels[i] = Channel(name)
    }
    return channels
}

fun CourseApp.performRandomUsersLogin(usersAmount: Int = 100): ArrayList<User> {
    val users = ArrayList<User>()
    for (i in 0..usersAmount) {
        val name = UUID.randomUUID().toString() // this is both the username & password
        users[i] = User(name, name, login(name, name))
    }
    return users
}

fun verifyQueriesCorrectness(statistics: CourseAppStatistics, users: ArrayList<User>,
                             channels: ArrayList<Channel>) {

    val compareChannelsByUsers = Comparator<Channel> { ch1, ch2 ->
        ch1.totalUsersCount - ch2.totalUsersCount
    }
    val compareChannelsByOnlineUsers = Comparator<Channel> { ch1, ch2 ->
        ch1.onlineUsersCount - ch2.onlineUsersCount
    }
    val compareUsersByChannels = Comparator<User> { user1, user2 ->
        user1.channels.size - user2.channels.size
    }

    val expectedTop10ChannelsByUsers = createMaxHeap(channels, compareChannelsByUsers)
    val expectedTop10ChannelsByActiveUsers = createMaxHeap(channels, compareChannelsByOnlineUsers)
    val expectedTop10UsersByChannels = createMaxHeap(users, compareUsersByChannels)

    Assertions.assertEquals(expectedTop10ChannelsByUsers, statistics.top10ChannelsByUsers())
    Assertions.assertEquals(expectedTop10ChannelsByActiveUsers, statistics.top10ActiveChannelsByUsers())
    Assertions.assertEquals(expectedTop10UsersByChannels, statistics.top10UsersByChannels())
}