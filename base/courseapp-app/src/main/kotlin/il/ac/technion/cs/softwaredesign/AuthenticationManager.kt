package il.ac.technion.cs.softwaredesign

import il.ac.technion.cs.softwaredesign.database.CollectionReference
import il.ac.technion.cs.softwaredesign.database.Database
import java.time.LocalDateTime

/**
 * Manages users in a database: this class wraps authentication functionality.
 * Provides common database operations regarding users and login session tokens
 *
 * @see CourseApp
 * @see Database
 *
 * @param usersDb: database in which to store app's users
 * @param tokensDb: (optional) database in which to store app's tokens
 * @param usersRoot: (optional) root collection in which to store users
 * @param tokensRoot: (optional) root collection in which to store tokens
 *
 */
class AuthenticationManager(usersDb: Database,
                            tokensDb: Database = usersDb,
                            private var usersRoot: CollectionReference = usersDb
                                    .collection("root_users"),
                            private var tokensRoot: CollectionReference = tokensDb
                                    .collection("root_tokens")) {

    fun performLogin(username: String, password: String): String {
        val userDocument = usersRoot.document(username)
        val storedPassword = userDocument.read("password")

        if (storedPassword != null && storedPassword != password)
            throw IllegalArgumentException("Incorrect password")
        if (userDocument.read("token") != null)
            throw IllegalArgumentException("User already logged in")

        val token = generateToken(username)
        userDocument.set(Pair("token", token))

        if (storedPassword == null)
            userDocument.set(Pair("password", password))

        userDocument.write()

        tokensRoot.document(token)
                .set(Pair("username", username))
                .write()

        return token
    }

    fun performLogout(token: String) {
        val tokenDocument = tokensRoot.document(token)
        val username = tokenDocument.read("username")
                ?: throw IllegalArgumentException("Invalid token")

        tokenDocument.delete()

        usersRoot.document(username)
                .delete(listOf("token"))
    }

    fun isUserLoggedIn(token: String, username: String): Boolean? {
        if (!tokensRoot.document(token)
                        .exists())
            throw IllegalArgumentException("Invalid token")

        if (!usersRoot.document(username)
                        .exists())
            return null

        val otherToken = usersRoot.document(username)
                .read("token")
        return otherToken != null
    }

    /**
     * Generates a unique token from a given username
     */
    private fun generateToken(username: String): String {
        return "$username+${LocalDateTime.now()}"
    }
}