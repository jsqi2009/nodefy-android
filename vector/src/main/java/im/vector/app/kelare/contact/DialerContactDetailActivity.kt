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

package im.vector.app.kelare.contact

import SelectedNumberEvent
import UpdateContactInfoEvent
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.avatarfirst.avatargenlib.AvatarGenerator
import com.labo.kaji.relativepopupwindow.RelativePopupWindow
import com.squareup.otto.Subscribe
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityDialerContactDetailBinding
import im.vector.app.databinding.ActivityOutgoingCallBinding
import im.vector.app.kelare.adapter.DialerPhoneAdapter
import im.vector.app.kelare.adapter.RecyclerItemClickListener
import im.vector.app.kelare.contact.widget.AddSIPContactDialog
import im.vector.app.kelare.contact.widget.SetDefaultNumberDialog
import im.vector.app.kelare.dialer.call.DialerCallActivity
import im.vector.app.kelare.message.SendMessageActivity
import im.vector.app.kelare.network.HttpClient
import im.vector.app.kelare.network.event.SetDefaultAccountEvent
import im.vector.app.kelare.network.event.SetDefaultCallAccountEvent
import im.vector.app.kelare.network.event.SetDefaultMessageAccountEvent
import im.vector.app.kelare.network.models.DialerAccountInfo
import im.vector.app.kelare.network.models.DialerContactInfo
import im.vector.app.kelare.network.models.PhoneInfo
import im.vector.app.kelare.widget.SipAccountPopup
import timber.log.Timber
import java.util.Locale

@AndroidEntryPoint
class DialerContactDetailActivity : VectorBaseActivity<ActivityDialerContactDetailBinding>(), View.OnClickListener, RecyclerItemClickListener {

    override fun getBinding() = ActivityDialerContactDetailBinding.inflate(layoutInflater)

    private var contactInfo:DialerContactInfo = DialerContactInfo()
    private var phoneAdapter: DialerPhoneAdapter? = null
    private var extAdapter: DialerPhoneAdapter? = null
    private var accountList:ArrayList<DialerAccountInfo> = ArrayList()
    private var selectedAccount:DialerAccountInfo = DialerAccountInfo()
    private var defaultNumber:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarWhiteColor(this)

        initView()
    }

    private fun initView() {
        contactInfo = intent.extras!!.getSerializable("info") as DialerContactInfo

        views.rlBack.setOnClickListener(this)
        views.tvEdit.setOnClickListener(this)
        views.llSetDefault.setOnClickListener(this)
        views.llMessage.setOnClickListener(this)
        views.llCall.setOnClickListener(this)

        views.recyclerPhone.layoutManager = LinearLayoutManager(this)
        phoneAdapter = DialerPhoneAdapter(this, this)
        views.recyclerPhone.adapter = phoneAdapter

        views.recyclerExt.layoutManager = LinearLayoutManager(this)
        extAdapter = DialerPhoneAdapter(this, this)
        views.recyclerExt.adapter = extAdapter

    }

    override fun onResume() {
        super.onResume()

        renderData()
        getRegisterSIPUser()
    }

    @SuppressLint("SetTextI18n")
    private fun renderData() {
        contactInfo.user_id = dialerSession.userID

        views.tvUsername.text = contactInfo.last_name + " " + contactInfo.first_name
        setDefaultNumber()

        phoneAdapter!!.clearDataList()
        phoneAdapter!!.addDataList(contactInfo.phone)
        phoneAdapter!!.notifyDataSetChanged()

        extAdapter!!.clearDataList()
        extAdapter!!.addDataList(contactInfo.online_phone)
        extAdapter!!.notifyDataSetChanged()

        views.ivAvatar.setImageDrawable(
                AvatarGenerator.AvatarBuilder(this)
                        .setLabel(contactInfo.first_name!!)
                        .setAvatarSize(120)
                        .setTextSize(30)
                        .toSquare()
                        .toCircle()
                        .setBackgroundColor(mContext.resources.getColor(R.color.room_avatar_color, null))
                        .build()
        )
    }

    private fun getRegisterSIPUser() {
        accountList.clear()
        dialerSession.accountListInfo!!.forEach {
            if (it.type_value!!.lowercase(Locale.ROOT) == "sip" && it.enabled && it.extension.isConnected) {
                accountList.add(it)
            }
        }
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.rl_back -> {
                mBus.unregister(this)
                finish()
            }
            R.id.tv_edit -> {
                AddSIPContactDialog(this, mBus, dialerSession, contactInfo).show(supportFragmentManager, "tag")
            }
            R.id.ll_message -> {
                if (core.accountList.isEmpty()) {
                    return
                }
                sendMessage()

                /*val intent = Intent(this, SendMessageActivity::class.java)
                intent.putExtra("remote_number", views.tvDefaultNumber.text.toString())
                startActivity(intent)*/
            }
            R.id.ll_call -> {
                if (core.accountList.isNotEmpty()) {
                    outgoingCall()
                } else {

                }

            }
            R.id.ll_set_default -> {
                changeDefaultAccount()
            }
            else -> {
            }
        }
    }

    private fun outgoingCall(){
        if (accountList.size > 0) {
            val filterList = checkDefaultDomain()
            if (filterList!!.isEmpty()) {
                return
            }
            if (filterList.size == 1) {
                setDefaultAccount(filterList[0])
                directlyToCall()
            } else {
                val callPopupWindow = SipAccountPopup(this, mBus, filterList, true, false, true, false)
                callPopupWindow!!.showOnAnchor(views.llCall, RelativePopupWindow.VerticalPosition.ABOVE,
                        RelativePopupWindow.HorizontalPosition.ALIGN_RIGHT, true)
            }
        }
    }

    private fun setDefaultAccount(item: DialerAccountInfo) {
        val mAccount = item
        selectedAccount = item
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

    private fun directlyToCall() {
        var proxy = ""
        if (TextUtils.isEmpty(selectedAccount.extension.outProxy)) {
            proxy = selectedAccount.domain!!
        } else {
            proxy = selectedAccount.extension.outProxy!!
        }

        val intent = Intent(this, DialerCallActivity::class.java)
        intent.putExtra("index", 1)
        intent.putExtra("remote_user", views.tvDefaultNumber.text.toString())
        intent.putExtra("local_user", selectedAccount.username)
        intent.putExtra("domain", proxy)
        intent.putExtra("proxy", proxy)
        startActivity(intent)
    }

    @Subscribe
    fun onSetDefaultCallEvent(event: SetDefaultCallAccountEvent){
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
        directlyToCall()
    }

    @Subscribe
    fun onSetDefaultMessageEvent(event: SetDefaultMessageAccountEvent){
        selectedAccount = event.item
        Timber.e("selected message account--${selectedAccount.username}")
        val list = core.accountList
        for (account in list) {
            val domain = account.findAuthInfo()!!.domain.toString()
            val username = account.findAuthInfo()!!.username
            if (username == selectedAccount.username && domain == selectedAccount.domain) {
                core.defaultAccount = account
                break
            }
        }
        directlyToMessage(event.item)
    }

    private fun setDefaultNumber() {
        contactInfo.phone!!.forEach {
            if (it.isDefault!!) {
                views.tvDefaultNumber.text = it.number
                defaultNumber = it.number
            }
        }
        contactInfo.online_phone!!.forEach {
            if (it.isDefault!!) {
                //views.tvDefaultNumber.text = it.number
                views.tvDefaultNumber.text = it.number!!.split("@")[0]
                defaultNumber = it.number
            }
        }
    }

    @Subscribe
    fun onUpdateEvent(event: UpdateContactInfoEvent) {
        try {
            contactInfo = event.info!!
            renderData()
        } catch (e: Exception) {

        }
    }

    private fun changeDefaultAccount() {

        val allNumberList:ArrayList<PhoneInfo> = ArrayList()
        contactInfo.phone!!.forEach {
            allNumberList.add(it)
        }
        contactInfo.online_phone!!.forEach {
            allNumberList.add(it)
        }

        val setDialog = SetDefaultNumberDialog(this, 2, mBus, allNumberList)
        val dialogWindow: Window = setDialog.window!!
        dialogWindow.decorView.setPadding(0,0,0,0)
        val lp = dialogWindow.attributes
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialogWindow.attributes = lp
        dialogWindow.setGravity(Gravity.BOTTOM)
        setDialog.show()
    }

    @Subscribe
    fun onSelectedNumberEvent(event: SelectedNumberEvent) {
        if (event.index == 2) {
            val selectedValue = event.selectedNum
            //views.tvDefaultNumber.text = event.selectedNum
            defaultNumber = event.selectedNum
            if (event.selectedNum.contains("@")) {
                views.tvDefaultNumber.text = event.selectedNum.split("@")[0]
            } else {
                views.tvDefaultNumber.text = event.selectedNum
            }
            Timber.e("selectedValue : ${event.selectedNum}")
            var flag = false
            contactInfo.phone!!.forEach {
                it.isDefault = false
            }
            contactInfo.online_phone!!.forEach {
                it.isDefault = false
            }

            for (phoneInfo in contactInfo.phone!!) {
                if (phoneInfo.number == event.selectedNum) {
                    phoneInfo.isDefault = true
                    flag = true
                    break
                }
            }
            if (!flag) {
                contactInfo.online_phone!!.forEach {
                    if (it.number == event.selectedNum) {
                        it.isDefault = true
                    }
                }
            }

            updateContactInfo()
        }
    }

    private fun updateContactInfo() {

        HttpClient.detailUpdateDialerContact(this, contactInfo)
    }

    override fun onRecyclerViewItemClick(view: View, position: Int) {

    }

    private fun checkDefaultDomain(): ArrayList<DialerAccountInfo>? {
        var filterAccountList:ArrayList<DialerAccountInfo> = ArrayList()

        if (!defaultNumber!!.contains("@")) {
            filterAccountList = accountList
        } else {
            filterAccountList.clear()
            val defaultDomain = defaultNumber!!.split("@")[1]
            accountList.forEach {
                if (it.domain!!.trim().trimEnd() == defaultDomain.trimEnd().trim()) {
                    filterAccountList.add(it)
                }
            }
        }
        return filterAccountList
    }

    private fun sendMessage(){
        if (accountList.size > 0) {
            val filterList = checkDefaultDomain()
            if (filterList!!.isEmpty()) {
                return
            }
            if (filterList.size == 1) {
                setDefaultAccount(filterList[0])
                directlyToMessage(filterList[0])
            } else {
                val callPopupWindow = SipAccountPopup(this, mBus, filterList, true, false, true, true)
                callPopupWindow.showOnAnchor(views.llMessage, RelativePopupWindow.VerticalPosition.ABOVE,
                        RelativePopupWindow.HorizontalPosition.ALIGN_LEFT, true)
            }
        }
    }

    private fun directlyToMessage(item: DialerAccountInfo) {
        val intent = Intent(this, SendMessageActivity::class.java)
        intent.putExtra("remote_number", views.tvDefaultNumber.text.toString())
        intent.putExtra("selected_account", item)
        startActivity(intent)
    }



}
