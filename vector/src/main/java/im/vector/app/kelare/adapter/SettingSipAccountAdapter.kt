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

package im.vector.app.kelare.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import im.vector.app.R
import im.vector.app.kelare.network.models.DialerAccountInfo

/**
 * author : Jason
 *  date   : 2022/4/11 16:02
 *  desc   :
 */
class SettingSipAccountAdapter(private val mContext: Activity) : AbstractBaseRecyclerAdapter<DialerAccountInfo>(mContext) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        var context = LayoutInflater.from(mContext)
        var view = context.inflate(R.layout.item_setting_sip_account_list, parent, false)
        return ViewHolder(view)

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val holder = holder as ViewHolder
        val info = getDataList()!![position]

        if (info.enabled) {
            if (info.extension.isConnected) {
                holder.iv_enable_online.visibility = View.VISIBLE
                holder.iv_enable_offline.visibility = View.GONE
                holder.iv_disable.visibility = View.GONE
            } else {
                holder.iv_enable_online.visibility = View.GONE
                holder.iv_enable_offline.visibility = View.VISIBLE
                holder.iv_disable.visibility = View.GONE
            }
        } else {
            holder.iv_disable.visibility = View.VISIBLE
            holder.iv_enable_offline.visibility = View.GONE
            holder.iv_enable_online.visibility = View.GONE
        }
        holder.tv_accountname.text = info.display_as

        /*if (position == 0) {
            holder.lineView.visibility = View.GONE
        }
*/
        /*holder.itemView.setOnClickListener {
            mOnItemClickListener.onSipAccountClick(holder.itemView, position)
        }*/
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tv_accountname: TextView
        var iv_disable: ImageView
        var iv_enable_offline: ImageView
        var iv_enable_online: ImageView
        var lineView: View
        var root: LinearLayout

        init {
            tv_accountname = itemView.findViewById(R.id.tv_account_name)
            iv_disable = itemView.findViewById(R.id.iv_disable)
            iv_enable_offline = itemView.findViewById(R.id.iv_enable_offline)
            iv_enable_online = itemView.findViewById(R.id.iv_enable_online)
            lineView = itemView.findViewById(R.id.line)
            root = itemView.findViewById(R.id.root)
        }

    }
}
