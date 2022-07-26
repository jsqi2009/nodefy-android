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

package im.vector.app.features.roomprofile.uploads.links

import android.os.Bundle
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.epoxy.EpoxyModel
import im.vector.app.core.platform.VectorBaseFragment
import im.vector.app.core.time.Clock
import im.vector.app.databinding.FragmentRoomUploadsLinksBinding
import im.vector.app.features.home.AvatarRenderer
import im.vector.app.features.home.room.detail.timeline.item.MessageTextItem
import im.vector.app.kelare.adapter.CallHistoryAdapter
import im.vector.app.kelare.adapter.RecyclerItemClickListener
import im.vector.app.kelare.content.Contants
import org.matrix.android.sdk.api.session.Session
import timber.log.Timber
import javax.inject.Inject

class RoomUploadsLinksFragment @Inject constructor(
        private val session: Session,
        private val avatarRenderer: AvatarRenderer,
        private val clock: Clock
) : VectorBaseFragment<FragmentRoomUploadsLinksBinding>(), RecyclerItemClickListener{

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRoomUploadsLinksBinding {
        return FragmentRoomUploadsLinksBinding.inflate(inflater, container, false)
    }

    private var mAdapter: RoomLinkAdapter? = null
    private var linkList: ArrayList<String> = ArrayList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Contants.roomTimeLineList.isNotEmpty()) {
            Timber.e("link fragment list---${Contants.roomTimeLineList}")
        } else {
            Timber.e("link fragment list is 0")
        }

        linkList = Contants.roomTimeLineList
        initView()
        renderData()
    }

    private fun initView() {
        views!!.linkRecyclerView.layoutManager = LinearLayoutManager(activity)
        mAdapter = RoomLinkAdapter(requireActivity(),this)
        views!!.linkRecyclerView.adapter = mAdapter
    }

    private fun renderData() {
        mAdapter!!.clearDataList()
        mAdapter!!.addDataList(linkList)
        mAdapter!!.notifyDataSetChanged()
    }

    override fun onRecyclerViewItemClick(view: View, position: Int) {
        val itemLink = linkList[position]
    }
}
