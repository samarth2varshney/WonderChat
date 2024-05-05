package com.google.firebase.codelab.friendlychat

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import com.google.firebase.codelab.friendlychat.databinding.ActivityAddGroupBinding
import com.google.gson.Gson

class AddGroup : AppCompatActivity() {
    private lateinit var binding: ActivityAddGroupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences: SharedPreferences = getSharedPreferences("arrayListString", Context.MODE_PRIVATE)
        var prefs: SharedPreferences.Editor = sharedPreferences.edit()
        var arrayListString: String? = sharedPreferences.getString("arrayListString",null)
        var Groupnames = Gson().fromJson(arrayListString, ArrayList::class.java) as ArrayList<String>

        binding.button3.setOnClickListener {
            var string = binding.editTextTextPersonName.text.toString().trim()
            Groupnames.add(string)
            arrayListString = Gson().toJson(Groupnames)
            prefs.putString("arrayListString", arrayListString).apply()
            SharedData.notenabel = false
            startActivity(Intent(this, HomePage::class.java))
            finish()
        }
    }
}