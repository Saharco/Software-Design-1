package il.ac.technion.cs.softwaredesign

import com.authzee.kotlinguice4.KotlinModule
import com.google.inject.Inject
import com.google.inject.Provides
import com.google.inject.Singleton
import il.ac.technion.cs.softwaredesign.database.*
import il.ac.technion.cs.softwaredesign.storage.SecureStorageFactory
import il.ac.technion.cs.softwaredesign.utils.DatabaseMapper

class CourseAppModule : KotlinModule() {

    override fun configure() {
        bind<CourseApp>().to<CourseAppImpl>()
        bind<CourseAppStatistics>().to<CourseAppStatisticsImpl>()
        bind<CourseAppInitializer>().to<CourseAppInitializerImpl>()
    }

    @Provides
    @Singleton
    @Inject
    fun dbMapperProvider(factory: SecureStorageFactory): DatabaseMapper {
        val dbFactory = SecureDatabaseFactory(factory)
        val statisticsFactory = SecureStatisticsFactory(factory)
        val dbMap = mutableMapOf<String, Database>()
        val statisticsMap = mutableMapOf<String, Statistical<Int, String>>()

        mapNewDatabase(dbFactory, dbMap, "users")
        mapNewDatabase(dbFactory, dbMap, "channels")

        mapNewStatistics(statisticsFactory, statisticsMap, "channels_by_users")
        mapNewStatistics(statisticsFactory, statisticsMap, "channels_by_active_users")
        mapNewStatistics(statisticsFactory, statisticsMap, "users_by_channels")

        return DatabaseMapper(dbMap, statisticsMap)
    }

    private fun mapNewDatabase(dbFactory: DatabaseFactory, dbMap: MutableMap<String, Database>, dbName: String) {
        dbMap[dbName] = dbFactory.open(dbName)
    }

    private fun mapNewStatistics(statisticsFactory: SecureStatisticsFactory,
                                 storageMap: MutableMap<String, Statistical<Int, String>>, statisticsName: String) {
        storageMap[statisticsName] = statisticsFactory.open(statisticsName)
    }
}