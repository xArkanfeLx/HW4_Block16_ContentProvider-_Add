package com.example.contentprovider

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.contentprovider.databinding.ActivityMainBinding
import android.Manifest
import android.content.ContentProviderOperation
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.provider.ContactsContract.RawContacts
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var contactList: MutableList<MyContact>? = null
    private var numberToCall:String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setSupportActionBar(binding.toolbarTB)
        binding.recyclerRV.layoutManager = LinearLayoutManager(this@MainActivity)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            permissionContact.launch(Manifest.permission.READ_CONTACTS)
        } else {
            getContact()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.addBTN.setOnClickListener{
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                permissionContact.launch(Manifest.permission.WRITE_CONTACTS)
            } else {
                Toast.makeText(this@MainActivity,"Добавляем",Toast.LENGTH_SHORT).show()
                addContact()
            }
        }
    }

    private fun addContact() {
        val newContactName = binding.nameET.text.toString()
        val newContactPhone = binding.phoneET.text.toString()
        val listCPO = ArrayList<ContentProviderOperation>()

        listCPO.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
            .withValue(RawContacts.ACCOUNT_TYPE,null)
            .withValue(RawContacts.ACCOUNT_NAME,null).build())

        listCPO.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,0)
            .withValue(ContactsContract.Data.MIMETYPE,StructuredName.CONTENT_ITEM_TYPE)
            .withValue(StructuredName.DISPLAY_NAME,newContactName).build())
        listCPO.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,0)
            .withValue(ContactsContract.Data.MIMETYPE,Phone.CONTENT_ITEM_TYPE)
            .withValue(Phone.NUMBER,newContactPhone).withValue(Phone.TYPE,Phone.TYPE_MOBILE).build())

        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY,listCPO)
            getContact()
        } catch (e:Exception) {
            Log.e("Exception ",e.message!!)
        }
    }

    @SuppressLint("Range")
    private fun getContact() {
        Toast.makeText(this@MainActivity,"Выводим контакты",Toast.LENGTH_SHORT).show()
        contactList = ArrayList()
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
            if(contactList!!.isNotEmpty() && contactList!!.last().name == name) continue
            contactList?.add(MyContact(name, phoneNumber))
        }
        phones.close()
        initAdapter(contactList as ArrayList<MyContact>)
    }

    private val permissionContact = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getContact()
        } else {
            Toast.makeText(this@MainActivity, "Отказано -> контакты", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initAdapter(contacts: MutableList<MyContact>) {
        val adapter = CustomAdapter(this,contacts)
        binding.recyclerRV.adapter = adapter
        binding.recyclerRV.setHasFixedSize(true)
        /*adapter.setOnItemClickListener(object :
            CustomAdapter.OnItemClickListener {
            override fun onItemClick(myContact: MyContact, position: Int) {
                val intent = Intent(this@MainActivity, ContactActivity::class.java)
                intent.putExtra("contact", myContact)
                startActivity(intent)
            }
        })*/
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
        val intentSms = Intent(this@MainActivity, ContactSmsActivity::class.java)
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
            Toast.makeText(this@MainActivity, "Отказано -> вызов", Toast.LENGTH_SHORT).show()
        }
    }

    private val permissionWriteContact = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this@MainActivity, "Разрешено -> запись", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this@MainActivity, "Отказано -> запись", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.exit -> finishAffinity()
            R.id.search -> startSearchIntent()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun startSearchIntent() {
        startActivity(Intent(this@MainActivity,ContactSearchActivity()::class.java))
    }
}