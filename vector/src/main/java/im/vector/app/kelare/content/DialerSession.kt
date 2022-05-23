package im.vector.app.kelare.content

import android.content.Context
import android.text.TextUtils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import im.vector.app.kelare.network.models.AddSipPhoneBean
import im.vector.app.kelare.network.models.DialerAccountInfo

class DialerSession(c: Context) {

    private val mHashStorage: HashStorage

    companion object {

        private const val FILE_NAME = ".session"

        private val KEY_BASE_IP = "base_ip"
        private val KEY_BASE_SERVER_URL = "base_server_url"
        private val KEY_ACCOUNT = "account"
        private val KEY_PASSWORD = "password"
        private val KEY_USER_INFO = "user_info"
        private val KEY_REMEMBER_FLAG = "remember_flag"
        private val KEY_LOGOUT_FLAG = "logout_flag"
        private val KEY_LOCALE_LANGUAGE  = "locale_language"
        private val KEY_LOCALE_COUNTRY  = "locale_country"
        private val KEY_CABINET_INFO = "cabinet_info"

        private const val KEY_DIALER_ACCOUNT_INFO = "dialer_account_info"
        private const val KEY_DIALER_PHONE_NUMBER = "dialer_phone_number"
        private const val KEY_ACCESS_TOKEN = "access_token"

        //Dialer
        private const val KEY_APP_HOME_SERVER = "app_home_server"
        private const val KEY_USER_ID = "user_id"

    }

    init {
        this.mHashStorage = HashStorage(c, FILE_NAME)
    }

    fun clear() {
        this.mHashStorage.clear()
    }

    fun hasToken(): Boolean {
        return !TextUtils.isEmpty(accessToken)
    }

    // clear information
    fun removeLoginInfo() {

        this.mHashStorage.remove(KEY_ACCESS_TOKEN)
        this.mHashStorage.remove(KEY_BASE_SERVER_URL)
        this.mHashStorage.remove(KEY_USER_INFO)
        this.mHashStorage.remove(KEY_LOGOUT_FLAG)
        this.mHashStorage.remove(KEY_LOCALE_LANGUAGE)
        this.mHashStorage.remove(KEY_LOCALE_COUNTRY)
        this.mHashStorage.remove(KEY_CABINET_INFO)
    }

    fun setData(data: Any, type: Class<*>, key: String) {
        val gson = Gson()
        val json = gson.toJson(data, type)
        this.mHashStorage.put(key, json)
    }

    fun <T> getData(type: Class<out T>, key: String): T {
        val gson = Gson()
        return gson.fromJson<T>(this.mHashStorage.getString(key), type)
    }

    var homeServer: String
        get() = this.mHashStorage.getString(KEY_APP_HOME_SERVER)
        set(paramString) = this.mHashStorage.put(KEY_APP_HOME_SERVER, paramString)

    var userID: String
        get() = this.mHashStorage.getString(KEY_USER_ID)
        set(paramString) = this.mHashStorage.put(KEY_USER_ID, paramString)

    var accessToken: String
        get() = this.mHashStorage.getString(KEY_ACCESS_TOKEN)
        set(paramString) = this.mHashStorage.put(KEY_ACCESS_TOKEN, paramString)

    var rememberFlag: Boolean
        get() = this.mHashStorage.getBoolean(KEY_REMEMBER_FLAG)
        set(paramFlag) = this.mHashStorage.put(KEY_REMEMBER_FLAG, paramFlag)

    var accountListInfo : ArrayList<DialerAccountInfo>?
        get() {
            val json = this.mHashStorage.getString(KEY_DIALER_ACCOUNT_INFO)
            if (json.isEmpty()) {
                return null
            } else {
                return Gson().fromJson<ArrayList<DialerAccountInfo>>(json, object : TypeToken<ArrayList<DialerAccountInfo>>() {
                }.type)
            }
        }
        set(accountListInfo) {
            val json = Gson().toJson(accountListInfo)
            this.mHashStorage.put(KEY_DIALER_ACCOUNT_INFO, json)
        }

    var phoneListInfo : ArrayList<AddSipPhoneBean>?
        get() {
            val json = this.mHashStorage.getString(KEY_DIALER_PHONE_NUMBER)
            if (json.isEmpty()) {
                return null
            } else {
                return Gson().fromJson<ArrayList<AddSipPhoneBean>>(json, object : TypeToken<ArrayList<AddSipPhoneBean>>() {
                }.type)
            }
        }
        set(phoneListInfo) {
            val json = Gson().toJson(phoneListInfo)
            this.mHashStorage.put(KEY_DIALER_PHONE_NUMBER, json)
        }


    /*var userInfo: UserInfo
        get() = this.getData(UserInfo::class.java, KEY_USER_INFO)
        set(info) = this.setData(info, UserInfo::class.java, KEY_USER_INFO)*/

}
