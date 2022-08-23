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
import com.squareup.otto.Subscribe
import im.vector.app.core.platform.VectorBaseFragment
import im.vector.app.databinding.FragmentHomeContactBinding
import im.vector.app.features.home.HomeActivity
import im.vector.app.kelare.adapter.AccountContactAdapter
import im.vector.app.kelare.network.HttpClient
import im.vector.app.kelare.network.event.GetAccountContactResponseEvent
import im.vector.app.kelare.network.event.PresenceStatusResponseEvent
import im.vector.app.kelare.network.models.AccountContactInfo

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private const val onlineStatus  = "online"
private const val nodefyType  = "nodefy"

class HomeContactFragment : VectorBaseFragment<FragmentHomeContactBinding>(), View.OnClickListener {
    private var param1: String? = null
    private var param2: String? = null


    private var contactList: ArrayList<AccountContactInfo> = ArrayList()
    private lateinit var mAdapter: AccountContactAdapter
    private var terms = ""

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
        getContacts()
    }

    private fun initView() {
        views.searchText.addTextChangedListener(textWatcher)

        initRecycler()
    }

    private fun getContacts() {
        showLoadingDialog()
        HttpClient.getAccountContact(requireActivity())
    }

    @SuppressLint("NotifyDataSetChanged")
    @Subscribe
    fun onContactEvent(event: GetAccountContactResponseEvent) {
        hideLoadingDialog()
        if (event.isSuccess) {
            contactList = event.model!!.data

            mAdapter.data.clear()
            mAdapter.data.addAll(contactList)
            mAdapter.notifyDataSetChanged()

            //check presence status
            checkStatus()
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
            startActivity(intent)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateList() {
        val filterList: ArrayList<AccountContactInfo> = ArrayList()
        contactList.forEach {
            if (it.displayname!!.contains(terms)) {
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



}
