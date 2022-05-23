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

import SearchXMPPContactEvent
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.squareup.otto.Subscribe
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseFragment
import im.vector.app.databinding.FragmentSipContactBinding
import im.vector.app.databinding.FragmentXmppContactBinding
import im.vector.app.kelare.adapter.RecyclerItemClickListener
import im.vector.app.kelare.adapter.XmppContactAdapter
import im.vector.app.kelare.message.PeopleChatMessageActivity
import im.vector.app.kelare.network.models.XmppContact
import org.jivesoftware.smack.roster.Roster
import timber.log.Timber
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class XmppContactFragment : VectorBaseFragment<FragmentXmppContactBinding>(), View.OnClickListener, RecyclerItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var mAdapter: XmppContactAdapter? = null
    private var contactList: ArrayList<XmppContact> = ArrayList()

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) =
            FragmentXmppContactBinding.inflate(inflater, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onResume() {
        super.onResume()

        getXmppContact("", false)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getXmppContact(terms: String, isSearch: Boolean) {
        contactList.clear()
        if (mConnectionList.isNotEmpty()) {
            Timber.e("mConnectionList size: ${mConnectionList.size}")
            for (connection in mConnectionList) {
                val roster = Roster.getInstanceFor(connection)
                Timber.e("roster entries  szie: ${roster.entries.size}")
                for (entry in roster.entries) {
                    val xContact = XmppContact()
                    xContact.jid = entry.jid
                    xContact.isAvailable = roster!!.getPresence(entry.jid).isAvailable
                    xContact.login_user_jid = connection.user.asBareJid().toString()
                    xContact.login_user = connection.user.split("@")[0]
                    xContact.login_account = getAccountName(xContact.login_user_jid!!)

                    if (!isSearch) {
                        if (TextUtils.isEmpty(terms) || terms == "all") {
                            contactList.add(xContact)
                        } else if (terms == "online") {
                            if (xContact.isAvailable!!) {
                                contactList.add(xContact)
                            }
                        } else {
                            contactList.add(xContact)
                        }
                    } else {
                        if (TextUtils.isEmpty(terms)) {
                            contactList.add(xContact)
                        } else if (xContact.jid.toString().contains(terms)) {
                            contactList.add(xContact)
                        }
                    }
                }
                Timber.e("xmpp contact list: ${Gson().toJson(contactList)}")
            }
        }

        mAdapter!!.clearDataList()
        mAdapter!!.addDataList(contactList)
        mAdapter!!.notifyDataSetChanged()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }

    private fun initView(){
        views!!.xmppContactRecycler.layoutManager = LinearLayoutManager(activity)
        mAdapter = XmppContactAdapter(requireActivity(),this)
        views!!.xmppContactRecycler.adapter = mAdapter
    }

    override fun onRecyclerViewItemClick(view: View, position: Int) {

        val contact = contactList[position]

        val intent = Intent(activity, PeopleChatMessageActivity::class.java)
        intent.putExtra("contact", contact)
        intent.putExtra("index", 1)
        startActivity(intent)

    }

    private fun getAccountName(jid: String): String? {
        var accountName = ""
        val accountList = dialerSession.accountListInfo
        accountList!!.forEach {
            if (it.username + "@" + it.domain == jid) {
                accountName = it.account_name!!
                return accountName
            }
        }
        return accountName
    }

    @Subscribe
    fun onSearchEvent(event: SearchXMPPContactEvent) {
        Timber.e("filter status: ${event.status}")

        getXmppContact(event.status.lowercase(Locale.ROOT), event.isSearch)
    }


    override fun onClick(view: View?) {

    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                XmppContactFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }


}
