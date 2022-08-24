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

package im.vector.app.features.accountcontact.widget

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.squareup.otto.Subscribe
import im.vector.app.R
import im.vector.app.kelare.adapter.AssociateContactAdapter
import im.vector.app.kelare.adapter.SelectDialerContactAdapter
import im.vector.app.kelare.content.AndroidBus
import im.vector.app.kelare.content.DialerSession
import im.vector.app.kelare.network.HttpClient
import im.vector.app.kelare.network.event.GetContactResponseEvent
import im.vector.app.kelare.network.models.AccountContactInfo
import im.vector.app.kelare.network.models.DialerContactInfo
import im.vector.app.kelare.network.models.XmppContact
import org.jivesoftware.smack.roster.Roster
import org.jivesoftware.smack.tcp.XMPPTCPConnection
import timber.log.Timber

/**
 * author : Jason
 *  date   : 2022/8/24 15:39
 *  desc   :
 */
class AssociateContactBottomDialog (val mContext: Context, val mBus: AndroidBus, val type: String, val currentUserID: String,
                                    val mConnectionList: ArrayList<XMPPTCPConnection>, val dialerSession: DialerSession, val mListener: InteractionListener) : BottomSheetDialogFragment(),
        OnItemChildClickListener, View.OnClickListener {

    interface InteractionListener {
        fun onRefreshRelations()
    }

    private var recyclerView: RecyclerView? = null
    private var backView: TextView? = null
    private var desView: TextView? = null

    private var interactionListener: InteractionListener? = null
    private lateinit var mAdapter: AssociateContactAdapter

    private var contactList: ArrayList<AccountContactInfo> = ArrayList()

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (activity == null) return super.onCreateDialog(savedInstanceState)
        val bottomDialog = BottomSheetDialog(activity!!, R.style.BottomSheetDialog)
        val rootView = LayoutInflater.from(activity).inflate(R.layout.bottom_sheet_contact_associate, null)
        bottomDialog.setContentView(rootView)
        bottomDialog.setCanceledOnTouchOutside(false)
        //设置宽度
        val params = rootView.layoutParams
        params.height = (0.85 * resources.displayMetrics.heightPixels).toInt()
        rootView.layoutParams = params

        initView(rootView)

        return bottomDialog
    }

    @SuppressLint("SetTextI18n")
    private fun initView(rootView: View) {
        interactionListener = mListener

        backView = rootView.findViewById<TextView>(R.id.backView)
        desView = rootView.findViewById<TextView>(R.id.descriptionView)
        recyclerView = rootView.findViewById<RecyclerView>(R.id.associateList)

        backView!!.setOnClickListener(this)

        desView!!.text = getString(R.string.account_contact_associate) + " " + type + " " + getString(R.string.account_contact_sheet_account_to_admin)

        recyclerView!!.layoutManager = LinearLayoutManager(context)
        mAdapter = AssociateContactAdapter(mContext, arrayListOf())
        recyclerView!!.adapter = mAdapter

        mAdapter.addChildClickViewIds(R.id.associateView)
        mAdapter.setOnItemChildClickListener(this)

        if (type.lowercase() == "sip") {
            getSipContact()
        } else if (type.lowercase() == "xmpp") {
            getXmppContact()
        } else {

        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.backView    -> {
                interactionListener!!.onRefreshRelations()
                dismiss()
            }
            else -> {}
        }
    }

    override fun onItemChildClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
        val info = mAdapter.getItem(position)
        when (view.id) {
            R.id.associateView    -> {
                Timber.e("clicked the associate")
            }
            else -> {}
        }
    }

    private fun getSipContact() {

        HttpClient.getDialerContact(requireActivity(), currentUserID)
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe
    fun onGetContactEvent(event: GetContactResponseEvent) {
        if (event.isSuccess) {
            contactList.clear()
            Timber.e("info: ${event.model!!.data}")

            val sipList = event.model!!.data
            sipList.forEach {
                val contactInfo: AccountContactInfo = AccountContactInfo()
                contactInfo.contacts_id = it.id
                contactInfo.contacts_type = "sip"
                contactInfo.displayname = it.first_name
                contactInfo.avatar_url = ""
                contactInfo.isOnline = false

                contactList.add(contactInfo)
            }

            mAdapter.data.clear()
            mAdapter.data.addAll(contactList)
            mAdapter.notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getXmppContact() {
        contactList.clear()
        if (mConnectionList.isNotEmpty()) {
            Timber.e("mConnectionList size: ${mConnectionList.size}")
            for (connection in mConnectionList) {
                val roster = Roster.getInstanceFor(connection)
                Timber.e("roster entries  szie: ${roster.entries.size}")
                for (entry in roster.entries) {

                    val contactInfo: AccountContactInfo = AccountContactInfo()
                    contactInfo.contacts_id = entry.jid.toString()
                    contactInfo.contacts_type = "xmpp"
                    contactInfo.displayname = entry.jid.toString()
                    contactInfo.avatar_url = ""
                    contactInfo.isOnline = roster!!.getPresence(entry.jid).isAvailable

                    contactList.add(contactInfo)

                    /*val xContact = XmppContact()
                    xContact.jid = entry.jid
                    xContact.isAvailable = roster!!.getPresence(entry.jid).isAvailable
                    xContact.login_user_jid = connection.user.asBareJid().toString()
                    xContact.login_user = connection.user.split("@")[0]
                    xContact.login_account = getAccountName(xContact.login_user_jid!!)*/

                }
                Timber.e("xmpp contact list: ${Gson().toJson(contactList)}")
            }

            mAdapter.data.clear()
            mAdapter.data.addAll(contactList)
            mAdapter.notifyDataSetChanged()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBus.register(this)
    }

    override fun onStart() {
        super.onStart()
        val view: FrameLayout = dialog?.findViewById(R.id.design_bottom_sheet)!!
        val behavior = BottomSheetBehavior.from(view)
        behavior.peekHeight = 3000
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }


}
