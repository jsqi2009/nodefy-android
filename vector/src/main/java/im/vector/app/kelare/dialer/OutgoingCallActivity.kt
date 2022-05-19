

package im.vector.app.kelare.dialer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityHomeBinding
import im.vector.app.databinding.ActivityOutgoingCallBinding
import im.vector.app.kelare.network.models.DialerAccountInfo

@AndroidEntryPoint
class OutgoingCallActivity : VectorBaseActivity<ActivityOutgoingCallBinding>(), View.OnClickListener, View.OnLongClickListener {

    override fun getBinding() = ActivityOutgoingCallBinding.inflate(layoutInflater)

    var accountList:ArrayList<DialerAccountInfo> = ArrayList()
    private var fullAccount = ""
    private var dialerNumber = ""
    private var selectedAccount:DialerAccountInfo = DialerAccountInfo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        statusBarWhiteColor(this)
    }


    override fun onClick(v: View?) {

    }

    override fun onLongClick(v: View?): Boolean {
        return true
    }
}
