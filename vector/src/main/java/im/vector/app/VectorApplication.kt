/*
 * Copyright 2019 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.app

import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Handler
import android.os.HandlerThread
import android.os.StrictMode
import androidx.core.provider.FontRequest
import androidx.core.provider.FontsContractCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDex
import com.airbnb.epoxy.EpoxyAsyncUtil
import com.airbnb.epoxy.EpoxyController
import com.airbnb.mvrx.Mavericks
import im.vector.app.kelare.sipcore.CoreContext
import im.vector.app.kelare.sipcore.CorePreferences
import im.vector.app.kelare.sipcore.CoreService
import com.facebook.stetho.Stetho
import com.gabrielittner.threetenbp.LazyThreeTen
import com.mapbox.mapboxsdk.Mapbox
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider
import dagger.hilt.android.HiltAndroidApp
import im.vector.app.core.di.ActiveSessionHolder
import im.vector.app.core.extensions.configureAndStart
import im.vector.app.core.extensions.startSyncing
import im.vector.app.core.time.Clock
import im.vector.app.features.analytics.VectorAnalytics
import im.vector.app.features.call.webrtc.WebRtcCallManager
import im.vector.app.features.configuration.VectorConfiguration
import im.vector.app.features.disclaimer.doNotShowDisclaimerDialog
import im.vector.app.features.invite.InvitesAcceptor
import im.vector.app.features.lifecycle.VectorActivityLifecycleCallbacks
import im.vector.app.features.notifications.NotificationDrawerManager
import im.vector.app.features.notifications.NotificationUtils
import im.vector.app.features.pin.PinLocker
import im.vector.app.features.popup.PopupAlertManager
import im.vector.app.features.rageshake.VectorFileLogger
import im.vector.app.features.rageshake.VectorUncaughtExceptionHandler
import im.vector.app.features.settings.VectorLocale
import im.vector.app.features.settings.VectorPreferences
import im.vector.app.features.themes.ThemeUtils
import im.vector.app.features.version.VersionProvider
import im.vector.app.kelare.content.AndroidBus
import im.vector.app.kelare.dialer.call.ComingCallActivity
import im.vector.app.kelare.greendao.DaoMaster
import im.vector.app.kelare.greendao.DaoSession
import im.vector.app.kelare.network.HttpClient
import im.vector.app.kelare.utils.LinphoneUtils
import im.vector.app.push.fcm.FcmHelper
import org.jitsi.meet.sdk.log.JitsiMeetDefaultLogHandler
import org.jivesoftware.smack.android.AndroidSmackInitializer
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.linphone.core.Call
import org.linphone.core.ChatMessage
import org.linphone.core.ChatRoom
import org.linphone.core.ChatRoomBackend
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.Factory
import org.linphone.core.InfoMessage
import org.linphone.core.LogCollectionState
import org.linphone.core.LogLevel
import org.linphone.core.tools.Log
import org.matrix.android.sdk.api.Matrix
import org.matrix.android.sdk.api.auth.AuthenticationService
import org.matrix.android.sdk.api.legacy.LegacySessionImporter
import org.zhx.common.bgstart.library.BgManager
import org.zhx.common.bgstart.library.impl.BgStart
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import javax.inject.Inject
import androidx.work.Configuration as WorkConfiguration

@HiltAndroidApp
class VectorApplication :
        Application(),
        WorkConfiguration.Provider {

    lateinit var appContext: Context
    @Inject lateinit var legacySessionImporter: LegacySessionImporter
    @Inject lateinit var authenticationService: AuthenticationService
    @Inject lateinit var vectorConfiguration: VectorConfiguration
    @Inject lateinit var emojiCompatFontProvider: EmojiCompatFontProvider
    @Inject lateinit var emojiCompatWrapper: EmojiCompatWrapper
    @Inject lateinit var vectorUncaughtExceptionHandler: VectorUncaughtExceptionHandler
    @Inject lateinit var activeSessionHolder: ActiveSessionHolder
    @Inject lateinit var clock: Clock
    @Inject lateinit var notificationDrawerManager: NotificationDrawerManager
    @Inject lateinit var vectorPreferences: VectorPreferences
    @Inject lateinit var versionProvider: VersionProvider
    @Inject lateinit var notificationUtils: NotificationUtils
    @Inject lateinit var appStateHandler: AppStateHandler
    @Inject lateinit var popupAlertManager: PopupAlertManager
    @Inject lateinit var pinLocker: PinLocker
    @Inject lateinit var callManager: WebRtcCallManager
    @Inject lateinit var invitesAcceptor: InvitesAcceptor
    @Inject lateinit var autoRageShaker: AutoRageShaker
    @Inject lateinit var vectorFileLogger: VectorFileLogger
    @Inject lateinit var vectorAnalytics: VectorAnalytics
    @Inject lateinit var matrix: Matrix

    // font thread handler
    private var fontThreadHandler: Handler? = null

    //nodefy sip
    lateinit var linphoneCore: Core
    lateinit var mBus: AndroidBus
    //private var daoSession: DaoSession? = null
    var mConnectionList: ArrayList<XMPPTCPConnection> = ArrayList()

    private val powerKeyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF &&
                    vectorPreferences.useFlagPinCode()) {
                pinLocker.screenIsOff()
            }
        }
    }

    override fun onCreate() {
        enableStrictModeIfNeeded()
        super.onCreate()
        appContext = this
        vectorAnalytics.init()
        invitesAcceptor.initialize()
        autoRageShaker.initialize()
        vectorUncaughtExceptionHandler.activate()

        // Remove Log handler statically added by Jitsi
        Timber.forest()
                .filterIsInstance(JitsiMeetDefaultLogHandler::class.java)
                .forEach { Timber.uproot(it) }

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.plant(vectorFileLogger)

        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this)
        }
        logInfo()
        LazyThreeTen.init(this)
        Mavericks.initialize(debugMode = false)
        EpoxyController.defaultDiffingHandler = EpoxyAsyncUtil.getAsyncBackgroundHandler()
        EpoxyController.defaultModelBuildingHandler = EpoxyAsyncUtil.getAsyncBackgroundHandler()
        registerActivityLifecycleCallbacks(VectorActivityLifecycleCallbacks(popupAlertManager))
        val fontRequest = FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                "Noto Color Emoji Compat",
                R.array.com_google_android_gms_fonts_certs
        )
        FontsContractCompat.requestFont(this, fontRequest, emojiCompatFontProvider, getFontThreadHandler())
        VectorLocale.init(this)
        ThemeUtils.init(this)
        vectorConfiguration.applyToApplicationContext()

        emojiCompatWrapper.init(fontRequest)

        notificationUtils.createNotificationChannels()

        // It can takes time, but do we care?
        val sessionImported = legacySessionImporter.process()
        if (!sessionImported) {
            // Do not display the name change popup
            doNotShowDisclaimerDialog(this)
        }

        if (authenticationService.hasAuthenticatedSessions() && !activeSessionHolder.hasActiveSession()) {
            val lastAuthenticatedSession = authenticationService.getLastAuthenticatedSession()!!
            activeSessionHolder.setActiveSession(lastAuthenticatedSession)
            lastAuthenticatedSession.configureAndStart(applicationContext, startSyncing = false)
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(startSyncOnFirstStart)

        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                Timber.i("App entered foreground")
                FcmHelper.onEnterForeground(appContext, activeSessionHolder)
                activeSessionHolder.getSafeActiveSession()?.also {
                    it.stopAnyBackgroundSync()
                }
            }

            override fun onPause(owner: LifecycleOwner) {
                Timber.i("App entered background")
                FcmHelper.onEnterBackground(appContext, vectorPreferences, activeSessionHolder, clock)
            }
        })
        ProcessLifecycleOwner.get().lifecycle.addObserver(appStateHandler)
        ProcessLifecycleOwner.get().lifecycle.addObserver(pinLocker)
        ProcessLifecycleOwner.get().lifecycle.addObserver(callManager)
        // This should be done as early as possible
        // initKnownEmojiHashSet(appContext)

        applicationContext.registerReceiver(powerKeyReceiver, IntentFilter().apply {
            // Looks like i cannot receive OFF, if i don't have both ON and OFF
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        })

        EmojiManager.install(GoogleEmojiProvider())

        // Initialize Mapbox before inflating mapViews
        Mapbox.getInstance(this)

        //dialer feature
        mBus = AndroidBus()
        HttpClient.init(this, mBus)
        mBus.register(this)
        //initLinPhone()
        createConfig(applicationContext)
        initGreenDao(applicationContext)
        AndroidSmackInitializer.initialize(this)
        BgManager.getInstance().init(this)

        ensureCoreExists(applicationContext)
    }

    private val startSyncOnFirstStart = object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            Timber.i("App process started")
            authenticationService.getLastAuthenticatedSession()?.startSyncing(appContext)
            ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        }
    }

    private fun enableStrictModeIfNeeded() {
        if (BuildConfig.ENABLE_STRICT_MODE_LOGS) {
            StrictMode.setThreadPolicy(
                    StrictMode.ThreadPolicy.Builder()
                            .detectAll()
                            .penaltyLog()
                            .build()
            )
        }
    }

    override fun getWorkManagerConfiguration(): WorkConfiguration {
        return WorkConfiguration.Builder()
                .setWorkerFactory(matrix.workerFactory())
                .setExecutor(Executors.newCachedThreadPool())
                .build()
    }

    private fun logInfo() {
        val appVersion = versionProvider.getVersion(longFormat = true, useBuildNumber = true)
        val sdkVersion = Matrix.getSdkVersion()
        val date = SimpleDateFormat("MM-dd HH:mm:ss.SSSZ", Locale.US).format(Date())

        Timber.d("----------------------------------------------------------------")
        Timber.d("----------------------------------------------------------------")
        Timber.d(" Application version: $appVersion")
        Timber.d(" SDK version: $sdkVersion")
        Timber.d(" Local time: $date")
        Timber.d("----------------------------------------------------------------")
        Timber.d("----------------------------------------------------------------\n\n\n\n")
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        vectorConfiguration.onConfigurationChanged()
    }

    private fun getFontThreadHandler(): Handler {
        return fontThreadHandler ?: createFontThreadHandler().also {
            fontThreadHandler = it
        }
    }

    private fun createFontThreadHandler(): Handler {
        val handlerThread = HandlerThread("fonts")
        handlerThread.start()
        return Handler(handlerThread.looper)
    }

    private fun initLinPhone(){
        // Core is the main object of the SDK. You can't do much without it.
        // To create a Core, we need the instance of the Factory.
        val factory = Factory.instance()
        // Some configuration can be done before the Core is created, for example enable debug logs.
        factory.setDebugMode(true, "Hello Linphone")
        factory.isChatroomBackendAvailable(ChatRoomBackend.Basic)
        Factory.instance().setLogCollectionPath(filesDir.absolutePath)
        factory.enableLogCollection(LogCollectionState.Enabled)

        // Your Core can use up to 2 configuration files, but that isn't mandatory.
        // On Android the Core needs to have the application context to work.
        // If you don't, the following method call will crash.
        linphoneCore = factory.createCore(null, null, this)
        // Make sure the core is configured to use push notification token from firebase
        linphoneCore.isPushNotificationEnabled = true
        /**
         * this is very important, or it will have SSL handshake fail: X509
         */
        linphoneCore.verifyServerCertificates(false)
        linphoneCore.addListener(coreListener)
    }

    /*fun getDaoSession(): DaoSession? {
        return daoSession
    }

    *//**
     * 初始化GreenDao,直接在Application中进行初始化操作
     *//*
    private fun initGreenDao() {
        val helper = DaoMaster.DevOpenHelper(this, "nodefy_sip.db")
        val db = helper.writableDatabase
        val daoMaster = DaoMaster(db)
        daoSession = daoMaster.newSession()
    }*/

    private val coreListener = object: CoreListenerStub() {

        override fun onMessageReceived(core: Core, receivedChatRoom: ChatRoom, message: ChatMessage) {

            //user the receivedPeerDomain, receivedLocalDomain is the IP, maybe not correct
            /**
             * receivedLocalDomain:,182.119.130.45
             * receivedLocalUser:, 2222
             * receivedPeerDomain:, comms.kelare-demo.com
             * receivedPeerUser:, 3333
             */
            val receivedLocalDomain = message.localAddress.domain
            val receivedLocalUser = message.localAddress.username
            val receivedPeerDomain = message.fromAddress.domain
            val receivedPeerUser = message.fromAddress.username
            Timber.e("receivedLocalDomain:,$receivedLocalDomain")
            Timber.e("receivedLocalUser:, $receivedLocalUser")
            Timber.e("receivedPeerDomain:, $receivedPeerDomain")
            Timber.e("receivedPeerUser:, $receivedPeerUser")

            Timber.e("toAddress:, ${message.toAddress.domain}")


            val localAccount = LinphoneUtils.getSipAccount(message, linphoneCore)
            LinphoneUtils.createBasicChatRoom(message, localAccount!!, linphoneCore)

            for (content in message.contents) {
                if (content.isText) {
                    LinphoneUtils.insertSipMessage(content.utf8Text.toString(), false, message, localAccount, getDaoSession()!!)
                }
            }
        }

        override fun onInfoReceived(core: Core, call: Call, message: InfoMessage) {

        }

        override fun onCallStateChanged(
                core: Core,
                call: Call,
                state: Call.State?,
                message: String
        ) {
            when (state) {
                Call.State.IncomingReceived -> {
                    Timber.e("receivedPeerUser: 监听到来电")

                    Timber.e("call remote domain: ${call.remoteAddress.domain}")
                    Timber.e("call remote user: ${call.remoteAddress.username}")
                    Timber.e("call local domain: ${call.callLog.localAddress.domain}")
                    Timber.e("call local user: ${call.callLog.localAddress.username}")

                    if (call.remoteAddress.username != call.callLog.localAddress.username) {
                        val intent = Intent(applicationContext, ComingCallActivity::class.java)
                        intent.putExtra("local_user", call.callLog.localAddress.username)
                        intent.putExtra("remote_user", call.remoteAddress.username)
                        intent.putExtra("domain", call.remoteAddress.domain)
                        BgStart.getInstance().startActivity(applicationContext, intent, ComingCallActivity::class.java.name)
                    }
                }
            }

        }
    }

    companion object{
        operator fun get(content: Context): VectorApplication {
            return content.applicationContext as VectorApplication
        }

        private var daoSession: DaoSession? = null

        fun getDaoSession(): DaoSession? {
            return daoSession
        }

        /**
         * 初始化GreenDao,直接在Application中进行初始化操作
         */
        private fun initGreenDao(content: Context) {
            val helper = DaoMaster.DevOpenHelper(content, "nodefy_sip.db")
            val db = helper.writableDatabase
            val daoMaster = DaoMaster(db)
            daoSession = daoMaster.newSession()
        }

        @SuppressLint("StaticFieldLeak")
        lateinit var corePreferences: CorePreferences
        @SuppressLint("StaticFieldLeak")
        lateinit var coreContext: CoreContext

        private fun createConfig(context: Context) {
            if (::corePreferences.isInitialized) {
                return
            }

            Factory.instance().setLogCollectionPath(context.applicationContext.filesDir.absolutePath)
            Factory.instance().enableLogCollection(LogCollectionState.Enabled)

            // For VFS
            //Factory.instance().setCacheDir(context.cacheDir.absolutePath)

            corePreferences = CorePreferences(context)
            corePreferences.copyAssetsFromPackage()

            if (corePreferences.vfsEnabled) {
                CoreContext.activateVFS()
            }

            val config = Factory.instance().createConfigWithFactory(corePreferences.configPath, corePreferences.factoryConfigPath)
            corePreferences.config = config

            val appName = context.getString(R.string.app_name)
            Factory.instance().setLoggerDomain(appName)
            Factory.instance().enableLogcatLogs(corePreferences.logcatLogsOutput)
            if (corePreferences.debugLogs) {
                Factory.instance().loggingService.setLogLevel(LogLevel.Message)
            }

            Log.i("[Application] Core config & preferences created")
        }

        fun ensureCoreExists(
                context: Context,
                pushReceived: Boolean = false,
                service: CoreService? = null,
                useAutoStartDescription: Boolean = false
        ): Boolean {
            if (::coreContext.isInitialized && !coreContext.stopped) {
                Log.d("[Application] Skipping Core creation (push received? $pushReceived)")
                return false
            }

            Log.i("[Application] Core context is being created ${if (pushReceived) "from push" else ""}")
            coreContext = CoreContext(context, corePreferences.config, service, useAutoStartDescription)
            coreContext.start()
            return true
        }

        fun contextExists(): Boolean {
            return ::coreContext.isInitialized
        }
    }
}
