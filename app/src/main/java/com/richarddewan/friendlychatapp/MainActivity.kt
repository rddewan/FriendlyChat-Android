package com.richarddewan.friendlychatapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object {
        const val RC_SIGN_IN = 1
        const val RC_PHOTO_PIC = 2
        const val TAG = "MainActivity"
    }

    val ANONYMOUS = "anonymous"
    val DEFAULT_MSG_LENGTH_LIMIT = 1000

    private var mMessageListView: ListView? = null
    private lateinit var mMessageAdapter: MessageAdapter
    private var mProgressBar: ProgressBar? = null
    private var mPhotoPickerButton: ImageButton? = null
    private var mMessageEditText: EditText? = null
    private var mSendButton: ImageButton? = null

    private var mUsername: String? = null

    // Write a message to the database
    private var mFirebaseDatabase = FirebaseDatabase.getInstance()
    private lateinit var mMessageDatabaseReference: DatabaseReference
    private lateinit var mChildEventListener: ChildEventListener
    private lateinit var mFirebaseAuth: FirebaseAuth
    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    private lateinit var mFireBaseStorage: FirebaseStorage
    private lateinit var mChatPhotoStorageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mUsername = ANONYMOUS

        mFirebaseDatabase = FirebaseDatabase.getInstance()
        mFirebaseAuth = FirebaseAuth.getInstance()
        mFireBaseStorage = FirebaseStorage.getInstance()
        mMessageDatabaseReference = mFirebaseDatabase.reference.child("message")
        mChatPhotoStorageReference = mFireBaseStorage.getReference().child("chat_photos")

        // Initialize references to views


        // Initialize references to views
        mProgressBar = findViewById<View>(R.id.progressBar) as ProgressBar
        mMessageListView =
            findViewById<View>(R.id.messageListView) as ListView
        mPhotoPickerButton = findViewById<View>(R.id.photoPickerButton) as ImageButton
        mMessageEditText = findViewById<View>(R.id.messageEditText) as EditText
        mSendButton = findViewById<View>(R.id.sendButton) as ImageButton
        mSendButton?.isEnabled = false

        // Initialize message ListView and its adapter

        // Initialize message ListView and its adapter
        val friendlyMessages: List<FriendlyMessage> =
            ArrayList<FriendlyMessage>()
        mMessageAdapter = MessageAdapter(this, R.layout.item_message, friendlyMessages)
        mMessageListView!!.adapter = mMessageAdapter

        // ImagePickerButton shows an image picker to upload a image for a message

        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton!!.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.setType("image/*")
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true)
            startActivityForResult(intent,RC_PHOTO_PIC)
        }

        // Enable Send button when there's text to send

        // Enable Send button when there's text to send
        mMessageEditText!!.addTextChangedListener(object : TextWatcher {
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

        //firebase auth
        mAuthStateListener = FirebaseAuth.AuthStateListener {
            val user: FirebaseUser? = it.currentUser
            if (user != null) {
                //user is signed in
                onSignedInInitialize(user.displayName)
                Log.d(TAG,"Login success")

            } else {
                //user in sign out
                onSignOutCleanUp()

                startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(
                            Arrays.asList(
                                GoogleBuilder().build(),
                                FacebookBuilder().build(),
                                //TwitterBuilder().build(),
                                //MicrosoftBuilder().build(),
                                //YahooBuilder().build(),
                                //AppleBuilder().build(),
                                //PhoneBuilder().build(),
                                //AnonymousBuilder().build(),
                                EmailBuilder().build()
                            )
                        )
                        .build(),
                    RC_SIGN_IN
                )

            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.sign_out_menu ->
                AuthUI.getInstance().signOut(this)
            else ->
                return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun attachDatabaseReadListener(){
        mProgressBar!!.visibility = ProgressBar.VISIBLE

        mChildEventListener = object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val message: FriendlyMessage? = p0.getValue(FriendlyMessage::class.java)
                mMessageAdapter.add(message)
                mProgressBar!!.visibility = ProgressBar.GONE
            }

            override fun onChildRemoved(p0: DataSnapshot) {}

        }
        mMessageDatabaseReference.addChildEventListener(mChildEventListener)
    }

    private fun detachDatabaseReadListener(){
        mMessageDatabaseReference.removeEventListener(mChildEventListener)
    }

    private fun onSignedInInitialize(userName: String?){
        mUsername = userName
        attachDatabaseReadListener()

    }

    private fun onSignOutCleanUp(){
        mUsername = ANONYMOUS
        mMessageAdapter.clear()
        detachDatabaseReadListener()


    }

    //its called before onResume
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN){
            if (resultCode == Activity.RESULT_OK){
                Log.d(TAG,"SignIn success")
            }
            else {
                Log.d(TAG, "SignIn canceled")
                finish()
            }

        }
        else if (requestCode == RC_PHOTO_PIC){
            //get the data as uri
            val selectedImageUri = data?.data
            //get a reference  to store file at chat_photos
            val photoRef = selectedImageUri?.lastPathSegment?.let {
                mChatPhotoStorageReference.child(it)
            }
            //upload file to firebase
            val uploadTask  = photoRef?.putFile(selectedImageUri)
            val urlTask = uploadTask?.continueWithTask{task->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw  it
                    }
                }
                photoRef.downloadUrl
            }?.addOnCompleteListener  { task->
                if (task.isSuccessful){
                    //get the download url
                    val downloadUri = task.result
                   //add photo to firebase database
                    val friendlyMessage =
                        FriendlyMessage(null, mUsername, downloadUri.toString())
                    mMessageDatabaseReference.push().setValue(friendlyMessage)
                }else {
                    // Handle failures
                    Log.e(TAG,task.exception?.message.toString())
                }
            }?.addOnFailureListener{
                Log.e(TAG, it.message.toString())
                Toast.makeText(applicationContext, it.message, Toast.LENGTH_LONG).show()

            }

        }
    }

    override fun onPause() {
        super.onPause()
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener)
        detachDatabaseReadListener()
        mMessageAdapter.clear()
    }

    override fun onResume() {
        super.onResume()
        mFirebaseAuth.addAuthStateListener(mAuthStateListener)
    }

}
