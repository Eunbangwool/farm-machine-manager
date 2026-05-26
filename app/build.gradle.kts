plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// google-services.json이 존재할 때만 Firebase 플러그인 적용.
// → Firebase 설정 전이라도 빌드 가능. 사용자가 google-services.json을 app/ 폴더에
//   배치하면 자동으로 Firebase가 활성화됨.
val googleServicesJsonExists = project.file("google-services.json").exists()
if (googleServicesJsonExists) {
    apply(plugin = "com.google.gms.google-services")
}

// CI 빌드에서는 GITHUB_RUN_NUMBER를 사용해 자동 버전 증가.
// 로컬 빌드에서는 1을 사용 (Android Studio에서 빌드/실행 시).
val ciRunNumber: Int = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 1

// 디버그 패키지 분리 — `-PdebugBuild=true` 로 빌드하면:
//  - applicationId 에 ".debug" suffix → 운영 앱과 별개로 동시 설치 가능
//  - versionName 에 "-debug" suffix → 식별 쉽게
//  - app_name "농돌이 디버그" → 홈 화면 라벨 구분
//  - BuildConfig.IS_DEBUG_APP=true → 설정 화면에서 진단 카드 노출
val debugBuild: Boolean = (project.findProperty("debugBuild") as? String)?.toBoolean() ?: false

android {
    namespace = "com.example.farmmachinemanager"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.farmmachinemanager"
        minSdk = 24
        targetSdk = 36
        // 빌드마다 +1 (1, 2, 3, ...). CI 빌드 번호와 동일.
        versionCode = ciRunNumber
        // 사람이 읽기 쉬운 버전. 예: "0.1.5" (CI 5번째 빌드)
        versionName = "0.1.$ciRunNumber"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 빌드 시간 (epoch ms) - 매 빌드마다 갱신. 설정 화면에서 사람 읽기 쉬운 형식으로 변환.
        buildConfigField("long", "BUILD_TIME_MS", "${System.currentTimeMillis()}L")
        // 디버그 앱 빌드 여부. 설정 화면에서 진단/등급전환 카드 노출 판정용.
        buildConfigField("boolean", "IS_DEBUG_APP", debugBuild.toString())

        // app_name 기본값 (운영 빌드). 디버그 빌드는 buildType 에서 덮어씀.
        resValue("string", "app_name", "농돌이")
    }

    // CI 빌드마다 같은 keystore를 사용하여 APK 서명.
    // → 새 APK를 기존 앱 위에 덮어 설치 가능 (uninstall 불필요)
    signingConfigs {
        getByName("debug") {
            storeFile = file("../debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
            if (debugBuild) {
                applicationIdSuffix = ".debug"
                versionNameSuffix = "-debug"
                resValue("string", "app_name", "농돌이 디버그")
            }
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (debugBuild) {
                applicationIdSuffix = ".debug"
                versionNameSuffix = "-debug"
                resValue("string", "app_name", "농돌이 디버그")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
        // app_name 을 build.gradle 에서 동적 주입 (운영/디버그 라벨 구분).
        // AGP 8.x 부터 기본 false → resValue() 호출하려면 명시적 활성화 필요.
        resValues = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Firebase BoM이 모든 Firebase 의존성 버전을 통일.
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    // Tasks API를 코루틴 .await()로 변환하기 위함
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.kotlinx.serialization.json)
}