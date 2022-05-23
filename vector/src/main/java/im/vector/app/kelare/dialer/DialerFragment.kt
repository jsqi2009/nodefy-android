
package im.vector.app.kelare.dialer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseFragment
import im.vector.app.databinding.FragmentDialerBinding
import im.vector.app.kelare.adapter.FragmentAdapter
import im.vector.app.kelare.dialer.call.OutgoingCallActivity
import im.vector.app.kelare.widget.DataGenerator

class DialerFragment : VectorBaseFragment<FragmentDialerBinding>(), View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) =
            FragmentDialerBinding.inflate(inflater, container, false)

    var fragments: ArrayList<Fragment>? = null
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

        fragments = DataGenerator.getDialerFragments("TabLayout Tab")
        initView()
    }

    @SuppressLint("SetTextI18n", "UseRequireInsteadOfGet")
    private fun initView() {

        views.rlBack.setOnClickListener(this)
        views.tvCall.setOnClickListener(this)
        views.ivSetting.setOnClickListener(this)

        views.viewPager2.adapter = FragmentAdapter(activity!!, fragments, titles)
        val mediator: TabLayoutMediator = TabLayoutMediator(
                views.mTabLayout,
                views.viewPager2,
                object : TabLayoutMediator.TabConfigurationStrategy {
                    override fun onConfigureTab(tab: TabLayout.Tab, position: Int) {
                        //tab.text = resources.getString(DataGenerator.mTabTitle[position])
                        tab.customView = DataGenerator.getTabView(activity!!, position)
                    }
                })
        mediator.attach()  //Don't forget attach()！！！
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.rl_back    -> {
            }
            R.id.tv_call    -> {
                val intent = Intent(activity, OutgoingCallActivity::class.java)
                startActivity(intent)
            }
            R.id.iv_setting    -> {
                val intent = Intent(activity, DialerSettingActivity::class.java)
                startActivity(intent)
            }
            else -> {}
        }
    }

    companion object {
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                DialerFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }


}
