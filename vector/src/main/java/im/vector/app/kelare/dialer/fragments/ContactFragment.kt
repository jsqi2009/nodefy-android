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

package im.vector.app.kelare.dialer.fragments

import SearchSIPContactEvent
import SearchXMPPContactEvent
import SelectedStatusEvent
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.otto.Subscribe
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseFragment
import im.vector.app.databinding.FragmentContactBinding
import im.vector.app.databinding.FragmentHistoryBinding
import im.vector.app.kelare.adapter.FragmentAdapter
import im.vector.app.kelare.contact.SipContactFragment
import im.vector.app.kelare.contact.XmppContactFragment
import im.vector.app.kelare.contact.widget.AddSIPContactDialog
import im.vector.app.kelare.contact.widget.DialerContactStatusDialog
import im.vector.app.kelare.widget.DataGenerator
import timber.log.Timber

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class ContactFragment : VectorBaseFragment<FragmentContactBinding>(), View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) =
            FragmentContactBinding.inflate(inflater, container, false)

    private var fragments: ArrayList<Fragment>? = ArrayList()
    private val titles: ArrayList<String> = ArrayList()
    private val statusList = arrayListOf("All", "Online", "Offline")

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

    private fun initView() {

        views.ivAddSipContact.setOnClickListener(this)
        views.llSearch.setOnClickListener(this)
        views.llStatus.setOnClickListener(this)

        fragments!!.add(SipContactFragment.newInstance("Switch Fragment1","tile"))
        fragments!!.add(XmppContactFragment.newInstance("Switch Fragment1","tile"))

        views.viewPager2.adapter = FragmentAdapter(requireActivity(), fragments, titles)
        val mediator: TabLayoutMediator = TabLayoutMediator(
                views.mTabLayout,
                views.viewPager2,
                object : TabLayoutMediator.TabConfigurationStrategy {
                    override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
                        tab.customView = DataGenerator.getContactTabView(requireContext(), position)
                    }
                })
        mediator.attach()  //Don't forget attach()！！！

        //page change listener
        views.viewPager2.registerOnPageChangeCallback(pageChangeCallback)
    }

    private var pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        @SuppressLint("UseCompatLoadingForDrawables")
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            Timber.e("ViewPager onPageSelected $position")
            if (position == 0) {
                views.ivAddSipContact.visibility = View.VISIBLE
            } else {
                views.ivAddSipContact.visibility = View.GONE
            }
            //custom tab style
            val tabCount: Int = views.mTabLayout.getTabCount()
            for (i in 0 until tabCount) {
                val tab: TabLayout.Tab? = views.mTabLayout.getTabAt(i)
                val tabView = tab!!.view
                val text = tabView!!.findViewById<View>(R.id.title_tab) as TextView
                val layout = tabView!!.findViewById<View>(R.id.ll_root) as LinearLayout
                if (tab.position == position) {
                    text.setTextColor(resources.getColor(R.color.white, null))
                    layout.background = resources.getDrawable(R.drawable.shap_tab_selected_20dp, null)
                } else {
                    text.setTextColor(resources.getColor(R.color.tab_bar_color_default, null))
                    layout.background = resources.getDrawable(R.drawable.shap_tab_20dp, null)
                }
            }
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.iv_add_sip_contact    -> {
                AddSIPContactDialog(activity!!, mBus, dialerSession, null).show(activity!!.supportFragmentManager, "tag")
            }
            R.id.ll_status    -> {
                showChooseStatusDialog()
            }
            R.id.ll_search    -> {
                filterData(views.etStatus.text.toString(), true)
            }
            else -> {
            }
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun showChooseStatusDialog() {
        val statusDialog = DialerContactStatusDialog(activity!!, mBus, statusList)
        val dialogWindow: Window = statusDialog.window!!
        dialogWindow.decorView.setPadding(0,0,0,0)
        val lp = dialogWindow.attributes
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialogWindow.attributes = lp
        dialogWindow.setGravity(Gravity.BOTTOM)
        statusDialog.show()
    }

    @Subscribe
    fun onSelectedStatus(event: SelectedStatusEvent) {
        views.tvStatus.text = event.status

        filterData(event.status, false)
    }

    private fun filterData(value: String, isSearch: Boolean) {
        if (views.mTabLayout.selectedTabPosition == 0) {
            mBus.post(SearchSIPContactEvent(value, isSearch))
        } else {
            mBus.post(SearchXMPPContactEvent(value, isSearch))
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                ContactFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }


}
