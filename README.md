# Android SDK Utils

This is a library that contains utilities which are useful when developing Android SDKs. They are intended for use internally by Rakuten's SDKs.

## Setup

Add to your `build.gradle`:

```groovy
repositories {
  jcenter()
}

dependency {
  implementation 'com.rakuten.tech.mobile.sdkutils:sdkutils:0.1.0'
}
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

## Changelog

### v0.1.0 (2019-11-22)

- Initial release.
