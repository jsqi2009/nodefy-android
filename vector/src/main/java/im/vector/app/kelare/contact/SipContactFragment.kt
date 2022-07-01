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

import RefreshDialerContactEvent
import SearchSIPContactEvent
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.mylhyl.circledialog.CircleDialog
import com.mylhyl.circledialog.view.listener.OnButtonClickListener
import com.squareup.otto.Subscribe
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseFragment
import im.vector.app.databinding.FragmentHistoryBinding
import im.vector.app.databinding.FragmentSipContactBinding
import im.vector.app.kelare.adapter.OnItemLongClickListener
import im.vector.app.kelare.adapter.RecyclerItemClickListener
import im.vector.app.kelare.adapter.SipContactAdapter
import im.vector.app.kelare.contact.widget.BottomDeleteContactDialog
import im.vector.app.kelare.network.HttpClient
import im.vector.app.kelare.network.event.DeleteContactResponseEvent
import im.vector.app.kelare.network.event.GetContactResponseEvent
import im.vector.app.kelare.network.models.ContactID
import im.vector.app.kelare.network.models.DeleteDialerContact
import im.vector.app.kelare.network.models.DialerContactInfo
import timber.log.Timber

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SipContactFragment : VectorBaseFragment<FragmentSipContactBinding>(), RecyclerItemClickListener, View.OnClickListener,
        OnItemLongClickListener, BottomDeleteContactDialog.OnItemSelected{
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) =
            FragmentSipContactBinding.inflate(inflater, container, false)

    private var mAdapter: SipContactAdapter? = null
    private var sipList: ArrayList<DialerContactInfo> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onResume() {
        super.onResume()

        getDialerContact()
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun getDialerContact() {
        //showLoadingDialog()
        HttpClient.getDialerContact(activity!!, dialerSession.userID)
    }

    @Subscribe
    fun onGetContactEvent(event: GetContactResponseEvent) {
        //hideLoadingDialog()
        if (event.isSuccess) {
            Timber.e("info: ${event.model!!.data}")

            sipList = event.model!!.data
            mAdapter!!.clearDataList()
            mAdapter!!.addDataList(sipList)
            mAdapter!!.notifyDataSetChanged()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }

    private fun initView() {
        views!!.sipContactRecycler.layoutManager = LinearLayoutManager(activity)
        mAdapter = SipContactAdapter(requireActivity(), this, this)
        views!!.sipContactRecycler.adapter = mAdapter
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onRecyclerViewItemClick(view: View, position: Int) {
        val intent = Intent(activity!!, DialerContactDetailActivity::class.java)
        intent.putExtra("info", mAdapter!!.getDataList()!![position])
        startActivity(intent)
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onItemLongClick(view: View, position: Int): Boolean {
        BottomDeleteContactDialog.showSheet(activity!!, sipList[position], this, null)
        return true
    }

    override fun onClick(p0: View?) {

    }



    @Subscribe
    fun onSearchEvent(event: SearchSIPContactEvent) {
        Timber.e("filter status: ${event.status}")
        if (event.isSearch) {
            filterData(event.status)
        }
    }

    private fun filterData(terms: String) {
        val filterList: ArrayList<DialerContactInfo> = ArrayList()
        sipList.forEach {
            if (it.first_name!!.contains(terms) || it.last_name!!.contains(terms)) {
                filterList.add(it)
            }
        }

        mAdapter!!.clearDataList()
        mAdapter!!.addDataList(filterList)
        mAdapter!!.notifyDataSetChanged()
    }

    override fun onClick(whichButton: Int, info: DialerContactInfo) {
        when (whichButton) {
            BottomDeleteContactDialog.CHOOSE_VALUE -> {
                confirmDeleteDialog(info)
            }
            BottomDeleteContactDialog.CANCEL -> {

            }
            else -> {
            }
        }
    }

    private fun confirmDeleteDialog(info: DialerContactInfo) {

        CircleDialog.Builder()
                .setTitle("Tips")
                .setTitleColor(resources.getColor(R.color.black, null))
                .configTitle() { params ->
                    params.textSize = 14
                }
                .setWidth(0.75f)
                .setText("Are you sure to delete ${info.last_name + " " + info.first_name}?")
                .setTextColor(resources.getColor(R.color.text_color_black, null))
                .configText { params ->
                    params.textSize = 12
                }
                .setPositive("Delete", object : OnButtonClickListener {
                    override fun onClick(v: View?): Boolean {
                        deleteContact(info)
                        return true
                    }
                })
                .configPositive { params ->
                    params!!.textColor = resources.getColor(R.color.red, null)
                    params!!.textSize = 14
                }
                .setNegative("Cancel") { true }
                .configNegative { params ->
                    params!!.textColor = resources.getColor(R.color.colorPrimary, null)
                    params!!.textSize = 14
                }
                .show(fragmentManager)
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun deleteContact(info: DialerContactInfo) {

        val deleteContact = DeleteDialerContact()
        val userId = ContactID()
        deleteContact.user_id = dialerSession.userID
        userId.id = info.id
        deleteContact.dialer_contacts!!.add(userId)

        showLoadingDialog()
        HttpClient.deleteDialerContact(activity!!, deleteContact)
    }

    @Subscribe
    fun onDeleteEvent(event: DeleteContactResponseEvent) {
        hideLoadingDialog()
        if (event.isSuccess) {
            getDialerContact()
        }
    }

    @Subscribe
    fun onRefreshEvent(event: RefreshDialerContactEvent) {
        getDialerContact()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                SipContactFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }



}
