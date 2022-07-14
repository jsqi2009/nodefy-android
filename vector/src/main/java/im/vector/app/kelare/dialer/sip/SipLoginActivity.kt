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

package im.vector.app.kelare.dialer.sip

import SipAdvancedInfoEvent
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import com.squareup.otto.Subscribe
import com.suke.widget.SwitchButton
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivitySipLoginBinding
import im.vector.app.kelare.network.HttpClient
import im.vector.app.kelare.network.event.SaveAccountInfoResponseEvent
import im.vector.app.kelare.network.event.UpdateAccountInfoResponseEvent
import im.vector.app.kelare.network.models.DialerAccountInfo
import im.vector.app.kelare.network.models.SIPAdvancedInfo
import im.vector.app.kelare.network.models.SaveAccountInfo
import im.vector.app.kelare.network.models.UpdateAccountInfo
import org.linphone.core.Account
import org.linphone.core.Address
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.Factory
import org.linphone.core.MediaEncryption
import org.linphone.core.RegistrationState
import org.linphone.core.TransportType
import timber.log.Timber
import java.util.Locale

@AndroidEntryPoint
class SipLoginActivity : VectorBaseActivity<ActivitySipLoginBinding>(), View.OnClickListener {

    override fun getBinding() = ActivitySipLoginBinding.inflate(layoutInflater)

    private var index = ""   //1:create new account   2:update account
    private var accountInfo : DialerAccountInfo = DialerAccountInfo()
    private var advancedInfo : SIPAdvancedInfo = SIPAdvancedInfo()
    private var isEnable = false
    private var isDefault = false
    private var transportList = arrayListOf("UDP", "TCP", "TLS")
    private var encryptList = arrayListOf("NONE", "SRTP", "ZRTP", "DLTS")
    private var isSave = false
    private var isUpload = false
    private var loginSuccessFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarWhiteColor(this)

        index = intent.extras!!.getString("index")!!
        if (index == "2") {
            accountInfo = intent.getSerializableExtra("account") as DialerAccountInfo
            isUpload = accountInfo.is_upload
            if (accountInfo.extension.encryptMedia!!.toLowerCase(Locale.ROOT) == "never") {
                accountInfo.extension.encryptMedia = "NONE"
            }
        }

        initView()
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        views.tvTitle.text = "SIP Account"

        views.rlBack.setOnClickListener(this)
        views.llAdvanced.setOnClickListener(this)
        views.tvSave.setOnClickListener(this)

        views.sbDefault.setOnCheckedChangeListener(object : SwitchButton.OnCheckedChangeListener{
            override fun onCheckedChanged(view: SwitchButton?, isChecked: Boolean) {
                isDefault = isChecked
                hideOrShow()
                accountInfo.is_default = isDefault
                Timber.e("isDefault: $isDefault")
            }
        })

        views.sbEnable.setOnCheckedChangeListener(object : SwitchButton.OnCheckedChangeListener{
            override fun onCheckedChanged(view: SwitchButton?, isChecked: Boolean) {
                isEnable = isChecked
                hideOrShow()
                Timber.e("isEnable: $isEnable")
                if (isEnable) {
                    formatAccountInfo(false)
                    loginAccount()
                    if (index == "2") {
                        updateServerAccount(false)
                    }
                } else {
                    unregisterSipAccount()
                    views.tvConnectStatus.text = "Not Registered"
                    views.tvConnectStatus.setTextColor(resources.getColor(R.color.red, null))
                }

                resetViewStatus()
            }
        })

        if (index == "2") {
            renderDefaultData()
        } else {
            accountInfo.extension.sipTransport = "UDP"
            accountInfo.extension.encryptMedia = "NONE"
            advancedInfo.sipTransport = "UDP"
            advancedInfo.encryptMedia = "NONE"
        }
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.rl_back -> {
                finish()
            }
            R.id.tv_save -> {
                if (index == "1") {
                    if (!loginSuccessFlag) {
                        saveServerAccount(false)
                    } else {
                        finish()
                    }
                } else {
                    isSave = true
                    updateServerAccount(false)
                }
            }
            R.id.ll_advanced -> {
                accountAdvanced()
            }
            else -> {
            }
        }
    }

    // Create a Core listener to listen for the callback we need
    // In this case, we want to know about the account registration status
    private val loginCoreListener = object: CoreListenerStub() {
        @SuppressLint("SetTextI18n")
        override fun onAccountRegistrationStateChanged(core: Core, account: Account, state: RegistrationState, message: String) {

            if (state == RegistrationState.Failed || state == RegistrationState.Cleared) {
                Timber.e("[Account] Login failure")
                views.tvConnectStatus.text = "Not Registered"
            } else if (state == RegistrationState.Ok) {
                Timber.e("[Account] Login success")
                views.tvConnectStatus.text = "Registered"
                loginSuccessFlag = true
                //Toast.makeText(this@SipLoginActivity, "Login Success", Toast.LENGTH_SHORT).show()
                if (index == "2") {
                    updateServerAccount(true)
                } else {
                    saveServerAccount(true)
                }
            }
        }
    }

    /**
     * edit account info from setting
     */
    @SuppressLint("SetTextI18n")
    private fun renderDefaultData() {

        views.etAccount.setText(accountInfo.account_name)
        views.etUsername.setText(accountInfo.username)
        views.etDisplay.setText(accountInfo.display_as)
        views.etPassword.setText(accountInfo.password)
        views.etDomain.setText(accountInfo.domain)
        views.etVoice.setText(accountInfo.voice_mail)

        isEnable = accountInfo.enabled
        isDefault = accountInfo.is_default
        views.sbEnable.isChecked = isEnable
        views.sbDefault.isChecked = isDefault

        if (accountInfo.enabled && accountInfo.extension.isConnected) {
            views.tvConnectStatus.text = "Registered"
            views.tvConnectStatus.setTextColor(resources.getColor(R.color.green, null))
        } else {
            views.tvConnectStatus.text = "Not Registered"
            views.tvConnectStatus.setTextColor(resources.getColor(R.color.red, null))
        }

        views.etAccount.addTextChangedListener(textWatcher)
        views.etDisplay.addTextChangedListener(textWatcher)
        views.etUsername.addTextChangedListener(textWatcher)
        views.etDomain.addTextChangedListener(textWatcher)
        views.etPassword.addTextChangedListener(textWatcher)
        views.etVoice.addTextChangedListener(textWatcher)

        resetViewStatus()
        setAdvancedInfo()
    }

    @SuppressLint("SetTextI18n")
    private fun loginAccount(){

        try {
            val accountName = views.etAccount.text.toString()
            val username = views.etUsername.text.toString()
            val password = views.etPassword.text.toString()
            val displayName = views.etDisplay.text.toString()
            var domain = ""
            var proxy = ""
            if (views.etDomain.text.toString().isEmpty()) {
                domain = ""
            } else {
                domain = views.etDomain.text.toString().replace(" ", "")
            }
            if (accountInfo.extension.outProxy.isNullOrEmpty()) {
                proxy = ""
            } else {
                proxy = accountInfo.extension.outProxy!!.replace(" ", "")
            }

            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(accountName) || TextUtils.isEmpty(password)
                    || TextUtils.isEmpty(domain) || TextUtils.isEmpty(displayName)) {
                return
            }

            var transportType: TransportType? = null
            var mediaEncryption: MediaEncryption? = null
            for (index in transportList.indices) {
                if (accountInfo.extension.sipTransport!!.toUpperCase(Locale.ROOT) == transportList[index]) {
                    transportType = TransportType.fromInt(index)
                }
            }

            for (index in encryptList.indices) {
                if (accountInfo.extension.encryptMedia!!.toUpperCase(Locale.ROOT) == encryptList[index]) {
                    mediaEncryption = MediaEncryption.fromInt(index)
                }
            }

            val authInfo = Factory.instance().createAuthInfo(username, null, password, null, null, domain, null)
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
            //core.config.setBool("video", "auto_resize_preview_to_keep_ratio", true)

            core.defaultProxyConfig = this.core.createProxyConfig()

            // Also set the newly added account as default
            if (accountInfo.is_default) {
                core.defaultAccount = account
            }

            // To be notified of the connection status of our account, we need to add the listener to the Core
            core.addListener(loginCoreListener)
            // We can also register a callback on the Account object
            account.addListener { _, state, message ->
                // There is a Log helper in org.linphone.core.tools package
                Timber.e("[Account] Registration state changed, $state + '---' + $message")
                if (state.toInt() == 2) {
                    views.tvConnectStatus.text = "Registered"
                    views.tvConnectStatus.setTextColor(resources.getColor(R.color.green, null))
                } else if (state.toInt() == 4) {
                    views.tvConnectStatus.text = "Registered Failure"
                    views.tvConnectStatus.setTextColor(resources.getColor(R.color.red, null))
                } else if (state.toInt() == 1) {
                    views.tvConnectStatus.text = "Registering"
                    views.tvConnectStatus.setTextColor(resources.getColor(R.color.red, null))
                } else {
//                    views.tvConnectStatus.text = "Not Registered"
                    views.tvConnectStatus.text = "Registering"
                    views.tvConnectStatus.setTextColor(resources.getColor(R.color.red, null))
                }

                if (index == "1") {
                    views.tvSave.visibility = View.VISIBLE
                }
            }

            // Finally we need the Core to be started for the registration to happen (it could have been started before)
            core.start()

            // We will need the RECORD_AUDIO permission for video call
            if (packageManager.checkPermission(Manifest.permission.RECORD_AUDIO, packageName) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 0)
                return
            }
        } catch (e: Exception) {

        }
    }

    private fun accountAdvanced() {
        val intent = Intent(this, SipAdvancedActivity::class.java)
        intent.putExtra("advancedInfo", advancedInfo)
        intent.putExtra("isEnable", isEnable)
        intent.putExtra("isDefault", isDefault)
        intent.putExtra("isUpload", isUpload)
        startActivity(intent)
    }

    private fun setAdvancedInfo() {

        advancedInfo.authName = accountInfo.extension.authName
        advancedInfo.displayAs = accountInfo.extension.displayAs
        if (accountInfo.extension.outProxy.isNullOrEmpty()) {
            advancedInfo.outProxy = accountInfo.extension.outProxy
        } else {
            advancedInfo.outProxy = accountInfo.extension.outProxy!!.replace(" ", "")
        }
        advancedInfo.incomingCalls = accountInfo.extension.incomingCalls
        advancedInfo.refreshInterval = accountInfo.extension.refreshInterval
        advancedInfo.interval = accountInfo.extension.interval
        advancedInfo.sipTransport = accountInfo.extension.sipTransport
        advancedInfo.encryptMedia = accountInfo.extension.encryptMedia
        advancedInfo.sipPortStart = accountInfo.extension.sipPortStart
        advancedInfo.sipPortEnd = accountInfo.extension.sipPortEnd
        advancedInfo.rtpAudioPortStart = accountInfo.extension.rtpAudioPortStart
        advancedInfo.rtpAudioPortEnd = accountInfo.extension.rtpAudioPortEnd
        advancedInfo.rtpVideoPortStart = accountInfo.extension.rtpVideoPortStart
        advancedInfo.rtpVideoPortEnd= accountInfo.extension.rtpVideoPortEnd
        advancedInfo.tlsEnable= accountInfo.extension.tlsEnable
    }

    @Subscribe
    fun onAdvancedEvent(event: SipAdvancedInfoEvent) {
        advancedInfo = event.info
        if (index == "2") {
            hideOrShow()
        }
    }

    private fun saveServerAccount(isConnected: Boolean) {

        //formatAccountInfo(isConnected)
        accountInfo.extension.isConnected = isConnected
        val saveAccountInfo = SaveAccountInfo()
        saveAccountInfo.sip_accounts!!.add(accountInfo)
        saveAccountInfo.primary_user_id = dialerSession.userID

        try {
            showLoadingDialog()
            HttpClient.saveDialerAccountInfo(this, saveAccountInfo)
        } catch (e: Exception) {
        }
    }

    @Subscribe
    fun onSaveAccountEvent(event: SaveAccountInfoResponseEvent) {
        try {
            hideLoadingDialog()
            if (event.isSuccess) {
                finish()
            }
        } catch (e: Exception) {
        }
    }

    private fun updateServerAccount(isConnected: Boolean) {

        formatAccountInfo(isConnected)
        val updateAccountInfo = UpdateAccountInfo()
        updateAccountInfo.sip_account = accountInfo
        updateAccountInfo.primary_user_id = dialerSession.userID

        try {
            showLoadingDialog()
            HttpClient.updateDialerAccountInfo(this, updateAccountInfo)
        } catch (e: Exception) {
        }
    }

    @Subscribe
    fun onUpdateAccountEvent(event: UpdateAccountInfoResponseEvent) {
        try {
            hideLoadingDialog()
            if (event.isSuccess) {
                if (isSave) {
                    finish()
                }
            }
        } catch (e: Exception) {
        }
    }

    private fun formatAccountInfo(isConnected: Boolean) {
        accountInfo.type_value = "sip"
        accountInfo.account_name = views.etAccount.text.toString()
        accountInfo.enabled = isEnable
        accountInfo.is_default = isDefault
        accountInfo.display_as = views.etDisplay.text.toString()
        accountInfo.username = views.etUsername.text.toString()
        accountInfo.password = views.etPassword.text.toString()
        accountInfo.domain = views.etDomain.text.toString().replace(" ", "")
        accountInfo.voice_mail = views.etVoice.text.toString()

        accountInfo.extension.accountName = views.etAccount.text.toString()
        accountInfo.extension.displayAs = views.etDisplay.text.toString()
        accountInfo.extension.username = views.etUsername.text.toString()
        accountInfo.extension.domain = views.etDomain.text.toString().replace(" ", "")
        accountInfo.extension.password = views.etPassword.text.toString()
        accountInfo.extension.enable = isEnable
        if (advancedInfo.outProxy.isNullOrEmpty()) {
            accountInfo.extension.outProxy = advancedInfo.outProxy
        } else {
            accountInfo.extension.outProxy = advancedInfo.outProxy!!.replace(" ", "")
        }
        accountInfo.extension.authName = advancedInfo.authName
        accountInfo.extension.incomingCalls = advancedInfo.incomingCalls
        accountInfo.extension.refreshInterval = advancedInfo.refreshInterval
        accountInfo.extension.interval = advancedInfo.interval
        accountInfo.extension.sipTransport = advancedInfo.sipTransport
        accountInfo.extension.encryptMedia = advancedInfo.encryptMedia
        accountInfo.extension.sipPortStart = advancedInfo.sipPortStart
        accountInfo.extension.sipPortEnd = advancedInfo.sipPortEnd
        accountInfo.extension.rtpAudioPortStart = advancedInfo.rtpAudioPortStart
        accountInfo.extension.rtpAudioPortEnd = advancedInfo.rtpAudioPortEnd
        accountInfo.extension.rtpVideoPortStart = advancedInfo.rtpVideoPortStart
        accountInfo.extension.rtpVideoPortEnd = advancedInfo.rtpVideoPortEnd
        accountInfo.extension.tlsEnable = advancedInfo.tlsEnable
    }

    private fun validateAccountInfo():Boolean {
        val userName = views.etUsername.text.toString()
        val account = views.etAccount.text.toString()
        val pwd = views.etPassword.text.toString()
        val domain = views.etDomain.text.toString()

        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(account) || TextUtils.isEmpty(pwd) || TextUtils.isEmpty(domain)){
            Toast.makeText(this, "Please input account info", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    /**
     * can not edit if enable
     */
    private fun resetViewStatus() {
        if (isUpload) {
            views.sbDefault.isEnabled = false
            views.etAccount.isEnabled = false
            views.sbDefault.isEnabled = false
            views.etDisplay.isEnabled = false
            views.etUsername.isEnabled = false
            views.etPassword.isEnabled = false
            views.etDomain.isEnabled = false
            views.etVoice.isEnabled = false
        } else {
            if (isDefault) {
                views.sbDefault.isEnabled = false
            }

            if (isEnable) {
                views.etAccount.isEnabled = false
                views.sbDefault.isEnabled = false
                views.etDisplay.isEnabled = false
                views.etUsername.isEnabled = false
                views.etPassword.isEnabled = false
                views.etDomain.isEnabled = false
                views.etVoice.isEnabled = false
            } else {
                views.etAccount.isEnabled = true
                views.sbDefault.isEnabled = true
                views.etDisplay.isEnabled = true
                views.etUsername.isEnabled = true
                views.etPassword.isEnabled = true
                views.etDomain.isEnabled = true
                views.etVoice.isEnabled = true
            }
        }
    }

    fun hideOrShow() {
        val flag = isShowSaveButton()
        if (flag) {
            views.tvSave.visibility = View.VISIBLE
        } else {
            views.tvSave.visibility = View.GONE
        }
    }

    private fun isShowSaveButton(): Boolean {
        if (index == "1") {
            return false
        }

        when {
            accountInfo.account_name != views.etAccount.text.toString() -> {
                return true
            }
            accountInfo.display_as != views.etDisplay.text.toString() -> {
                return true
            }
            accountInfo.is_default != isDefault -> {
                return true
            }
            accountInfo.enabled != isEnable -> {
                return true
            }
            accountInfo.display_as != views.etDisplay.text.toString() -> {
                return true
            }
            accountInfo.username != views.etUsername.text.toString() -> {
                return true
            }
            accountInfo.password != views.etPassword.text.toString() -> {
                return true
            }
            accountInfo.domain != views.etDomain.text.toString() -> {
                return true
            }
            accountInfo.voice_mail != views.etVoice.text.toString() -> {
                return true
            }
            accountInfo.extension.outProxy != advancedInfo.outProxy -> {
                return true
            }
            accountInfo.extension.authName != advancedInfo.authName -> {
                return true
            }
            accountInfo.extension.incomingCalls != advancedInfo.incomingCalls -> {
                return true
            }
            accountInfo.extension.refreshInterval != advancedInfo.refreshInterval -> {
                return true
            }
            accountInfo.extension.interval != advancedInfo.interval -> {
                return true
            }
            accountInfo.extension.sipTransport != advancedInfo.sipTransport -> {
                return true
            }
            accountInfo.extension.encryptMedia != advancedInfo.encryptMedia -> {
                return true
            }
            accountInfo.extension.sipPortStart != advancedInfo.sipPortStart -> {
                return true
            }
            accountInfo.extension.sipPortEnd != advancedInfo.sipPortEnd -> {
                return true
            }
            accountInfo.extension.rtpAudioPortStart != advancedInfo.rtpAudioPortStart -> {
                return true
            }
            accountInfo.extension.rtpAudioPortEnd != advancedInfo.rtpAudioPortEnd -> {
                return true
            }
            accountInfo.extension.rtpVideoPortStart != advancedInfo.rtpVideoPortStart -> {
                return true
            }
            accountInfo.extension.rtpVideoPortEnd != advancedInfo.rtpVideoPortEnd -> {
                return true
            }
            accountInfo.extension.tlsEnable != advancedInfo.tlsEnable -> {
                return true
            }
            else -> return false
        }
    }

    private var textWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            hideOrShow()
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private fun unregisterSipAccount() {

        val accountList = core.accountList
        if (accountList.isNotEmpty()) {
            for (account in accountList) {
                val domain = account.findAuthInfo()!!.domain.toString()
                val username = account.findAuthInfo()!!.username
                val info = "$username@$domain"
                if (info == (accountInfo.username + "@" + accountInfo.domain)) {

                    core.removeAccount(account)

                    /*val params = account.params
                    // Returned params object is const, so to make changes we first need to clone it
                    val clonedParams = params.clone()
                    // Now let's make our changes
                    clonedParams.registerEnabled = false
                    // And apply them
                    account.params = clonedParams*/
                    break
                }
            }
        }
    }
}
