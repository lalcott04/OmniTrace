
plugins {
    id("com.android.application") version "8.4.2" apply false
    id("com.chaquo.python") version "16.0.0" apply false
}

repositories {
    google()
    mavenCentral()
    maven("https://chaquo.com/gradle")
}
