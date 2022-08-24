package im.vector.app.features.accountcontact

import android.os.Bundle
import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.databinding.ActivityAccountContactDetailBinding
import im.vector.app.features.accountcontact.widget.ContactAssociateBottomSheet

@AndroidEntryPoint
class AccountContactDetailActivity : VectorBaseActivity<ActivityAccountContactDetailBinding>(), View.OnClickListener, ContactAssociateBottomSheet.InteractionListener {

    override fun getBinding() = ActivityAccountContactDetailBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarWhiteColor(this)

        initView()
    }

    private fun initView() {

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
        ContactAssociateBottomSheet.newInstance(accountType, this).show(supportFragmentManager, "associate")
    }

    override fun onRefreshRelations() {

    }
}
