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

package im.vector.app.kelare.dialer.xmpp

import AccountInfoEvent
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
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
import im.vector.app.databinding.ActivityComingCallBinding
import im.vector.app.databinding.ActivityXmppLoginBinding
import im.vector.app.kelare.network.HttpClient
import im.vector.app.kelare.network.event.SaveAccountInfoResponseEvent
import im.vector.app.kelare.network.event.UpdateAccountInfoResponseEvent
import im.vector.app.kelare.network.models.DialerAccountInfo
import im.vector.app.kelare.network.models.SaveAccountInfo
import im.vector.app.kelare.network.models.UpdateAccountInfo
import im.vector.app.kelare.utils.XMPPLoginUtil
import im.vector.app.kelare.utils.XmppHelper
import org.apache.commons.lang3.concurrent.BasicThreadFactory
import org.jivesoftware.smack.ConnectionListener
import org.jivesoftware.smack.ReconnectionManager
import org.jivesoftware.smack.XMPPConnection
import org.jivesoftware.smack.roster.Roster
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import timber.log.Timber
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class XMPPLoginActivity : VectorBaseActivity<ActivityXmppLoginBinding>(),View.OnClickListener, ConnectionListener, SwitchButton.OnCheckedChangeListener  {

    override fun getBinding() = ActivityXmppLoginBinding.inflate(layoutInflater)

    private var xmppConnection: XMPPTCPConnection? = null
    private var roster: Roster? = null
    private var executor: ScheduledThreadPoolExecutor? = null
    private var accountInfo : DialerAccountInfo = DialerAccountInfo()
    private var isEnable = false
    private var index = ""   //1:create new account   2:update account

    //advanced info
    private var proxy : String? = null
    private var interval : String? = null
    private var isUsePing = false
    private var isVerifyTLS = false
    private var isSave = false
    private var isUpload = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarWhiteColor(this)

        index = intent.extras!!.getString("index")!!
        if (index == "2") {
            accountInfo = intent.getSerializableExtra("account") as DialerAccountInfo
            isUpload = accountInfo.is_upload
        }

        initView()
    }

    @SuppressLint("SetTextI18n")
    private fun initView(){

        views.tvTitle.text = "XMPP Account"

        views.rlBack.setOnClickListener(this)
        views.llAdvanced.setOnClickListener(this)
        views.tvSave.setOnClickListener(this)
        views.sbEnable.setOnCheckedChangeListener(this)

        if (index == "2") {
            renderDefaultData()
        } else {
            /*bindingView.etAccount.setText("pidong")
            bindingView.etUsername.setText("pidong")
            bindingView.etPassword.setText("4333657zyg")
            bindingView.etDomain.setText("jabber.hot-chilli.net")*/

            /*bindingView.etAccount.setText("kelaredemo2")
            bindingView.etUsername.setText("kelaredemo2")
            bindingView.etPassword.setText("P@55word1!")
            bindingView.etDomain.setText("chat.kelare-demo.com")*/
        }
    }

    /**
     * edit account info from setting
     */
    @SuppressLint("SetTextI18n")
    private fun renderDefaultData() {

        views.etAccount.setText(accountInfo.account_name)
        views.etUsername.setText(accountInfo.username)
        views.etPassword.setText(accountInfo.password)
        views.etDomain.setText(accountInfo.domain)
        isEnable = accountInfo.enabled
        views.sbEnable.isChecked = isEnable
        if (accountInfo.extension.isConnected) {
            views.tvConnectStatus.text = "Connected"
            views.tvConnectStatus.setTextColor(resources.getColor(R.color.green, null))
        } else {
            views.tvConnectStatus.text = "Not Connected"
            views.tvConnectStatus.setTextColor(resources.getColor(R.color.red, null))
        }

        isUsePing = accountInfo.extension.usePing
        isVerifyTLS = accountInfo.extension.verifyCert
        proxy = accountInfo.extension.proxy
        interval = accountInfo.extension.interval

        views.etAccount.addTextChangedListener(textWatcher)
        views.etUsername.addTextChangedListener(textWatcher)
        views.etDomain.addTextChangedListener(textWatcher)
        views.etPassword.addTextChangedListener(textWatcher)

        resetViewStatus()
    }

    private fun initXMPPTCPConnection(domain: String, proxyAdd: String?) {
        xmppConnection = XmppHelper().initXmppConfig(domain, proxyAdd)
        xmppConnection!!.addConnectionListener(this)

        val mReconnectionManager = ReconnectionManager.getInstanceFor(xmppConnection)
        mReconnectionManager.enableAutomaticReconnection()

        xmppConnection!!.setParsingExceptionCallback {
            //Timber.e("Exception---${it.content}")
            Timber.e("Exception message---${it.parsingException.message}")
        }
    }

    override fun onClick(view: View?) {

        when (view!!.id) {
            R.id.rl_back -> {
                finish()
            }
            R.id.tv_save -> {
                isSave = true
                updateServerAccount(isEnable)
            }
            R.id.ll_advanced -> {
                accountAdvanced()
            }
            else -> {
            }
        }
    }

    /**
     * switch enable or disable
     */
    override fun onCheckedChanged(view: SwitchButton?, isChecked: Boolean) {
        isEnable = isChecked
        hideOrShow()
        resetViewStatus()
        //login or disconnect
        if (index == "1") {
            handleCreate(isChecked)
        } else {
            handleEdit(isChecked)
        }
    }

    private fun handleCreate(isChecked: Boolean) {
        if (!validateAccountInfo()) {
            return
        }
        if (isChecked) {
            loginOrDisconnect()
        } else {
            disconnectExecutor()
            //XMPPLoginUtil(this, daoSession, mConnectionList).disconnectExecutor(accountInfo)
            //updateServerAccount(false)
        }
    }

    private fun handleEdit(isChecked: Boolean) {
        if (!validateAccountInfo()) {
            return
        }
        if (isChecked) {
            loginOrDisconnect()

        } else {
            disconnectExecutor()
            //XMPPLoginUtil(this, daoSession, mConnectionList).disconnectExecutor(accountInfo)
            //updateServerAccount(false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun disconnectExecutor() {
        var position = -1
        if (mConnectionList.isNotEmpty()) {
            for (index in mConnectionList.indices) {
                if (accountInfo.username + "@" + accountInfo.domain == mConnectionList[index].user.asBareJid().toString()) {
                    position = index
                    break
                }
            }
        }

        val mExecutor = ScheduledThreadPoolExecutor(2,
                BasicThreadFactory.Builder().namingPattern("xmpp disconnect").daemon(true)
                        .build()
        )
        mExecutor!!.schedule({
            mConnectionList[position].disconnect()

            //update mConnectionList
            mConnectionList.removeAt(position)
        }, 0, TimeUnit.MILLISECONDS)

        //update connect status
        views.tvConnectStatus.text = "Not Connected"
        views.tvConnectStatus.setTextColor(resources.getColor(R.color.red, null))

        updateServerAccount(false)
    }

    /**
     * can not edit if enable
     */
    private fun resetViewStatus() {

        if (isUpload) {
            views.etAccount.isEnabled = false
            views.etUsername.isEnabled = false
            views.etPassword.isEnabled = false
            views.etDomain.isEnabled = false
            views.sbEnable.isEnabled = false
        } else {
            views.etAccount.isEnabled = !isEnable
            views.etUsername.isEnabled = !isEnable
            views.etPassword.isEnabled = !isEnable
            views.etDomain.isEnabled = !isEnable
        }
    }

    @SuppressLint("SetTextI18n")
    private fun loginOrDisconnect() {

        showLoadingDialog()
        views.tvConnectStatus.text = "Connecting"
        initXMPPTCPConnection(views.etDomain.text.toString(), proxy)
        loginExecutor(views.etUsername.text.toString(), views.etPassword.text.toString())
    }

    private fun loginExecutor(userName: String, pwd:String) {

        if (executor == null) {
            executor = ScheduledThreadPoolExecutor(3,
                    BasicThreadFactory.Builder().namingPattern("xmpp login thread").daemon(true)
                            .build()
            )
            executor!!.schedule({
                loginOpenFire(userName, pwd)
            }, 0, TimeUnit.MILLISECONDS)
        }
    }

    private fun connectOPenFire(){
        try {
            if (xmppConnection != null) {
                if (!xmppConnection!!.isConnected) {
                    xmppConnection!!.connect()
                }
            } else {
                xmppConnection = XmppHelper().initXmppConfig(views.etDomain.text.toString(), accountInfo.extension.proxy)
                xmppConnection!!.connect()
            }
        } catch (e: Exception) {
            Timber.e("connect Exception: ${e.message}")
        }
    }

    private fun loginOpenFire(name:String, pwd:String){
        if (xmppConnection == null) {
            return
        }
        try {
            //如果没有连接openfire服务器，则连接；若已连接openfire服务器则跳过。
            if (xmppConnection!!.isConnected) {
                xmppConnection!!.login(name, pwd)
            } else {
                connectOPenFire()
                xmppConnection!!.login(name, pwd)
            }
        } catch (e: Exception) {
            Timber.e("login Exception: ${e.message}")
        }
    }

    private fun accountAdvanced() {
        val intent = Intent(this, XMPPAdvancedActivity::class.java)
        intent.putExtra("isEdit", false)
        intent.putExtra("isEnable", isEnable)
        intent.putExtra("isPing", isUsePing)
        intent.putExtra("isVerify", isVerifyTLS)
        intent.putExtra("isUpload", isUpload)
        intent.putExtra("proxy", proxy)
        intent.putExtra("interval", interval)
        startActivity(intent)
    }

    @Subscribe
    fun onAdvancedAccountEvent(event:AccountInfoEvent) {
        Timber.e("advanced proxy: ${event.proxy}")

        isUsePing = event.isPing
        isVerifyTLS = event.isVerify
        proxy = event.proxy
        interval = event.interval

        if (index == "2") {
            hideOrShow()
        }
    }

    override fun connected(connection: XMPPConnection?) {
        Timber.e("xmpp connected")
    }

    @SuppressLint("SetTextI18n")
    override fun connectionClosed() {
        Timber.e("connectionClosed")
        runOnUiThread {
            views.tvConnectStatus.text = "Not Connected"
            views.tvConnectStatus.setTextColor(resources.getColor(R.color.red, null))
        }
        xmppConnection!!.removeConnectionListener(this)
        executor = null
        //handel disconnect logic

    }

    override fun connectionClosedOnError(e: Exception?) {
        Timber.e("connectionClosedOnError")
    }

    override fun authenticated(connection: XMPPConnection?, resumed: Boolean) {
        Timber.e("xmpp authenticated")
        if (xmppConnection!!.isAuthenticated) {
            hideLoadingDialog()
            executor = null
            Timber.e("xmpp authenticated")
            Timber.e("current authenticated user: ${xmppConnection!!.user.asBareJid()}")
            if (mConnectionList.isNotEmpty()) {
                var flag = true
                for (item in mConnectionList) {
                    if (item.user == xmppConnection!!.user) {
                        flag = false
                        break
                    }
                }
                if (flag) {
                    mConnectionList.add(xmppConnection!!)
                }

            } else {
                mConnectionList.add(xmppConnection!!)
            }

            if (mConnectionList.isNotEmpty()) {
                for (item in mConnectionList) {
                    if (item.user.asBareJid().toString() == xmppConnection!!.user.asBareJid().toString()) {
                        XMPPLoginUtil(this, daoSession, mConnectionList).initMultiChat(item)
                    }
                }
            }
        } else {
            Timber.e("xmpp not Authenticated")
        }

        handelAccount()

        //finish()
    }

    @SuppressLint("SetTextI18n")
    private fun handelAccount() {

        runOnUiThread {
            views.tvConnectStatus.text = "Connected"
            views.tvConnectStatus.setTextColor(resources.getColor(R.color.green, null))

            showLoadingDialog()
            if (index == "1") {
                saveServerAccount(true)
            } else {
                updateServerAccount(true)
            }
        }
    }

    private fun saveServerAccount(isConnected: Boolean) {

        formatAccountInfo(isConnected)
        val saveAccountInfo = SaveAccountInfo()
        saveAccountInfo.sip_accounts!!.add(accountInfo)
        saveAccountInfo.primary_user_id = dialerSession.userID

        showLoadingDialog()
        HttpClient.saveDialerAccountInfo(this, saveAccountInfo)
    }

    @Subscribe
    fun onSaveAccountEvent(event: SaveAccountInfoResponseEvent) {
        hideLoadingDialog()
        if (event.isSuccess) {

        }
    }

    private fun updateServerAccount(isConnected: Boolean) {

        formatAccountInfo(isConnected)
        val updateAccountInfo = UpdateAccountInfo()
        updateAccountInfo.sip_account = accountInfo
        updateAccountInfo.primary_user_id = dialerSession.userID

        showLoadingDialog()
        HttpClient.updateDialerAccountInfo(this, updateAccountInfo)
    }

    @Subscribe
    fun onUpdateAccountEvent(event: UpdateAccountInfoResponseEvent) {
        hideLoadingDialog()
        if (event.isSuccess) {
            if (isSave) {
                finish()
            }
        }
    }

    private fun formatAccountInfo(isConnected: Boolean) {
        accountInfo.type_value = "xmpp"
        accountInfo.account_name = views.etAccount.text.toString()
        accountInfo.enabled = isEnable
        accountInfo.username = views.etUsername.text.toString()
        accountInfo.password = views.etPassword.text.toString()
        accountInfo.domain = views.etDomain.text.toString()

        accountInfo.extension.accountName = views.etAccount.text.toString()
        accountInfo.extension.username = views.etUsername.text.toString()
        accountInfo.extension.domain = views.etDomain.text.toString()
        accountInfo.extension.password = views.etPassword.text.toString()
        accountInfo.extension.enable = isEnable
        accountInfo.extension.proxy = proxy
        accountInfo.extension.usePing = isUsePing
        accountInfo.extension.interval = interval
        accountInfo.extension.verifyCert = isVerifyTLS
        accountInfo.extension.isConnected = isConnected
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

    private fun stopExecutorTimer() {
        try {
            if (executor != null) {
                executor!!.shutdownNow()
                Timber.e("stop thread")
                executor = null
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()

        stopExecutorTimer()
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
            accountInfo.enabled != isEnable -> {
                return true
            }
            accountInfo.username != views.etUsername.text.toString() -> {
                return true
            }
            accountInfo.password != views.etPassword.text.toString() -> {
                return true
            }
            accountInfo.extension.domain != views.etDomain.text.toString() -> {
                return true
            }
            accountInfo.extension.proxy != proxy -> {
                return true
            }
            accountInfo.extension.interval != interval -> {
                return true
            }
            accountInfo.extension.usePing != isUsePing -> {
                return true
            }
            accountInfo.extension.verifyCert != isVerifyTLS -> {
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


}
