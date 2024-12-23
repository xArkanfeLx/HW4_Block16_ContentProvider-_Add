package com.example.contentprovider

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.contentprovider.databinding.ActivityContactSmsBinding

class ContactSmsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactSmsBinding
    private lateinit var contact: MyContact

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityContactSmsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(binding.toolbarTB)
        contact = intent.getSerializableExtra("contactSms") as MyContact
        binding.smsIV.setOnClickListener{
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                permissionSms.launch(Manifest.permission.SEND_SMS)
            } else {
                sendSms(contact.phone)
            }
        }
    }

    fun sendSms(phoneNumber: String) {
        if(binding.messageET.text.isEmpty()) return
        val message = binding.messageET.text.toString().trim()
        try {
            val smsManager: SmsManager
            if (Build.VERSION.SDK_INT >= 23) {
                smsManager = this.getSystemService(SmsManager::class.java)
            } else {
                smsManager = SmsManager.getDefault()
            }
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Toast.makeText(applicationContext, "Message Sent", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(applicationContext, "Please enter all the data.." + e.message.toString(), Toast.LENGTH_LONG).show()
        }
    }

    private val permissionSms = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            sendSms(contact.phone)
        } else {
            Toast.makeText(this@ContactSmsActivity, "Отказано -> сообщение", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_dop, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }
}