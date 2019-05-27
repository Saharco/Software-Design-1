package il.ac.technion.cs.softwaredesign

import com.github.salomonbrys.kotson.toJson
import il.ac.technion.cs.softwaredesign.database.CollectionReference
import il.ac.technion.cs.softwaredesign.database.Database
import il.ac.technion.cs.softwaredesign.exceptions.InvalidTokenException
import il.ac.technion.cs.softwaredesign.exceptions.NameFormatException
import il.ac.technion.cs.softwaredesign.exceptions.NoSuchEntityException
import il.ac.technion.cs.softwaredesign.exceptions.UserNotAuthorizedException


/**
 * Manages channels in the app: this class wraps all functionality that corresponds to channels
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

    private val usersMetadataRoot: CollectionReference = dbUsers.collection("metadata")
    private val channelsMetadataRoot: CollectionReference = dbChannels.collection("metadata")

    fun channelJoin(token: String, channel: String) {
        val tokenUsername = tokenToUser(token)

        if (!validChannelName(channel)) throw NameFormatException("invalid channel name")

        if (!channelsRoot.document(channel).exists()) {
            if (!isAdmin(tokenUsername))
                throw UserNotAuthorizedException("only an administrator may create a new channel")
            channelsRoot.document("channel")
                    //TODO: .set (...), user is an operator!
                    .write()
        }

        //TODO: add user to channel here
    }

    fun channelPart(token: String, channel: String) {
        val tokenUsername = tokenToUser(token)
        verifyChannelExists(channel)

        //TODO: delete user from channel & potentially delete channel
    }

    fun channelMakeOperator(token: String, channel: String, username: String) {
        val tokenUsername = tokenToUser(token)
        verifyChannelExists(channel)


    }

    private fun verifyChannelExists(channel: String) {
        if (!channelsRoot.document(channel)
                .exists())
            throw NoSuchEntityException("given channel does not exist")
    }


    private fun tokenToUser(token: String): String {
        return tokensRoot.document(token)
                .read("username")
                ?: throw InvalidTokenException("token does not match any active user")
    }

    private fun isAdmin(username: String): Boolean {
        return usersRoot.document(username)
                .read("isAdmin")
                .equals("true")
    }

    private fun validChannelName(channel: String): Boolean {
        if (channel[0] != '#') return false
        val validCharPool = ('a'..'z') + ('A'..'Z') + ('0'..'9') + '#' + '_'
        for (c in channel)
            if (!validCharPool.contains(c)) return false
        return true
    }
}