# CourseApp: Assignment 1

## Authors
* Sahar Cohen, 206824088
* Yuval Nahon, 206866832

### Previous assignment
This assignment uses the code from the submission by: 206824088-206866832

## Notes

### Implementation Summary

#### Database Abstraction
Provides a convenient file system hierarchy for remote storage. Split into five interfaces:

* **DatabaseFactory**: opens new / existing databases. Implemented in CourseAppDatabaseFactory.
* **Database**: akin to a *file system*: references its root. Used solely for navigating to nested collections.
* **CollectionReference**: akin to a *folder* in a file system. Used solely for holding documents.
* **DocumentReference**: akin to a *file* in a file system. Values are set in a key-value fashion. Terminal operations include: write/read/update/delete etc.
* **ExtendableDocumentReference**: a DocumentReference that can contain its own collections.

**Example use:**

```kotlin
// Factory pattern for opening databases
val db = CourseAppDatabaseFactory( /* injected SecureStorageFactory */ ).open("cool database")

// Fluent API for interactions with the database.
// Overloaded versions of methods to provide comfortable use in many common cases
db.collection("users")
  .document("sahar")
  .set(Pair("lastname", "cohen"))
  .set("email accounts", listOf("example@gmail.com", "undergrad@campus.technion.ac.il"))
  .write()

val lastnameResult = db.collection("users")
                  .document("sahar")
                  .read("lastname") // = "cohen"
                  
// Many more methods!
```

#### CourseApp Database Managers
The database is utilized by the following classes for managing database operations within the app:
* **AuthenticationManager**: provides the required operations on users in the app.
* **ChannelsManager**: provides the required operations on channels in the app.

#### Data Strcutures
* **List**: the database can store entire lists under a single field. (De)serialization done with JSON.
* **AVL Tree**: self balancing binary tree that receives a Secure Storage instance on which to operate on (not the database abstraction for efficiency reasons). Key (reference) to the tree's root is stored under a designated "root" key. (De)serialization done with JSON. Global functions are provided and serve as entry points to query / update the tree. Tree logic (rotations, etc) described in detail here: https://en.wikipedia.org/wiki/AVL_tree

#### Technical Details
* The project uses Gson for (de)serialization: https://github.com/google/gson
* Tokens are generated by chaining the username with the current time (in miliseconds).
* Document names are encrypted with the SHA-256 one-way encryption algorithm.
* Deletions in the database are *logical* deletions. So, when a document / document's field is deleted, a byte array block of "0" is chained to it. Likewise, an "activated" segment is prefixed by a byte array block of "1". The database abstraction preserves this invariant.
* Managers store metadata under a "metadata" collection (e.g. online users, total users).

### Testing Summary
The following components were thoroughly tested:
* **Database**: tested in CourseAppDatabaseTest. All five components that make up the complete implementation of the database are tested together here. The components work together and you can't do anything useful without all of them at once, hence they're tested together.
* **CourseApp**: tested in CourseAppTest. All the manager classes that make up the complete functionality of the CourseApp are tested together here. The managers are tested together with CourseApp here for the same reason above.
* **CourseAppStatistics**: tested in CourseAppStatisticsTest. The "top 10" methods' correctness is tested via load testing, and utilizes classes, methods & extension methods provided by utils.kt.
* **AVLTree**: tested in AVLTreeTest.

In total the tests span nearly 100% code coverage across all the classes we've implemented.

The tests run on JUnit 5.5 & Guice 1.9 and use the SecureStorageFactoryMock and SecureStorageMock classes to mock the missing behavior of the remote storage. GUice is used to provide the constructor parameter (database mapper) for CourseApp and CourseAppInitializer, and bind the interfaces to the implementations we wrote.

### Difficulties

#### HW 0
We had no prior experience with programming in Kotlin & using MockK so we were pretty clueless at the start. Fortunately, they proved to be very easy and intuitive to use. Our main problem was trying to figure out the database's design. At first, we went with a short and easy API that "did the job", but it felt very "C-style" and low-level, so we scrapped it. When moving on to a better API - we faced the problem of reserved special character "/" in specifying file paths. We decided to used encryption as stated above in order to solve this issue, and made sure to test that it worked. Along the way we got to learn more about Kotlin's standard library.

#### HW 1
We feel like we got the hang of Kotlin quite fast from the previous assignment, but implementing the tree proved to be the greatest obstacle in our development process, specifically the delete method. We faced our first obstacle when trying to fit the previous assignment's code to work with the new skeleton: this refactoring process took many hours (configuring Guice, creating factory for database instances & mocks, general refactoring). It took us quite a while to realize that JSON is a helpful tool for our implementation, and we utilized it to add support for storing lists inside the database. From there, the implementation of CourseApp was quite straight-forward, thanks to proper planning (until we got to the tree part...).

### Feedback
* We had a total of 7 reserve duty days since the assignment was published, but we were penalized and only got 2.5 days extension. We feel this wasn't right; we really gave this assignment our all whenever we could and would like to be graded accordingly.
* We feel that implementing a balanced tree (which was more or less a requirement for this assignment) is in disconnect from the course's goals somewhat. We didn't enjoy implementing it to say the least, and hope that the future assignments' challenges will not be of this type.
