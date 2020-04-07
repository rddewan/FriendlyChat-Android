package com.richarddewan.friendlychatapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

class MessageAdapter constructor(context: Context, resource: Int, objects: List<FriendlyMessage>):
    ArrayAdapter<FriendlyMessage>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        view = if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            inflater.inflate(R.layout.item_message, parent,false)

        } else {
            convertView
        }

        val photoImageView =
            view.findViewById<View>(R.id.photoImageView) as ImageView
        val messageTextView =
            view.findViewById<View>(R.id.messageTextView) as TextView
        val authorTextView =
            view.findViewById<View>(R.id.nameTextView) as TextView

        val message: FriendlyMessage? = getItem(position)

        val isPhoto = message?.photoUrl != null
        if (isPhoto) {
            messageTextView.visibility = View.GONE
            photoImageView.visibility = View.VISIBLE
            Glide.with(photoImageView.context)
                .load(message?.photoUrl)
                .into(photoImageView)
        } else {
            messageTextView.visibility = View.VISIBLE
            photoImageView.visibility = View.GONE
            messageTextView.text = message?.text
        }
        authorTextView.text = message?.name

        return view
    }


}