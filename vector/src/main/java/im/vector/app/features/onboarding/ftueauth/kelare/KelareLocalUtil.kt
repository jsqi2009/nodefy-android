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

package im.vector.app.features.onboarding.ftueauth.kelare

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import androidx.core.content.edit
import im.vector.app.BuildConfig
import im.vector.app.R
import im.vector.app.core.di.DefaultSharedPreferences
import im.vector.app.features.settings.FontScale
import im.vector.app.features.settings.VectorLocale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.IllformedLocaleException
import java.util.Locale

/**
 * author : Jason
 *  date   : 2022/7/20 10:49
 *  desc   :
 */
@SuppressLint("StaticFieldLeak")
object KelareLocalUtil {

    private const val ISO_15924_LATN = "Latn"

    /**
     * Get String from a locale
     *
     * @param context    the context
     * @param locale     the locale
     * @param resourceId the string resource id
     * @return the localized string
     */
    private fun getString(context: Context, locale: Locale, resourceId: Int): String {
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return try {
            context.createConfigurationContext(config).getText(resourceId).toString()
        } catch (e: Exception) {
            Timber.e(e, "## getString() failed")
            // use the default one
            context.getString(resourceId)
        }
    }

    /**
     * get the supported application locales list
     */
    fun getApplicationLocales(mContext: Context) : ArrayList<Locale> {
        val knownLocalesSet = HashSet<Triple<String, String, String>>()
        try {
            val availableLocales = Locale.getAvailableLocales()
            for (locale in availableLocales) {
                knownLocalesSet.add(
                        Triple(
                                getString(mContext, locale, R.string.resources_language),
                                getString(mContext, locale, R.string.resources_country_code),
                                getString(mContext, locale, R.string.resources_script)
                        )
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "## getApplicationLocales() : failed")
            knownLocalesSet.add(
                    Triple(
                            mContext.getString(R.string.resources_language),
                            mContext.getString(R.string.resources_country_code),
                            mContext.getString(R.string.resources_script)
                    )
            )
        }
        val list = knownLocalesSet.mapNotNull { (language, country, script) ->
            try {
                Locale.Builder()
                        .setLanguage(language)
                        .setRegion(country)
                        .setScript(script)
                        .build()
            } catch (exception: IllformedLocaleException) {
                if (BuildConfig.DEBUG) {
                    throw exception
                }
                // Ignore this locale in production
                null
            }
        }
                // sort by human display names
                .sortedBy { localeToLocalisedString(it).lowercase(it) }

        return ArrayList(list)
    }

    /**
     * Convert a locale to a string
     *
     * @param locale the locale to convert
     * @return the string
     */
    private fun localeToLocalisedString(locale: Locale): String {
        return buildString {
            append(locale.getDisplayLanguage(locale))

            if (locale.script != ISO_15924_LATN && locale.getDisplayScript(locale).isNotEmpty()) {
                append(" - ")
                append(locale.getDisplayScript(locale))
            }

            if (locale.getDisplayCountry(locale).isNotEmpty()) {
                append(" (")
                append(locale.getDisplayCountry(locale))
                append(")")
            }
        }
    }

    fun applyToApplicationContext(context: Context) {
        val locale = VectorLocale.applicationLocale
        val fontScale = FontScale.getFontScaleValue(context)

        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        @Suppress("DEPRECATION")
        config.locale = locale
        config.fontScale = fontScale.scale
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }



}
