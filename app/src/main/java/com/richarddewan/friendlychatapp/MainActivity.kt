package com.richarddewan.friendlychatapp

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    val ANONYMOUS = "anonymous"
    val DEFAULT_MSG_LENGTH_LIMIT = 1000

    private var mMessageListView: ListView? = null
    private lateinit var mMessageAdapter: MessageAdapter
    private var mProgressBar: ProgressBar? = null
    private var mPhotoPickerButton: ImageButton? = null
    private var mMessageEditText: EditText? = null
    private var mSendButton: Button? = null

    private var mUsername: String? = null

    // Write a message to the database
    var mFirebaseDatabase = FirebaseDatabase.getInstance()
    lateinit var mMessageDatabaseReference: DatabaseReference
    lateinit var mChildEventListener: ChildEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mUsername = ANONYMOUS

        mFirebaseDatabase = FirebaseDatabase.getInstance()
        mMessageDatabaseReference = mFirebaseDatabase.reference.child("message")

        // Initialize references to views


        // Initialize references to views
        mProgressBar = findViewById<View>(R.id.progressBar) as ProgressBar
        mMessageListView =
            findViewById<View>(R.id.messageListView) as ListView
        mPhotoPickerButton = findViewById<View>(R.id.photoPickerButton) as ImageButton
        mMessageEditText = findViewById<View>(R.id.messageEditText) as EditText
        mSendButton = findViewById<View>(R.id.sendButton) as Button

        // Initialize message ListView and its adapter

        // Initialize message ListView and its adapter
        val friendlyMessages: List<FriendlyMessage> =
            ArrayList<FriendlyMessage>()
        mMessageAdapter = MessageAdapter(this, R.layout.item_message, friendlyMessages)
        mMessageListView!!.adapter = mMessageAdapter

        // Initialize progress bar

        // Initialize progress bar
        mProgressBar!!.visibility = ProgressBar.INVISIBLE

        // ImagePickerButton shows an image picker to upload a image for a message

        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton!!.setOnClickListener {
            // TODO: Fire an intent to show an image picker
        }

        // Enable Send button when there's text to send

        // Enable Send button when there's text to send
        mMessageEditText!!.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mSendButton!!.isEnabled = s.toString().trim().isNotEmpty()

            }

        })

        mMessageEditText!!.filters =
            arrayOf<InputFilter>(LengthFilter(DEFAULT_MSG_LENGTH_LIMIT))

        // Send button sends a message and clears the EditText
        mSendButton!!.setOnClickListener {
            val friendlyMessage =
                FriendlyMessage(mMessageEditText!!.text.toString(), mUsername, null)
            mMessageDatabaseReference.push().setValue(friendlyMessage)

            // Clear input box
            mMessageEditText!!.setText("")
        }

        mChildEventListener = object: ChildEventListener{
            override fun onCancelled(p0: DatabaseError) { }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) { }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) { }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
               val message: FriendlyMessage? =  p0.getValue(FriendlyMessage::class.java)
                mMessageAdapter.add(message)
            }

            override fun onChildRemoved(p0: DataSnapshot) { }

        }

        mMessageDatabaseReference.addChildEventListener(mChildEventListener)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

}
