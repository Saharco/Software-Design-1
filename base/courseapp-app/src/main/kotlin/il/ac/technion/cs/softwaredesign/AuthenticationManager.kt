package il.ac.technion.cs.softwaredesign

import il.ac.technion.cs.softwaredesign.database.CollectionReference
import il.ac.technion.cs.softwaredesign.database.Database
import il.ac.technion.cs.softwaredesign.exceptions.InvalidTokenException
import il.ac.technion.cs.softwaredesign.exceptions.NoSuchEntityException
import il.ac.technion.cs.softwaredesign.exceptions.UserAlreadyLoggedInException
import il.ac.technion.cs.softwaredesign.exceptions.UserNotAuthorizedException
import java.time.LocalDateTime

/**
 * Manages users in a database: this class wraps authentication functionality.
 * Provides common database operations regarding users and login session tokens
 *
 * @see CourseApp
 * @see Database
 *
 * @param db: database in which to store app's users & tokenss
 * @param usersRoot: (optional) root collection in which to store users
 * @param tokensRoot: (optional) root collection in which to store tokens
 *
 */
class AuthenticationManager(db: Database,
                            private val usersRoot: CollectionReference = db
                                    .collection("all_users"),
                            private val metadataRoot: CollectionReference = db
                                    .collection("metadata"),
                            private val tokensRoot: CollectionReference = db
                                    .collection("tokens")) {

    fun performLogin(username: String, password: String): String {
        val userDocument = usersRoot.document(username)
        val storedPassword = userDocument.read("password")

        if (storedPassword != null && storedPassword != password)
            throw NoSuchEntityException("incorrect password")
        if (userDocument.read("token") != null)
            throw UserAlreadyLoggedInException()

        val token = generateToken(username)
        userDocument.set(Pair("token", token))

        if (storedPassword == null)
            userDocument.set(Pair("password", password))

        val usersCountDocument = metadataRoot.document("users_data")
        var usersCount = usersCountDocument.read("users_count")!!.toInt()

        if (usersCount == 0) userDocument.set(Pair("isAdmin", "true"))

        usersCountDocument.set(Pair("users_count", (++usersCount).toString()))
                .update()

        userDocument.write()

        tokensRoot.document(token)
                .set(Pair("username", username))
                .write()

        return token
    }

    fun performLogout(token: String) {
        val tokenDocument = tokensRoot.document(token)
        val username = tokenDocument.read("username")
                ?: throw InvalidTokenException("token does not match any active user")

        tokenDocument.delete()

        usersRoot.document(username)
                .delete(listOf("token"))
    }

    fun isUserLoggedIn(token: String, username: String): Boolean? {
        if (!tokensRoot.document(token)
                        .exists())
            throw InvalidTokenException("token does not match any active user")

        if (!usersRoot.document(username)
                        .exists())
            return null

        val otherToken = usersRoot.document(username)
                .read("token")
        return otherToken != null
    }


    fun makeAdministrator(token: String, username: String) {
        val tokenUsername = tokensRoot.document(token)
                .read("username")
                ?: throw InvalidTokenException("token does not match any active user")

        usersRoot.document(tokenUsername)
                .read("isAdmin") ?: throw UserNotAuthorizedException("no admin permission")

        if (!usersRoot.document(username)
                        .exists())
            throw NoSuchEntityException("given user does not exist")

        usersRoot.document(username)
                .set(Pair("isAdmin", "true"))
                .update()
    }

    /**
     * Generates a unique token from a given username
     */
    private fun generateToken(username: String): String {
        return "$username+${LocalDateTime.now()}"
    }
}