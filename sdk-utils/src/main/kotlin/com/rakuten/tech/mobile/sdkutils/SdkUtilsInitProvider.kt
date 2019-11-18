package com.rakuten.tech.mobile.sdkutils

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

/**
 * Fake ContentProvider that initializes the SDK Utils.
 *
 * @suppress
 **/
@Suppress("UndocumentedPublicClass")
class SdkUtilsInitProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        val context = context ?: return false

        AppInfo.init(context)

        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int = 0

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
}
