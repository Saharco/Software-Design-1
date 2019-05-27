package il.ac.technion.cs.softwaredesign

import com.authzee.kotlinguice4.KotlinModule
import com.google.inject.Provides
import com.google.inject.Singleton
import il.ac.technion.cs.softwaredesign.database.CourseAppDatabaseFactory
import il.ac.technion.cs.softwaredesign.database.Database
import il.ac.technion.cs.softwaredesign.database.DatabaseMap
import il.ac.technion.cs.softwaredesign.database.mocks.SecureStorageFactoryMock
import il.ac.technion.cs.softwaredesign.storage.SecureStorageFactory

class CourseAppModule : KotlinModule() {

    private val factory = SecureStorageFactoryMock() //TODO: change this when submitting
    private val dbFactory = CourseAppDatabaseFactory(factory)

    override fun configure() {
        /*
        This binding should be changed to the actual remote storage factory class in courseapp-test.
        For this package's purposes: we will use a *mock* implementation (no persistent storage) in
        order to properly test the CourseApp functionality
         */
//        bind<SecureStorageFactory>().to<SecureStorageFactoryMock>()

        /*
        These bindings inject our implementation for the provided CourseApp interfaces
         */
        bind<CourseApp>().to<CourseAppImpl>()
        bind<CourseAppStatistics>().to<CourseAppStatisticsImpl>()
        bind<CourseAppInitializer>().to<CourseAppInitializerImpl>()
    }

    @Provides
    @Singleton
    fun courseAppProvider(): DatabaseMap {
        val map = mutableMapOf<String, Database>()
        mapNewDatabase(map, "users")
        mapNewDatabase(map, "channels")
        return DatabaseMap(map)
    }

    private fun mapNewDatabase(dbMap: MutableMap<String, Database>, dbName: String) {
        dbMap[dbName] = dbFactory.open(dbName)
    }
}