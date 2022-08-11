/*
 * Copyright (c) 2010-2020 Belledonne Communications SARL.
 *
 * This file is part of linphone-android
 * (see https://www.linphone.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package im.vector.app.kelare.sipcore

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStoreException
import im.vector.app.VectorApplication.Companion.coreContext
import im.vector.app.kelare.compatibility.Compatibility
import org.linphone.core.BuildConfig
import org.linphone.core.Config
import org.linphone.core.tools.Log
import im.vector.app.R
import timber.log.Timber

class CorePreferences constructor(private val context: Context) {
    private var _config: Config? = null
    var config: Config
        get() = _config ?: coreContext.core.config
        set(value) {
            _config = value
        }

    /* VFS encryption */

    companion object {
        const val OVERLAY_CLICK_SENSITIVITY = 10

        private const val encryptedSharedPreferencesFile = "encrypted.pref"
    }

    val encryptedSharedPreferences: SharedPreferences? by lazy {
        val masterKey: MasterKey = MasterKey.Builder(
            context,
            MasterKey.DEFAULT_MASTER_KEY_ALIAS
        ).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        try {
            EncryptedSharedPreferences.create(
                context, encryptedSharedPreferencesFile, masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (kse: KeyStoreException) {
            Log.e("[VFS] Keystore exception: $kse")
            null
        } catch (e: Exception) {
            Log.e("[VFS] Exception: $e")
            null
        }
    }

    var vfsEnabled: Boolean
        get() = encryptedSharedPreferences?.getBoolean("vfs_enabled", false) ?: false
        set(value) {
            val preferences = encryptedSharedPreferences
            if (preferences == null) {
                Log.e("[VFS] Failed to get encrypted SharedPreferences")
                return
            }

            if (!value && preferences.getBoolean("vfs_enabled", false)) {
                Log.w("[VFS] It is not possible to disable VFS once it has been enabled")
                return
            }

            preferences.edit().putBoolean("vfs_enabled", value)?.apply()
            // When VFS is enabled we disable logcat output for linphone logs
            // TODO: decide if we do it
            // logcatLogsOutput = false
        }

    fun chatRoomMuted(id: String): Boolean {
        val sharedPreferences: SharedPreferences = coreContext.context.getSharedPreferences("notifications", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean(id, false)
    }

    /* App settings */

    var debugLogs: Boolean
        get() = config.getBool("app", "debug", BuildConfig.DEBUG)
        set(value) {
            config.setBool("app", "debug", value)
        }

    var logcatLogsOutput: Boolean
        get() = config.getBool("app", "print_logs_into_logcat", true)
        set(value) {
            config.setBool("app", "print_logs_into_logcat", value)
        }

    var autoStart: Boolean
        get() = config.getBool("app", "auto_start", true)
        set(value) {
            config.setBool("app", "auto_start", value)
        }

    var keepServiceAlive: Boolean
        get() = config.getBool("app", "keep_service_alive", false)
        set(value) {
            config.setBool("app", "keep_service_alive", value)
        }

    /* UI */

    var forcePortrait: Boolean
        get() = config.getBool("app", "force_portrait_orientation", false)
        set(value) {
            config.setBool("app", "force_portrait_orientation", value)
        }

    var replaceSipUriByUsername: Boolean
        get() = config.getBool("app", "replace_sip_uri_by_username", false)
        set(value) {
            config.setBool("app", "replace_sip_uri_by_username", value)
        }

    var enableAnimations: Boolean
        get() = config.getBool("app", "enable_animations", false)
        set(value) {
            config.setBool("app", "enable_animations", value)
        }

    /** -1 means auto, 0 no, 1 yes */
    var darkMode: Int
        get() {
            if (!darkModeAllowed) return 0
            return config.getInt("app", "dark_mode", -1)
        }
        set(value) {
            config.setInt("app", "dark_mode", value)
        }

    var makePublicMediaFilesDownloaded: Boolean
        // Keep old name for backward compatibility
        get() = config.getBool("app", "make_downloaded_images_public_in_gallery", true)
        set(value) {
            config.setBool("app", "make_downloaded_images_public_in_gallery", value)
        }

    var deviceName: String
        get() = config.getString("app", "device_name", Compatibility.getDeviceName(context))!!
        set(value) = config.setString("app", "device_name", value)

    /* Contacts */

    var storePresenceInNativeContact: Boolean
        get() = config.getBool("app", "store_presence_in_native_contact", false)
        set(value) {
            config.setBool("app", "store_presence_in_native_contact", value)
        }

    /* Call */

    var acceptEarlyMedia: Boolean
        get() = config.getBool("sip", "incoming_calls_early_media", false)
        set(value) {
            config.setBool("sip", "incoming_calls_early_media", value)
        }

    var autoAnswerEnabled: Boolean
        get() = config.getBool("app", "auto_answer", false)
        set(value) {
            config.setBool("app", "auto_answer", value)
        }

    var autoAnswerDelay: Int
        get() = config.getInt("app", "auto_answer_delay", 0)
        set(value) {
            config.setInt("app", "auto_answer_delay", value)
        }

    // Show overlay inside of application
    var showCallOverlay: Boolean
        get() = config.getBool("app", "call_overlay", true)
        set(value) {
            config.setBool("app", "call_overlay", value)
        }

    // Show overlay even when app is in background, requires permission
    var systemWideCallOverlay: Boolean
        get() = config.getBool("app", "system_wide_call_overlay", false)
        set(value) {
            config.setBool("app", "system_wide_call_overlay", value)
        }

    var automaticallyStartCallRecording: Boolean
        get() = config.getBool("app", "auto_start_call_record", false)
        set(value) {
            config.setBool("app", "auto_start_call_record", value)
        }

    var useTelecomManager: Boolean
        // Some permissions are required, so keep it to false so user has to manually enable it and give permissions
        get() = config.getBool("app", "use_self_managed_telecom_manager", false)
        set(value) {
            config.setBool("app", "use_self_managed_telecom_manager", value)
            // We need to disable audio focus requests when enabling telecom manager, otherwise it creates conflicts
            config.setBool("audio", "android_disable_audio_focus_requests", value)
        }

    var routeAudioToBluetoothIfAvailable: Boolean
        get() = config.getBool("app", "route_audio_to_bluetooth_if_available", true)
        set(value) {
            config.setBool("app", "route_audio_to_bluetooth_if_available", value)
        }

    // This won't be done if bluetooth or wired headset is used
    var routeAudioToSpeakerWhenVideoIsEnabled: Boolean
        get() = config.getBool("app", "route_audio_to_speaker_when_video_enabled", true)
        set(value) {
            config.setBool("app", "route_audio_to_speaker_when_video_enabled", value)
        }

    /* Assistant */

    var firstStart: Boolean
        get() = config.getBool("app", "first_start", true)
        set(value) {
            config.setBool("app", "first_start", value)
        }

    /* Dialog related */

    var limeSecurityPopupEnabled: Boolean
        get() = config.getBool("app", "lime_security_popup_enabled", true)
        set(value) {
            config.setBool("app", "lime_security_popup_enabled", value)
        }

    /* Other */

    var voiceMailUri: String?
        get() = config.getString("app", "voice_mail", null)
        set(value) {
            config.setString("app", "voice_mail", value)
        }

    var redirectDeclinedCallToVoiceMail: Boolean
        get() = config.getBool("app", "redirect_declined_call_to_voice_mail", true)
        set(value) {
            config.setBool("app", "redirect_declined_call_to_voice_mail", value)
        }

    var defaultAccountAvatarPath: String?
        get() = config.getString("app", "default_avatar_path", null)
        set(value) {
            config.setString("app", "default_avatar_path", value)
        }

    /* *** Read only application settings, some were previously in non_localizable_custom *** */

    /* UI related */

    val contactOrganizationVisible: Boolean
        get() = config.getBool("app", "display_contact_organization", true)

    private val darkModeAllowed: Boolean
        get() = config.getBool("app", "dark_mode_allowed", true)

    /* Feature related */

    val showScreenshotButton: Boolean
        get() = config.getBool("app", "show_take_screenshot_button_in_call", false)

    val fetchContactsFromDefaultDirectory: Boolean
        get() = config.getBool("app", "fetch_contacts_from_default_directory", true)

    val delayBeforeShowingContactsSearchSpinner: Int
        get() = config.getInt("app", "delay_before_showing_contacts_search_spinner", 200)

    // From Android Contact APIs we can also retrieve the internationalized phone number
    // By default we display the same value as the native address book app
    val preferNormalizedPhoneNumbersFromAddressBook: Boolean
        get() = config.getBool("app", "prefer_normalized_phone_numbers_from_address_book", false)

    // Will disable chat feature completely
    val disableChat: Boolean
        get() = config.getBool("app", "disable_chat_feature", false)

    // This will prevent UI from showing up, except for the launcher & the foreground service notification
    val preventInterfaceFromShowingUp: Boolean
        get() = config.getBool("app", "keep_app_invisible", false)

    val defaultDomain: String
        get() = config.getString("app", "default_domain", "sip.linphone.org")!!

    val defaultRlsUri: String
        get() = config.getString("sip", "rls_uri", "sips:rls@sip.linphone.org")!!

    val defaultLimeServerUrl: String
        get() = config.getString("lime", "lime_server_url", "https://lime.linphone.org/lime-server/lime-server.php")!!

    // If there is more participants than this value in a conference, force ActiveSpeaker layout
    val maxConferenceParticipantsForMosaicLayout: Int
        get() = config.getInt("app", "conference_mosaic_layout_max_participants", 6)

    val conferenceServerUri: String
        get() = config.getString(
            "app",
            "default_conference_factory_uri",
            "sip:conference-factory@sip.linphone.org"
        )!!

    /* Assets stuff */

    val configPath: String
        get() = context.filesDir.absolutePath + "/.linphonerc"

    val factoryConfigPath: String
        get() = context.filesDir.absolutePath + "/linphonerc"

    val linphoneDefaultValuesPath: String
        get() = context.filesDir.absolutePath + "/assistant_linphone_default_values"

    val defaultValuesPath: String
        get() = context.filesDir.absolutePath + "/assistant_default_values"

    val userCertificatesPath: String
        get() = context.filesDir.absolutePath + "/user-certs"

    val staticPicturePath: String
        get() = context.filesDir.absolutePath + "/share/images/nowebcamcif.jpg"

    fun copyAssetsFromPackage() {
        copy("linphonerc_default", configPath)
        copy("linphonerc_factory", factoryConfigPath, true)
        copy("assistant_linphone_default_values", linphoneDefaultValuesPath, true)
        copy("assistant_default_values", defaultValuesPath, true)

        move(context.filesDir.absolutePath + "/linphone-log-history.db", context.filesDir.absolutePath + "/call-history.db")
        move(context.filesDir.absolutePath + "/zrtp_secrets", context.filesDir.absolutePath + "/zrtp-secrets.db")
    }

    fun getString(resource: Int): String {
        return context.getString(resource)
    }

    private fun copy(from: String, to: String, overrideIfExists: Boolean = false) {
        val outFile = File(to)
        if (outFile.exists()) {
            if (!overrideIfExists) {
                Timber.tag(context.getString(R.string.app_name)).i("[Preferences] File " + to + " already exists")
                return
            }
        }
        Timber.tag(context.getString(R.string.app_name)).i("[Preferences] Overriding " + to + " by " + from + " asset")

        val outStream = FileOutputStream(outFile)
        val inFile = context.assets.open(from)
        val buffer = ByteArray(1024)
        var length: Int = inFile.read(buffer)

        while (length > 0) {
            outStream.write(buffer, 0, length)
            length = inFile.read(buffer)
        }

        inFile.close()
        outStream.flush()
        outStream.close()
    }

    private fun move(from: String, to: String, overrideIfExists: Boolean = false) {
        val inFile = File(from)
        val outFile = File(to)
        if (inFile.exists()) {
            if (outFile.exists() && !overrideIfExists) {
                Timber.tag(context.getString(R.string.app_name)).w("[Preferences] Can't move [" + from + "] to [" + to + "], destination file already exists")
            } else {
                val inStream = FileInputStream(inFile)
                val outStream = FileOutputStream(outFile)

                val buffer = ByteArray(1024)
                var read: Int
                while (inStream.read(buffer).also { read = it } != -1) {
                    outStream.write(buffer, 0, read)
                }

                inStream.close()
                outStream.flush()
                outStream.close()

                inFile.delete()
                Timber.tag(context.getString(R.string.app_name)).i("[Preferences] Successfully moved [" + from + "] to [" + to + "]")
            }
        } else {
            Timber.tag(context.getString(R.string.app_name)).w("[Preferences] Can't move [" + from + "] to [" + to + "], source file doesn't exists")
        }
    }
}
