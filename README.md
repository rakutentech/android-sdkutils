# Android SDK Utils

This is a library that contains utilities which are useful when developing Android SDKs. They are intended for use internally by Rakuten's SDKs.

## This page covers

* [Setup](#setup)
* [Usage](#usage)
* [Changelog](#changelog)

## Setup

### 1. Add dependency to build.gradle

Add to your `build.gradle`:

```groovy
repositories {
  mavenCentral()
}

dependency {
  implementation "io.github.rakutentech.sdkutils:sdk-utils:$version"
}
```

### 2. Set initialization order (optional)

⚠️  This SDK uses a `ContentProvider` in order to automatically initialize. It is not recommended to override the default value set for `initOrder`. 
However, you may need to set it if you are facing an Exception when trying to use features of this SDK at launch time. If you are also using a ContentProvider in your App or you are developing an SDK which uses a ContentProvider to automatically initialize, then you may need to manually set the
`initOrder` for this SDK so that it is initialized first. This can be set in `AndroidManifest.xml`:

```xml
  <application>
    <!-- The `initOrder` for this SDK is set to `9999` by default. -->
    <!-- ⚠️ It is not recommended to override the `initOrder`, however, if you need to manually set it, make sure that it is initialized before your own ContentProvider. -->
    <provider
      tools:replace="android:initOrder"
      android:name="com.rakuten.tech.mobile.sdkutils.SdkUtilsInitProvider"
      android:authorities="${applicationId}.SdkUtilsInitProvider"
      android:exported="false"
      android:initOrder="9999" />
  </application>
```

## Usage

### OkHttp Header Interceptor

This library contains an extension function for `OkHttpClient.Builder` so you can more easily add headers to all network requests which are made using that client.

```kotlin
private val client = OkHttpClient.Builder()
    .addHeaderInterceptor(
        "header_name" to "header_value",
        "another_header" to "another_value"
    ).build()
```

### Application Info

The `AppInfo` class can be used to retrieve some properties of the App which normally require a `Context` to retrieve. This class is automatically initialized, so no `Context` needs to be provided.

```kotlin
val appName = AppInfo.instance.name
val appVersion = AppInfo.instance.version
```

### Logging Utility

This library contains a simple logging facility.

Logging conventions are:

* Debug: for SDK developers. Will print source file, method and line.
* Info: for SDK consumers. They should know, but is no problem.
* Warn: for SDK consumers. An unexpected situation, that the SDK can recover from.
* Error: for SDK consumers: An error that may cause the SDK to stop working.

By default only info, warn and error are logged. Debug is only logged if `loggerInstance.setDebug(true)` is called.

The level of the stack trace to be logged is set by default to 5 (it's the trace level of the running thread when SDKUtils is implemented in an application). 
You may need to upgrade the level using ```loggerInstance.setDebugLevel(yourLevel)``` if SDKUtils is implemented in another SDK.

```kotlin
private val log = Logger(TAG)
log.setDebugLevel(6)
```

All log calls come in 2 variants:

* ```log(String template, Object.. args)``` - will use ```String.format``` to format the string.
* ```log(Throwable cause, String template, Object.. args)``` - same as above, but will add a "Caused by" and stacktrace to the log.

```kotlin
private val log = Logger(MainActivity::class.java.simpleName)

//enable debug logs (by default only info, warn and error are logged)
log.setDebug(true)

//Examples:
log.debug("simple debug log") // simple debug log  
log.debug("simple debug log at %s", listOf(Date())) // simple debug log at [Mon Dec 20 16:51:12 GMT+09:00 2021]

```

### Preferences Utility

Please check the following sample code to use the preferences utility feature for caching data.

```kotlin
// We can put int, string, float, long, string set and boolean to cache in preferences
PreferencesUtil.putString(appContext, preferencesFileName, key, value)

// We can get int, string, float, long, string set and boolean to retrieve from preferences
PreferencesUtil.getString(appContext, preferencesFileName, key, defaultValue)
```

### Json Utility

This utility can be used to convert a JSON string loaded from app resources to an equivalent object.

#### Examples

```kotlin
//deserializes a Json loaded from app resources into a generic type object.
val path = "my-file.json"
val devices: List<Device>? = gson.fromResources(path, object: TypeToken<List<Device>>(){}.type)

//deserializes a Json loaded from app resources into an object.
val path = "my-file.json"
val device: Device? = gson.fromResources(path, Device::class.java)
```

### Common Utility

Please check the following sample code to use the extension function (s).

```kotlin
// To check the device mode setting, i.e. dark/ light
Context.isDarkMode(context)

// To check the play service of device
Activity.checkPlayServices(activity)

// To get the Sha256 digest data
String.getSha256HashData("Test")

// To get the MD5 digest data
String.getMD5HashData("Test")
```

Please see [StringExtension](./sdk-utils/src/main/kotlin/com/rakuten/tech/mobile/sdkutils/StringExtension.kt) and [ContextExtension](sdk-utils/src/main/kotlin/com/rakuten/tech/mobile/sdkutils/ContextExtension.kt) classes for available APIs.

### Networking Utility

See the following networking utility classes and extension functions for easier usage of networking framework.

<ul>
<li>

```NetworkUtil```: a class that queries network connectivity state and capabilities.

```kotlin
// Init
// Wrap in try-catch block since exception can be thrown when network callback registration fails.
val networkUtil = NetworkUtil(context)
try {
  networkUtil.initialize()
} catch (ex: Exception) {
  // Call any error callback if needed.
}

// Check whether device has network connectivity
networkUtil.isOnline()

// Get network capabilities
networkUtil.networkCapabilities()
```

</li>
<li>

```HttpCallback```: a Callback Class that communicate the remote server response.

 ```kotlin
// by using an HttpCallback object:
client.newCall(request).enqueue(HttpCallback(
    { response ->  ... },{ exception ->  ... }
))

 //or by using a lambda function:

 //1- create your function e.g.
 fun get(success: (response: Response) -> Unit, failure: (exception: Exception) -> Unit) {
     val request = Request.Builder().url(url).build()
     ...
     client.newCall(request).enqueue(HttpCallback(success, failure))
 }

 // 2- get the response
 get({ response = it  ... }, { exception = it  ... })
```

</li>
<li>

Kotlin extension to get a reference of ```Retrofit```

```kotlin
//Get a reference of Retrofit using the default parameters 
val retrofit = Retrofit.Builder().build("your_baseUrl")

// Get a reference of Retrofit using your personalysed parameter(s) 
val okHttpClient: OkHttpClient = your_httpClient
val gsonConverterFactory: GsonConverterFactory = your_converterFactory
val executor: ExecutorService = your_executor

val retrofit = Retrofit.Builder().build("your_baseUrl", okHttpClient gsonConverterFactory, executor)

```

</li>
</ul>

### Event Logger

A remote troubleshooting utility which helps to track or send important events to a remote API server.
This is intended to be used internally by Rakuten's SDKs.

<ul>
<li>

```configure```: Sets the server configuration and other initialization processing. Call this as early as possible in the application lifecycle, such as `onCreate` or other initialization methods. Calling other APIs without calling this API has no effect.

| Parameter | Description              | Datatype | Required? |
|-----------| ------------------------ |----------|-----------|
| context   | Application context      | Context  | `Yes`     |
| apiUrl    | Non-empty server API URL | String   | `Yes`     |
| apiKey    | Non-empty server API Key | String   | `Yes`     |

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    // API: configure
    EventLogger.configure(this, "my-api-url", "my-api-key")
}

```

</li>

<li>

```send*Event```: Logs an event and sends to server based on criticality.

| Parameter | Description | Datatype | Required? |
|----------|---------|----------|-----------|
| sourceName | Non-empty source of the event, e.g. "sdkutils" | String | `Yes` |
| sourceVersion | Non-empty source' version, e.g. "2.0.0" | String | `Yes` |
| errorCode | Non-empty source' error code or HTTP backend response code e.g. "500" | String | `Yes` |
| errorMessage | Non-empty description of the error. Make it as descriptive as possible, for example, the stacktrace of an exception | String | `Yes` |
| info | Optional parameter to attach other useful key-value pair, such as filename, method, line number, etc. | Map | `No` |

```kotlin

// API: sendCriticalEvent
// Logs a critical event, wherein the unique event will be sent to the server immediately.
EventLogger.sendCriticalEvent(
    sourceName = "sdkutils",
    sourceVersion = "2.0.0",
    errorCode = "XXX",
    errorMessage = "java.lang.ArithmeticException: divide by zero
                    at com.rakuten.test.MainActivityFragment.onViewCreated(MainActivityFragment.kt:58)
                    at androidx.fragment.app.FragmentManagerImpl.ensureInflatedFragmentView(FragmentManagerImpl.java:1144)
                    at androidx.fragment.app.FragmentManagerImpl.moveToState(FragmentManagerImpl.java:851)"
    info = mapOf("filename" to "MyFile.kt")
)

// API: sendWarningEvent
// Logs a warning event, which will be sent at a later time based on TTL.
EventLogger.sendWarningEvent(
    sourceName = "sdkutils",
    sourceVersion = "2.0.0",
    errorCode = "YYY",
    errorMessage = "Incorrect credentials",
    info = mapOf("filename" to "MyFile.kt")
)

```

</li>
<li>

## Changelog

### v2.2.0 (2024-05-14)
* SDKCF-6859: Added `EventLogger` feature which sends events to a remote API server. It is intended to be used internally by Rakuten's SDKs. See [Event Logger](#event-logger) section for details.

### v2.1.1 (2022-08-04)
* SDKCF-5737: Reverted `Logger` constructor to single parameter.

### v2.1.0 (2022-08-02)
* SDKCF-5307: Added version string resource which can be referenced by the name `sdk_utils__version`.
* SDKCF-4921: Added support for building with Java 11.

### v2.0.0 (2022-06-23)
* SDKCF-5390: **Breaking Changes:** Moved `setDebugLevel()` and `setDebug()` APIs from static to `Logger` class APIs. This will allow multiple SDK app dependencies to have their own debug logging configuration.
  - Deprecated `Logger.setDebugLevel()` and `Logger.setDebug()` static APIs. These APIs will no longer work but not removed to avoid crashes on incompatible SDK versions.

### v1.2.0 (2022-05-19)
* SDKCF-5292: Set initOrder of the content provider to a high value to make sure that it is initialized before the host app ContentProvider.

### v1.1.0 (2022-03-17)

* SDKCF-4887: Added NetworkUtil for checking network connectivity and capabilities. Please see [usage](#networking-utility) section for details.
* SDKCF-5015: Fixed test library dependency as normal API dependency.

### v1.0.0 (2022-02-07)

* SDKCF-4686: Added SharedPreferences handling and App/Env Info retrieval utilities. Please see [usage](#preferences-utility) section for details.
* SDKCF-4685: Added Logging facility and Json deserializer utility. Please see [Logging Utility](#logging-utility) and [JSON Utility](#json-utility) sections for details.
* SDKCF-4688: Added String and Context class extension to SDKUtils. Please see [usage](#common-utility) section for details.
* SDKCF-4687: Added APIs for networking facility. Please see [usage](#networking-utility) section for details.

### v0.2.0 (2021-03-05)

* Changed Maven Group ID to `io.github.rakutentech.sdkutils`. You must upudate your dependency declaration to `io.github.rakutentech.sdkutils:sdk-utils:0.2.0`
* Migrated publishing to Maven Central due to Bintray/JCenter being [shutdown](https://jfrog.com/blog/into-the-sunset-bintray-jcenter-gocenter-and-chartcenter/). You must add `mavenCentral()` to your `repositories``.

### v0.1.1 (2019-11-29)

* Changed Device OS header from `ras-device-os` to `ras-os-version`.

### v0.1.0 (2019-11-22)

* Initial release.
