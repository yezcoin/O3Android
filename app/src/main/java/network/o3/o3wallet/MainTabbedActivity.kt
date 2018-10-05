package network.o3.o3wallet

import android.app.Activity
import android.app.Dialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.view.MenuItem
import network.o3.o3wallet.Feed.NewsFeedFragment
import network.o3.o3wallet.Portfolio.HomeFragment
import network.o3.o3wallet.Settings.SettingsFragment
import network.o3.o3wallet.Wallet.TabbedAccount
import android.content.Intent
import android.support.design.bottomnavigation.LabelVisibilityMode
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.ActionBar
import android.text.Layout
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import co.getchannel.channel.Channel
import co.getchannel.channel.callback.ChannelCallback
import co.kyash.rkd.KeyboardDetector
import co.kyash.rkd.KeyboardStatus
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.google.firebase.iid.FirebaseInstanceId
import com.google.zxing.integration.android.IntentIntegrator
import network.o3.o3wallet.MarketPlace.MarketplaceTabbedFragment
import network.o3.o3wallet.Wallet.SendV2.SendV2Activity
import org.jetbrains.anko.alert
import org.jetbrains.anko.find
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.content.res.Resources
import android.os.Handler
import android.util.Log
import android.widget.ImageButton
import com.amplitude.api.Amplitude
import com.tapadoo.alerter.Alerter
import network.o3.o3wallet.API.Switcheo.SwitcheoAPI
import zendesk.core.AnonymousIdentity
import zendesk.core.Zendesk
import zendesk.support.Support


class MainTabbedActivity : AppCompatActivity() {

    var activeTabID: Int? = 0
    var activeTabPosition: Int? = 0
    var fragments: Array<Fragment>? = arrayOf(HomeFragment.newInstance(),
            TabbedAccount.newInstance(), MarketplaceTabbedFragment.newInstance(), NewsFeedFragment.newInstance(),
            SettingsFragment.newInstance())
    var deepLink: String? = null

    override fun onBackPressed() {
        alert(resources.getString(R.string.TABBAR_logout_warning)) {
            yesButton { super.onBackPressed() }
            noButton { }
        }.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //QR-Scanned Activitry
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x0000c0de) {
            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (result != null && result.contents == null) {
                Toast.makeText(this, resources.getString(R.string.ALERT_cancelled), Toast.LENGTH_LONG).show()
            } else {
                val intent = Intent(this, SendV2Activity::class.java)
                intent.putExtra("uri", result.contents)
                startActivity(intent)
            }
        }
    }

    fun setupKeyboardDetector() {
        KeyboardDetector(this).observe().subscribe({ status ->
            when (status) {
                KeyboardStatus.OPENED -> {
                    find<BottomNavigationView>(R.id.bottom_navigation).visibility = View.GONE
                }
                KeyboardStatus.CLOSED -> {
                    find<BottomNavigationView>(R.id.bottom_navigation).visibility = View.VISIBLE
                }
            }
        })
    }

    fun setupChannel() {

        Channel.setupApplicationContextWithApplicationKey(O3Wallet.appContext, "app_gUHDmimXT8oXRSpJvCxrz5DZvUisko_mliB61uda9iY", object: ChannelCallback {
            override fun onSuccess() {
    val refreshedToken = FirebaseInstanceId.getInstance().token
                        Channel.saveDeviceToken(refreshedToken, object : ChannelCallback {
            override fun onSuccess() {}

            override fun onFail(message: String) {}
        })

                Channel.subscribeToTopic(Account.getWallet().address.toString(), object : ChannelCallback {
                    override fun onSuccess() {

                    }

                    override fun onFail(message: String) {

                    }
                })
            }

            override fun onFail(message: String) {

            }
        })
    }

    fun setupZendesk() {
        Zendesk.INSTANCE.init(this, resources.getString(R.string.Zendesk_url),
                resources.getString(R.string.Zendesk_API_key),
                resources.getString(R.string.Zendesk_client_key))
        val identity = AnonymousIdentity()
        Zendesk.INSTANCE.setIdentity(identity)
        Support.INSTANCE.init(Zendesk.INSTANCE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tabbar_activity_main_tabbed)
        if (!BuildConfig.DEBUG) {
            Amplitude.getInstance().initialize(this, resources.getString(R.string.Amplitude_API_Key)).enableForegroundTracking(application)
        }

        val selectedFragment = fragments!!.get(0)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame_layout, selectedFragment, 0.toString())
        transaction.commit()
        if (intent != null) {
            if (intent.getStringExtra("deepLink") != null) {
                deepLink = intent.getStringExtra("deepLink")
            }
        }
        setupKeyboardDetector()
        setupChannel()
        setupZendesk()
        supportActionBar?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        supportActionBar?.setCustomView(R.layout.actionbar_layout)
        find<TextView>(R.id.mytext).text = resources.getString(R.string.TABBAR_portfolio)
        find<ImageButton>(R.id.rightNavButton).visibility = View.GONE

        activeTabID = selectedFragment.id
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED)
        bottomNavigationView.setOnNavigationItemSelectedListener(object : BottomNavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                var selectedFragment: Fragment? = null
                //avoid loading the data again when tap at the same tab
                if (activeTabID == item.itemId) {
                    return false
                }

                var tabName = ""
                when (item.itemId) {
                    R.id.action_item1 -> {
                        switchFragment(0)
                        activeTabID = item.itemId
                        activeTabPosition = 0
                        tabName = "Home"
                        find<TextView>(R.id.mytext).text = resources.getString(R.string.TABBAR_portfolio)
                        find<ImageButton>(R.id.rightNavButton).visibility = View.GONE

                    }
                    R.id.action_item2 -> {
                        switchFragment(1)
                        activeTabID = item.itemId
                        activeTabPosition = 1
                        tabName = "Wallet"
                        find<TextView>(R.id.mytext).text = resources.getString(R.string.WALLET_my_o3_wallet)
                        find<ImageButton>(R.id.rightNavButton).visibility = View.VISIBLE
                    }
                    R.id.action_item3 -> {
                        switchFragment(2)
                        activeTabID = item.itemId
                        activeTabPosition = 2
                        tabName = "Marketplace"
                        find<TextView>(R.id.mytext).text = resources.getString(R.string.MARKETPLACE_marketplace)
                        find<ImageButton>(R.id.rightNavButton).visibility = View.GONE
                    }
                    R.id.action_item4 -> {
                        switchFragment(3)
                        activeTabID = item.itemId
                        activeTabPosition = 3
                        tabName = "News"
                        find<TextView>(R.id.mytext).text = resources.getString(R.string.TABBAR_news_feed)
                        find<ImageButton>(R.id.rightNavButton).visibility = View.GONE
                    }
                    R.id.action_item5 -> {
                        switchFragment(4)
                        activeTabID = item.itemId
                        activeTabPosition = 4
                        tabName = ""
                        find<TextView>(R.id.mytext).text = resources.getString(R.string.SETTINGS_more)
                        find<ImageButton>(R.id.rightNavButton).visibility = View.GONE
                    }
                }
                Answers().logCustom(CustomEvent("Tab Tapped")
                        .putCustomAttribute("Tab Name", tabName))
                return true
            }
        })
    }

    private fun switchFragment(index: Int) {

        val transaction = supportFragmentManager.beginTransaction()

        // if the fragment has not yet been added to the container, add it first
        if (supportFragmentManager.findFragmentByTag(index.toString()) == null) {
            transaction.add(R.id.frame_layout, fragments!!.get(index), index.toString())
        }

        transaction.hide(fragments!!.get(activeTabPosition!!))
        transaction.show(fragments!!.get(index))
        transaction.commit()
        if (index == 0) {
           (fragments!!.get(index) as HomeFragment).homeModel.getDisplayedAssets(false)
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this).registerReceiver((mMessageReceiver), IntentFilter("Alert"))
        LocalBroadcastManager.getInstance(this).registerReceiver(needReloadThemeReciever, IntentFilter("need-reload-theme"))
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver((mMessageReceiver))
        LocalBroadcastManager.getInstance(this).unregisterReceiver(needReloadThemeReciever)
    }

    fun getActivity(): Activity {
        return this
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

    private val needReloadThemeReciever = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            getActivity().finish()
            getActivity().startActivity(getActivity().intent)
        }
    }

    private val mMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Alerter.isShowing) {
                return
            }
            Alerter.create(getActivity())
                    .setTitle(intent.extras!!.getString("alert_title"))
                    .setText(intent.extras!!.getString("alert_message"))
                    .setBackgroundColorRes(R.color.colorPrimaryTranslucent)
                    .setIcon(R.drawable.ic_notifciation_luna)
                    .setTextAppearance(R.style.NotificationText)
                    .setTitleAppearance(R.style.NotificationTitle)
                    .setIconColorFilter(0)
                    .enableSwipeToDismiss()
                    .setDuration(3000)
                    .show()
        }
    }
}
