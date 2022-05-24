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
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityGroupCreateBinding
import im.vector.app.databinding.ActivitySendMessageBinding
import im.vector.app.kelare.adapter.RecyclerItemClickListener
import im.vector.app.kelare.adapter.SelectGroupContactAdapter
import im.vector.app.kelare.greendao.DaoUtils
import im.vector.app.kelare.network.models.DialerAccountInfo
import im.vector.app.kelare.network.models.GroupRoom
import im.vector.app.kelare.network.models.SelectGroupContact
import org.apache.commons.lang3.StringUtils
import org.jivesoftware.smack.SmackException
import org.jivesoftware.smack.XMPPException
import org.jivesoftware.smack.roster.Roster
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import org.jivesoftware.smackx.muc.MultiUserChat
import org.jivesoftware.smackx.muc.MultiUserChatManager
import org.jxmpp.jid.impl.JidCreate
import org.jxmpp.jid.parts.Resourcepart
import timber.log.Timber
import java.util.Locale

@AndroidEntryPoint
class GroupCreateActivity : VectorBaseActivity<ActivityGroupCreateBinding>(), View.OnClickListener, RecyclerItemClickListener {

    override fun getBinding() = ActivityGroupCreateBinding.inflate(layoutInflater)

    private var contactList: ArrayList<SelectGroupContact> = ArrayList()
    private var selectedAccount : DialerAccountInfo? = null
    private var groupName = ""
    private var mAdapter: SelectGroupContactAdapter? = null
    private var mConnection: XMPPTCPConnection? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarWhiteColor(this)
        initView()
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {

        selectedAccount = intent.extras!!.getSerializable("user_info")!! as DialerAccountInfo
        groupName = intent.extras!!.getString("group_name")!!

        views.tvTitle.text = "Select Users"

        views.rlBack.setOnClickListener(this)
        views.tvDone.setOnClickListener(this)

        views.contactRecycler.layoutManager = LinearLayoutManager(this)
        mAdapter = SelectGroupContactAdapter(this,this)
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
                val filter: ArrayList<SelectGroupContact> = ArrayList()
                for (selectGroupContact in contactList) {
                    if (selectGroupContact.isSelected) {
                        filter.add(selectGroupContact)
                    }
                }
                if (filter.isEmpty()) {
                    finish()
                } else {
                    try {
                        showLoadingDialog()
                        createRoomAndAddUser(filter)
                    } catch (e: Exception) {
                        hideLoadingDialog()
                        finish()
                        //Toast.makeText(this, "create failure, please try again later", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else -> {
            }
        }
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

    @SuppressLint("NotifyDataSetChanged")
    private fun getXmppContact() {
        contactList.clear()
        if (mConnectionList.isEmpty()) {
            refreshList()
            return
        }

        val selectedUser = selectedAccount!!.username + "@" + selectedAccount!!.domain
        for (connection in mConnectionList) {
            if (selectedUser == connection.user.asBareJid().toString()) {
                mConnection = connection
                val roster = Roster.getInstanceFor(connection)
                for (entry in roster.entries) {
                    val xContact = SelectGroupContact()
                    xContact.jid = entry.jid
                    xContact.isAvailable = roster!!.getPresence(entry.jid).isAvailable
                    xContact.login_user_jid = connection.user.asEntityBareJidString()
                    xContact.login_user = connection.user.split("@")[0]

                    contactList.add(xContact)
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

    private fun createRoomAndAddUser(filter: ArrayList<SelectGroupContact>){

        if (mConnection == null || !mConnection!!.isConnected || !mConnection!!.isAuthenticated) {
            return
        }
        val muc: MultiUserChat? = createGroupChatRoom(groupName, groupName)

        if (muc == null) {
            //Toast.makeText(this, "create failure, please try again later", Toast.LENGTH_SHORT).show()
            return
        }

        if (mConnection!!.isConnected && mConnection!!.isAuthenticated) {
            val contactList = ArrayList<String>()
            for (selectGroupContact in filter) {
                contactList.add(selectGroupContact.jid!!.asBareJid().toString())
            }
            contactList.add(mConnection!!.user.asBareJid().toString())
            Timber.e("xmpp filter contact list: ${StringUtils.join(contactList, ",")}")
            val contactStr = StringUtils.join(contactList, ",")

            //insert group room
            createGroupRoom(contactStr)

            for (item in filter) {
                muc.invite(JidCreate.entityBareFrom(item.jid), "$groupName, $contactStr")
            }
            /**
             * no need to join again, you create , you already in the room
             */
            //muc.join(Resourcepart.from(mConnection!!.user.asBareJid().toString()))


        }

        hideLoadingDialog()
        finish()
    }

    /**
     * 创建群聊聊天室
     * @param roomName 聊天室名字
     * @param nickName 创建者在聊天室中的昵称
     * @return
     */
    private fun createGroupChatRoom(roomName: String, nickName: String): MultiUserChat? {
        var muc: MultiUserChat? = null
        try {
            //组装群聊jid,群jid的格式：群名称@conference.openfire服务器名称
            val jid: String = roomName + "@conference." + mConnection!!.xmppServiceDomain.toString()
            //create MultiUserChat
            muc = MultiUserChatManager.getInstanceFor(mConnection).getMultiUserChat(JidCreate.entityBareFrom(jid))

            //val owners = JidUtil.jidSetFrom(arrayOf(connection.user.asBareJid().toString()))
            //var manager = MultiUserChatManager.getInstanceFor(connection)

            if (muc.isJoined) {
                Toast.makeText(this, "Already joined this room", Toast.LENGTH_SHORT).show()
                return null
            } else {

                //muc!!.createOrJoin(Resourcepart.from(mConnection!!.user.asBareJid().toString()))

                muc.create(Resourcepart.from(nickName)).makeInstant()

                //if not has the room, will have XMPPException, so need to create room in the catch block
                /*val room =  MultiUserChatManager.getInstanceFor(mConnection).getRoomInfo(JidCreate.entityBareFrom(jid))
                if (room != null) {
                    Timber.e("room info: ${room.name}")
                    //muc.join(Resourcepart.from(mConnection!!.user.asBareJid().toString()))

                    muc!!.createOrJoin(Resourcepart.from(mConnection!!.user.asBareJid().toString()))
                } else {
                    muc.create(Resourcepart.from(nickName)).makeInstant()
                }*/
            }

            /*muc.create(Resourcepart.from("test")).configFormManager
                .setRoomOwners(owners)
                .setMembersOnly(false)
                .submitConfigurationForm()*/

            return muc

        } catch (e: XMPPException) {
            e.printStackTrace()
            Timber.e("XMPPException: failure")
            Timber.e("XMPPException: ${e.message}")

            muc!!.create(Resourcepart.from(nickName)).makeInstant()

            return muc!!
        } catch (e: SmackException) {
            e.printStackTrace()
            Timber.e("SmackException: failure")
            Timber.e("SmackException: ${e.message}")

            muc!!.create(Resourcepart.from(nickName)).makeInstant()
            return muc
        }
    }

    private fun createGroupRoom(contact: String) {
        val groupRoom = GroupRoom()

        Timber.e("insert group chat room")
        groupRoom.group_room_id = groupName.toLowerCase(Locale.ROOT) + "@conference." + mConnection!!.xmppServiceDomain.toString()
        groupRoom.group_room_name = groupName.toLowerCase(Locale.ROOT)
        groupRoom.latest_message = ""
        groupRoom.message_from = ""
        groupRoom.participants = contact
        groupRoom.room_owner = mConnection!!.user.asBareJid().toString()

        DaoUtils.insertGroupChatRoom(daoSession, groupRoom)
    }
}
