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

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseFragment
import im.vector.app.databinding.FragmentHistoryBinding
import im.vector.app.databinding.FragmentPeopleMessageBinding
import im.vector.app.kelare.adapter.PeopleChatRoomAdapter
import im.vector.app.kelare.adapter.RecyclerItemClickListener
import im.vector.app.kelare.greendao.DaoUtils
import im.vector.app.kelare.network.models.PeopleRoom
import im.vector.app.kelare.network.models.XmppContact
import org.jxmpp.jid.impl.JidCreate

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class PeopleMessageFragment : VectorBaseFragment<FragmentPeopleMessageBinding>() , RecyclerItemClickListener, View.OnClickListener{
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) =
            FragmentPeopleMessageBinding.inflate(inflater, container, false)

    private var mAdapter: PeopleChatRoomAdapter? = null
    private var peopleRoomList: ArrayList<PeopleRoom> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onResume() {
        super.onResume()

        getChatRoom()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }

    private fun initView() {

        views!!.recyclerPeopleRoom.layoutManager = LinearLayoutManager(activity)
        mAdapter = PeopleChatRoomAdapter(requireActivity(),this)
        views!!.recyclerPeopleRoom.adapter = mAdapter
    }

    private fun getChatRoom(){

        peopleRoomList.clear()
        val roomList = DaoUtils.queryAllPeopleRoom(daoSession)
        if (roomList.isEmpty()) {
            refreshChatRoom()
            return
        }
        if (mConnectionList.isEmpty()) {
            refreshChatRoom()
            return
        }

        for (peopleRoom in roomList) {
            for (xmpptcpConnection in mConnectionList) {
                if (peopleRoom.login_account_jid == xmpptcpConnection.user.asBareJid().toString()) {
                    val roomID = peopleRoom.login_account_jid + peopleRoom.chat_with_jid
                    val filterSipList = DaoUtils.queryPeopleMsgById(daoSession, roomID)
                    if (filterSipList.isNotEmpty()) {
                        peopleRoom.latest_message = filterSipList[filterSipList.size - 1].message
                    } else {
                        peopleRoom.latest_message = ""
                    }
                    peopleRoomList.add(peopleRoom)
                }
            }
        }

        refreshChatRoom()
    }

    private fun refreshChatRoom() {
        mAdapter!!.clearDataList()
        mAdapter!!.addDataList(peopleRoomList)
        mAdapter!!.notifyDataSetChanged()
    }

    override fun onRecyclerViewItemClick(view: View, position: Int) {

        var contact = XmppContact()
        contact.login_user_jid = peopleRoomList[position].login_account_jid
        contact.login_user = peopleRoomList[position].login_account_jid.split("@")[0]
        contact.jid = JidCreate.bareFrom(peopleRoomList[position].chat_with_jid)
        contact.isAvailable = false
        contact.login_account = ""

        val intent = Intent(activity, PeopleChatMessageActivity::class.java)
        intent.putExtra("contact", contact)
        intent.putExtra("index", 2)
        startActivity(intent)
    }

    override fun onClick(v: View?) {

    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                PeopleMessageFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}
