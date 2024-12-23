package com.example.contentprovider

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contentprovider.databinding.ActivityContactSearchBinding

class ContactSearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactSearchBinding
    private var allContactList: MutableList<MyContact> = mutableListOf()
    private var contactList: MutableList<MyContact> = mutableListOf()
    private var numberToCall:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityContactSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(binding.toolbarTB)
        binding.recyclerRV.layoutManager = LinearLayoutManager(this@ContactSearchActivity)
        binding.searchBTN.setOnClickListener{
            val textSearch = binding.searchET.text
            if(textSearch.isNotEmpty()) {
                searchContact(textSearch.toString())
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            permissionContact.launch(Manifest.permission.READ_CONTACTS)
        } else {
            getContact()
        }

        Toast.makeText(this@ContactSearchActivity,allContactList.size.toString(),Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("Range")
    private fun getContact() {
        if(allContactList.isEmpty()) {
            val phones = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + "ASC"
            )
            while (phones!!.moveToNext()) {
                val name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                if (allContactList.isNotEmpty() && allContactList.last().name == name) continue
                allContactList.add(MyContact(name, phoneNumber))
            }
            phones.close()
        }
    }

    private fun searchContact(search:String) {
        contactList.clear()
        for(i in allContactList) {
            if (i.name.contains(search,true)) contactList.add(i)
        }
        initAdapter(contactList as ArrayList<MyContact>)
    }

    private fun initAdapter(contacts: MutableList<MyContact>) {
        val adapter = CustomAdapter(this,contacts)
        binding.recyclerRV.adapter = adapter
        binding.recyclerRV.setHasFixedSize(true)

    }

    private val permissionContact = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getContact()
        } else {
            Toast.makeText(this@ContactSearchActivity, "Отказано -> контакты", Toast.LENGTH_SHORT).show()
        }
    }

    fun startCall(number:String?){
        numberToCall=number
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            permissionCall.launch(Manifest.permission.CALL_PHONE)
        } else {
            callTheNumber(numberToCall)
        }
    }

    fun startSmsActivity(contact:MyContact){
        val intentSms = Intent(this@ContactSearchActivity, ContactSmsActivity::class.java)
        intentSms.putExtra("contactSms", contact)
        startActivity(intentSms)
    }

    private fun callTheNumber(number:String?) {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:$number")
        startActivity(intent)
    }

    private val permissionCall = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            callTheNumber(numberToCall)
        } else {
            Toast.makeText(this@ContactSearchActivity, "Отказано -> вызов", Toast.LENGTH_SHORT).show()
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