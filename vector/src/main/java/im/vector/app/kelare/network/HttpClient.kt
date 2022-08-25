package im.vector.app.kelare.network

import android.annotation.SuppressLint
import android.content.Context
import im.vector.app.kelare.network.event.LoginResponseEvent
import im.vector.app.kelare.content.AndroidBus
import im.vector.app.kelare.content.DialerSession
import im.vector.app.kelare.network.event.DefaultContactRelationResponseEvent
import im.vector.app.kelare.network.event.DeleteAccountInfoResponseEvent
import im.vector.app.kelare.network.event.DeleteContactRelationResponseEvent
import im.vector.app.kelare.network.event.DeleteContactResponseEvent
import im.vector.app.kelare.network.event.DialerAccountInfoResponseEvent
import im.vector.app.kelare.network.event.GetAccountContactResponseEvent
import im.vector.app.kelare.network.event.GetContactRelationResponseEvent
import im.vector.app.kelare.network.event.GetContactResponseEvent
import im.vector.app.kelare.network.event.GetLicenseResponseEvent
import im.vector.app.kelare.network.event.PresenceStatusResponseEvent
import im.vector.app.kelare.network.event.GetPublicRoomResponseEvent
import im.vector.app.kelare.network.event.GetThemesResponseEvent
import im.vector.app.kelare.network.event.SaveAccountInfoResponseEvent
import im.vector.app.kelare.network.event.SaveContactInfoResponseEvent
import im.vector.app.kelare.network.event.UpdateAccountInfoResponseEvent
import im.vector.app.kelare.network.event.UpdateContactInfoResponseEvent
import im.vector.app.kelare.network.event.UpdateContactRelationResponseEvent
import im.vector.app.kelare.network.event.UpdateDetailContactInfoResponseEvent
import im.vector.app.kelare.network.models.DefaultContactRelationInfo
import im.vector.app.kelare.network.response.LoginResponse
import im.vector.app.kelare.network.models.DeleteAccountInfo
import im.vector.app.kelare.network.models.DeleteDialerContact
import im.vector.app.kelare.network.models.DialerContactInfo
import im.vector.app.kelare.network.models.SaveAccountInfo
import im.vector.app.kelare.network.models.UpdateAccountInfo
import im.vector.app.kelare.network.models.UpdateContactRelationInfo
import im.vector.app.kelare.network.response.DefaultContactRelationResponse
import im.vector.app.kelare.network.response.DeleteAccountInfoResponse
import im.vector.app.kelare.network.response.DeleteContactRelationResponse
import im.vector.app.kelare.network.response.DeleteContactResponse
import im.vector.app.kelare.network.response.DialerAccountInfoResponse
import im.vector.app.kelare.network.response.GetAccountContactResponse
import im.vector.app.kelare.network.response.GetContactRelationResponse
import im.vector.app.kelare.network.response.GetContactResponse
import im.vector.app.kelare.network.response.GetLicenseResponse
import im.vector.app.kelare.network.response.GetPublicRoomResponse
import im.vector.app.kelare.network.response.GetThemesResponse
import im.vector.app.kelare.network.response.PresenceStatusResponse
import im.vector.app.kelare.network.response.SaveAccountInfoResponse
import im.vector.app.kelare.network.response.SaveContactInfoResponse
import im.vector.app.kelare.network.response.UpdateAccountInfoResponse
import im.vector.app.kelare.network.response.UpdateContactInfoResponse
import im.vector.app.kelare.network.response.UpdateContactRelationResponse
import im.vector.app.kelare.network.response.UpdateDetailContactInfoResponse
import im.vector.app.kelare.utils.UserAgentUtil
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.KeyStore
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.Arrays
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

object HttpClient {

    private val HTTP_RESPONSE_CACHE = 10485760L

    private val HTTP_TIMEOUT_MS = 20000

    private var mHttpApi: HttpApi? = null

    private var httpClient: OkHttpClient? = null

    private var mBus: AndroidBus? = null

    var authorization: String? = null

    var mSession: DialerSession? = null

    var dispatchClient: DispatchClient? = null

    //var severRootUrl: String? = "http://" + mSession!!.baseIP + "/api/"
    var severRootUrl: String? = null


    fun init(context: Context, bus: AndroidBus) {
        //severRootUrl = ManifestMetaReader.getMetaValue(context, "SERVER_ROOT_URL")
        mSession = DialerSession(context)
        //severRootUrl = Contants.HOME_SERVER
        severRootUrl = mSession!!.homeServer
        initOkHTTP()
        mBus = bus
        dispatchClient = DispatchClient(mBus!!)

    }

    /**
     * Init OKHttp
     */
    private fun initOkHTTP() {
        httpClient = provideOkHttpClient()
        initHttpClientApi()
    }

    private fun provideOkHttpClient(): OkHttpClient {
        // Log Info
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        val builder = OkHttpClient.Builder()
        builder.connectTimeout(HTTP_TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
        builder.readTimeout(HTTP_TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
        builder.addInterceptor(loggingInterceptor)
        builder.addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()

            chain.proceed(request.build())
        }

        var trustManagerFactory: TrustManagerFactory?
        trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(null as KeyStore?)
        val trustManagers = trustManagerFactory.trustManagers
        check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
            ("Unexpected default trust managers:" + Arrays.toString(trustManagers))
        }
        val trustManager = trustManagers.get(0) as X509TrustManager

        builder.sslSocketFactory(createInsecureSslSocketFactory(), trustManager)
        //builder.sslSocketFactory(createInsecureSslSocketFactory())
        builder.hostnameVerifier { _, _ -> true }
        return builder.build()
    }

    private fun createInsecureSslSocketFactory(): SSLSocketFactory {
        try {
            val context = SSLContext.getInstance("TLS")
            val permissive = @SuppressLint("CustomX509TrustManager") object : X509TrustManager {
                @Throws(CertificateException::class)
                override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {
                    checkServerTrusted(certs, authType)
                }

                @SuppressLint("TrustAllX509TrustManager")
                @Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            }
            context.init(null, arrayOf<TrustManager>(permissive), null)
            return context.socketFactory
        } catch (e: Exception) {
            throw AssertionError(e)
        }

    }

    private fun initHttpClientApi() {
        try {
            val restAdapter = Retrofit.Builder()
                    .client(httpClient!!)
                    .baseUrl(severRootUrl!!)
                    //.baseUrl(mSession!!.baseServerURL
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

            mHttpApi = restAdapter.create(HttpApi::class.java)
        } catch (E: Throwable) {

        }

    }

    private fun getHeaders(context: Context): HashMap<String, String> {
        var headerMap: HashMap<String, String> = HashMap<String, String>()
        headerMap.put("Authorization", "Bearer " + mSession!!.accessToken)
        headerMap.put("User-Agent", UserAgentUtil.userAgent(context))
        //headerMap.put("User-Agent", APPUtil.getUserAgent(context))

        return headerMap
    }

    /**
     * login
     */
    fun login(account: String, pwd: String) {

        var formMap: HashMap<String, Any> = HashMap<String, Any>()
        formMap.put("name", account)
        formMap.put("password",pwd)

        val call = mHttpApi!!.request("api", "account","token", formMap)
        dispatchClient!!.enqueue(call, LoginResponse::class.java, LoginResponseEvent::class.java)
    }

    /**
     * get dialer account info
     */
    fun getDialerAccountInfo(context: Context,userID: String) {

        var formMap: HashMap<String, Any> = HashMap<String, Any>()
        formMap.put("primary_user_id",userID)

        //val call = mHttpApi!!.getDialerAccount(getAuthor(), formMap)
        val call = mHttpApi!!.getDialerAccount2(getHeaders(context), formMap)
        dispatchClient!!.enqueue(call, DialerAccountInfoResponse::class.java, DialerAccountInfoResponseEvent::class.java)
    }

    /**
     * save dialer account info
     */
    fun saveDialerAccountInfo(context: Context,info: SaveAccountInfo) {

        val call = mHttpApi!!.saveDialerAccount(getHeaders(context), info)
        dispatchClient!!.enqueue(call, SaveAccountInfoResponse::class.java, SaveAccountInfoResponseEvent::class.java)
    }

    /**
     * update dialer account info
     */
    fun updateDialerAccountInfo(context: Context,info: UpdateAccountInfo) {

        val call = mHttpApi!!.updateDialerAccount(getHeaders(context), info)
        dispatchClient!!.enqueue(call, UpdateAccountInfoResponse::class.java, UpdateAccountInfoResponseEvent::class.java)
    }

    /**
     * delete dialer account info
     */
    fun deleteDialerAccountInfo(context: Context,info: DeleteAccountInfo) {

        val call = mHttpApi!!.deleteDialerAccount(getHeaders(context), info)
        dispatchClient!!.enqueue(call, DeleteAccountInfoResponse::class.java, DeleteAccountInfoResponseEvent::class.java)
    }

    /**
     * get dialer contact
     */
    fun getDialerContact(context: Context, userID: String) {

        val formMap: HashMap<String, Any> = HashMap<String, Any>()
        formMap.put("user_id",userID)

        val call = mHttpApi!!.getDialerContact(getHeaders(context), formMap)
        dispatchClient!!.enqueue(call, GetContactResponse::class.java, GetContactResponseEvent::class.java)
    }

    /**
     * save dialer contact
     */
    fun saveDialerContact(context: Context, info: DialerContactInfo) {

        val call = mHttpApi!!.saveDialerContact(getHeaders(context), info)
        dispatchClient!!.enqueue(call, SaveContactInfoResponse::class.java, SaveContactInfoResponseEvent::class.java)
    }

    /**
     * update dialer contact
     */
    fun updateDialerContact(context: Context, info: DialerContactInfo) {

        val call = mHttpApi!!.updateDialerContact(getHeaders(context), info)
        dispatchClient!!.enqueue(call, UpdateContactInfoResponse::class.java, UpdateContactInfoResponseEvent::class.java)
    }

    /**
     * update dialer contact
     */
    fun detailUpdateDialerContact(context: Context, info: DialerContactInfo) {

        val call = mHttpApi!!.updateDialerContact(getHeaders(context), info)
        dispatchClient!!.enqueue(call, UpdateDetailContactInfoResponse::class.java, UpdateDetailContactInfoResponseEvent::class.java)
    }

    /**
     * delete dialer contact
     */
    fun deleteDialerContact(context: Context, info: DeleteDialerContact) {

        val call = mHttpApi!!.deleteDialerContact(getHeaders(context), info)
        dispatchClient!!.enqueue(call, DeleteContactResponse::class.java, DeleteContactResponseEvent::class.java)
    }

    /**
     * get public room info
     */
    fun getPublicRoomInfo() {
        val call = mHttpApi!!.getPublicRoomInfo()
        dispatchClient!!.enqueue(call, GetPublicRoomResponse::class.java, GetPublicRoomResponseEvent::class.java)
    }

    /**
     * get license
     */
    fun getLicense() {
        val call = mHttpApi!!.getLicense()
        dispatchClient!!.enqueue(call, GetLicenseResponse::class.java, GetLicenseResponseEvent::class.java)
    }

    /**
     * get themes
     */
    fun getThemes() {
        val call = mHttpApi!!.getThemes()
        dispatchClient!!.enqueue(call, GetThemesResponse::class.java, GetThemesResponseEvent::class.java)
    }

    /**
     * get Account Contact
     */
    fun getAccountContact(context: Context) {
        val call = mHttpApi!!.getAccountContact(getHeaders(context))
        dispatchClient!!.enqueue(call, GetAccountContactResponse::class.java, GetAccountContactResponseEvent::class.java)
    }

    /**
     * get presence status
     */
    fun getPresenceStatus(context: Context, userID: String) {
        val call = mHttpApi!!.getPresenceStatus(getHeaders(context), userID)
        dispatchClient!!.enqueue(call, PresenceStatusResponse::class.java, PresenceStatusResponseEvent::class.java, userID)
    }

    /**
     * get Contact Relations
     */
    fun getContactRelations(context: Context, contactID: String) {

        val formMap: HashMap<String, Any> = HashMap<String, Any>()
        formMap.put("primary_user_id",contactID)

        val call = mHttpApi!!.getContactRelations(getHeaders(context), formMap)
        dispatchClient!!.enqueue(call, GetContactRelationResponse::class.java, GetContactRelationResponseEvent::class.java)
    }

    /**
     * set default Contact communication channel
     */
    fun setContactDefaultChannel(context: Context, relationInfo: DefaultContactRelationInfo) {

        val call = mHttpApi!!.setContactDefaultChannel(getHeaders(context), relationInfo)
        dispatchClient!!.enqueue(call, DefaultContactRelationResponse::class.java, DefaultContactRelationResponseEvent::class.java)
    }

    /**
     * update Contact Relation
     */
    fun updateContactRelation(context: Context, relationInfo: UpdateContactRelationInfo) {

        val call = mHttpApi!!.updateContactRelations(getHeaders(context), relationInfo)
        dispatchClient!!.enqueue(call, UpdateContactRelationResponse::class.java, UpdateContactRelationResponseEvent::class.java, relationInfo.children_users[0].user_id!!)
    }

    /**
     * delete Contact Relation
     */
    fun deleteContactRelation(context: Context, relationInfo: UpdateContactRelationInfo) {

        val call = mHttpApi!!.deleteContactRelation(getHeaders(context), relationInfo)
        dispatchClient!!.enqueue(call, DeleteContactRelationResponse::class.java, DeleteContactRelationResponseEvent::class.java)
    }


}
