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

package im.vector.app.kelare.message.widget

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.circularreveal.CircularRevealCompat
import com.google.android.material.circularreveal.cardview.CircularRevealCardView
import com.labo.kaji.relativepopupwindow.RelativePopupWindow
import im.vector.app.R
import im.vector.app.kelare.adapter.RecyclerItemClickListener
import im.vector.app.kelare.adapter.SipAccountAdapter
import im.vector.app.kelare.content.AndroidBus
import im.vector.app.kelare.content.DialerSession
import im.vector.app.kelare.network.event.SetDefaultSMSAccountEvent
import im.vector.app.kelare.network.models.DialerAccountInfo
import java.lang.Math.hypot
import java.lang.Math.max
import kotlin.math.hypot

/**
 * author : Jason
 *  date   : 2022/5/24 14:32
 *  desc   :
 */
class SelectSendMsgAccountPopup(context: Context, private val mBus: AndroidBus, accountList: ArrayList<DialerAccountInfo>) : RelativePopupWindow(context),
        RecyclerItemClickListener {


    private var mSession: DialerSession? = null
    private var mContext:Context? = null
    private lateinit var mAdapter: SipAccountAdapter
    var sipList: ArrayList<DialerAccountInfo> = ArrayList()
    private var mListView: RecyclerView? = null

    init {
        @SuppressLint("InflateParams")
        contentView = LayoutInflater.from(context).inflate(R.layout.popup_select_send_msg_acount_list, null)
        width = ViewGroup.LayoutParams.WRAP_CONTENT
        height = ViewGroup.LayoutParams.WRAP_CONTENT
        isFocusable = true
        isOutsideTouchable = true
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        setBackgroundDrawable(ColorDrawable(Color.CYAN))

        mContext = context
        mSession = DialerSession(context)
        mBus.register(this)
        sipList = accountList

        // Disable default animation for circular reveal
        animationStyle = 0

        initView(contentView)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initView(view: View) {
        mListView = view.findViewById(R.id.mListView)

        mAdapter = SipAccountAdapter(mContext as Activity, this)
        mListView!!.adapter = mAdapter
        mListView!!.layoutManager = LinearLayoutManager(mContext)
        mListView!!.setHasFixedSize(true)

        mAdapter.addDataList(sipList)
        mAdapter.notifyDataSetChanged()

    }

    /*override fun showOnAnchor(anchor: View, vertPos: Int, horizPos: Int, x: Int, y: Int, fitInScreen: Boolean) {
        super.showOnAnchor(anchor, vertPos, horizPos, x, y, fitInScreen)
        circularReveal(anchor)
    }*/

    override fun onRecyclerViewItemClick(view: View, position: Int) {
        mBus.post(SetDefaultSMSAccountEvent(sipList[position]))
        dismiss()

    }

    private fun circularReveal(anchor: View) {
        (contentView as CircularRevealCardView).run {
            post {
                val myLocation = IntArray(2).apply { getLocationOnScreen(this) }
                val anchorLocation = IntArray(2).apply { anchor.getLocationOnScreen(this) }
                val cx = anchorLocation[0] - myLocation[0] + anchor.width/2
                val cy = anchorLocation[1] - myLocation[1] + anchor.height/2
                val windowRect = Rect().apply { getWindowVisibleDisplayFrame(this) }

                measure(
                        makeDropDownMeasureSpec(this@SelectSendMsgAccountPopup.width, windowRect.width()),
                        makeDropDownMeasureSpec(this@SelectSendMsgAccountPopup.height, windowRect.height())
                )
                val dx = max(cx, measuredWidth - cx)
                val dy = max(cy, measuredHeight - cy)
                val finalRadius = hypot(dx.toFloat(), dy.toFloat())
                CircularRevealCompat.createCircularReveal(this, cx.toFloat(), cy.toFloat(), 0f, finalRadius).run {
                    duration = 200
                    start()
                }
            }
        }
    }

    companion object {
        private fun makeDropDownMeasureSpec(measureSpec: Int, maxSize: Int): Int {
            return View.MeasureSpec.makeMeasureSpec(
                    getDropDownMeasureSpecSize(measureSpec, maxSize),
                    getDropDownMeasureSpecMode(measureSpec)
            )
        }

        private fun getDropDownMeasureSpecSize(measureSpec: Int, maxSize: Int): Int {
            return when (measureSpec) {
                ViewGroup.LayoutParams.MATCH_PARENT -> maxSize
                else -> View.MeasureSpec.getSize(measureSpec)
            }
        }

        private fun getDropDownMeasureSpecMode(measureSpec: Int): Int {
            return when (measureSpec) {
                ViewGroup.LayoutParams.WRAP_CONTENT -> View.MeasureSpec.UNSPECIFIED
                else -> View.MeasureSpec.EXACTLY
            }
        }
    }


}
