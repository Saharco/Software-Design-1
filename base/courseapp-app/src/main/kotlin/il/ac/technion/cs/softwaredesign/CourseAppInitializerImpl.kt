package il.ac.technion.cs.softwaredesign

import com.google.inject.Inject
import il.ac.technion.cs.softwaredesign.database.DatabaseMap

/**
 * Implementation of CourseAppInitializer, which initializes the data-store from an empty state
 * @see CourseAppInitializer
 */
class CourseAppInitializerImpl @Inject constructor(private val map: DatabaseMap): CourseAppInitializer {
    override fun setup() {
        val dbUsers = map().getValue("users")
        dbUsers.collection("metadata")
                .document("users_data")
                .set(Pair("users_count", "0"))
                .write()
    }
}