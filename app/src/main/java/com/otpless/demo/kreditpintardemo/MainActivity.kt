package com.otpless.demo.kreditpintardemo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.otpless.loginpage.BuildConfig
import com.otpless.loginpage.main.OtplessController
import com.otpless.loginpage.model.CctSupportConfig
import com.otpless.loginpage.model.CctSupportType
import com.otpless.loginpage.model.CustomTabParam
import com.otpless.loginpage.model.LoginPageParams
import com.otpless.loginpage.model.OtplessResult
import com.otpless.loginpage.util.Utility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val LoginAppId = "PULP572EREYRMYBHXZJG"


class MainActivity : AppCompatActivity() {

    private lateinit var loginController: OtplessController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // create the instance of otpless login controller in onCreate of Login Activity
        loginController = OtplessController.getInstance(this)
        // enable the logging as per build config
        Utility.isLoggingEnabled = BuildConfig.DEBUG
        // initialize the login controller in onCreate for prefetch and warmup
        loginController.initializeOtpless(LoginAppId, CctSupportConfig(type = CctSupportType.Cct)) { traceId ->
            // trace id is received here for tracking the login journey
            Log.d("KreditApp", "traceId: $traceId")
        }
        // set the result callback, response will be sent in this function
        loginController.registerResultCallback(this::onLoginResult)
        findViewById<Button>(R.id.tic_start_btn).setOnClickListener { onStartClicked() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // call the onNewIntent in onNewIntent of login activity
        loginController.onNewIntent(this, intent)
    }

    private fun onStartClicked() {
        // modify the following params as per theme
        val loginPageParams = LoginPageParams(
            customTabParam = CustomTabParam(
                toolbarColor = "#FFFFFF", navigationBarColor = "#FFFFFF",
                navigationBarDividerColor = "#1CC812",
                backgroundColor = "#FFFFFF"
            ),
        )
        // startOtplessWithLoginPage inside the lifeCycleScope or viewModelScope
        lifecycleScope.launch(Dispatchers.Default) {
            loginController.startOtplessWithLoginPage(loginPageParams)
        }
    }

    private fun onLoginResult(result: OtplessResult) {
        when(result) {
            is OtplessResult.Success -> {
                // validate token with otpless
                // follow the following document
                // https://otpless.com/docs/api-reference/endpoint/verifytoken/verify-token-with-secure-data
                val token = result.token
                Toast.makeText(this, "token: $token", Toast.LENGTH_LONG).show()
            }
            is OtplessResult.Error -> {
                val traceId = result.traceId
                // log to get the trace of user journey
                Log.d("KreditApp", result.toString())
                Toast.makeText(this, "message", Toast.LENGTH_LONG).show()
                // check the doc result.errorCode
                // https://otpless.com/docs/frontend-sdks/app-sdks/android/new/references/error-codes
                //
                //
                // check the doc for result.errorType
            }
        }
    }
}