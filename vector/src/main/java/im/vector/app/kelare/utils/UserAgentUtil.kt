/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.app.kelare.utils

import android.content.Context
import okhttp3.internal.userAgent
import org.matrix.android.sdk.BuildConfig
import timber.log.Timber

/**
 * author : Jason
 *  date   : 2022/7/8 11:25
 *  desc   :
 */
object UserAgentUtil {

    private const val applicationFlavor: String = "Default-application-flavor"

    /**
     * Create an user agent with the application version.
     * Ex: Element/1.0.0 (Linux; U; Android 6.0.1; SM-A510F Build/MMB29; Flavour GPlay; MatrixAndroidSdk2 1.0)
     *
     */
    fun userAgent(context: Context): String {
        var appName = ""
        var appVersion = ""
        var userAgent = ""

        try {
            val appPackageName = context.applicationContext.packageName
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(appPackageName, 0)
            appName = pm.getApplicationLabel(appInfo).toString()

            val pkgInfo = pm.getPackageInfo(context.applicationContext.packageName, 0)
            appVersion = pkgInfo.versionName ?: ""

            // Use appPackageName instead of appName if appName contains any non-ASCII character
            if (!appName.matches("\\A\\p{ASCII}*\\z".toRegex())) {
                appName = appPackageName
            }
        } catch (e: Exception) {
            Timber.e(e, "## initUserAgent() : failed")
        }

        val systemUserAgent = System.getProperty("http.agent")

        // cannot retrieve the application version
        if (appName.isEmpty() || appVersion.isEmpty()) {
            if (null == systemUserAgent) {
                userAgent = "Java" + System.getProperty("java.version")
            }
            return userAgent
        }

        // if there is no user agent or cannot parse it
        if (null == systemUserAgent || systemUserAgent.lastIndexOf(")") == -1 || !systemUserAgent.contains("(")) {
            userAgent = (appName + "/" + appVersion + " ( Flavour " + applicationFlavor +
                    "; MatrixAndroidSdk2 " + BuildConfig.SDK_VERSION + ")")
        } else {
            // update
            userAgent = appName + "/" + appVersion + " " +
                    systemUserAgent.substring(systemUserAgent.indexOf("("), systemUserAgent.lastIndexOf(")") - 1) +
                    "; Flavour " + applicationFlavor +
                    "; MatrixAndroidSdk2 " + BuildConfig.SDK_VERSION + ")"
        }

        return userAgent
    }
}
