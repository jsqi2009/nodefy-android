package im.vector.app.kelare.network

/**
 * Tool class has static method to retreive meta datas from AndroidManifest.xml
 *
 * @see android.content.Context
 *
 * @see android.content.pm.ApplicationInfo
 *
 * @see android.content.pm.PackageManager
 */
object ManifestMetaReader {

    /**
     * Retrieve the value of the meta data by query the key.
     *
     * @param  context  The current context of the application
     * @param  key      The key of the meta data
     * @return The value of the meta data
     * @see android.content.pm.PackageItemInfo
     *
     */
    fun getMetaValue(context: android.content.Context, key: String): String? {

        try {
            val applicationInfo = context.packageManager
                    .getApplicationInfo(context.packageName,
                            android.content.pm.PackageManager.GET_META_DATA)

            return applicationInfo.metaData.getString(key)
        } catch (e: Throwable) {

        }

        return null
    }
}
