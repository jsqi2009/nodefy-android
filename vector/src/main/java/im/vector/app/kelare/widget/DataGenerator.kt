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

package im.vector.app.kelare.widget

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import im.vector.app.R
import im.vector.app.kelare.dialer.fragments.ContactFragment
import im.vector.app.kelare.dialer.fragments.HistoryFragment
import im.vector.app.kelare.dialer.fragments.MessagingFragment

/**
 * author : Jason
 *  date   : 2022/5/19 18:17
 *  desc   :
 */
object DataGenerator {

    private val mTabRes = intArrayOf(R.drawable.ic_dialer_history, R.drawable.ic_dialer_contact, R.drawable.ic_dialer_message)
    val mTabResPressed = intArrayOf(R.drawable.ic_icon_overview_light, R.drawable.icon_me_light, R.drawable.ic_icon_overview_light)
    private val mTabTitle = intArrayOf(R.string.tab_overview, R.string.tab_devices, R.string.tab_me)
    private val mHistoryTabTitle = intArrayOf(R.string.all_history, R.string.missed_history)
    private val mContactTabTitle = intArrayOf(R.string.dialer_contact_sip, R.string.dialer_contact_xmpp)
    private val mMessageTabTitle = intArrayOf(R.string.dialer_message_people, R.string.dialer_message_group, R.string.dialer_message_sms)

    fun getFragments(from: String): Array<Fragment?> {
        var fragments = arrayOfNulls<Fragment>(3)
        fragments[0] = HistoryFragment.newInstance("", "")
        fragments[1] = ContactFragment.newInstance("", "")
        fragments[2] = MessagingFragment.newInstance("", "")
        return fragments
    }

    fun getDialerFragments(from: String): ArrayList<Fragment> {
        var fragments = ArrayList<Fragment>(3)
        fragments.add(0, HistoryFragment.newInstance("", ""))
        fragments.add(1, ContactFragment.newInstance("", ""))
        fragments.add(2, MessagingFragment.newInstance("", ""))
        return fragments
    }

    /**
     * Tab display
     *
     * @param context
     * @param position
     * @return
     */
    fun getTabView(context: Context, position: Int): View {
        val view = LayoutInflater.from(context).inflate(R.layout.main_tab_view, null)
        val tabIcon = view.findViewById<View>(R.id.icon_tab) as ImageView
        tabIcon.setImageResource(mTabRes[position])
        val tabText = view.findViewById<View>(R.id.title_tab) as TextView
        tabText.text = context.resources.getString(mTabTitle[position])
        return view
    }

    fun getHistoryTabView(context: Context, position: Int): View {
        val view = LayoutInflater.from(context).inflate(R.layout.main_sub_tab_view, null)
        val tabText = view.findViewById<View>(R.id.title_tab) as TextView
        tabText.text = context.resources.getString(mHistoryTabTitle[position])
        return view
    }

    fun getContactTabView(context: Context, position: Int): View {
        val view = LayoutInflater.from(context).inflate(R.layout.main_sub_tab_view, null)
        val tabText = view.findViewById<View>(R.id.title_tab) as TextView
        tabText.text = context.resources.getString(mContactTabTitle[position])
        return view
    }

    fun getMessageTabView(context: Context, position: Int): View {
        val view = LayoutInflater.from(context).inflate(R.layout.main_sub_tab_view, null)
        val tabText = view.findViewById<View>(R.id.title_tab) as TextView
        tabText.text = context.resources.getString(mMessageTabTitle[position])
        return view
    }
}
