package com.google.firebase.codelab.friendlychat

import android.content.Intent

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.BuildConfig
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.codelab.friendlychat.SharedData.languageArrayList
import com.google.firebase.codelab.friendlychat.databinding.ActivityMainBinding
import com.google.firebase.codelab.friendlychat.model.FriendlyMessage
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.mlkit.nl.translate.TranslateLanguage
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var manager: LinearLayoutManager
    private var targetLanguageTitle = "Hindi"
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var adapter: FriendlyMessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val MESSAGES_CHILD = SharedData.Message_Child
        loadAvialablelanguage()

        if (BuildConfig.DEBUG) {
            Firebase.database.useEmulator("10.0.2.2", 9000)
            Firebase.auth.useEmulator("10.0.2.2", 9099)
            Firebase.storage.useEmulator("10.0.2.2", 9199)
        }

        // Initialize Firebase Auth and check if the user is signed in
        auth = Firebase.auth
        if (auth.currentUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }

        // Initialize Realtime Database
        db = Firebase.database
        val messagesRef = db.reference.child(MESSAGES_CHILD!!)

        // The FirebaseRecyclerAdapter class and options come from the FirebaseUI library
        // See: https://github.com/firebase/FirebaseUI-Android
        val options = FirebaseRecyclerOptions.Builder<FriendlyMessage>()
            .setQuery(messagesRef, FriendlyMessage::class.java)
            .build()
        adapter = FriendlyMessageAdapter(options, getUserName())
        manager = LinearLayoutManager(this)
        manager.stackFromEnd = true
        binding.messageRecyclerView.layoutManager = manager
        binding.messageRecyclerView.adapter = adapter

        binding.addMessageImageView.setOnClickListener {
            val popupMenu = PopupMenu(this, binding.addMessageImageView)
            for (i in languageArrayList!!.indices) {
                popupMenu.menu.add(Menu.NONE, i, i,languageArrayList!![i].languagetitle)
            }
            popupMenu.show()
            popupMenu.setOnMenuItemClickListener { menuItem ->

                val position = menuItem.itemId
                SharedData.targetLanguageCode = languageArrayList!![position].languagecode

                finish()
                startActivity(Intent(this,MainActivity::class.java))

                false
            }
        }

        // Scroll down when a new message arrives
        // See MyScrollToBottomObserver for details
        adapter.registerAdapterDataObserver(
            MyScrollToBottomObserver(binding.messageRecyclerView, adapter, manager)
        )

        // Disable the send button when there's no text in the input field
        // See MyButtonObserver for details
        binding.messageEditText.addTextChangedListener(MyButtonObserver(binding.sendButton))

        // When the send button is clicked, send a text message
        binding.sendButton.setOnClickListener {
            val mesage = AESEncyption.encrypt(binding.messageEditText.text.toString())
//            val ency = AESEncyption.encrypt(binding.messageEditText.text.toString())
//            Log.i("samarth",ency)
//            val dec = AESEncyption.decrypt(ency)
//            Log.i("samarth",dec)
            val friendlyMessage = FriendlyMessage(
                mesage,
                getUserName(),
                getPhotoUrl(),
                null
            )
            db.reference.child(MESSAGES_CHILD).push().setValue(friendlyMessage)
            binding.messageEditText.setText("")
        }

    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in.
        if (auth.currentUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }
    }

    public override fun onPause() {
        adapter.stopListening()
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
        adapter.startListening()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sign_out_menu -> {
                signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun signOut() {
        AuthUI.getInstance().signOut(this)
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }

    private fun getPhotoUrl(): String? {
        val user = auth.currentUser
        return user?.photoUrl?.toString()
    }

    private fun getUserName(): String? {
        val user = auth.currentUser
        return if (user != null) {
            user.displayName
        } else ANONYMOUS
    }

    private fun Encryption(encryptmessage: String): Any {
        val s = encryptmessage
        val ini = "11111111"

        var cu = 0

        val arr = IntArray(11111111)

        for (i in 0 until s.length) {
            arr[i] = s[i].code
            cu++
        }
        var res = ""
        val bin = IntArray(111)
        var idx = 0

        for (i1 in 0 until cu) {
            var temp = arr[i1]
            for (j in 0 until cu) bin[j] = 0
            idx = 0
            while (temp > 0) {
                bin[idx++] = temp % 2
                temp = temp / 2
            }
            var dig = ""
            var temps: String

            for (j in 0..6) {

                temps = Integer.toString(bin[j])

                dig = dig + temps
            }
            var revs = ""

            for (j in dig.length - 1 downTo 0) {
                val ca = dig[j]
                revs = revs + ca.toString()
            }
            res = res + revs
        }
        var comp:String = ini+res
        var a:String = ""
        var i:Int = 0
        var n = comp.length
        while(i<n){
            if(comp[i]=='1'){
                var count: Int = 0
                while(i<n&&comp[i]=='1'&&count<9){
                    count++
                    i++
                }
                a =a + count.toString()
                a= a + "1"
            }
            else if(comp[i]=='0'){
                var count: Int = 0
                while(i<n&&comp[i]=='0'&&count<9){
                    count++
                    i=i+1
                }
                a =a + count.toString()
                a= a + "0"
            }
            else
                i = i + 1
        }
        return a
    }

    private fun loadAvialablelanguage() {
        languageArrayList= ArrayList()
        val languagecodelist = TranslateLanguage.getAllLanguages()
        for(languagecode in languagecodelist){
            val languageTitle= Locale(languagecode).displayLanguage

            val modeLanguage = ModleLanguage(languagecode,languageTitle)
            languageArrayList!!.add(modeLanguage)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        const val ANONYMOUS = "anonymous"
        private const val LOADING_IMAGE_URL = "https://www.google.com/images/spin-32.gif"
    }
}
