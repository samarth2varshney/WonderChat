/**
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.codelab.friendlychat

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.codelab.friendlychat.MainActivity.Companion.ANONYMOUS
import com.google.firebase.codelab.friendlychat.databinding.ImageMessageBinding
import com.google.firebase.codelab.friendlychat.databinding.MessageBinding
import com.google.firebase.codelab.friendlychat.databinding.RecievermessageBinding
import com.google.firebase.codelab.friendlychat.model.FriendlyMessage
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import java.util.*
import kotlin.collections.ArrayList
// The FirebaseRecyclerAdapter class and options come from the FirebaseUI library
// See: https://github.com/firebase/FirebaseUI-Android
class FriendlyMessageAdapter(
    private val options: FirebaseRecyclerOptions<FriendlyMessage>,
    private val currentUserName: String?
) : FirebaseRecyclerAdapter<FriendlyMessage, ViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_TEXT) {
            val view = inflater.inflate(R.layout.message, parent, false)
            val binding = MessageBinding.bind(view)
            MessageViewHolder(binding)
        }
        else if(viewType == VIEW_RTYPE_TEXT){
            val view = inflater.inflate(R.layout.recievermessage, parent, false)
            val binding2 = RecievermessageBinding.bind(view)
            MessageViewHolder2(binding2)
        }
        else {
            val view = inflater.inflate(R.layout.image_message, parent, false)
            val binding = ImageMessageBinding.bind(view)
            ImageMessageViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: FriendlyMessage) {
        if (options.snapshots[position].text != null&&options.snapshots[position].name!= null&&options.snapshots[position].name!= currentUserName) {
            (holder as MessageViewHolder).bind(model)
        }
        else if(options.snapshots[position].text != null&&options.snapshots[position].name!= null&&options.snapshots[position].name== currentUserName) {
            (holder as MessageViewHolder2).bind(model)
        }
        else {
            (holder as ImageMessageViewHolder).bind(model)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val userName = options.snapshots[position].name
        return if (userName != ANONYMOUS && currentUserName != userName && userName != null)
            VIEW_TYPE_TEXT
        else if(userName != ANONYMOUS && currentUserName == userName && userName != null)
            VIEW_RTYPE_TEXT
        else
            VIEW_TYPE_IMAGE
    }

    inner class MessageViewHolder2(private val binding: RecievermessageBinding) : ViewHolder(binding.root) {

        fun bind(item: FriendlyMessage) {

            val mesage = AESEncyption.decrypt(item.text!!)

            binding.RmessageTextView.text = mesage

            setTextColor(item.name, binding.RmessageTextView)
            binding.RmessengerTextView.text = item.name ?: ANONYMOUS
            if (item.photoUrl != null) {
                loadImageIntoView(binding.RmessengerImageView, item.photoUrl)
            } else {
                binding.RmessengerImageView.setImageResource(R.drawable.ic_account_circle_black_36dp)
            }

        }

        private fun setTextColor(userName: String?, textView: TextView) {
            if (userName != ANONYMOUS && currentUserName == userName && userName != null) {
                textView.setBackgroundResource(R.drawable.rounded_message_blue)
                textView.setTextColor(Color.BLACK)
            } else {
                textView.setBackgroundResource(R.drawable.rounded_message_gray)
                textView.setTextColor(Color.BLACK)
            }
        }

    }

    inner class MessageViewHolder(private val binding: MessageBinding) : ViewHolder(binding.root) {

        private var sourceLanguageCode = SharedData.sourceLanguageCode
        private var sourceLanguageTitle = "English"
        private var targetLanguageCode = SharedData.targetLanguageCode
        private var targetLanguageTitle = "Hindi"
        private var sourceLanguageText = ""
        private lateinit var translatoroptions: TranslatorOptions
        private lateinit var translator: Translator

        fun bind(item: FriendlyMessage) {

            val mesage = AESEncyption.decrypt(item.text!!)

            binding.messageTextView.text = mesage
            sourceLanguageText = mesage
            binding.tranlation.text = mesage
            startTranslation()
            setTextColor(item.name, binding.messageTextView)
            binding.messengerTextView.text = item.name ?: ANONYMOUS
            if (item.photoUrl != null) {
                loadImageIntoView(binding.messengerImageView, item.photoUrl)
            } else {
                binding.messengerImageView.setImageResource(R.drawable.ic_account_circle_black_36dp)
            }
        }


        private fun setTextColor(userName: String?, textView: TextView) {
            if (userName != ANONYMOUS && currentUserName == userName && userName != null) {
                textView.setBackgroundResource(R.drawable.rounded_message_blue)
                textView.setTextColor(Color.BLACK)
            } else {
                textView.setBackgroundResource(R.drawable.rounded_message_gray)
                textView.setTextColor(Color.BLACK)
            }
        }
        private fun startTranslation() {

            translatoroptions = TranslatorOptions.Builder().setSourceLanguage(sourceLanguageCode).setTargetLanguage(targetLanguageCode).build()
            translator = Translation.getClient(translatoroptions)

            val downloadConditions = DownloadConditions.Builder().requireWifi().build()

            translator.downloadModelIfNeeded(downloadConditions)
                .addOnSuccessListener {
                    translator.translate(sourceLanguageText)
                        .addOnSuccessListener {translatedText ->
                            binding.tranlation.text = translatedText
                        }.addOnFailureListener{ e->
                           // Toast.makeText(this,"failed to translate due to ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }.addOnFailureListener{ e->
                   // Toast.makeText(this,"failed to translate due to ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    inner class ImageMessageViewHolder(private val binding: ImageMessageBinding) :
        ViewHolder(binding.root) {
        fun bind(item: FriendlyMessage) {
            loadImageIntoView(binding.messageImageView, item.imageUrl!!, false)

            binding.messengerTextView.text = item.name ?: ANONYMOUS
            if (item.photoUrl != null) {
                loadImageIntoView(binding.messengerImageView, item.photoUrl)
            } else {
                binding.messengerImageView.setImageResource(R.drawable.ic_account_circle_black_36dp)
            }
        }
    }

    private fun loadImageIntoView(view: ImageView, url: String, isCircular: Boolean = true) {
        if (url.startsWith("gs://")) {
            val storageReference = Firebase.storage.getReferenceFromUrl(url)
            storageReference.downloadUrl
                .addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    loadWithGlide(view, downloadUrl, isCircular)
                }
                .addOnFailureListener { e ->
                    Log.w(
                        TAG,
                        "Getting download url was not successful.",
                        e
                    )
                }
        } else {
            loadWithGlide(view, url, isCircular)
        }
    }

    private fun loadWithGlide(view: ImageView, url: String, isCircular: Boolean = true) {
        Glide.with(view.context).load(url).into(view)
        var requestBuilder = Glide.with(view.context).load(url)
        if (isCircular) {
            requestBuilder = requestBuilder.transform(CircleCrop())
        }
        requestBuilder.into(view)
    }


    companion object {
        const val TAG = "MessageAdapter"
        const val VIEW_TYPE_TEXT = 1
        const val VIEW_RTYPE_TEXT = 3
        const val VIEW_TYPE_IMAGE = 2

        private fun Decryption(toString: String): Any {
            var s = toString
            var i=0
            var x =""
            var n=s.length
            while(i<n){
                if(i%2==0){
                    var len:Int = s[i].code-48
                    var z:Int = 0
                    while(z<len){
                        x= x+s[i+1]
                        z++
                    }
                }
                i++
            }
            s=x
            val invalid = "Invalid Code"

            val ini = "11111111"
            var flag = true

            for (i in 0..7) {
                if (ini[i] != s[i]) {
                    flag = false
                    break
                }
            }
            var `val` = ""

            for (i in 8 until s.length) {
                val ch = s[i]
                `val` = `val` + ch.toString()
            }

            val arr = Array(11101) { IntArray(8) }
            var ind1 = -1
            var ind2 = 0

            for (i in 0 until `val`.length) {

                if (i % 7 == 0) {

                    ind1++
                    ind2 = 0
                    val ch = `val`[i]
                    arr[ind1][ind2] = ch.code - '0'.code
                    ind2++
                } else {

                    val ch = `val`[i]
                    arr[ind1][ind2] = ch.code - '0'.code
                    ind2++
                }
            }
            val num = IntArray(11111)
            var nind = 0
            var tem = 0
            var cu = 0

            for (i in 0..ind1) {
                cu = 0
                tem = 0

                for (j in 6 downTo 0) {
                    val tem1 = Math.pow(2.0, cu.toDouble()).toInt()
                    tem += arr[i][j] * tem1
                    cu++
                }
                num[nind++] = tem
            }
            var ret = ""
            var ch: Char

            for (i in 0 until nind) {
                ch = num[i].toChar()
                ret = ret + ch.toString()
            }
            if (`val`.length % 7 == 0 && flag == true) {
                return ret
            } else {
                return invalid
            }
        }

    }
}
