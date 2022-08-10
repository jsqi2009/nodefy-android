package im.vector.app.kelare.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.text.TextUtils
import androidx.core.app.ActivityCompat.requestPermissions
import im.vector.app.VectorApplication
import im.vector.app.kelare.content.DialerSession
import im.vector.app.kelare.greendao.DaoSession
import im.vector.app.kelare.network.models.DialerAccountInfo
import org.linphone.core.*
import timber.log.Timber
import java.util.*

/**
 * @author : Jason
 * @date   : 4/26/22
 * @desc   :
 */
class SIPLoginUtil(val context: Context, val core: Core, private val accountInfo : DialerAccountInfo, private val mDaoSession: DaoSession) {

    private var transportList = arrayListOf("UDP", "TCP", "TLS")
    private var encryptList = arrayListOf("NONE", "SRTP", "ZRTP", "DLTS")

    fun loginSIPAccount(){

        val accountName = accountInfo.account_name
        val username = accountInfo.username
        val password = accountInfo.password
        val domain = accountInfo.domain
        val proxy = accountInfo.extension.outProxy

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(accountName) || TextUtils.isEmpty(password) || TextUtils.isEmpty(domain)) {
            return
        }

        var transportType: TransportType? = null
        var mediaEncryption: MediaEncryption? = null
        for (index in transportList.indices) {
            if (accountInfo.extension.sipTransport!!.toUpperCase(Locale.ROOT) == transportList[index]) {
                transportType = TransportType.fromInt(index)
            }
        }

        var isMatched = false
        for (index in encryptList.indices) {
            if (accountInfo.extension.encryptMedia!!.toUpperCase(Locale.ROOT) == encryptList[index]) {
                mediaEncryption = MediaEncryption.fromInt(index)
                isMatched = true
                break
            }
        }
        if (!isMatched) {
            mediaEncryption = MediaEncryption.fromInt(0)
        }

        val authInfo = Factory.instance().createAuthInfo(username!!, null, password, null, null, domain, null)
        val accountParams = core.createAccountParams()

        // A SIP account is identified by an identity address that we can construct from the username and domain
        val identity = Factory.instance().createAddress("sip:$username@$domain")
        accountParams.identityAddress = identity
        // Ensure push notification is enabled for this account
        accountParams.pushNotificationAllowed = true
        accountParams.registerEnabled = true


        // We also need to configure where the proxy server is located
        //val address = Factory.instance().createAddress("sip:${proxy}")
        var address: Address? = null
        if (TextUtils.isEmpty(proxy)) {
            address = Factory.instance().createAddress("sip:${domain}")
        } else {
            address = Factory.instance().createAddress("sip:${proxy}")
        }

        // We use the Address object to easily set the transport protocol
        address?.transport = transportType
        accountParams.serverAddress = address
        core.mediaEncryption = mediaEncryption

        // Now that our AccountParams is configured, we can create the Account object
        val account: Account = core.createAccount(accountParams)

        // Now let's add our objects to the Core
        core.addAuthInfo(authInfo)
        core.addAccount(account)

        // Asks the CaptureTextureView to resize to match the captured video's size ratio
        core.config.setBool("video", "auto_resize_preview_to_keep_ratio", true)

        // Also set the newly added account as default
        /*if (accountInfo.is_default) {
            core.defaultAccount = account
        }*/

        core.defaultAccount = account

        // To be notified of the connection status of our account, we need to add the listener to the Core
        core.addListener(loginCoreListener)
        // We can also register a callback on the Account object
        account.addListener { _, state, message ->
            // There is a Log helper in org.linphone.core.tools package
            Timber.e("[Account] Registration state changed, $state + '---' + $message")
        }

        // Finally we need the Core to be started for the registration to happen (it could have been started before)
        core.start()

        VectorApplication.coreContext.newAccountConfigured(false)
        // We will need the RECORD_AUDIO permission for video call
        if (context.packageManager.checkPermission(Manifest.permission.RECORD_AUDIO, context.packageName) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(context as Activity, arrayOf(Manifest.permission.RECORD_AUDIO), 0)
            return
        }

        if (!core.isPushNotificationAvailable) {
            Timber.e("Something is wrong with the linphone push setup!")
        }

    }

    // Create a Core listener to listen for the callback we need
    // In this case, we want to know about the account registration status
    private val loginCoreListener = object: CoreListenerStub() {
        override fun onAccountRegistrationStateChanged(core: Core, account: Account, state: RegistrationState, message: String) {
            if (state == RegistrationState.Failed || state == RegistrationState.Cleared) {
                Timber.e("[Account] Login failure")
            } else if (state == RegistrationState.Ok) {
                Timber.e("[Account] Login success")

            }
        }
    }

    private fun updateConnectStatus() {
        val mSession : DialerSession = DialerSession(context)

    }
}
