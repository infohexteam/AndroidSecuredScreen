# SecuredScreen

Демо-проект для проверки  защиты чувствительного UI от чтения через `AccessibilityService`, скриншотов и части вспомогательных системных каналов.

В репозиторий включен отдельный модуль `attacker`, который показывает, какие данные реально доступны внешнему accessibility service до и после включения защиты.

## Что внутри

- `app` — demo-приложениес двумя экранами: Compose и XML.
- `secure-ui` — библиотечный модуль с политиками защиты для Activity, View и Compose.
- `attacker` — demo-приложение с `AccessibilityService`, который логирует доступный UI.

## Что делает `secure-ui`

При включённом secure mode библиотека может:

- скрывать accessibility-дерево через `importantForAccessibility`;
- очищать `AccessibilityEvent` и `AccessibilityNodeInfo`;
- ставить `FLAG_SECURE` для блокировки скриншотов и записи экрана;
- помечать View как sensitive на Android 14+ через `setAccessibilityDataSensitive(...)`;
- отключать assist/autofill best-effort;
- включать `filterTouchesWhenObscured`.

Текущее состояние режима хранится в `SharedPreferences` через `SecurityModeStore` и применяется ко всем экранам, которые на него подписаны.

## Сценарий проверки

1. Собери и установи `app` и `attacker`.
2. Включи `UI Stealer Service` в системных настройках Accessibility.
3. Открой `Secure Screen Demo`.
4. Перейди на `Compose Screen` или `XML Screen`.
5. С выключенным switch в `attacker` будут видны тексты, значения полей и снимок accessibility-дерева.
6. Включи `Скрыть содержимое экрана`.
7. Открой тот же экран ещё раз и проверь, что логи в `attacker` стали пустыми, обрезанными или потеряли чувствительный контент.

## Публичное API `secure-ui`

### Для Activity

```kotlin
@SecureScreen
class SensitiveActivity : SecureActivity()
```

`@SecureScreen` позволяет включать или отключать отдельные части политики:

```kotlin
@SecureScreen(
    blockAccessibilityTree = true,
    scrubAccessibilityPayload = true,
    markAccessibilityDataSensitive = true,
    blockAssistAndAutofill = true,
    blockScreenshots = true,
    filterTouchesWhenObscured = true,
)
class SensitiveActivity : SecureActivity()
```

### Для Compose

Применение host policy к корневому Compose View:

```kotlin
@Composable
fun SensitiveScreen() {
    ApplySecureComposeHostPolicy()
    val hidden by rememberSecureContentHidden()

    Column(
        modifier = Modifier.secureSemantics(hidden),
    ) {
        // sensitive content
    }
}
```

Полезные Compose API:

- `ApplySecureComposeHostPolicy(...)`
- `rememberSecurityModeStore()`
- `rememberSecureContentHidden()`
- `Modifier.secureSemantics()`
- `Modifier.secureSemantics(enabled)`

### Для View/XML

Разовое применение политики:

```kotlin
rootView.applySecurePolicy(
    enabled = true,
    config = SecureScreenConfig(),
)
```

Подписка View-дерева на глобальный режим:

```kotlin
rootView.bindSecurityMode(owner = viewLifecycleOwner)
```

Для XML в демо используется `SecureFrameLayout`, но это обычный `FrameLayout`-контейнер без собственной логики.

### Управление режимом

```kotlin
val store = SecurityModeStore.get(context)
store.setContentHidden(true)
```

Сокращение для `Context`:

```kotlin
val store = context.securityModeStore()
```


## Инструкция по запуску

Требования:

- JDK 17
- Android SDK 36
- minSdk проекта: 24

Сборка debug APK:

```bash
./gradlew :app:assembleDebug :attacker:assembleDebug
```

Запуск из Android Studio:

- запускай `app` как уязвимое приложение;
- запускай `attacker` отдельно;
- после установки вручную включи accessibility service у `attacker`.

## Подключение `secure-ui` как библиотеки

Модуль `secure-ui` можно подключить к любому Android-проекту. Ниже — три способа, от самого простого до полноценного Maven-репозитория.

### Способ 1. JitPack

Самый простой способ для публичных GitHub-репозиториев. Не требует никакой дополнительной инфраструктуры.

**1.** Добавь JitPack-репозиторий в `settings.gradle.kts` проекта-потребителя:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

**2.** Добавь зависимость в `build.gradle.kts` нужного модуля:

```kotlin
dependencies {
    implementation("com.github.infohexteam:AndroidSecuredScreen:secure-ui:<tag>")
}
```

Где `<tag>` — тег релиза или коммит, например `1.0.0` или `main-SNAPSHOT`.

> JitPack собирает библиотеку прямо из исходников при первом запросе. Для этого в репозитории уже настроен `maven-publish` плагин.

### Способ 2. GitHub Packages

Подходит для приватных репозиториев и корпоративных проектов.

**Публикация (мейнтейнер):**

**1.** Создай GitHub Personal Access Token (PAT) с правом `write:packages`.

**2.** Добавь в `secure-ui/build.gradle.kts` внутрь блока `afterEvaluate { publishing { ... } }` репозиторий:

```kotlin
repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/infohexteam/AndroidSecuredScreen")
        credentials {
            username = System.getenv("GITHUB_ACTOR") ?: project.findProperty("gpr.user") as String? ?: ""
            password = System.getenv("GITHUB_TOKEN") ?: project.findProperty("gpr.token") as String? ?: ""
        }
    }
}
```

**3.** Опубликуй:

```bash
GITHUB_ACTOR=your-username GITHUB_TOKEN=ghp_xxx... \
  ./gradlew :secure-ui:publishReleasePublicationToGitHubPackagesRepository
```

**Подключение (потребитель):**

**1.** Создай GitHub PAT с правом `read:packages`.

**2.** Добавь репозиторий в `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/infohexteam/AndroidSecuredScreen")
            credentials {
                username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR") ?: ""
                password = providers.gradleProperty("gpr.token").orNull ?: System.getenv("GITHUB_TOKEN") ?: ""
            }
        }
    }
}
```

**3.** Добавь зависимость:

```kotlin
dependencies {
    implementation("com.hexteam.screenprotect:secure-ui:1.0.0")
}
```

### Способ 3. Локальный AAR-файл

Подходит для быстрого прототипирования или если нет доступа к Maven-репозиториям.

**Сборка артефакта:**

```bash
./gradlew :secure-ui:assembleRelease
```

AAR появится в `secure-ui/build/outputs/aar/secure-ui-release.aar`.

**Подключение в проект-потребитель:**

**1.** Скопируй `secure-ui-release.aar` в папку `libs/` модуля-потребителя.

**2.** Добавь в `build.gradle.kts` модуля-потребителя:

```kotlin
dependencies {
    implementation(files("libs/secure-ui-release.aar"))

    // Транзитивные зависимости (AAR не тянет их автоматически)
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.core:core-ktx:1.10.1")
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui")
}
```

### Способ 4. Публикация в `mavenLocal`

Удобно для локальной разработки, когда библиотека и приложение — разные проекты на одной машине.

**Публикация:**

```bash
./gradlew :secure-ui:publishReleasePublicationToMavenLocal
```

Артефакт попадёт в `~/.m2/repository/com/hexteam/screenprotect/secure-ui/1.0.0/`.

**Подключение:**

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
}

// build.gradle.kts модуля
dependencies {
    implementation("com.hexteam.screenprotect:secure-ui:1.0.0")
}
```
