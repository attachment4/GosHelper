import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

// local.properties Gradle сам в свойства проекта НЕ загружает (оттуда берётся
// только sdk.dir), поэтому читаем файл явно — иначе ключи окажутся пустыми.
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun secret(name: String, default: String = ""): String =
    localProps.getProperty(name) ?: project.findProperty(name)?.toString() ?: default

android {
    namespace  = "com.gospomoshnik"
    compileSdk = 35

    defaultConfig {
        applicationId   = "com.gospomoshnik"
        minSdk          = 26
        targetSdk       = 35
        versionCode     = 1
        versionName     = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // GigaChat credentials — задать в local.properties: GIGACHAT_AUTH=<base64(clientId:secret)>
        buildConfigField("String", "GIGACHAT_AUTH", "\"${secret("GIGACHAT_AUTH")}\"")

        // ── Платежи (ЮKassa) ──────────────────────────────────────────────
        // Публикуемый ключ магазина и shopId — безопасны для APK.
        // Секретный ключ живёт ТОЛЬКО на бэкенде.
        buildConfigField("String", "YOOKASSA_SHOP_ID", "\"${secret("YOOKASSA_SHOP_ID")}\"")
        buildConfigField("String", "YOOKASSA_KEY",     "\"${secret("YOOKASSA_KEY")}\"")
        buildConfigField("String", "PAYMENTS_BASE_URL",
            "\"${secret("PAYMENTS_BASE_URL", "https://example.invalid/api/")}\"")

        // Room: экспорт схемы для миграций
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
            arg("room.incremental",    "true")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled    = true
            isShrinkResources  = true   // выкинуть неиспользуемые ресурсы из APK
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Прод: только реальная оплата через бэкенд, без симуляции
            buildConfigField("boolean", "PAYMENTS_SIMULATE", "false")
        }
        debug {
            isDebuggable    = true
            isMinifyEnabled = false
            // Dev: симулировать успешную оплату, чтобы прокликать сценарий без бэкенда
            buildConfigField("boolean", "PAYMENTS_SIMULATE", "true")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose     = true
        buildConfig = true
    }
}

dependencies {
    // Kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.coroutines.android)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.icons)
    implementation(libs.activity.compose)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.preview)

    // Lifecycle / ViewModel
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.runtime)

    // Navigation
    implementation(libs.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Retrofit + OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // DataStore
    implementation(libs.datastore.preferences)

    // OCR (распознавание текста с фото, офлайн, кириллица) — Tesseract
    // Требует файл языка: app/src/main/assets/tessdata/rus.traineddata
    implementation("cz.adaptech.tesseract4android:tesseract4android:4.7.0")

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.room.testing)
}
