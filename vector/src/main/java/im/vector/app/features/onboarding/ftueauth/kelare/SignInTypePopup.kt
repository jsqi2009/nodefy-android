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

package im.vector.app.features.onboarding.ftueauth.kelare

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.labo.kaji.relativepopupwindow.RelativePopupWindow
import im.vector.app.R
import im.vector.app.kelare.adapter.RecyclerItemClickListener
import im.vector.app.kelare.adapter.SignInTypeAdapter
import im.vector.app.kelare.content.AndroidBus
import im.vector.app.kelare.content.DialerSession
import im.vector.app.kelare.utils.UIUtils

/**
 * author : Jason
 *  date   : 2022/5/27 14:15
 *  desc   :
 */
class SignInTypePopup(context: Context, private val mBus: AndroidBus, private val selectedType: String, private val typeList: ArrayList<String>) : RelativePopupWindow(context),
        RecyclerItemClickListener {


    private var mSession: DialerSession? = null
    private var mContext:Context? = null
    private lateinit var mAdapter: SignInTypeAdapter
    private var mListView: RecyclerView? = null

    init {
        @SuppressLint("InflateParams")
        contentView = LayoutInflater.from(context).inflate(R.layout.popup_sign_in_type_list, null)
        width = UIUtils.dip2px(context, 150)
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        isFocusable = true
        isOutsideTouchable = true
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        mContext = context
        mSession = DialerSession(context)
        mBus.register(this)

        // Disable default animation for circular reveal
        animationStyle = 0

        initView(contentView)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initView(view: View) {
        mListView = view.findViewById(R.id.mListView)

        mAdapter = SignInTypeAdapter(mContext as Activity, selectedType, this)
        mListView!!.adapter = mAdapter
        mListView!!.layoutManager = LinearLayoutManager(mContext)
        mListView!!.setHasFixedSize(true)

        mAdapter.addDataList(typeList)
        mAdapter.notifyDataSetChanged()

    }

    override fun onRecyclerViewItemClick(view: View, position: Int) {
        dismiss()

    }

}
