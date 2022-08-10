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



package im.vector.app.kelare.dialer.call

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.labo.kaji.relativepopupwindow.RelativePopupWindow
import com.squareup.otto.Subscribe
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityHomeBinding
import im.vector.app.databinding.ActivityOutgoingCallBinding
import im.vector.app.kelare.network.event.SetDefaultAccountEvent
import im.vector.app.kelare.network.models.DialerAccountInfo
import im.vector.app.kelare.widget.SipAccountPopup
import org.linphone.core.Account
import org.linphone.core.Call
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.Factory
import org.linphone.core.MediaEncryption
import org.linphone.core.RegistrationState
import timber.log.Timber
import java.util.Locale

@AndroidEntryPoint
class OutgoingCallActivity : VectorBaseActivity<ActivityOutgoingCallBinding>(), View.OnClickListener, View.OnLongClickListener {

    override fun getBinding() = ActivityOutgoingCallBinding.inflate(layoutInflater)

    private var accountList:ArrayList<DialerAccountInfo> = ArrayList()
    private var fullAccount = ""
    private var dialerNumber = ""
    private var selectedAccount:DialerAccountInfo = DialerAccountInfo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarWhiteColor(this)
        initView()
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {

        views.rlBack.setOnClickListener(this)
        views.tvPopup.setOnClickListener(this)
        views.ivSwitchAccount.setOnClickListener(this)
        views.ll0.setOnClickListener(this)
        views.ll1.setOnClickListener(this)
        views.ll2.setOnClickListener(this)
        views.ll3.setOnClickListener(this)
        views.ll4.setOnClickListener(this)
        views.ll5.setOnClickListener(this)
        views.ll6.setOnClickListener(this)
        views.ll7.setOnClickListener(this)
        views.ll8.setOnClickListener(this)
        views.ll9.setOnClickListener(this)
        views.ll11.setOnClickListener(this)
        views.ll12.setOnClickListener(this)
        views.ivVm.setOnClickListener(this)
        views.ivCall.setOnClickListener(this)
        views.ivDelete.setOnClickListener(this)
        views.ivHangUp.setOnClickListener(this)

        views.ll0.setOnLongClickListener(this)

        core.addListener(outgoingCallCoreListener)
        core.isVideoCaptureEnabled = true
        core.isVideoDisplayEnabled = true

        getRegisterSIPUser()
    }

    override fun onLongClick(view: View?): Boolean {

        when (view!!.id) {
            R.id.ll_0 -> {
                fullAccount += views.tv0Char.text.toString()
            }
            else -> {
            }
        }
        views.etAccount.setText(fullAccount)
        return true
    }

    private fun getRegisterSIPUser() {
        dialerSession.accountListInfo!!.forEach {
            if (it.type_value!!.lowercase() == "sip" && it.enabled && it.extension.isConnected) {
                accountList.add(it)
            }
        }
    }

    private fun call(){
        if (accountList.size > 0) {
            if (accountList.size == 1) {
                setDefaultAccount()
                directlyToCall(false)
//                outgoingCall()
            } else {
                val callPopupWindow = SipAccountPopup(this, mBus, accountList, true, false)
                callPopupWindow!!.showOnAnchor(views.ivSwitchAccount, RelativePopupWindow.VerticalPosition.BELOW,
                        RelativePopupWindow.HorizontalPosition.ALIGN_RIGHT, true)
            }
        }
    }

    private fun vmCall(){
        if (accountList.size > 0) {
            if (accountList.size == 1) {
                if (!TextUtils.isEmpty(accountList[0].voice_mail)) {
                    setDefaultAccount()
                    directlyToCall(true)
                }
            } else {
                val callPopupWindow = SipAccountPopup(this, mBus, accountList, true, true)
                callPopupWindow!!.showOnAnchor(views.ivSwitchAccount, RelativePopupWindow.VerticalPosition.BELOW,
                        RelativePopupWindow.HorizontalPosition.ALIGN_RIGHT, true)
            }
        }
    }

    private fun setDefaultAccount() {
        val mAccount = accountList[0]
        selectedAccount = accountList[0]
        val list = core.accountList
        for (account in list) {
            val domain = account.findAuthInfo()!!.domain.toString()
            val username = account.findAuthInfo()!!.username
            if (username == mAccount.username && domain == mAccount.domain) {
                core.defaultAccount = account
                break
            }
        }
    }

    @Subscribe
    fun onSetDefaultEvent(event: SetDefaultAccountEvent){
        selectedAccount = event.item
        Timber.e("selected account--${selectedAccount.username}")
        val list = core.accountList
        for (account in list) {
            val domain = account.findAuthInfo()!!.domain.toString()
            val username = account.findAuthInfo()!!.username
            if (username == selectedAccount.username && domain == selectedAccount.domain) {
                core.defaultAccount = account
                break
            }
        }
        if (event.isVM) {
            if (!TextUtils.isEmpty(selectedAccount.voice_mail)) {
                directlyToCall(true)
            }
        } else {
            if (!TextUtils.isEmpty(views.etAccount.text.toString())) {
                if (event.flag) {
                    //outgoingCall()
                    directlyToCall(false)
                }
            }
        }
    }

    private fun directlyToCall(isVM: Boolean) {
        var proxy = ""
        if (TextUtils.isEmpty(selectedAccount.extension.outProxy)) {
            proxy = selectedAccount.domain!!
        } else {
            proxy = selectedAccount.extension.outProxy!!
        }

        val intent = Intent(this, DialerCallActivity::class.java)
        intent.putExtra("index", 1)
        if (isVM) {
            intent.putExtra("remote_user", selectedAccount.voice_mail)
        } else {
            intent.putExtra("remote_user", views.etAccount.text.toString())
        }
        intent.putExtra("local_user", selectedAccount.username)
        intent.putExtra("domain", proxy)
//        intent.putExtra("domain", selectedAccount.domain)
        intent.putExtra("proxy", proxy)
        /*intent.putExtra("account", selectedAccount)
        intent.putExtra("callNumber", dialerNumber)*/
        startActivity(intent)

        views.etAccount.setText("")
        fullAccount = ""
    }

    private fun outgoingCall() {

        views.ivHangUp.visibility = View.VISIBLE
        views.ivCall.visibility = View.GONE

        var proxy = ""
        if (TextUtils.isEmpty(selectedAccount.extension.outProxy)) {
            proxy = selectedAccount.domain!!
        } else {
            proxy = selectedAccount.extension.outProxy!!
        }

        // As for everything we need to get the SIP URI of the remote and convert it to an Address
        //val remoteSipUri = "sip:" + binding.etAccount.text.toString() + "@" + Contants.Proxy_Domain
        //val remoteSipUri = "sip:" + binding.etAccount.text.toString() + "@" + proxy
        val remoteSipUri = "sip:" + views.etAccount.text.toString() + "@" + proxy
//        val remoteSipUri = "sip:" + binding.etAccount.text.toString() + "@" + Contants.Proxy_Domain
//        val remoteSipUri = "sip:" + binding.etAccount.text.toString() + "@" + Contants.Sip_Domain
        Timber.tag("sip uri").e(remoteSipUri)
        val remoteAddress = Factory.instance().createAddress(remoteSipUri)
        remoteAddress ?: return // If address parsing fails, we can't continue with outgoing call process

        // We also need a CallParams object
        // Create call params expects a Call object for incoming calls, but for outgoing we must use null safely
        val params = core.createCallParams(null)
        params ?: return // Same for params

        // Here we ask for no encryption but we could ask for ZRTP/SRTP/DTLS
        params.mediaEncryption = MediaEncryption.SRTP
        // If we wanted to start the call with video directly
        //params.enableVideo(true)

        core.interpretUrl(views.etAccount.text.toString())

        // Finally we start the call
        core.inviteAddressWithParams(remoteAddress, params)
        // Call process can be followed in onCallStateChanged callback from core listener

        //reset number
        views.etAccount.setText("")
    }

    private fun hangUp() {

        if (core.callsNb == 0) return

        // If the call state isn't paused, we can get it using core.currentCall
        val call = if (core.currentCall != null) core.currentCall else core.calls[0]
        call ?: return

        // Terminating a call is quite simple
        call.terminate()

        views.ivHangUp.visibility = View.GONE
        views.ivCall.visibility = View.VISIBLE
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.iv_vm-> {

                vmCall()
            }
            R.id.iv_call-> {
                if (TextUtils.isEmpty(views.etAccount.text.toString())) {
                    showToast("Please input number")
                    return
                }
                call()
            }
            R.id.rl_back-> {
                finish()
            }
            R.id.iv_hang_up-> {
                hangUp()
            }
            R.id.iv_switch_account-> {
                val mPopupWindow = SipAccountPopup(this, mBus, accountList, false, false)
                mPopupWindow!!.showOnAnchor(views.ivSwitchAccount, RelativePopupWindow.VerticalPosition.BELOW,
                        RelativePopupWindow.HorizontalPosition.ALIGN_RIGHT, true)
            }
            R.id.ll_0-> {
                fullAccount += views.tv0Num.text.toString()
                views.etAccount.setText(fullAccount)
            }
            R.id.ll_1-> {
                fullAccount += views.tv1Num.text.toString()
                views.etAccount.setText(fullAccount)
            }
            R.id.ll_2-> {
                fullAccount += views.tv2Num.text.toString()
                views.etAccount.setText(fullAccount)
            }
            R.id.ll_3-> {
                fullAccount += views.tv3Num.text.toString()
                views.etAccount.setText(fullAccount)
            }
            R.id.ll_4-> {
                fullAccount += views.tv4Num.text.toString()
                views.etAccount.setText(fullAccount)
            }
            R.id.ll_5-> {
                fullAccount += views.tv5Num.text.toString()
                views.etAccount.setText(fullAccount)
            }
            R.id.ll_6-> {
                fullAccount += views.tv6Num.text.toString()
                views.etAccount.setText(fullAccount)
            }
            R.id.ll_7-> {
                fullAccount += views.tv7Num.text.toString()
                views.etAccount.setText(fullAccount)
            }
            R.id.ll_8-> {
                fullAccount += views.tv8Num.text.toString()
                views.etAccount.setText(fullAccount)
            }
            R.id.ll_9-> {
                fullAccount += views.tv9Num.text.toString()
                views.etAccount.setText(fullAccount)
            }
            R.id.ll_11-> {
                fullAccount += views.tv11Num.text.toString()
                views.etAccount.setText(fullAccount)
            }
            R.id.ll_12-> {
                fullAccount += views.tv12Num.text.toString()
                views.etAccount.setText(fullAccount)
            }
            R.id.iv_delete-> {
                if (fullAccount.length > 1) {
                    fullAccount = fullAccount.substring(0, fullAccount.length - 1)
                } else {
                    fullAccount = ""
                }
                views.etAccount.setText(fullAccount)
            }
            else  -> {

            }
        }
    }

    private val outgoingCallCoreListener = object: CoreListenerStub() {
        override fun onAccountRegistrationStateChanged(core: Core, account: Account, state: RegistrationState, message: String) {
            // If account has been configured correctly, we will go through Progress and Ok states
            // Otherwise, we will be Failed.

            if (state == RegistrationState.Failed || state == RegistrationState.Cleared) {
                views.ivCall.isEnabled = false
            } else if (state == RegistrationState.Ok) {
                views.ivCall.isEnabled = true
            }
        }

        override fun onCallStateChanged(core: Core, call: Call, state: Call.State?, message: String) {
            //super.onCallStateChanged(core, call, state, message)
            Timber.e("call state: ${state}")
            when (state) {
                Call.State.OutgoingInit     -> {
                    // First state an outgoing call will go through
                }
                Call.State.OutgoingProgress -> {
                    // Right after outgoing init
                }
                Call.State.OutgoingRinging  -> {
                    // This state will be reached upon reception of the 180 RINGING
                }
                Call.State.Connected        -> {
                    // When the 200 OK has been received
                }
                Call.State.StreamsRunning   -> {
                    // This state indicates the call is active.
                    // You may reach this state multiple times, for example after a pause/resume
                    // or after the ICE negotiation completes
                    // Wait for the call to be connected before allowing a call update
                }
                Call.State.Paused           -> {
                    // When you put a call in pause, it will became Paused

                }
                Call.State.PausedByRemote   -> {
                    // When the remote end of the call pauses it, it will be PausedByRemote
                }
                Call.State.Updating         -> {
                    // When we request a call update, for example when toggling video
                }
                Call.State.UpdatedByRemote  -> {
                    // When the remote requests a call update
                }
                Call.State.Released         -> {
                    // Call state will be released shortly after the End state
                    views.ivHangUp.visibility = View.GONE
                    views.ivCall.visibility = View.VISIBLE
                }
                Call.State.Error            -> {

                }else                       -> {

            }

            }
        }
    }

    override fun onPause() {
        super.onPause()
        core.removeListener(outgoingCallCoreListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        core.removeListener(outgoingCallCoreListener)
    }

    private fun unregister() {
        // Here we will disable the registration of our Account
        val account = core.defaultAccount
        account ?: return

        val params = account.params
        // Returned params object is const, so to make changes we first need to clone it
        val clonedParams = params.clone()

        // Now let's make our changes
        //clonedParams.registerEnabled = false

        // And apply them
        account.params = clonedParams
    }

    private fun delete() {
        // To completely remove an Account
        val account = core.defaultAccount
        account ?: return
        core.removeAccount(account)

        // To remove all accounts use
        core.clearAccounts()
        // Same for auth info
        core.clearAllAuthInfo()
    }
}
