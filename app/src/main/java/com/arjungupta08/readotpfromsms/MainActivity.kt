package com.arjungupta08.readotpfromsms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.telephony.SmsMessage
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private var selectedETPosition: Int = 0
    private val otpET1: EditText
        get() = findViewById(R.id.otpET1)
    private val otpET2: EditText
        get() = findViewById(R.id.otpET2)
    private val otpET3: EditText
        get() = findViewById(R.id.otpET3)
    private val otpET4: EditText
        get() = findViewById(R.id.otpET4)
    private val otpET5: EditText
        get() = findViewById(R.id.otpET5)
    private val otpET6: EditText
        get() = findViewById(R.id.otpET6)

    /* ---RESEND-OTP---*/
    private val resendOTP: TextView
        get() = findViewById(R.id.text_resend_otp)
    private val countDownTimer: TextView
        get() = findViewById(R.id.countDownTimer)

    /* ---VERIFY-OTP-Btn---*/
    private val cardVerifyOTP : CardView
        get() = findViewById(R.id.card_verify_otp)


    private val SMS_PERMISSION_REQUEST_CODE = 123
    private val SMS_RECEIVE_ACTION = "android.provider.Telephony.SMS_RECEIVED"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        otpET1.addTextChangedListener(textWatcher)
        otpET2.addTextChangedListener(textWatcher)
        otpET3.addTextChangedListener(textWatcher)
        otpET4.addTextChangedListener(textWatcher)
        otpET5.addTextChangedListener(textWatcher)
        otpET6.addTextChangedListener(textWatcher)

        // Check and request SMS permission
        if (!checkSmsPermission()) {
            requestSmsPermission()
        } else {
            registerSmsReceiver()
        }

        showKeyBoard(otpET1)

        /* ---RESEND-OTP---*/
        resendOTP.setOnClickListener {
            resendOTP.visibility = View.GONE
            countDownTimer.visibility = View.VISIBLE
            val timer = object: CountDownTimer(60000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    countDownTimer.text =
                        getString(R.string.countdownTimer, "${millisUntilFinished / 1000}")
//                    sendVerificationCode(phone)
                }

                override fun onFinish() {
                    resendOTP.visibility = View.VISIBLE
                    countDownTimer.visibility = View.GONE
                }
            }
            timer.start()
        }

        cardVerifyOTP.setOnClickListener {
            val otp1 = otpET1.text.toString()
            val otp2 = otpET2.text.toString()
            val otp3 = otpET3.text.toString()
            val otp4 = otpET4.text.toString()
            val otp5 = otpET5.text.toString()
            val otp6 = otpET6.text.toString()
            val otp = "$otp1$otp2$otp3$otp4$otp5$otp6"

            if (otp.isNotEmpty()) {
                Toast.makeText(this, otp, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Enter OTP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkSmsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestSmsPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.RECEIVE_SMS),
            SMS_PERMISSION_REQUEST_CODE
        )
    }

    private fun registerSmsReceiver() {
        val intentFilter = IntentFilter(SMS_RECEIVE_ACTION)
        registerReceiver(smsReceiver, intentFilter)
    }

    private val smsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == SMS_RECEIVE_ACTION) {
                val bundle = intent.extras
                if (bundle != null) {
                    val pdus = bundle.get("pdus") as Array<Any>?
                    if (pdus != null) {
                        for (pdu in pdus) {
                            val smsMessage = getIncomingMessage(pdu as ByteArray, bundle)
                            val sender = smsMessage.displayOriginatingAddress
                            val messageBody = smsMessage.messageBody
                            // Here you can extract the OTP from messageBody
                            Toast.makeText(applicationContext, "Message: $messageBody", Toast.LENGTH_SHORT).show()
                            Log.d("SMSReceiver", "Sender: $sender, Message: $messageBody")
                        }
                    }
                }
            }
        }
    }

    private fun getIncomingMessage(aObject: Any, bundle: Bundle): SmsMessage {
        val format = bundle.getString("format")
        return SmsMessage.createFromPdu(aObject as ByteArray, format)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(smsReceiver)
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }
        override fun afterTextChanged(s: Editable?) {
            if (s!!.isNotEmpty()) {
                when (selectedETPosition) {
                    0 -> {
                        selectedETPosition = 1
                        showKeyBoard(otpET2)
                    }
                    1 -> {
                        selectedETPosition = 2
                        showKeyBoard(otpET3)
                    }
                    2 -> {
                        selectedETPosition = 3
                        showKeyBoard(otpET4)
                    }
                    3 -> {
                        selectedETPosition = 4
                        showKeyBoard(otpET5)
                    }
                    4 -> {
                        selectedETPosition = 5
                        showKeyBoard(otpET6)
                    }
                    5 -> {
                        cardVerifyOTP.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
    private fun showKeyBoard(otpET: EditText) {
        otpET.requestFocus()
        val inputMethodManager: InputMethodManager =
            baseContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(otpET, InputMethodManager.SHOW_IMPLICIT)
    }
    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DEL) {
            when (selectedETPosition) {
                5 -> {
                    selectedETPosition = 4
                    showKeyBoard(otpET6)
                }
                4 -> {
                    selectedETPosition = 3
                    showKeyBoard(otpET5)
                }
                3 -> {
                    selectedETPosition = 2
                    showKeyBoard(otpET4)
                }
                2 -> {
                    selectedETPosition = 1
                    showKeyBoard(otpET3)
                }
                1 -> {
                    selectedETPosition = 0
                    showKeyBoard(otpET2)
                }
                0 -> {
                    selectedETPosition = 0
                    showKeyBoard(otpET1)
                }
            }
            cardVerifyOTP.visibility = View.GONE
            return true
        } else {
            return super.onKeyUp(keyCode, event)
        }
    }
}
