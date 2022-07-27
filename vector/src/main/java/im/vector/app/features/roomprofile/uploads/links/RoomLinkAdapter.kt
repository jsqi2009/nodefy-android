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

import android.annotation.SuppressLint
import android.app.Activity
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import im.vector.app.R
import im.vector.app.kelare.adapter.AbstractBaseRecyclerAdapter
import im.vector.app.kelare.adapter.RecyclerItemClickListener
import org.apache.commons.lang3.StringUtils
import java.util.regex.Pattern

class RoomLinkAdapter(private val mContext: Activity, private val mOnItemClickListener: RecyclerItemClickListener) : AbstractBaseRecyclerAdapter<String>(mContext){

    private val regexStr = "(((https|http)?://)?([a-z0-9]+[.])|(www.))" + "\\w+[.|\\/]([a-z0-9]{0,})?[[.]([a-z0-9]{0,})]+((/[\\S&&[^,;\u4E00-\u9FA5]]+)+)?([.][a-z0-9]{0,}+|/?)"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val context = LayoutInflater.from(mContext)
        val view = context.inflate(R.layout.item_room_link_list, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder = holder as ViewHolder
        val info = getDataList()!![position]

        var url = judgeString(info)
        var allUrl: String = info
        var charSequence: CharSequence
        if (url != null) {
            if (url.isNotEmpty()) {
                url.forEach {
                    if (StringUtils.isNotBlank(it)) {
                        val str = "<font color='#0099cc'> <a href=\"$it\">$it</a></font>"
                        allUrl = allUrl.replace(it!!, str)
                    }
                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    charSequence = Html.fromHtml(allUrl, Html.FROM_HTML_MODE_LEGACY)
                } else {
                    charSequence = Html.fromHtml(allUrl)
                }

                holder.tvName.text = charSequence
            }
        }

        //holder.tvName.text = info

        holder.itemView.setOnClickListener {
            mOnItemClickListener.onRecyclerViewItemClick(holder.itemView, position)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvName: TextView
        var lineView: View

        init {
            tvName = itemView.findViewById(R.id.tv_name)
            lineView = itemView.findViewById(R.id.line)
        }
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
            count++
            url[count] = m.group()
        }
        return url
    }
}
