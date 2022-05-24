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

package im.vector.app.kelare.message.group

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityGroupParticipantBinding
import im.vector.app.databinding.ActivitySendMessageBinding
import im.vector.app.kelare.adapter.GroupParticipantAdapter
import im.vector.app.kelare.adapter.RecyclerItemClickListener

@AndroidEntryPoint
class GroupParticipantActivity : VectorBaseActivity<ActivityGroupParticipantBinding>(), View.OnClickListener, RecyclerItemClickListener {

    override fun getBinding() = ActivityGroupParticipantBinding.inflate(layoutInflater)

    private var groupName : String ? = null
    private var participant : String ? = null
    private var mAdapter: GroupParticipantAdapter? = null
    private var mList: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarWhiteColor(this)

        groupName = intent.extras!!.getString("group_name")!!
        participant = intent.extras!!.getString("participant")!!

        initView()
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {

        val array = participant!!.split(",")
        array.forEach {
            mList.add(it)
        }

        views.tvRoom.text = groupName
        views.tvTitle.text = "Members"
        views.rlBack.setOnClickListener(this)

        views.participantRecycler.layoutManager = LinearLayoutManager(this)
        mAdapter = GroupParticipantAdapter(this,this)
        views.participantRecycler.adapter = mAdapter

        mAdapter!!.clearDataList()
        mAdapter!!.addDataList(mList)
        mAdapter!!.notifyDataSetChanged()
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

    override fun onRecyclerViewItemClick(view: View, position: Int) {

    }
}
