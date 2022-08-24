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
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.squareup.otto.Subscribe
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseBottomSheetDialogFragment
import im.vector.app.databinding.BottomSheetContactAssociateBinding
import im.vector.app.kelare.adapter.AssociateContactAdapter
import im.vector.app.kelare.network.HttpClient
import im.vector.app.kelare.network.event.GetContactResponseEvent
import im.vector.app.kelare.network.models.AccountContactInfo
import kotlinx.parcelize.Parcelize
import timber.log.Timber

@Parcelize
data class ContactAssociateBottomSheetArgs(
        val type: String
) : Parcelable

@AndroidEntryPoint
class ContactAssociateBottomSheet (val type: String,val currentUserID: String, val mListener: InteractionListener) : VectorBaseBottomSheetDialogFragment<BottomSheetContactAssociateBinding>(), OnItemChildClickListener {

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): BottomSheetContactAssociateBinding {
        return BottomSheetContactAssociateBinding.inflate(inflater, container, false)
    }

    interface InteractionListener {
        fun onRefreshRelations()
    }

    private var interactionListener: InteractionListener? = null
    private lateinit var mAdapter: AssociateContactAdapter
    private var contactList: ArrayList<AccountContactInfo> = ArrayList()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.interactionListener = mListener

        initRecycler()

        getSipContact()
    }

    private fun initRecycler() {
        mAdapter = AssociateContactAdapter(requireActivity(), arrayListOf())
        views.associateList.layoutManager = LinearLayoutManager(context)
        views.associateList.adapter = mAdapter

        mAdapter.addChildClickViewIds(R.id.associateView)
        mAdapter.setOnItemChildClickListener(this)

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



}
