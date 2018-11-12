package network.o3.o3wallet.MultiWallet.ManageMultiWallet


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import network.o3.o3wallet.MultiWallet.AddNewMultiWallet.MultiWalletAddNew
import network.o3.o3wallet.NEP6

import network.o3.o3wallet.R
import network.o3.o3wallet.RoundedBottomSheetDialogFragment
import org.jetbrains.anko.find
import org.jetbrains.anko.image
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.sdk15.coroutines.onClick

class ManageWalletsBottomSheet : RoundedBottomSheetDialogFragment() {

    lateinit var mView: View
    lateinit var listView: ListView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.multiwallet_manage_wallets_bottom_sheet, container, false)
        listView = mView.find(R.id.manage_wallets_list)
        listView.adapter = ManageWalletsAdapter(context!!)
        return mView
    }

    class ManageWalletsAdapter(context: Context): BaseAdapter() {
        var mContext: Context
        init {
            mContext = context
        }

        override fun getView(position: Int, view: View?, viewGroup: ViewGroup?): View {
            if (position == getCount() - 1) {
                val addRow = mContext.layoutInflater.inflate(R.layout.multiwallet_manage_wallets_add_wallet_row, viewGroup, false)
                addRow.onClick {
                    val intent = Intent(mContext, MultiWalletAddNew::class.java)
                    ContextCompat.startActivity(mContext, intent, null)
                }
                return addRow
            } else {
                val walletRow = mContext.layoutInflater.inflate(R.layout.multiwallet_manage_wallets_wallet_row, viewGroup, false)
                val account = getItem(position)
                walletRow.find<TextView>(R.id.walletNameTextView).text = account.label
                if (account.isDefault) {
                    walletRow.find<ImageView>(R.id.walletLockIcon).image = ResourcesCompat.getDrawable(mContext.resources, R.drawable.ic_unlocked, null)
                } else if(account.key == null){
                    walletRow.find<ImageView>(R.id.walletLockIcon).image = ResourcesCompat.getDrawable(mContext.resources, R.drawable.ic_eye, null)
                } else {
                    walletRow.find<ImageView>(R.id.walletLockIcon).image = ResourcesCompat.getDrawable(mContext.resources, R.drawable.ic_locked, null)
                }
                walletRow.onClick {
                    val intent = Intent(mContext, MultiwalletManageWallet::class.java)
                    intent.putExtra("address", account.address)
                    intent.putExtra("key", account.key ?: "")
                    intent.putExtra("name", account.label)
                    intent.putExtra("isDefault", account.isDefault)
                    ContextCompat.startActivity(mContext, intent, null)
                }
                return walletRow
            }
        }

        override fun getItem(position: Int): NEP6.Account {
            return NEP6.getFromFileSystem().accounts[position]
        }

        override fun getCount(): Int {
            return 1 + NEP6.getFromFileSystem().accounts.count()
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

    }

    companion object {
        fun newInstance(): ManageWalletsBottomSheet {
            return ManageWalletsBottomSheet()
        }
    }
}
