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

You only need to set this if you are facing an Exception when trying to use features of this SDK at launch time.

This SDK uses a `ContentProvider` in order to automatically initialize. If you are also using a ContentProvider in your App
or you are developing an SDK which uses a ContentProvider to automatically initialize, then you may need to manually set the
`initOrder` for this SDK so that it is initialized first. This can be set in `AndroidManifest.xml`:

```xml
  <application>
    <!-- The `initOrder` for this SDK is set to `99` by default. -->
    <!-- Set it to something higher to make it initialize before your own ContentProvider. -->
    <provider
      tools:replace="android:initOrder"
      android:name="com.rakuten.tech.mobile.sdkutils.SdkUtilsInitProvider"
      android:authorities="${applicationId}.SdkUtilsInitProvider"
      android:exported="false"
      android:initOrder="100" />
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
 
By default only info, warn and error are logged. Debug is only logged if [Logger.setDebug] is called with `true`.

All log calls come in 2 variants:

* log(String template, Object.. args) - will use [String.format] to format the string.
* log(Throwable cause, String template, Object.. args) - same as above, but will add a "Caused by" and stacktrace to the log.

```kotlin
private val log = Logger(MainActivity::class.java.simpleName)
//enable debug logs (by default only info, warn and error are logged)
Logger.setDebug(true)

//Examples:
log.debug("simple debug log") // simple debug log  
log.debug("simple debug log at %s", listOf(Date())) // simple debug log at [Mon Dec 20 16:51:12 GMT+09:00 2021]

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

## Changelog

### v0.3.0 (In progress)

* SDKCF-4685: Added APIs for:

  1. logging facility, Please see [usage](#logging-utility) section for details.
  2. Json deserializer utility, Please see [usage](#json-utility) section for details.

### v0.2.0 (2021-03-05)

* Changed Maven Group ID to `io.github.rakutentech.sdkutils`. You must upudate your dependency declaration to `io.github.rakutentech.sdkutils:sdk-utils:0.2.0`
* Migrated publishing to Maven Central due to Bintray/JCenter being [shutdown](https://jfrog.com/blog/into-the-sunset-bintray-jcenter-gocenter-and-chartcenter/). You must add `mavenCentral()` to your `repositories``.

### v0.1.1 (2019-11-29)

* Changed Device OS header from `ras-device-os` to `ras-os-version`.

### v0.1.0 (2019-11-22)

* Initial release.
