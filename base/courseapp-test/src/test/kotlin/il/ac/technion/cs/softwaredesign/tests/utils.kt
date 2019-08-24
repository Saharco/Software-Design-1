package il.ac.technion.cs.softwaredesign.tests

import com.natpryce.hamkrest.*
import il.ac.technion.cs.softwaredesign.CourseApp
import org.junit.jupiter.api.Assertions.assertTimeoutPreemptively
import org.junit.jupiter.api.function.ThrowingSupplier
import java.io.BufferedReader
import java.io.FileReader
import java.time.Duration
import java.util.ArrayList
import kotlin.streams.toList

// This should be standard.
val isTrue = equalTo(true)
val isFalse = equalTo(false)

fun <T> containsElementsInOrder(vararg elements: T): Matcher<Collection<T>> {
    val perElementMatcher = object : Matcher.Primitive<Collection<T>>() {
        override fun invoke(actual: Collection<T>): MatchResult {
            elements.zip(actual).forEach {
                if (it.first != it.second)
                    return MatchResult.Mismatch("${it.first} does not equal ${it.second}")
            }
            return MatchResult.Match
        }

        override val description = "is ${describe(elements)}"
        override val negatedDescription = "is not ${describe(elements)}"
    }
    return has(Collection<T>::size, equalTo(elements.size)) and perElementMatcher
}

// This is a tiny wrapper over assertTimeoutPreemptively which makes the syntax slightly nicer.
fun <T> runWithTimeout(timeout: Duration, executable: () -> T): T =
        assertTimeoutPreemptively(timeout, ThrowingSupplier(executable))

enum class UserCsvIdx(val idx: Int) {
    USER_NAME_IDX(0),
    PASS_IDX(1),
    IS_LOGGED_OUT_IDX(2)
}

data class User(val id: String,
                val pass: String,
                val isLoggedOut: Boolean) {
    var token: String = ""
}

data class ChannelUserEntry(val channel: String,
                            val user: String,
                            val isOperator: Boolean)

enum class ChannelUserCsvIdx(val idx: Int) {
    CHANNEL_IDX(0),
    USER_NAME_IDX(1),
    IS_OPERATOR_IDX(2)
}

fun readUsersCsv(fileName: String): List<User> {
    var userData = ArrayList<User>()
    var line: String?
    var fileReader = BufferedReader(FileReader(fileName))
    fileReader.readLine()
    line = fileReader.readLine()
    while (line != null) {

        val tokens = line.split(',') // TODO: Use a CSV parsing library. opencsv looks OK: https://mvnrepository.com/artifact/com.opencsv/opencsv/4.5
        if (tokens.size > 0) {
            val user = User(
                    id = tokens[UserCsvIdx.USER_NAME_IDX.idx],
                    pass = tokens[UserCsvIdx.PASS_IDX.idx],
                    isLoggedOut = tokens[UserCsvIdx.IS_LOGGED_OUT_IDX.idx] == "1"
            )
            userData.add(user)
        }
        line = fileReader.readLine()
    }
    return userData
}

fun readChannelsCsv(fileName: String): List<ChannelUserEntry> {
    var channelUserData = ArrayList<ChannelUserEntry>()
    var line: String?
    var fileReader = BufferedReader(FileReader(fileName))
    fileReader.readLine()
    line = fileReader.readLine()
    while (line != null) {

        val tokens = line.split(',')
        if (tokens.size > 0) {
            val user = ChannelUserEntry(
                    channel = tokens[ChannelUserCsvIdx.CHANNEL_IDX.idx],
                    user = tokens[ChannelUserCsvIdx.USER_NAME_IDX.idx],
                    isOperator = tokens[ChannelUserCsvIdx.IS_OPERATOR_IDX.idx] == "1"
            )
            channelUserData.add(user)
        }
        line = fileReader.readLine()
    }
    return channelUserData
}

fun getPathOfFile(fileName: String): String {
    return object {}.javaClass.classLoader.getResource(fileName).path
}

fun loadDataForTest(courseApp: CourseApp, baseTestName: String): Map<String, String> {
    val userData = readUsersCsv(getPathOfFile(baseTestName + "_users.csv"))
    val channelUserData = readChannelsCsv(getPathOfFile(baseTestName + "_channels_to_user.csv"))
    val uniqueChannels = channelUserData.stream().map { entry -> entry.channel }.distinct().toList()

    val tokenMap = HashMap<String, String>()
    tokenMap.put("MainAdmin", courseApp.login("MainAdmin", "Password"))

    uniqueChannels.stream().forEach {
        initChannel(it, tokenMap, courseApp)
    }

    for (user in userData) {
        val token = courseApp.login(user.id, user.pass)
        tokenMap.put(user.id, token)
    }

    channelUserData.stream().forEach {
        courseApp.channelJoin(tokenMap[it.user]!!, it.channel)
        if (it.isOperator) {
            val channelFirstUserName = getAdminOfChannel(it.channel)
            courseApp.channelMakeOperator(tokenMap[channelFirstUserName]!!, it.channel, it.user)
        }
    }

    userData.stream().filter({ u -> u.isLoggedOut }).forEach(
            { u -> courseApp.logout(tokenMap.get(u.id)!!) }
    )

    return tokenMap
}

private fun initChannel(channel: String, tokenMap: HashMap<String, String>, courseApp: CourseApp) {
    val channelFirstUserName = getAdminOfChannel(channel)
    tokenMap.put(channelFirstUserName, courseApp.login(channelFirstUserName, "Password2"))
    courseApp.makeAdministrator(tokenMap["MainAdmin"]!!, channelFirstUserName)
    courseApp.channelJoin(tokenMap[channelFirstUserName]!!, channel)
}

fun getAdminOfChannel(channel: String) = channel.substring(1) + "_Admin"


fun assertWithTimeout(executable: () -> Unit, timeout: Duration): Unit =
        runWithTimeout(timeout, executable)

fun assertWithTimeout(executable: () -> Unit): Unit =
        runWithTimeout(Duration.ofSeconds(10), executable)