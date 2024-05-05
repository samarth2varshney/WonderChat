package com.google.firebase.codelab.friendlychat
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Button
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.*
import kotlin.collections.ArrayList



class MainActivity2 : AppCompatActivity() {
    private lateinit var  sourcelanguageEt: EditText
    private lateinit var  targetlanguageTv: TextView
    private lateinit var sourceLanguagechoosebtn:Button
    private lateinit var targetlanguagechoosebtn:Button
    private lateinit var translatebtn:Button
    private lateinit var auth: FirebaseAuth
    private lateinit var signoutBtn: Button

    private var languageArrayList:ArrayList<ModleLanguage>?=null

    companion object{
        private const val TAG="MAIN_TAG"
    }

    private lateinit var translatoroptions: TranslatorOptions

    private lateinit var translator: Translator

    private lateinit var progressDialog: ProgressDialog

    private var sourceLanguageCode = "en"
    private var sourceLanguageTitle = "English"
    private var targetLanguageCode = "hi"
    private var targetLanguageTitle = "Hindi"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        sourcelanguageEt=findViewById(R.id.sourcelanguageEt)
        targetlanguageTv=findViewById(R.id.targetlanguageTv)
        sourceLanguagechoosebtn=findViewById(R.id.sourcelanguagechoosebtn)
        targetlanguagechoosebtn=findViewById(R.id.targetlanguagechoosebtn)
        translatebtn=findViewById(R.id.translatebtn)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        loadAvialablelanguage()
        auth = FirebaseAuth.getInstance()
        signoutBtn = findViewById(R.id.signoutBtn)

        signoutBtn.setOnClickListener {
            auth.signOut()
            //startActivity(Intent(this, PhoneActivity::class.java))
        }

        findViewById<Button>(R.id.cryptographybtn).setOnClickListener {
            //startActivity(Intent(this,cryptography::class.java))
        }
        sourceLanguagechoosebtn.setOnClickListener{
            sourceLanguageChoose()
        }
        targetlanguagechoosebtn.setOnClickListener{
            targetLanguageChoose()
        }
        translatebtn.setOnClickListener {
            validateData()
        }
    }
    private var sourceLanguageText = ""
    private fun validateData() {
        sourceLanguageText = sourcelanguageEt.text.toString().trim()
        if(sourceLanguageText.isEmpty()){
            Toast.makeText(this,"Enter text to translate",Toast.LENGTH_LONG).show()
        }
        else{
            startTranslation()
        }
    }

    private fun startTranslation() {
        progressDialog.setMessage("Processing language model")
        progressDialog.show()

        translatoroptions = TranslatorOptions.Builder().setSourceLanguage(sourceLanguageCode).setTargetLanguage(targetLanguageCode).build()
        translator = Translation.getClient(translatoroptions)

        val downloadConditions = DownloadConditions.Builder().requireWifi().build()

        translator.downloadModelIfNeeded(downloadConditions)
        .addOnSuccessListener {
            progressDialog.setMessage("translating")
            translator.translate(sourceLanguageText)
                .addOnSuccessListener {translatedText ->
                    progressDialog.dismiss()
                    targetlanguageTv.text = translatedText
                }.addOnFailureListener{ e->
                    progressDialog.dismiss()
                    Toast.makeText(this,"failed to translate due to ${e.message}",Toast.LENGTH_LONG).show()
                }
        }.addOnFailureListener{ e->
            progressDialog.dismiss()
                Toast.makeText(this,"failed to translate due to ${e.message}",Toast.LENGTH_LONG).show()
        }

    }

    private fun loadAvialablelanguage() {
        languageArrayList= ArrayList()
        val languagecodelist = TranslateLanguage.getAllLanguages()
        for(languagecode in languagecodelist){
            val languageTitle= Locale(languagecode).displayLanguage
            Log.d(TAG,"loadavilable languages: languagecode:$languagecode")
            Log.d(TAG,"loadavilable languages: languagecode:$languageTitle")

            val modeLanguage = ModleLanguage(languagecode,languageTitle)
            languageArrayList!!.add(modeLanguage)
        }
    }
    private fun sourceLanguageChoose(){
        val popupMenu = PopupMenu(this,sourceLanguagechoosebtn)
        for(i in languageArrayList!!.indices){
            popupMenu.menu.add(Menu.NONE,i,i,languageArrayList!![i].languagetitle)
        }
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { menuItem ->

            val position = menuItem.itemId
            sourceLanguageCode = languageArrayList!![position].languagecode
            sourceLanguageTitle=languageArrayList!![position].languagetitle

            sourceLanguagechoosebtn.text = sourceLanguageTitle
            sourcelanguageEt.hint = "Enter $sourceLanguageTitle"

            false
        }
    }
    private fun targetLanguageChoose(){
        val popupMenu = PopupMenu(this,targetlanguagechoosebtn)
        for(i in languageArrayList!!.indices){
            popupMenu.menu.add(Menu.NONE,i,i,languageArrayList!![i].languagetitle)
        }
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener { menuItem ->

            val position = menuItem.itemId
            targetLanguageCode = languageArrayList!![position].languagecode
            targetLanguageTitle=languageArrayList!![position].languagetitle

            targetlanguagechoosebtn.text = targetLanguageTitle

            false
        }
    }
}