package il.ac.technion.cs.softwaredesign

import il.ac.technion.cs.softwaredesign.database.CollectionReference
import il.ac.technion.cs.softwaredesign.database.Database
import il.ac.technion.cs.softwaredesign.exceptions.InvalidTokenException
import il.ac.technion.cs.softwaredesign.exceptions.NameFormatException
import il.ac.technion.cs.softwaredesign.exceptions.NoSuchEntityException
import il.ac.technion.cs.softwaredesign.exceptions.UserNotAuthorizedException


/**
 * Manages channels in the app: this class wraps channels functionality
 *
 * @see CourseApp
 * @see Database
 *
 * @param dbUsers: database in which to store app's users & tokens
 * @param dbChannels: database in which to store the app's channels
 * @param usersRoot: (optional) root collection in which to store users
 * @param tokensRoot: (optional) root collection in which to store tokens
 * @param channelsRoot: (optional) root collection in which to store channels
 *
 */
class ChannelsManager(private val dbUsers: Database, private val dbChannels: Database,
                      private val usersRoot: CollectionReference = dbUsers
                              .collection("all_users"),
                      private val tokensRoot: CollectionReference = dbUsers
                              .collection("tokens"),
                      private val channelsRoot: CollectionReference = dbChannels
                              .collection("all_channels")) {

    fun channelJoin(token: String, channel: String) {
        val tokenUsername = tokenToUser(token)

        if (!validChannelName(channel)) throw NameFormatException("invalid channel name")

        var newChannelFlag = false

        if (!channelsRoot.document(channel).exists()) {
            if (!isAdmin(tokenUsername))
                throw UserNotAuthorizedException("only an administrator may create a new channel")
            channelsRoot.document(channel)
                    .set("operators", listOf(tokenUsername))
                    .set(Pair("users_count", "1"))
                    .set(Pair("online_users_count", "1"))
                    .write()
            newChannelFlag = true
        }

        val userChannels = usersRoot.document(tokenUsername)
                .readList("channels")?.toMutableList() ?: mutableListOf()


        if (userChannels.contains(channel))
            // user attempted joining a channel they're already members of: finish
            return

        userChannels.add(channel)

        usersRoot.document(tokenUsername)
                .set("channels", userChannels)
                .update()

        if (!newChannelFlag) {
            val usersCount = channelsRoot.document(channel)
                    .read("users_count")?.toInt()?.plus(1) ?: 1
            channelsRoot.document(channel)
                    .set(Pair("users_count", usersCount.toString()))
                    .update()

            val onlineUsersCount = channelsRoot.document(channel)
                    .read("online_users_count")?.toInt()?.plus(1) ?: 1
            channelsRoot.document(channel)
                    .set(Pair("online_users_count", onlineUsersCount.toString()))
                    .update()
        }
    }

    fun channelPart(token: String, channel: String) {
        val tokenUsername = tokenToUser(token)
        verifyChannelExists(channel)

        if (!isMemberOfChannel(tokenUsername, channel))
            throw NoSuchEntityException("user is not a member of the channel")

        expelChannelMember(tokenUsername, channel)
    }

    fun channelMakeOperator(token: String, channel: String, username: String) {
        val tokenUsername = tokenToUser(token)
        verifyChannelExists(channel)

        val isUserAdministrator = isAdmin(tokenUsername)
        val isUserOperator = isOperator(tokenUsername, channel)

        if (!isUserAdministrator && !isUserOperator)
            throw UserNotAuthorizedException("user is not an operator / administrator")

        if (isUserAdministrator && !isUserOperator && tokenUsername != username)
            throw UserNotAuthorizedException("administrator who's not an operator cannot appoint" +
                    "other users to be operators")

        if (!isMemberOfChannel(tokenUsername, channel))
            throw UserNotAuthorizedException("user is not a member in the channel")

        val otherUserExists = usersRoot.document(username)
                .exists()

        if (!otherUserExists || !isMemberOfChannel(username, channel))
            throw NoSuchEntityException("given username is not a member in the channel")

        // all requirements are filled: appoint user to channel operator

        val operators = channelsRoot.document(channel)
                .readList("operators")?.toMutableList() ?: mutableListOf()
        operators.add(username)

        channelsRoot.document(channel)
                .set("operators", operators)
                .update()
    }

    fun channelKick(token: String, channel: String, username: String) {
        val tokenUsername = tokenToUser(token)
        verifyChannelExists(channel)

        if (!isOperator(tokenUsername, channel))
            throw UserNotAuthorizedException("must have operator privileges")

        if (!isMemberOfChannel(username, channel))
            throw NoSuchEntityException("provided username is not a member of this channel")

        expelChannelMember(username, channel)
    }

    fun isUserInChannel(token: String, channel: String, username: String): Boolean? {
        verifyValidAndPrivilegedToQuery(token, channel)

        if (!usersRoot.document(username)
                        .exists())
            return null
        return isMemberOfChannel(username, channel)
    }

    fun numberOfActiveUsersInChannel(token: String, channel: String): Long {
        verifyValidAndPrivilegedToQuery(token, channel)

        return channelsRoot.document(channel)
                .read("online_users_count")?.toLong() ?: 0

    }

    fun numberOfTotalUsersInChannel(token: String, channel: String): Long {
        verifyValidAndPrivilegedToQuery(token, channel)

        return channelsRoot.document(channel)
                .read("users_count")!!.toLong()
    }

    /**
     * Verifies the token & channel for *querying operations*
     *
     * @throws InvalidTokenException If the auth [token] is invalid.
     * @throws NoSuchEntityException If [channel] does not exist.
     * @throws UserNotAuthorizedException If [token] identifies a user who is not an administrator and is not a member
     */
    private fun verifyValidAndPrivilegedToQuery(token: String, channel: String) {
        val tokenUsername = tokenToUser(token)
        verifyChannelExists(channel)
        if (!isAdmin(tokenUsername) && !isMemberOfChannel(tokenUsername, channel))
            throw UserNotAuthorizedException("must be an admin or a member of the channel")
    }

    private fun expelChannelMember(username: String, channel: String) {
        val userChannelsList = usersRoot.document(username)
                .readList("channels")?.toMutableList() ?: mutableListOf()
        userChannelsList.remove(channel)
        usersRoot.document(username)
                .set("channels", userChannelsList)
                .update()

        val operators = channelsRoot.document(channel)
                .readList("operators")?.toMutableList() ?: mutableListOf()

        if (operators.contains(username)) {
            operators.remove(username)
            channelsRoot.document(channel)
                    .set("operators", operators)
                    .update()
        }

        val usersCount = channelsRoot.document(channel)
                .read("users_count")!!.toInt() - 1
        if (usersCount == 0) {
            // the last user has left the channel: delete the channel
            channelsRoot.document(channel)
                    .delete()
            return
        }

        if (usersRoot.document(username)
                        .read("token") != null) {
            val onlineUsersCount = channelsRoot.document(channel)
                    .read("online_users_count")?.toInt()?.minus(1) ?: 0

            channelsRoot.document(channel)
                    .set(Pair("online_users_count", onlineUsersCount.toString()))
                    .update()
        }

        channelsRoot.document(channel)
                .set(Pair("users_count", usersCount.toString()))
                .update()
    }

    /**
     * Returns whether a given user is a member of a given channel or not
     */
    private fun isMemberOfChannel(username: String, channel: String): Boolean {
        val channelsList = usersRoot.document(username)
                .readList("channels")
        return channelsList != null && channelsList.contains(channel)
    }

    /**
     * Makes sure that a given channel exists
     *
     * @throws NoSuchEntityException if the channel does not exist
     */
    private fun verifyChannelExists(channel: String) {
        if (!channelsRoot.document(channel)
                        .exists())
            throw NoSuchEntityException("given channel does not exist")
    }

    /**
     * Translates a token to its corresponding user
     *
     * @throws InvalidTokenException if the token does not belong to any user
     */
    private fun tokenToUser(token: String): String {
        return tokensRoot.document(token)
                .read("username")
                ?: throw InvalidTokenException("token does not match any active user")
    }

    /**
     * Returns whether or not a given user is an administrator
     */
    private fun isAdmin(username: String): Boolean {
        return usersRoot.document(username)
                .read("isAdmin")
                .equals("true")
    }

    /**
     * Returns whether or not a given user is an operator of a given channel
     */
    private fun isOperator(username: String, channel: String): Boolean {
        val channelModerators = channelsRoot.document(channel)
                .readList("operators") ?: return false
        return channelModerators.contains(username)
    }

    /**
     * Checks channels' names validity.
     * A channel's name is valid only if the following hold:
     *  - first letter is '#'
     *  - contains *only* a-z, A-Z, 0-9, '#' or '_' characters
     *
     * @param channel: name of the channel
     * @return true if the channel's name is valid, false otherwise
     */
    private fun validChannelName(channel: String): Boolean {
        if (channel[0] != '#') return false
        val validCharPool = ('a'..'z') + ('A'..'Z') + ('0'..'9') + '#' + '_'
        for (c in channel)
            if (!validCharPool.contains(c)) return false
        return true
    }
}