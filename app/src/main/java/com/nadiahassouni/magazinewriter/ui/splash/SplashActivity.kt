package com.nadiahassouni.magazinewriter.ui.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.nadiahassouni.magazinewriter.R
import com.nadiahassouni.magazinewriter.ui.main.MainActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        supportActionBar?.hide()

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        Handler().postDelayed({ openMainActivity() }, 2000)
    }

    private fun openMainActivity() {
        startActivity(Intent(this , MainActivity::class.java))
        startActivity(intent)
        finish()
    }

}