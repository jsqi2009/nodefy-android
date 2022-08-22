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
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.otto.Subscribe
import im.vector.app.AppStateHandler
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseFragment
import im.vector.app.core.resources.ColorProvider
import im.vector.app.databinding.FragmentDialerBinding
import im.vector.app.databinding.FragmentHomeContactBinding
import im.vector.app.features.accountcontact.util.AvatarRendererUtil
import im.vector.app.features.call.webrtc.WebRtcCallManager
import im.vector.app.features.home.AvatarRenderer
import im.vector.app.features.popup.PopupAlertManager
import im.vector.app.features.settings.VectorPreferences
import im.vector.app.kelare.adapter.AccountContactAdapter
import im.vector.app.kelare.network.HttpClient
import im.vector.app.kelare.network.event.GetAccountContactResponseEvent
import im.vector.app.kelare.network.models.AccountContactInfo
import org.matrix.android.sdk.api.session.Session
import javax.inject.Inject

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeContactFragment : VectorBaseFragment<FragmentHomeContactBinding>(), View.OnClickListener {
    private var param1: String? = null
    private var param2: String? = null

    private var contactList: ArrayList<AccountContactInfo> = ArrayList()
    private lateinit var mAdapter: AccountContactAdapter

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

        initRecycler()

        getContacts()
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
        }
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

}
