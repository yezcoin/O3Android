package network.o3.o3wallet.MultiWallet.ManageMultiWallet

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.text.SpannableStringBuilder
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.zxing.integration.android.IntentIntegrator
import neoutils.Neoutils
import network.o3.o3wallet.MultiWallet.DialogInputEntryFragment
import network.o3.o3wallet.NEP6
import network.o3.o3wallet.O3Wallet
import network.o3.o3wallet.PersistentStore
import network.o3.o3wallet.R
import network.o3.o3wallet.Settings.PrivateKeyFragment
import org.jetbrains.anko.find
import org.jetbrains.anko.findOptional
import org.jetbrains.anko.image
import org.jetbrains.anko.layoutInflater

class MultiwalletManageWallet : AppCompatActivity() {

    var viewModel = ManageWalletViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val address = intent.getStringExtra("address")
        val encryptedKey = intent.getStringExtra("key")
        val name = intent.getStringExtra("name")
        val isDefault = intent.getBooleanExtra("isDefault", false)

        viewModel.address = address
        if (encryptedKey == "") {
            viewModel.key = null
        } else {
            viewModel.key = encryptedKey
        }
        viewModel.name = name
        viewModel.isDefault = isDefault
        setContentView(R.layout.multiwallet_manage_wallet_activity)
    }

    override fun getTheme(): Resources.Theme {
        val theme = super.getTheme()
        if (PersistentStore.getTheme() == "Dark") {
            theme.applyStyle(R.style.AppTheme_Dark, true)
        } else {
            theme.applyStyle(R.style.AppTheme_White, true)
        }
        return theme
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result == null || result.contents == null) {
        } else {
            find<EditText>(R.id.wipTextView).text = SpannableStringBuilder(result.contents)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (findOptional<EditText>(R.id.walletEntryEditText) != null) {
            find<ImageButton>(R.id.rightNavButton)?.image = ContextCompat.getDrawable(this, R.drawable.ic_edit)
            find<ImageButton>(R.id.rightNavButton)?.setOnClickListener {
                val newNameEntry = DialogInputEntryFragment.newInstance()
                newNameEntry.submittedInput = { newName ->
                    var nep6 = NEP6.getFromFileSystem()
                    val index = nep6.accounts.indexOfFirst { it.address == viewModel.address }!!
                    nep6.accounts[index].label = newName
                    nep6.writeToFileSystem()
                    viewModel.name = newName
                    find<TextView>(R.id.mytext)?.text = viewModel.name
                    val intent = Intent("need-update-watch-address-event")
                    intent.putExtra("reset", true)
                    LocalBroadcastManager.getInstance(O3Wallet.appContext!!).sendBroadcast(intent)
                }
                newNameEntry.showNow(supportFragmentManager, "change name")
            }
        }
    }
}