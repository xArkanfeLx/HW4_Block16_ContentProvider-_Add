package com.example.contentprovider

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class CustomAdapter(val activity: Activity, private val items: MutableList<MyContact>) :
    RecyclerView.Adapter<CustomAdapter.ContactViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(myContact: MyContact, position: Int)
    }

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTV: TextView = itemView.findViewById(R.id.nameTV)
        val phoneTV: TextView = itemView.findViewById(R.id.phoneTV)
        val callIV: ImageView = itemView.findViewById(R.id.callIV)
        val smsIV: ImageView = itemView.findViewById(R.id.smsIV)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate((R.layout.list_item), parent, false)
        return ContactViewHolder(itemView)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = items[position]
        holder.nameTV.text = contact.name
        holder.phoneTV.text = contact.phone

        holder.callIV.setOnClickListener{
            when (activity) {
                is MainActivity -> activity.startCall(contact.phone)
                is ContactSearchActivity -> activity.startCall(contact.phone)
            }
        }
        holder.smsIV.setOnClickListener{
            when (activity) {
                is MainActivity -> activity.startSmsActivity(contact)
                is ContactSearchActivity -> activity.startSmsActivity(contact)
            }
        }

        /*holder.itemView.setOnClickListener {
            if (onItemClickListener != null) {
                onItemClickListener!!.onItemClick(contact, position)
            }
        }*/
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }
}