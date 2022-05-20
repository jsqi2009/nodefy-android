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

import CreateGroupNameEvent
import SelectedXMPPAccountEvent
import android.annotation.SuppressLint
import android.content.Intent
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
import im.vector.app.databinding.FragmentMessagingBinding
import im.vector.app.kelare.adapter.FragmentAdapter
import im.vector.app.kelare.message.GroupMessageFragment
import im.vector.app.kelare.message.PeopleMessageFragment
import im.vector.app.kelare.message.SMSMessageFragment
import im.vector.app.kelare.message.SendMessageActivity
import im.vector.app.kelare.message.widget.ChooseXMPPAccountDialog
import im.vector.app.kelare.message.widget.CreateXMPPGroupDialog
import im.vector.app.kelare.network.event.DialerAccountInfoResponseEvent
import im.vector.app.kelare.network.models.DialerAccountInfo
import im.vector.app.kelare.widget.DataGenerator
import timber.log.Timber

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MessagingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MessagingFragment : VectorBaseFragment<FragmentMessagingBinding>(), View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) =
            FragmentMessagingBinding.inflate(inflater, container, false)

    private var fragments: ArrayList<Fragment>? = ArrayList()
    private val titles: ArrayList<String> = ArrayList()
    private var xmppAccountList: ArrayList<DialerAccountInfo> = ArrayList()

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

    @Subscribe
    fun onDialerAccountEvent(event: DialerAccountInfoResponseEvent) {
        xmppAccountList.clear()
        hideLoadingDialog()
        if (event.isSuccess) {
            var accountList = event.model!!.sip_accounts!!
            for (dialerAccountInfo in accountList) {
                if (dialerAccountInfo.type_value == "xmpp") {
                    xmppAccountList.add(dialerAccountInfo)
                }
            }
            Timber.e("xmpp account info: ${xmppAccountList}")
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.iv_send    -> {
                val intent = Intent(requireContext(), SendMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.iv_create    -> {
                showChooseAccountDialog()
            }
            else -> {
            }
        }
    }

    private fun initView() {

        views.tvSend.setOnClickListener(this)
        views.tvCreate.setOnClickListener(this)
        views.ivSend.setOnClickListener(this)
        views.ivCreate.setOnClickListener(this)

        fragments!!.add(PeopleMessageFragment.newInstance("Switch Fragment1","tile"))
        fragments!!.add(GroupMessageFragment.newInstance("Switch Fragment1","tile"))
        fragments!!.add(SMSMessageFragment.newInstance("Switch Fragment1","tile"))

        views.viewPager2.adapter = FragmentAdapter(requireActivity(), fragments, titles)
        val mediator: TabLayoutMediator = TabLayoutMediator(
                views.mTabLayout,
                views.viewPager2,
                object : TabLayoutMediator.TabConfigurationStrategy {
                    override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
                        tab.customView = DataGenerator.getMessageTabView(requireContext(), position)
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
            if (position == 1) {
                views.ivSend.visibility = View.GONE
                views.ivCreate.visibility = View.VISIBLE
            } else if (position == 2) {
                views.ivSend.visibility = View.VISIBLE
                views.ivCreate.visibility = View.GONE
            } else {
                views.ivSend.visibility = View.GONE
                views.ivCreate.visibility = View.GONE
            }

            //custom tab style
            val tabCount: Int = views.mTabLayout.tabCount
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
    private fun showChooseAccountDialog() {
        var xmppList: ArrayList<DialerAccountInfo> = ArrayList()
        val listInfo = mSession.accountListInfo
        if (mConnectionList.isEmpty()) {
            xmppList = ArrayList()
        } else {
            if (listInfo != null && listInfo.isNotEmpty()) {
                listInfo!!.forEach {
                    if (it.type_value == "xmpp" && it.enabled && it.extension.isConnected) {
                        xmppList.add(it)
                    }
                }
            }
        }

        val chooseXMPPAccountDialog = ChooseXMPPAccountDialog(activity!!, mBus, xmppList)
        val dialogWindow: Window = chooseXMPPAccountDialog.window!!
        dialogWindow.decorView.setPadding(0,0,0,0)
        val lp = dialogWindow.attributes
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialogWindow.attributes = lp
        dialogWindow.setGravity(Gravity.BOTTOM)
        chooseXMPPAccountDialog.show()
    }

    @SuppressLint("UseRequireInsteadOfGet")
    @Subscribe
    fun onSelectedAccountEvent(event: SelectedXMPPAccountEvent) {
        Timber.e("selected xmpp: ${event.info}")

        val createGroupDialog = CreateXMPPGroupDialog(activity!!, mBus, event.info)
        createGroupDialog.show()
    }

    @Subscribe
    fun onGroupNameEvent(event: CreateGroupNameEvent){
        Timber.e("user info: ${event.info}")
        Timber.e("group name: ${event.groupName}")

        /*val intent = Intent(requireContext(), GroupCreateActivity::class.java)
        intent.putExtra("user_info", event.info)
        intent.putExtra("group_name", event.groupName)
        startActivity(intent)*/

    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                MessagingFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }


}
