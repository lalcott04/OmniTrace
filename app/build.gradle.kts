plugins {
    id("com.android.application")
    id("com.chaquo.python")
}

android {
    compileSdk = 34

    namespace = "com.example.omnitrace"

    flavorDimensions += "pyVersion"
    productFlavors {
        create("py310") { dimension = "pyVersion"}
    }

    defaultConfig {
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
    }
}
dependencies {
    implementation(libs.core)
    implementation(libs.appcompat)
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    implementation("com.google.android.material:material:1.12.0")
}

chaquopy {
    defaultConfig { }
    productFlavors {
        getByName("py310") {
            version = "3.8"
            pip {
                install("pandas")
                install("scikit-learn")
            }
        }
    }
    sourceSets { }
}
