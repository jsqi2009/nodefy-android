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

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseFragment
import im.vector.app.databinding.FragmentDialerBinding
import im.vector.app.databinding.FragmentHistoryBinding
import im.vector.app.kelare.adapter.FragmentAdapter
import im.vector.app.kelare.history.AllHistoryFragment
import im.vector.app.kelare.history.MissedHistoryFragment
import im.vector.app.kelare.widget.DataGenerator
import timber.log.Timber

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HistoryFragment : VectorBaseFragment<FragmentHistoryBinding>(), View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) =
            FragmentHistoryBinding.inflate(inflater, container, false)

    private var fragments: ArrayList<Fragment>? = ArrayList()
    private val titles: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragments!!.add(AllHistoryFragment.newInstance("Switch Fragment1","tile"))
        fragments!!.add(MissedHistoryFragment.newInstance("Switch Fragment1","tile"))
        initView()
    }

    private fun initView() {

        views.viewPager2.adapter = FragmentAdapter(requireActivity(), fragments, titles)
        val mediator: TabLayoutMediator = TabLayoutMediator(
                views.mTabLayout,
                views.viewPager2,
                object : TabLayoutMediator.TabConfigurationStrategy {
                    override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
                        //tab.text = resources.getString(DataGenerator.mTabTitle[position])
                        tab.customView = DataGenerator.getHistoryTabView(requireContext(), position)
                    }
                })
        mediator.attach()  //Don't forget attach()！！！

        //page change listener
        views.viewPager2.registerOnPageChangeCallback(pageChangeCallback)
    }

    private var pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            Timber.e("ViewPager onPageSelected $position")
            //custom tab style
            val tabCount: Int = views.mTabLayout.tabCount
            for (i in 0 until tabCount) {
                val tab: TabLayout.Tab? = views.mTabLayout.getTabAt(i)
                val tabView = tab!!.view
                val text = tabView!!.findViewById<View>(R.id.title_tab) as TextView
                val layout = tabView!!.findViewById<View>(R.id.ll_root) as LinearLayout
                if (tab.position == position) {
                    //text.textSize = 16F
//                        text.typeface = Typeface.DEFAULT_BOLD
                    text.setTextColor(resources.getColor(R.color.white, null))
                    //text.typeface = Typeface.DEFAULT
                    layout.background = resources.getDrawable(R.drawable.shap_tab_selected_20dp, null)
                } else {
                    //text!!.textSize = 16F
                    text.setTextColor(resources.getColor(R.color.tab_bar_color_default, null))
                    //text!!.typeface = Typeface.DEFAULT
                    layout.background = resources.getDrawable(R.drawable.shap_tab_20dp, null)
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                HistoryFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }

    override fun onClick(v: View?) {
        TODO("Not yet implemented")
    }
}
