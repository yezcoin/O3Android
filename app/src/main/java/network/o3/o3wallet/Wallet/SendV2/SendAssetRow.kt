package network.o3.o3wallet.Wallet.SendV2


import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import network.o3.o3wallet.R


/**
 * A simple [Fragment] subclass.
 */
class SendAssetRow : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.wallet_send_fragment_token_row, container, false)
    }

}// Required empty public constructor
