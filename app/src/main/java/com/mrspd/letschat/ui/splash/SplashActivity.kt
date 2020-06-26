package com.mrspd.letschat.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mrspd.letschat.ui.mainActivity.MainActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //show logo till app finish loading
        startActivity(Intent(this, MainActivity::class.java))
        finish()


    }
}
