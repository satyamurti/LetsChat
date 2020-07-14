package com.mrspd.letschat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.BaseOnTabSelectedListener
import com.mrspd.letschat.models.ScreenItem
import com.mrspd.letschat.ui.mainActivity.MainActivity
import kotlinx.android.synthetic.main.activity_walk_through.*
import java.util.*


class WalkThroughActivity : AppCompatActivity() {
    var introViewPagerAdapter: IntroViewPagerAdapter? = null
    var position = 0
    var btnAnim: Animation? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // make the activity on full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )


        // when this activity is about to be launch we need to check if its openened before or not
        if (restorePrefData()) {
            val mainActivity = Intent(applicationContext, MainActivity::class.java)
            startActivity(mainActivity)
            finish()
        }
        setContentView(R.layout.activity_walk_through)


        // ini views
        btnAnim = AnimationUtils.loadAnimation(
            applicationContext,
            R.anim.button_animation
        )

        // fill list screen
        val mList: MutableList<ScreenItem> = ArrayList<ScreenItem>()
        mList.add(
            ScreenItem(
                "One to One Chat",
                "Chat with your freinds with simple and secure realtime messaging",
                R.drawable.ic_undraw_chatting_2yvo
            )
        )
        mList.add(
            ScreenItem(
                "Share Files",
                "Send photos, videos , audios instantly to your freinds",
                R.drawable.ic_undraw_online_connection_6778
            )
        )
        mList.add(
            ScreenItem(
                "Group Chat",
                "Create groups and add more freinds to increase your connection. No limits on the size of group chats.",
                R.drawable.ic_undraw_group_chat_unwm
            )
        )
        mList.add(
            ScreenItem(
                "Beautiful Camera",
                "Express yourself with your beautiful selfies and connect with friends",
                R.drawable.ic_undraw_selfie_time_cws4
            )
        )

        mList.add(
            ScreenItem(
                "Augmented Reality",
                "Use the power of augmented reality to bring your dream world infront of you",
                R.drawable.arselfie_card_icon
            )
        )
        mList.add(
            ScreenItem(
                "Audio and Video Calls",
                "Call your freinds and family free (Yet to implement)",
                R.drawable.ic_undraw_group_hangout_5gmq
            )
        )
        mList.add(
            ScreenItem(
                "Live Streaming",
                "Enjoy Live streaming by you or your friends", R.drawable.ic_undraw_youtube_tutorial_2gn3
            )
        )

        // setup viewpager

        introViewPagerAdapter = IntroViewPagerAdapter(this, mList)
        screenPager.setAdapter(introViewPagerAdapter)

        // setup tablayout with viewpager
        tabIndicator.setupWithViewPager(screenPager)

        // next button click Listner
        btn_next.setOnClickListener(View.OnClickListener {
            position = screenPager.getCurrentItem()
            if (position < mList.size) {
                position++
                screenPager.setCurrentItem(position)
            }
            if (position == mList.size - 1) { // when we rech to the last screen

                // TODO : show the GETSTARTED Button and hide the indicator and the next button
                loaddLastScreen()
            }
        })

        // tablayout add change listener
        tabIndicator.addOnTabSelectedListener(object : BaseOnTabSelectedListener<TabLayout.Tab?> {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab!!.position == mList.size - 1) {
                    loaddLastScreen()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })


        // Get Started button click listener
        btn_get_started.setOnClickListener(View.OnClickListener { //open main activity
            val mainActivity = Intent(applicationContext, MainActivity::class.java)
            startActivity(mainActivity)
            // also we need to save a boolean value to storage so next time when the user run the app
            // we could know that he is already checked the intro screen activity
            // i'm going to use shared preferences to that process
            savePrefsData()
            finish()
        })

        // skip button click listener
        tv_skip.setOnClickListener(View.OnClickListener { screenPager.setCurrentItem(mList.size) })
    }

    private fun restorePrefData(): Boolean {
        val pref = applicationContext.getSharedPreferences(
            "myPrefs",
            Context.MODE_PRIVATE
        )
        return pref.getBoolean("isIntroOpnend", false)
    }

    private fun savePrefsData() {
        val pref = applicationContext.getSharedPreferences(
            "myPrefs",
            Context.MODE_PRIVATE
        )
        val editor = pref.edit()
        editor.putBoolean("isIntroOpnend", true)
        editor.commit()
    }

    // show the GETSTARTED Button and hide the indicator and the next button
    private fun loaddLastScreen() {
        btn_next!!.visibility = View.INVISIBLE
        btn_get_started!!.visibility = View.VISIBLE
        tv_skip!!.visibility = View.INVISIBLE
        tabIndicator!!.visibility = View.INVISIBLE
        // TODO : ADD an animation the getstarted button
        // setup animation
        btn_get_started!!.animation = btnAnim
    }
}