plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    `maven-publish`
}

val libGroup = "com.grigorevmp"
val libArtifact = "secure-ui"
val libVersion = "1.0.0"

group = libGroup
version = libVersion

android {
    namespace = "com.grigorevmp.secureui"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            consumerProguardFiles("consumer-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])

                groupId = libGroup
                artifactId = libArtifact
                this.version = libVersion

                pom {
                    name.set("Secure UI")
                    description.set(
                        "Best-effort protection of sensitive Android UI from " +
                        "AccessibilityService snooping, screenshots, screen recording, " +
                        "overlay attacks, and assist/autofill data leaks."
                    )
                    url.set("https://github.com/grigorevmp/SecuredScreen")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("grigorevmp")
                            name.set("grigorevmp")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/grigorevmp/SecuredScreen.git")
                        developerConnection.set("scm:git:ssh://github.com/grigorevmp/SecuredScreen.git")
                        url.set("https://github.com/grigorevmp/SecuredScreen")
                    }
                }
            }
        }
    }
}

dependencies {
    // Exposed in public API (supertypes, parameter types, return types)
    api(libs.androidx.appcompat)               // SecureActivity extends AppCompatActivity
    api(libs.androidx.lifecycle.runtime.ktx)    // LifecycleOwner in bindSecurityMode()
    api(libs.androidx.activity.compose)         // ComponentActivity, LocalContext, LocalView
    api(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.foundation)       // Modifier extensions
    api(libs.androidx.compose.ui)               // Modifier, @Composable, State<>

    // Internal only
    implementation(libs.androidx.core.ktx)
}
