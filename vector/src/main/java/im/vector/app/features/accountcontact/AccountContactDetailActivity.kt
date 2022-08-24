package im.vector.app.features.accountcontact

import android.os.Bundle
import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.di.ActiveSessionHolder
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityAccountContactDetailBinding
import im.vector.app.features.accountcontact.widget.AssociateContactBottomDialog
import im.vector.app.features.accountcontact.widget.ContactAssociateBottomSheet
import im.vector.app.kelare.network.models.AccountContactInfo
import org.matrix.android.sdk.api.session.Session
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class AccountContactDetailActivity : VectorBaseActivity<ActivityAccountContactDetailBinding>(), View.OnClickListener, AssociateContactBottomDialog.InteractionListener {

    override fun getBinding() = ActivityAccountContactDetailBinding.inflate(layoutInflater)

    @Inject lateinit var sessionHolder: ActiveSessionHolder

    var session: Session? = null
    private var contactList: ArrayList<AccountContactInfo> = ArrayList()
    private var targetContact: AccountContactInfo = AccountContactInfo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarWhiteColor(this)

        initView()
    }

    private fun initView() {

        session = sessionHolder.getActiveSession()

        views.rlBack.setOnClickListener(this)
        views.sipAssociate.setOnClickListener(this)
        views.xmppAssociate.setOnClickListener(this)
        views.skypeAssociate.setOnClickListener(this)
        views.slackAssociate.setOnClickListener(this)
        views.telegramAssociate.setOnClickListener(this)
        views.whatsappAssociate.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.rl_back -> {
                finish()
            }
            R.id.sipAssociate -> {
                associateContact(getString(R.string.account_contact_sip))
            }
            R.id.xmppAssociate -> {
                associateContact(getString(R.string.account_contact_xmpp))
            }
            R.id.skypeAssociate -> {
                associateContact(getString(R.string.account_contact_skype))
            }
            R.id.slackAssociate -> {
                associateContact(getString(R.string.account_contact_slack))
            }
            R.id.telegramAssociate -> {
                associateContact(getString(R.string.account_contact_telegram))
            }
            R.id.whatsappAssociate -> {
                associateContact(getString(R.string.account_contact_whatsapp))
            }
            else         -> {}
        }
    }

    private fun associateContact(accountType: String) {

        AssociateContactBottomDialog(this, mBus, accountType, session!!.myUserId, mConnectionList, dialerSession, this).show(supportFragmentManager, "associate")
    }

    override fun onRefreshRelations() {

        Timber.e("call the refresh relation")
    }
}
