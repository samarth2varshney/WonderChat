package com.google.firebase.codelab.friendlychat

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.widget.Button
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.codelab.friendlychat.SharedData.languageArrayList
import com.google.firebase.codelab.friendlychat.databinding.ActivityHomePageBinding
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.mlkit.nl.translate.TranslateLanguage
import java.util.*
import kotlin.collections.ArrayList

class HomePage : AppCompatActivity() {
    private lateinit var binding: ActivityHomePageBinding
    private lateinit var auth: FirebaseAuth
    private var sourceLanguageTitle = "English"
    private var targetLanguageTitle = "Hindi"
    //private var languageArrayList = SharedData.languageArrayList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        if (auth.currentUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }

        loadAvialablelanguage()

        val sharedPreferences: SharedPreferences = getSharedPreferences("arrayListString", Context.MODE_PRIVATE)
        var arrayListString: String? = sharedPreferences.getString("arrayListString",null)
        var Groupnames = Gson().fromJson(arrayListString, ArrayList::class.java)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = RecyclerViewAdapter(Groupnames as ArrayList<String>)
        binding.recyclerView.adapter = adapter

        val mintent = Intent(this, MainActivity::class.java)
        adapter.setOnItemClickListener(object : RecyclerViewAdapter.onItemClickListener{
            override fun onItemClick(position: Int) {
                mintent.putExtra("MESSAGES_CHILD",Groupnames[position])
                SharedData.Message_Child = Groupnames[position]
                startActivity(mintent)
            }
        })

        binding.floatingActionButton2.setOnClickListener {
            startActivity(Intent(this,AddGroup::class.java))
        }

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
}