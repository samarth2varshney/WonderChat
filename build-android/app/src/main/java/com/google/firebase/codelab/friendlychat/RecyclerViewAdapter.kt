package com.google.firebase.codelab.friendlychat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewAdapter(private val myDataset: ArrayList<String>) :
    RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>() {

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener{
        fun onItemClick(position: Int){

        }
    }

    fun setOnItemClickListener(listener: onItemClickListener){
        mListener = listener
    }

    class MyViewHolder(val view: View,listener: onItemClickListener) : RecyclerView.ViewHolder(view){
        val textview = view.findViewById<TextView>(R.id.textView)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewAdapter.MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.my_text_view, parent, false)
        return MyViewHolder(view,mListener)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.textview.text = myDataset[position]
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}