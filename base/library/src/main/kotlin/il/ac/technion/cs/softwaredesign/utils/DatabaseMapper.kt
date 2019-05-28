package il.ac.technion.cs.softwaredesign.utils

import il.ac.technion.cs.softwaredesign.database.Database

class DatabaseMapper(private val dbMap: Map<String, Database>) {

    operator fun invoke(dbName: String): Database {
        return dbMap.getValue(dbName)
    }
}