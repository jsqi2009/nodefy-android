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

package im.vector.app.kelare.dialer.sip

import SelectValueEvent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivitySipEncryptBinding
import im.vector.app.databinding.ActivitySipLoginBinding
import im.vector.app.kelare.adapter.RecyclerItemClickListener
import im.vector.app.kelare.adapter.SipTransportAdapter
import im.vector.app.kelare.network.models.SipTransportInfo
import java.util.Locale

@AndroidEntryPoint
class SipEncryptActivity : VectorBaseActivity<ActivitySipEncryptBinding>(), View.OnClickListener, RecyclerItemClickListener {

    override fun getBinding() = ActivitySipEncryptBinding.inflate(layoutInflater)

    private var mAdapter: SipTransportAdapter? = null
    private val portList: ArrayList<SipTransportInfo> = ArrayList()
    private var selectedValue: String ? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarWhiteColor(this)
        initView()
    }

    private fun initView() {

        selectedValue = intent.extras!!.getString("value")
        views.rlBack.setOnClickListener(this)

        views.recycler.layoutManager = LinearLayoutManager(this)
        mAdapter = SipTransportAdapter(this,this)
        views.recycler.adapter = mAdapter
    }


    override fun onResume() {
        super.onResume()

        renderData()
    }


    private fun renderData() {
        portList.clear()
        portList.add(SipTransportInfo("NONE", false))
        portList.add(SipTransportInfo("SRTP", false))
        portList.add(SipTransportInfo("ZRTP", false))
        portList.add(SipTransportInfo("DLTS", false))

        portList.forEach {
            if (selectedValue!!.toUpperCase(Locale.ROOT) == it.name) {
                it.isCheck = true
            }
        }

        refreshData()
    }

    private fun refreshData() {
        mAdapter!!.clearDataList()
        mAdapter!!.addDataList(portList)
        mAdapter!!.notifyDataSetChanged()
    }

    override fun onRecyclerViewItemClick(view: View, position: Int) {

        mBus.post(SelectValueEvent(2, portList[position]!!.name!!))
        finish()
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.rl_back -> {
                finish()
            }
            else         -> {
            }
        }
    }
}
