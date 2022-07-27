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

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import im.vector.app.core.platform.VectorBaseFragment
import im.vector.app.core.time.Clock
import im.vector.app.core.utils.openUrlInExternalBrowser
import im.vector.app.databinding.FragmentRoomUploadsLinksBinding
import im.vector.app.features.home.AvatarRenderer
import im.vector.app.kelare.adapter.RecyclerItemClickListener
import im.vector.app.kelare.content.Contants
import org.matrix.android.sdk.api.session.Session
import timber.log.Timber
import java.util.regex.Pattern
import javax.inject.Inject

class RoomUploadsLinksFragment @Inject constructor(
        private val session: Session,
        private val avatarRenderer: AvatarRenderer
) : VectorBaseFragment<FragmentRoomUploadsLinksBinding>(), RecyclerItemClickListener{

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRoomUploadsLinksBinding {
        return FragmentRoomUploadsLinksBinding.inflate(inflater, container, false)
    }

    private var mAdapter: RoomLinkAdapter? = null
    private var linkList: ArrayList<String> = ArrayList()
    private val regexStr = "(((https|http)?://)?([a-z0-9]+[.])|(www.))" + "\\w+[.|\\/]([a-z0-9]{0,})?[[.]([a-z0-9]{0,})]+((/[\\S&&[^,;\u4E00-\u9FA5]]+)+)?([.][a-z0-9]{0,}+|/?)"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Contants.roomTimeLineList.isNotEmpty()) {
            Timber.e("link fragment list---${Contants.roomTimeLineList}")
        } else {
            Timber.e("link fragment list is 0")
        }

        linkList.clear()
        if (Contants.roomTimeLineList.isNotEmpty()) {
            Contants.roomTimeLineList.forEach {
                if (hasLink(it)) {
                    linkList.add(it)
                }
            }
        }
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

        val url = judgeString(itemLink)
        if (url != null && url.isNotEmpty()) {
            Timber.e("valid link-------${url[0]}")
            openUrlInExternalBrowser(requireContext(), url[0].toString())
        }

        //openUrlInExternalBrowser(requireContext(), itemLink)
    }

    private fun hasLink(str: String): Boolean {
        val m = Pattern.compile(regexStr).matcher(str)
        if (m.find()) {
            return true
        }
        return false
    }

    /**
     * 判断字符串中是否有超链接，若有，则返回超链接。
     * @param str
     * @return
     */
    private fun judgeString(str: String): Array<String?>? {
        val m = Pattern.compile(regexStr).matcher(str)
        val url = arrayOfNulls<String>(str.length / 5)
        var count = 0
        while (m.find()) {
            url[count] = m.group()
            count++
        }
        return url
    }

}
