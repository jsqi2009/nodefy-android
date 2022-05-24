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

package im.vector.app.kelare.message.group

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityInviteContactBinding
import im.vector.app.databinding.ActivitySendMessageBinding
import im.vector.app.kelare.adapter.InviteContactAdapter
import im.vector.app.kelare.adapter.RecyclerItemClickListener
import im.vector.app.kelare.greendao.DaoUtils
import im.vector.app.kelare.network.models.SelectGroupContact
import org.apache.commons.lang3.StringUtils
import org.jivesoftware.smack.SmackException
import org.jivesoftware.smack.XMPPException
import org.jivesoftware.smack.roster.Roster
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jivesoftware.smackx.muc.MultiUserChat
import org.jivesoftware.smackx.muc.MultiUserChatManager
import org.jxmpp.jid.impl.JidCreate
import timber.log.Timber

@AndroidEntryPoint
class InviteContactActivity : VectorBaseActivity<ActivityInviteContactBinding>(), View.OnClickListener, RecyclerItemClickListener {

    override fun getBinding() = ActivityInviteContactBinding.inflate(layoutInflater)

    private var mAdapter: InviteContactAdapter? = null
    private var groupName : String ? = null
    private var participant : String ? = null
    private var owner : String ? = null
    private var roomID : String ? = null
    private var contactList: ArrayList<SelectGroupContact> = ArrayList()
    private var mConnection: XMPPTCPConnection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarWhiteColor(this)

        initView()
    }
    @SuppressLint("SetTextI18n")
    private fun initView() {

        groupName = intent.extras!!.getString("group_name")!!
        participant = intent.extras!!.getString("participant")!!
        owner = intent.extras!!.getString("owner")!!
        roomID = intent.extras!!.getString("room_id").toString()

        views.tvRoom.text = groupName
        views.tvTitle.text = "Invite"

        views.rlBack.setOnClickListener(this)
        views.tvDone.setOnClickListener(this)

        views.contactRecycler.layoutManager = LinearLayoutManager(this)
        mAdapter = InviteContactAdapter(this,this)
        views.contactRecycler.adapter = mAdapter

    }

    override fun onResume() {
        super.onResume()

        getXmppContact()
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.rl_back -> {
                finish()
            }
            R.id.tv_done -> {
                validateData()
            }
            else -> {
            }
        }
    }

    private fun validateData() {

        val filter: ArrayList<SelectGroupContact> = ArrayList()
        for (selectGroupContact in contactList) {
            if (selectGroupContact.isSelected) {
                filter.add(selectGroupContact)
            }
        }
        if (filter.isEmpty()) {
            return
            finish()
        } else {
            inviteNewUser(filter)
        }
    }

    private fun inviteNewUser(filter: ArrayList<SelectGroupContact>) {
        var muc: MultiUserChat? = null
        try {
            muc = MultiUserChatManager.getInstanceFor(mConnection).getMultiUserChat(JidCreate.entityBareFrom(roomID))

            val contactList = ArrayList<String>()
            for (selectGroupContact in filter) {
                contactList.add(selectGroupContact.jid!!.asBareJid().toString())
            }

            Timber.e("xmpp contact list: ${StringUtils.join(contactList, ",")}")
            val contactStr = StringUtils.join(contactList, ",")
            for (item in filter) {
                muc.invite(JidCreate.entityBareFrom(item.jid), "$groupName, $contactStr")
            }

            updateGroupRoom("$participant,$contactStr")

            finish()

        } catch (e: XMPPException) {
            e.printStackTrace()
            Timber.e("XMPPException: failure")
            Timber.e("XMPPException: ${e.message}")
        } catch (e: SmackException) {
            e.printStackTrace()
            Timber.e("SmackException: failure")
            Timber.e("SmackException: ${e.message}")
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getXmppContact() {
        contactList.clear()
        if (mConnectionList.isEmpty()) {
            refreshList()
            return
        }

        val selectedUser = owner
        for (connection in mConnectionList) {
            if (selectedUser == connection.user.asBareJid().toString()) {
                mConnection = connection
                val roster = Roster.getInstanceFor(connection)
                for (entry in roster.entries) {
                    val xContact = SelectGroupContact()
                    if (!participant!!.contains(entry.jid)) {
                        xContact.jid = entry.jid
                        xContact.isAvailable = roster!!.getPresence(entry.jid).isAvailable
                        xContact.login_user_jid = connection.user.asEntityBareJidString()
                        xContact.login_user = connection.user.split("@")[0]

                        contactList.add(xContact)
                    }
                }
                Timber.e("xmpp contact list: ${Gson().toJson(contactList)}")
                break
            }
        }
        refreshList()
    }

    private fun refreshList() {
        mAdapter!!.clearDataList()
        mAdapter!!.addDataList(contactList)
        mAdapter!!.notifyDataSetChanged()
    }

    override fun onRecyclerViewItemClick(view: View, position: Int) {
        val item = contactList[position]
        for (selectGroupContact in contactList) {
            if (item.jid == selectGroupContact.jid) {
                selectGroupContact.isSelected = !selectGroupContact.isSelected
            }
        }
        mAdapter!!.notifyDataSetChanged()
    }

    private fun updateGroupRoom(contact: String) {

        DaoUtils.updateGroupChatRoom(daoSession, roomID!!, contact)
    }
}
