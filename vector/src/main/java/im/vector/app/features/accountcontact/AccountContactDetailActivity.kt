package im.vector.app.features.accountcontact

import android.os.Bundle
import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityAccountContactDetailBinding

@AndroidEntryPoint
class AccountContactDetailActivity : VectorBaseActivity<ActivityAccountContactDetailBinding>(), View.OnClickListener {

    override fun getBinding() = ActivityAccountContactDetailBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarWhiteColor(this)

        initView()
    }

    private fun initView() {

        views.rlBack.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.rl_back -> {
                finish()
            }
            else         -> {}
        }
    }
}
