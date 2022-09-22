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

package im.vector.app.kelare.history

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseFragment
import im.vector.app.databinding.FragmentAllHistoryBinding
import im.vector.app.databinding.FragmentDialerBinding
import im.vector.app.kelare.adapter.CallHistoryAdapter
import im.vector.app.kelare.adapter.RecyclerItemClickListener
import im.vector.app.kelare.dialer.call.DialerCallActivity
import org.linphone.core.Account
import org.linphone.core.CallLog
import org.linphone.core.LoggingServiceListenerStub
import org.linphone.core.RegistrationState
import timber.log.Timber

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class AllHistoryFragment : VectorBaseFragment<FragmentAllHistoryBinding>(), View.OnClickListener, RecyclerItemClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) =
            FragmentAllHistoryBinding.inflate(inflater, container, false)

    private var mAdapter: CallHistoryAdapter? = null
    private var callList: ArrayList<CallLog> = ArrayList()

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
    }

    override fun onResume() {
        super.onResume()
        getAllCallHistory()
    }

    private fun initView() {
        views!!.recyclerCall.layoutManager = LinearLayoutManager(activity)
        mAdapter = CallHistoryAdapter(requireActivity(),this)
        views!!.recyclerCall.adapter = mAdapter
    }

    private fun getAllCallHistory() {
        callList.clear()
        val allCallLogs = core.callLogs
        if (allCallLogs.isNotEmpty()) {
            allCallLogs.forEach {
                val fromDomain = it.fromAddress.domain
                val fromUser = it.fromAddress.username
                val localDomain = it.localAddress.domain
                val localUser = it.fromAddress.username
                val callDir = it.dir.toInt()   //0:outgoing    1:incoming

                Timber.e("fromDomain: $fromDomain")
                Timber.e("fromUser: $fromUser")
                Timber.e("localDomain: $localDomain")
                Timber.e("localUser: $localUser")
                Timber.e("time: ${it.startDate}")

                if (it.status.toInt() != 2) {
                    callList.add(it)
                }
            }
        }

        refreshCallHistory()


    }

    private fun refreshCallHistory() {
        mAdapter!!.clearDataList()
        mAdapter!!.addDataList(callList)
        mAdapter!!.notifyDataSetChanged()
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onRecyclerViewItemClick(view: View, position: Int) {

        val callLog = mAdapter!!.getDataList()!![position]
        val mAccount = verifyAccount(mAdapter!!.getDataList()!![position])
        if (mAccount != null) {
            val intent = Intent(activity, DialerCallActivity::class.java)
            intent.putExtra("index", 1)
            intent.putExtra("remote_user", callLog.remoteAddress.username)
            intent.putExtra("local_user", callLog.localAddress.username)
            intent.putExtra("domain", mAccount.params.serverAddress!!.domain)
            intent.putExtra("proxy", mAccount.params.serverAddress!!.domain)
            activity!!.startActivity(intent)
        } else {
            Toast.makeText(activity, "There are no available sip accounts", Toast.LENGTH_SHORT).show()
        }
    }

    private fun verifyAccount(callLog: CallLog): Account? {
        var account: Account? = null
        val accountList = core.accountList
        for (item in accountList) {
            if (item.state == RegistrationState.Ok) {
                if (item.findAuthInfo()!!.domain == callLog.localAddress.domain && item.findAuthInfo()!!.username == callLog.localAddress.username) {
                    account = item
                    break
                }
            }
        }
        return account
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                AllHistoryFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }

    override fun onClick(v: View?) {

    }
}
