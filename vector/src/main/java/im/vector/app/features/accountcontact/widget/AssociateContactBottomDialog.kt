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
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
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
import im.vector.app.kelare.content.Contants
import im.vector.app.kelare.content.DialerSession
import im.vector.app.kelare.network.HttpClient
import im.vector.app.kelare.network.event.GetContactResponseEvent
import im.vector.app.kelare.network.event.UpdateContactRelationResponseEvent
import im.vector.app.kelare.network.models.AccountContactInfo
import im.vector.app.kelare.network.models.ChildrenUserInfo
import im.vector.app.kelare.network.models.ContactRelationInfo
import im.vector.app.kelare.network.models.DialerContactInfo
import im.vector.app.kelare.network.models.UpdateContactRelationInfo
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
        val mConnectionList: ArrayList<XMPPTCPConnection>, val dialerSession: DialerSession, val mList: ArrayList<AccountContactInfo>,
        val sipList:ArrayList<DialerContactInfo>, val xmppList: ArrayList<XmppContact>, val targetContact: AccountContactInfo,
        val relList: ArrayList<ContactRelationInfo>, val mListener: InteractionListener) : BottomSheetDialogFragment(),
        OnItemChildClickListener, View.OnClickListener {

    interface InteractionListener {
        fun onRefreshRelations()
    }

    private var recyclerView: RecyclerView? = null
    private var backView: TextView? = null
    private var desView: TextView? = null
    private var searchView: EditText? = null

    private var interactionListener: InteractionListener? = null
    private lateinit var mAdapter: AssociateContactAdapter

    private var contactList: ArrayList<AccountContactInfo> = ArrayList()
    private var sipContactList:ArrayList<DialerContactInfo> = ArrayList()
    private var xmppContactList: ArrayList<XmppContact> = ArrayList()
    private var selectedContact: AccountContactInfo = AccountContactInfo()
    private var filterContactList: ArrayList<AccountContactInfo> = ArrayList()
    private var relationsList: ArrayList<ContactRelationInfo> = ArrayList()

    private var terms = ""

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (activity == null) return super.onCreateDialog(savedInstanceState)
        val bottomDialog = BottomSheetDialog(activity!!, R.style.BottomSheetDialog)
        val rootView = LayoutInflater.from(activity).inflate(R.layout.bottom_sheet_contact_associate, null)
        bottomDialog.setContentView(rootView)
        bottomDialog.setCanceledOnTouchOutside(false)
        //set height
        val params = rootView.layoutParams
        params.height = (0.85 * resources.displayMetrics.heightPixels).toInt()
        rootView.layoutParams = params

        initView(rootView)

        return bottomDialog
    }

    @SuppressLint("SetTextI18n")
    private fun initView(rootView: View) {
        interactionListener = mListener

        contactList = mList
        sipContactList = sipList
        xmppContactList = xmppList
        selectedContact = targetContact
        relationsList = relList

        backView = rootView.findViewById<TextView>(R.id.backView)
        desView = rootView.findViewById<TextView>(R.id.descriptionView)
        recyclerView = rootView.findViewById<RecyclerView>(R.id.associateList)
        searchView = rootView.findViewById<EditText>(R.id.searchText)

        backView!!.setOnClickListener(this)
        searchView!!.addTextChangedListener(textWatcher)

        if (type.lowercase() == Contants.SIP_TYPE.lowercase()) {
            desView!!.text = getString(R.string.account_contact_associate) + " " + mContext.getString(R.string.account_contact_sip) + " " + getString(R.string.account_contact_sheet_account_to_admin)
        } else {
            desView!!.text = getString(R.string.account_contact_associate) + " " + type + " " + getString(R.string.account_contact_sheet_account_to_admin)
        }


        recyclerView!!.layoutManager = LinearLayoutManager(context)
        mAdapter = AssociateContactAdapter(mContext, arrayListOf())
        recyclerView!!.adapter = mAdapter

        mAdapter.addChildClickViewIds(R.id.associateView)
        mAdapter.setOnItemChildClickListener(this)

        filterData()
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
                associateContact(info)
            }
            else -> {}
        }
    }

    private fun associateContact(item: AccountContactInfo) {

        val childrenInfo = ChildrenUserInfo()
        childrenInfo.user_id = item.contacts_id
        childrenInfo.account_type = item.contacts_type
        childrenInfo.is_main = false

        val info: UpdateContactRelationInfo = UpdateContactRelationInfo()
        info.primary_user_id = selectedContact.contacts_id
        info.children_users.add(childrenInfo)

        HttpClient.updateContactRelation(mContext, info)
    }

    @Subscribe
    fun onAssociateContactEvent(event: UpdateContactRelationResponseEvent) {
        if (event.isSuccess) {
            filterContactList.forEach {
                it.isAssociate = it.contacts_id == event.model!!.flag
            }
            mAdapter.notifyDataSetChanged()
            Timber.e("associate contact success")
        } else {
            Toast.makeText(mContext, event.model!!.error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun filterData() {
        filterContactList.clear()
        if (type.lowercase() == Contants.SIP_TYPE.lowercase()) {
            contactList.forEach {
                if (it.contacts_type!!.lowercase() == Contants.SIP_TYPE.lowercase()) {
                    filterContactList.add(it)
                }
            }
        } else if (type.lowercase() == Contants.XMPP_TYPE) {
            contactList.forEach {
                if (it.contacts_type!!.lowercase() == Contants.XMPP_TYPE) {
                    filterContactList.add(it)
                }
            }
        } else if (type.lowercase() == Contants.SLACK_TYPE) {
            contactList.forEach {
                if (it.contacts_type!!.lowercase() == Contants.SLACK_TYPE) {
                    filterContactList.add(it)
                }
            }
        } else if (type.lowercase() == Contants.SKYPE_TYPE) {
            contactList.forEach {
                if (it.contacts_type!!.lowercase() == Contants.SKYPE_TYPE) {
                    filterContactList.add(it)
                }
            }
        }else if (type.lowercase() == Contants.TELEGRAM_TYPE) {
            contactList.forEach {
                if (it.contacts_type!!.lowercase() == Contants.TELEGRAM_TYPE) {
                    filterContactList.add(it)
                }
            }
        }else if (type.lowercase() == Contants.WHATSAPP_TYPE) {
            contactList.forEach {
                if (it.contacts_type!!.lowercase() == Contants.WHATSAPP_TYPE) {
                    filterContactList.add(it)
                }
            }
        }

        if (relationsList.isNotEmpty()) {
            relationsList.forEach {
                filterContactList.forEach { item ->
                    if (it.account_type!!.lowercase() == item.contacts_type!!.lowercase() && it.user_id == item.contacts_id) {
                        item.isAssociate = true
                        return@forEach
                    }
                }
            }
        }

        mAdapter.data.clear()
        mAdapter.data.addAll(filterContactList)
        mAdapter.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateList() {
        val list: ArrayList<AccountContactInfo> = ArrayList()
        filterContactList.forEach {
            if (it.displayname!!.contains(terms.lowercase())) {
                list.add(it)
            }
        }

        mAdapter.data.clear()
        mAdapter.data.addAll(list)
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

    override fun onDestroy() {
        super.onDestroy()
        try {
            mBus.unregister(this)
        } catch (e: Exception) {
        }
    }


}
