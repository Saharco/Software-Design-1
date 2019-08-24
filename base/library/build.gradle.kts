val junitVersion = "5.5.0-M1"
val hamkrestVersion = "1.7.0.0"
val mockkVersion = "1.9"
val guiceVersion: String? by extra
val kotlinGuiceVersion: String? by extra
val gsonVersion = "2.8.5"
val kotsonVersion = "2.5.0"

dependencies {
    compile("il.ac.technion.cs.softwaredesign", "primitive-storage-layer", "1.1")

    testCompile("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testCompile("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    compile("com.google.inject", "guice", guiceVersion)
    compile("com.authzee.kotlinguice4", "kotlin-guice", kotlinGuiceVersion)
    testCompile("com.natpryce:hamkrest:$hamkrestVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")

    compile("com.google.code.gson", "gson", gsonVersion)
    compile("com.github.salomonbrys.kotson", "kotson", kotsonVersion)

    runtime("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}