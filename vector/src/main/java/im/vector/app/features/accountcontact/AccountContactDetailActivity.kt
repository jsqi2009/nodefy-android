

package im.vector.app.features.accountcontact

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityAccountContactDetailBinding
import im.vector.app.databinding.ActivityOutgoingCallBinding

@AndroidEntryPoint
class AccountContactDetailActivity : VectorBaseActivity<ActivityAccountContactDetailBinding>() {

    override fun getBinding() = ActivityAccountContactDetailBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarWhiteColor(this)
    }
}
