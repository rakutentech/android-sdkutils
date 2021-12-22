package com.rakuten.tech.mobile.sdkutils.json

import com.google.gson.Gson
import com.rakuten.tech.mobile.sdkutils.logger.Logger
import java.io.*
import java.lang.reflect.Type


/**
 * This method deserializes the given Json loaded from app resources into an object of the given type.
 *
 * @param <T> the type of the desired object.
 *
 * @param path the path file from which the object is to be deserialized.
 * For example, to get the object from ``resources/my-file.json``, you should use:
 * `gson.fromResources("my-file.json", typeOfT)`.
 *
 * @param typeOfT The specific genericized type of src. You can obtain this type by using the
 * `com.google.gson.reflect.TypeToken` class. For example, to get the type for `Collection<Foo>`,
 * you should use: `typeOfT: Type = TypeToken<Collection<Foo>>(){}.type`.
 *
 * @return an object of type `T` from the resources file. Returns `null` if an exception is thrown
 * or if the file is empty.
 */
fun <T> Gson.fromResources(path: String, typeOfT: Type) : T? {
    return try {
        fromJson<T>(InputStreamReader(javaClass.classLoader?.getResourceAsStream(path)), typeOfT)
    } catch (e: Exception) {
        Logger("JsonUtils")
            .error(e, "Failed to deserialize the Json from $path path file.")
        null
    }
}

/**
 * This method deserializes the given Json loaded from app resources into an object of the given
 * type. If the specified class is a generic type use [fromResources(String, Type)].
 *
 * @param <T> the type of the desired object.
 *
 * @param path the path file from which the object is to be deserialized.
 * For example, to get the object from ``resources/my-file.json``, you should use:
 * `gson.fromResources("my-file.json", classOfT)`.
 *
 * @param classOfT the class of `T`.
 *
 * @return an object of type `T` from the resources file. Returns `null` if an exception is thrown
 * or if the file is empty.
 **/
fun <T> Gson.fromResources(path: String, classOfT: Class<T>) : T? {
    return try {
        fromJson<T>(InputStreamReader(javaClass.classLoader?.getResourceAsStream(path)), classOfT)
    } catch (e: Exception) {
        Logger("JsonUtils")
            .error(e, "Failed to deserialize the Json from $path path file.")
        null
    }
}
