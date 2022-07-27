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

package im.vector.app.kelare.message

import SelectedDialerContactEvent
import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.labo.kaji.relativepopupwindow.RelativePopupWindow
import com.squareup.otto.Subscribe
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivitySendMessageBinding
import im.vector.app.kelare.adapter.RecyclerItemClickListener
import im.vector.app.kelare.adapter.RoomMessageAdapter
import im.vector.app.kelare.greendao.DaoUtils
import im.vector.app.kelare.message.widget.SMSContactBottomDialog
import im.vector.app.kelare.message.widget.SelectSendMsgAccountPopup
import im.vector.app.kelare.network.HttpClient
import im.vector.app.kelare.network.HttpClient.mSession
import im.vector.app.kelare.network.event.GetContactResponseEvent
import im.vector.app.kelare.network.event.SetDefaultSMSAccountEvent
import im.vector.app.kelare.network.models.DialerAccountInfo
import im.vector.app.kelare.network.models.DialerContactInfo
import im.vector.app.kelare.network.models.SipChatRoom
import im.vector.app.kelare.network.models.SipMessage
import org.linphone.core.ChatMessage
import org.linphone.core.ChatMessageListenerStub
import org.linphone.core.ChatRoom
import org.linphone.core.ChatRoomBackend
import org.linphone.core.ChatRoomCapabilities
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.Factory
import timber.log.Timber
import java.util.Locale

@AndroidEntryPoint
class SendMessageActivity : VectorBaseActivity<ActivitySendMessageBinding>(), View.OnClickListener, RecyclerItemClickListener {

    override fun getBinding() = ActivitySendMessageBinding.inflate(layoutInflater)

    private var chatRoom: ChatRoom? = null
    private var sipRoom: SipChatRoom? = null
    private var chatRoomID: String? = null
    private var mAdapter: RoomMessageAdapter? = null
    //private var selectedAccount:Account? = null

    private var messageList : List<SipMessage> = ArrayList()
    private var popupWindow: SelectSendMsgAccountPopup? = null
    private var accountList:ArrayList<DialerAccountInfo> = ArrayList()
    private var contactList: ArrayList<DialerContactInfo> = ArrayList()
    private var defaultSipAccount:DialerAccountInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarWhiteColor(this)

        initView()
        getRegisterUser()
    }

    override fun onResume() {
        super.onResume()

        if (!TextUtils.isEmpty(chatRoomID)) {

            views.ivAdd.visibility = View.GONE
            getHistoryMessage()
        }
        getDialerContact()
    }

    @SuppressLint("SetTextI18n")
    private fun initView(){

        /*val accountList = core.accountList
        if (accountList.isNotEmpty()) {
            view.tvDefaultAccount.text = core.defaultAccount!!.findAuthInfo()!!.username
            val proxy = core.defaultAccount!!.params.serverAddress!!.domain
            Timber.e("proxy domain, $proxy")
            selectedAccount = core.defaultAccount
        }*/

        if (intent.hasExtra("remote_number")) {
            views.etUserContact.setText(intent.extras!!.getString("remote_number"))
        }

        if (intent.hasExtra("chat_room")) {
            sipRoom = intent.extras!!.get("chat_room") as SipChatRoom?
            chatRoomID = sipRoom!!.localUserName + sipRoom!!.localDomain + sipRoom!!.peerUserName + sipRoom!!.peerDomain
            Timber.e("sip room: $sipRoom")
            views.etUserContact.isEnabled = false
            views.etUserContact.setText(sipRoom!!.peerUserName)
            views.tvDefaultAccount.text = sipRoom!!.localUserName
        }

        views.rlBack.setOnClickListener(this)
        views.sendMessage.setOnClickListener(this)
        views.sendImage.setOnClickListener(this)
        views.ivAdd.setOnClickListener(this)
        views.tvDefaultAccount.setOnClickListener(this)

        views.messageRecycler.layoutManager = LinearLayoutManager(this)
        mAdapter = RoomMessageAdapter(this,this)
        views.messageRecycler.adapter = mAdapter

        // To be notified of the connection status of our account, we need to add the listener to the Core
        core.addListener(coreReceivedListener)

        views.etUserContact.setOnEditorActionListener(object : TextView.OnEditorActionListener{
            override fun onEditorAction(p0: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)) {

                    //view.etUserContact.flushSpans()
                    views.etUserContact.flushSpans()
                    views.etUserContact.isFocusable = true
                    views.etUserContact.requestFocus()
                    views.etUserContact.findFocus()
                    return true
                }
                return false
            }
        })
    }

    override fun onClick(item: View?) {
        when (item!!.id) {
            R.id.rl_back      -> {
                finish()
            }
            R.id.send_message -> {
                receivedList()
            }
            R.id.iv_add   -> {
                SMSContactBottomDialog(this, mBus, contactList).show(supportFragmentManager, "tag")
            }
            R.id.tv_default_account   -> {
                if (!TextUtils.isEmpty(chatRoomID)) {
                    return
                }
                if (popupWindow == null) {
                    popupWindow = SelectSendMsgAccountPopup(this, mBus, accountList)
                }
                popupWindow!!.showOnAnchor(views.tvFrom, RelativePopupWindow.VerticalPosition.BELOW,
                        RelativePopupWindow.HorizontalPosition.ALIGN_LEFT, true)
            }
            else -> {
            }
        }
    }

    @Subscribe
    fun onSelectedAccount(event: SetDefaultSMSAccountEvent){
        Timber.e("selected domain, ${event.item.username}")
        defaultSipAccount = event.item
        views.tvDefaultAccount.text = defaultSipAccount!!.username
        setDefaultAccount()
    }

    @Subscribe
    fun onSelectedContact(event: SelectedDialerContactEvent){
        Timber.e("selected contact, ${event.contactList}")
        event.contactList.forEach {
            views.etUserContact.addSpan(it, it)
        }
    }

    private fun setDefaultAccount() {
        val list = core.accountList
        for (account in list) {
            val domain = account.findAuthInfo()!!.domain.toString()
            val username = account.findAuthInfo()!!.username
            if (username == defaultSipAccount!!.username && domain == defaultSipAccount!!.domain) {
                core.defaultAccount = account
                break
            }
        }
    }

    private fun getRegisterUser() {
        mSession!!.accountListInfo!!.forEach {
            if (it.type_value!!.toLowerCase(Locale.ROOT) == "sip" && it.enabled) {
                accountList.add(it)
            }
        }

        if (!TextUtils.isEmpty(chatRoomID)) {
            accountList.forEach {
                if (it.username == sipRoom!!.localUserName && it.domain == sipRoom!!.localDomain) {
                    defaultSipAccount  = it
                    return@forEach
                }
            }
        } else {
            accountList.forEach {
                if (it.enabled && it.is_default) {
                    defaultSipAccount  = it
                    return@forEach
                }
            }
        }

        try {
            views.tvDefaultAccount.text = defaultSipAccount!!.username
            setDefaultAccount()
        } catch (e: Exception) {
        }
    }

    private fun receivedList(){
        val contactList = views.etUserContact.allReturnStringList
        if (contactList.size > 1) {
            for (index in contactList.indices) {
                Timber.e("user name, ${contactList[index]}")
                //view.etUserContact.addSpan("", "")
                sendMessage(contactList[index], true, index, contactList.size)
            }
            /*for (name in contactList) {
                Timber.e("user name, $name")
                //view.etUserContact.addSpan("", "")
                sendMessage(name, true)
            }*/
        } else if (contactList.size == 1) {
            sendMessage(contactList[0], false, 0, 1)
        }
        // Clear the message input field
        views.message.text.clear()
    }

    private fun getDialerContact() {
        showLoadingDialog()
        HttpClient.getDialerContact(this, dialerSession.userID)
    }

    @Subscribe
    fun onGetContactEvent(event: GetContactResponseEvent) {
        hideLoadingDialog()
        if (event.isSuccess) {
            Timber.e("info: ${event.model!!.data}")
            contactList = event.model!!.data
        }
    }

    private fun getHistoryMessage(){
        //按照条件查找
        val filterSipList = DaoUtils.querySipMsgByName(daoSession, chatRoomID!!)
        Timber.e("filter sip message, ${Gson().toJson(filterSipList)}")

        messageList = filterSipList

        mAdapter!!.clearDataList()
        mAdapter!!.addDataList(filterSipList)
        mAdapter!!.notifyDataSetChanged()
        //auto scroll to bottom
        views.messageRecycler.scrollToPosition(mAdapter!!.itemCount - 1)
    }

    private val coreReceivedListener = object: CoreListenerStub() {

        override fun onMessageReceived(core: Core, receivedChatRoom: ChatRoom, message: ChatMessage) {
            // We will be called in this when a message is received
            // If the chat room wasn't existing, it is automatically created by the library
            // If we already sent a chat message, the chatRoom variable will be the same as the one we already have
            if (this@SendMessageActivity.chatRoom == null) {
                if (receivedChatRoom.hasCapability(ChatRoomCapabilities.Basic.toInt())) {
                    // Keep the chatRoom object to use it to send messages if it hasn't been created yet
                    this@SendMessageActivity.chatRoom = receivedChatRoom
                    //view.remoteAddress.setText(chatRoom.peerAddress.asStringUriOnly())
                    views.remoteAddress.setText(receivedChatRoom.peerAddress.username)
                    views.remoteAddress.isEnabled = false
                }
            }

            // We will notify the sender the message has been read by us
            receivedChatRoom.markAsRead()

            val contactList = views.etUserContact.allReturnStringList
            if (contactList.size == 1) {
                for (content in message.contents) {
                    if (content.isText) {
                        insertSipMessage(content.utf8Text.toString(), false, message.fromAddress.username!!,
                                false, 0, 1)
                    }
                }
            }

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
        }
    }


    private fun sendMessage(name:String, isMultiple:Boolean, index:Int, size:Int) {

        if (TextUtils.isEmpty(views.tvDefaultAccount.text.toString())) {
            return
        }

        var mRoom:ChatRoom
        if (isMultiple) {
            mRoom = createBasicChatRoom(name, isMultiple)
        } else {
            if (chatRoom == null) {
                // We need a ChatRoom object to send chat messages in it, so let's create it if it hasn't been done yet
                createBasicChatRoom(name, isMultiple)
            }
            mRoom = chatRoom!!
        }

        val message = views.message.text.toString()
        // We need to create a ChatMessage object using the ChatRoom
        val chatMessage = mRoom!!.createMessageFromUtf8(message)
        // Then we can send it, progress will be notified using the onMsgStateChanged callback
        chatMessage.addListener(chatMessageListener)

        // Send the message
        chatMessage.send()

        insertSipMessage(message, true, name, true, index, size)

    }

    private fun createBasicChatRoom(name: String, isMultiple:Boolean): ChatRoom {
        var createdRoom: ChatRoom? = null
        val params = core.createDefaultChatRoomParams()
        params.backend = ChatRoomBackend.Basic
        //params.enableEncryption(false)
        //params.enableGroup(false)

        if (params.isValid) {
            // We also need the SIP address of the person we will chat with
            val remoteSipUri = "sip:$name@${core.defaultAccount?.params!!.serverAddress!!.domain}"
            val remoteAddress = Factory.instance().createAddress(remoteSipUri)

            if (remoteAddress != null) {
                // And finally we will need our local SIP address
                val localAddress = core.defaultAccount?.params?.identityAddress
                val room = core.createChatRoom(params, localAddress, arrayOf(remoteAddress))
                createdRoom = room
                if (room != null) {
                    if (!isMultiple) {
                        chatRoom = room
                    }
                    views.remoteAddress.isEnabled = false
                }
            }
        }
        return createdRoom!!
    }

    private fun insertSipMessage(content: String, isSend: Boolean, name: String, isInsert: Boolean,index:Int, size:Int){

        var proxy = ""
        val domain = defaultSipAccount!!.domain!!
        if (TextUtils.isEmpty(defaultSipAccount!!.extension.outProxy)) {
            proxy = defaultSipAccount!!.domain!!
        } else {
            proxy = defaultSipAccount!!.extension.outProxy!!
        }

        var info = SipMessage()

        info.chat_room_id = defaultSipAccount!!.username + domain + name + proxy
        info.received_username = name
        info.received_domain = domain
        info.send_username = defaultSipAccount!!.username
        info.send_domain = domain
        info.timestamp = System.currentTimeMillis()
        info.isSend = isSend
        info.message_text = content

        /*info.chat_room_id = selectedAccount!!.params.identityAddress!!.username + selectedAccount!!.params.identityAddress!!.domain +
                name + selectedAccount!!.params.serverAddress!!.domain
        info.received_username = name
        info.received_domain = selectedAccount!!.params.identityAddress!!.domain
        info.send_username = selectedAccount!!.params.identityAddress!!.username
        info.send_domain = selectedAccount!!.params.identityAddress!!.domain
        info.timestamp = System.currentTimeMillis()
        info.isSend = isSend
        info.message_text = content*/

        if (isInsert) {
            DaoUtils.insertSipMsg(daoSession, info)
        }

        if (isSend) {
            //only add once if send to multiple contacts
            if (index == size - 1) {
                mAdapter!!.addDataList(info)
                mAdapter!!.notifyDataSetChanged()
                views.messageRecycler.scrollToPosition(mAdapter!!.itemCount - 1)
            }
        } else {
            mAdapter!!.addDataList(info)
            mAdapter!!.notifyDataSetChanged()
            views.messageRecycler.scrollToPosition(mAdapter!!.itemCount - 1)
        }

    }

    override fun onRecyclerViewItemClick(view: View, position: Int) {

    }

    private val chatMessageListener = object: ChatMessageListenerStub() {
        override fun onMsgStateChanged(message: ChatMessage, state: ChatMessage.State?) {
            val messageView = message.userData as? View
            when (state) {
                ChatMessage.State.InProgress       -> {
                    messageView?.setBackgroundColor(getColor(R.color.yellow))
                }
                ChatMessage.State.Delivered        -> {
                    // The proxy server has acknowledged the message with a 200 OK
                    messageView?.setBackgroundColor(getColor(R.color.orange))
                }
                ChatMessage.State.DeliveredToUser  -> {
                    // User as received it
                    messageView?.setBackgroundColor(getColor(R.color.blue))
                }
                ChatMessage.State.Displayed        -> {
                    // User as read it (client called chatRoom.markAsRead()
                    messageView?.setBackgroundColor(getColor(R.color.green))
                }
                ChatMessage.State.NotDelivered     -> {
                    // User might be invalid or not registered
                    messageView?.setBackgroundColor(getColor(R.color.red))
                }
                ChatMessage.State.FileTransferDone -> {
                    // We finished uploading/downloading the file
                    if (!message.isOutgoing) {
                        views.messages.removeView(messageView)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        core.removeListener(coreReceivedListener)
    }

    override fun onPause() {
        super.onPause()
        core.removeListener(coreReceivedListener)
    }
}
