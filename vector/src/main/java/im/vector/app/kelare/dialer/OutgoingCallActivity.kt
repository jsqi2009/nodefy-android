

package im.vector.app.kelare.dialer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityHomeBinding
import im.vector.app.databinding.ActivityOutgoingCallBinding

@AndroidEntryPoint
class OutgoingCallActivity : VectorBaseActivity<ActivityOutgoingCallBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        statusBarWhiteColor(this)
    }

    override fun getBinding() = ActivityOutgoingCallBinding.inflate(layoutInflater)
}
