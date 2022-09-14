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

package im.vector.app.features.accountcontact

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.kaopiz.kprogresshud.KProgressHUD
import com.squareup.otto.Subscribe
import im.vector.app.R
import im.vector.app.core.di.ActiveSessionHolder
import im.vector.app.core.platform.VectorBaseFragment
import im.vector.app.databinding.FragmentHomeContactBinding
import im.vector.app.features.accountcontact.event.RefreshContactEvent
import im.vector.app.features.home.HomeActivity
import im.vector.app.kelare.adapter.AccountContactAdapter
import im.vector.app.kelare.content.Contants
import im.vector.app.kelare.network.HttpClient
import im.vector.app.kelare.network.event.GetAccountContactResponseEvent
import im.vector.app.kelare.network.event.GetAllContactRelationResponseEvent
import im.vector.app.kelare.network.event.GetContactResponseEvent
import im.vector.app.kelare.network.event.PresenceStatusResponseEvent
import im.vector.app.kelare.network.models.AccountContactInfo
import im.vector.app.kelare.network.models.ContactRelationInfo
import im.vector.app.kelare.network.models.DialerAccountInfo
import im.vector.app.kelare.network.models.DialerContactInfo
import im.vector.app.kelare.network.models.XmppContact
import org.jivesoftware.smack.roster.Roster
import org.matrix.android.sdk.api.session.Session
import timber.log.Timber
import java.io.Serializable
import javax.inject.Inject

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private const val onlineStatus  = "online"
private const val nodefyType  = "nodefy"

class HomeContactFragment : VectorBaseFragment<FragmentHomeContactBinding>(), View.OnClickListener {
    private var param1: String? = null
    private var param2: String? = null

    private var contactList: ArrayList<AccountContactInfo> = ArrayList()
    private var filterContactList: ArrayList<AccountContactInfo> = ArrayList()
    private lateinit var mAdapter: AccountContactAdapter
    private var terms = ""
    private var sipContactList:ArrayList<DialerContactInfo> = ArrayList()
    private var xmppContactList: ArrayList<XmppContact> = ArrayList()
    private var allRelationsList: ArrayList<ContactRelationInfo> = ArrayList()
    private var loading: KProgressHUD? = null
    private var isAlreadyRequest = false

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) =
            FragmentHomeContactBinding.inflate(inflater, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        //getContacts()
    }

    override fun onResume() {
        super.onResume()

        try {
            mBus.register(this)
            getContacts()
        } catch (e: Exception) {
            getContacts()
        }

    }

    private fun initView() {
        views.searchText.addTextChangedListener(textWatcher)

        initRecycler()
    }

    private fun getContacts() {
        showLoading()
        Timber.e("contact fragment resume")
        HttpClient.getAccountContact(this@HomeContactFragment.vectorBaseActivity)
        /*if (!isAlreadyRequest) {
            Timber.e("contact fragment resume")
            isAlreadyRequest = true
            HttpClient.getAccountContact(this@HomeContactFragment.vectorBaseActivity)
        }*/

    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe
    fun onContactEvent(event: GetAccountContactResponseEvent) {
        hideLoading()
        contactList.clear()
        if (event.isSuccess) {
            val mList = event.model!!.data
            contactList.addAll(mList)

            getXmppContact()
            getSipContact()
        }
    }

    private fun checkStatus() {
        contactList.forEach {
            if (it.contacts_type == nodefyType) {
                HttpClient.getPresenceStatus(this@HomeContactFragment.vectorBaseActivity, it.contacts_id!!)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe
    fun onPresenceStatusEvent(event:PresenceStatusResponseEvent) {
        if (event.isSuccess) {
            contactList.forEach {
                if (event.model!!.flag == it.contacts_id) {
                    it.isOnline = event.model!!.presence == onlineStatus
                }
            }
        }
        mAdapter.notifyDataSetChanged()
    }

    override fun onClick(v: View?) {

    }

    private fun initRecycler() {
        mAdapter = AccountContactAdapter(requireActivity(), arrayListOf())
        views.contactListView.layoutManager = LinearLayoutManager(context)
        views.contactListView.adapter = mAdapter
        mAdapter.setOnItemClickListener { adapter, view, position ->
            val intent = Intent(context, AccountContactDetailActivity::class.java)
            intent.putExtra("contactList", contactList as Serializable)
            intent.putExtra("sipContactList", sipContactList as Serializable)
            intent.putExtra("xmppContactList", xmppContactList as Serializable)
            intent.putExtra("item", mAdapter.getItem(position) as Serializable)
            requireActivity().startActivity(intent)
            //startActivity(intent)
        }
        views.searchText.setText("")
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateList() {
        val filterList: ArrayList<AccountContactInfo> = ArrayList()
//        contactList.forEach {
        filterContactList.forEach {
            if (it.displayname!!.contains(terms.lowercase())) {
                filterList.add(it)
            }
        }

        mAdapter.data.clear()
        mAdapter.data.addAll(filterList)
        mAdapter.notifyDataSetChanged()
    }

    private val textWatcher: TextWatcher =  object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            terms = s.toString()
            if (s.toString().isEmpty()) {
                terms = ""
            }
            updateList()
        }
        override fun afterTextChanged(s: Editable?) {
        }
    }

    private fun getSipContact() {

        //showLoadingDialog()
        HttpClient.getDialerContact(this@HomeContactFragment.vectorBaseActivity, dialerSession.userID)
    }

    @Subscribe
    fun onGetSipContactEvent(event: GetContactResponseEvent) {
        //hideLoadingDialog()
        //hideLoading()
        if (event.isSuccess) {
            Timber.e("contact page info: ${event.model!!.data}")
            sipContactList = event.model!!.data

            sipContactList.forEach {
                val contactInfo: AccountContactInfo = AccountContactInfo()
                contactInfo.contacts_id = it.id
//                contactInfo.contacts_type = Contants.SIP_TYPE.lowercase()
                contactInfo.contacts_type = Contants.SIP_TYPE
                contactInfo.displayname = it.first_name
                contactInfo.avatar_url = ""
                contactInfo.isOnline = false

                contactList.add(contactInfo)
            }

            /*mAdapter.data.clear()
            mAdapter.data.addAll(contactList)
            mAdapter.notifyDataSetChanged()*/

            //get all contact relations
            getAllRelations()
            //check presence status
            //checkStatus()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getXmppContact() {
        /*contactList.forEach {
            if (it.contacts_type == Contants.XMPP_TYPE) {
                contactList.remove(it)
            }
        }*/
        if (mConnectionList.isNotEmpty()) {
            xmppContactList.clear()
            Timber.e("mConnectionList size: ${mConnectionList.size}")
            for (connection in mConnectionList) {
                val roster = Roster.getInstanceFor(connection)
                Timber.e("roster entries  size: ${roster.entries.size}")
                for (entry in roster.entries) {
                    val xContact = XmppContact()
                    xContact.jid = entry.jid
                    xContact.isAvailable = roster!!.getPresence(entry.jid).isAvailable
                    xContact.login_user_jid = connection.user.asBareJid().toString()
                    xContact.login_user = connection.user.split("@")[0]
                    xContact.login_account = getAccountName(xContact.login_user_jid!!)

                    xmppContactList.add(xContact)
                }
            }

            xmppContactList.forEach {
                val contactInfo: AccountContactInfo = AccountContactInfo()
                contactInfo.contacts_id = it.jid.toString()
                contactInfo.contacts_type = Contants.XMPP_TYPE
                contactInfo.displayname = it.jid.toString()
                contactInfo.avatar_url = ""
                contactInfo.isOnline = false

                contactList.add(contactInfo)
            }
        }
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

    private fun getAllRelations() {
        //showLoading()
        HttpClient.getAllContactRelations(this@HomeContactFragment.vectorBaseActivity)
    }

    @Subscribe
    fun onAllRelationsEvent(event: GetAllContactRelationResponseEvent) {
        //hideLoading()
        if (event.isSuccess) {
            allRelationsList.clear()
            filterContactList.clear()
            allRelationsList = event.model!!.related_contacts

            contactList.forEach { item ->
                var isExist = false
                allRelationsList.forEach {
                    if (it.user_id == item.contacts_id) {
                        isExist = true
                        return@forEach
                    }
                }
                if (!isExist) {
                  filterContactList.add(item)
                }
            }

            mAdapter.data.clear()
            mAdapter.data.addAll(filterContactList)
            mAdapter.notifyDataSetChanged()

            //check presence status
            //checkStatus()
        }
    }

    @Subscribe
    fun refreshData(event: RefreshContactEvent) {
        Timber.e("get the refresh event")
        //getContacts()
    }


    override fun onPause() {
        super.onPause()
        try {
            mBus.unregister(this)
            isAlreadyRequest = false
        } catch (e: Exception) {
        }
    }

    private fun showLoading() {
        if (loading == null || !loading!!.isShowing) {
            loading = KProgressHUD.create(activity)
                    .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setDimAmount(0.5f)
            loading!!.show()
        }
    }

    private fun hideLoading() {
        if (loading != null && loading!!.isShowing) {
            loading!!.dismiss()
            loading = null
        }
    }


}
