package il.ac.technion.cs.softwaredesign.database

class DatabaseMap(val dbMap: Map<String, Database>) {

    operator fun invoke(): Map<String, Database> {
        return dbMap
    }
}