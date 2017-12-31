package com.prentice.ethan.smartmirror1

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import android.util.Log
import android.widget.EditText
import com.google.firebase.database.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.view.MotionEvent
import android.content.Intent


class SplashActivity : AppCompatActivity() {

    private val TAG: String = "SplashActivity"

    // Firebase Authentication
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    // Firebase Database Storage
    private val database = FirebaseDatabase.getInstance().reference
    var databaseUsers: HashMap<String, String> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        database.addValueEventListener(object : ValueEventListener {
            @Suppress("UNCHECKED_CAST")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val td: HashMap<String, Any> = dataSnapshot.value as HashMap<String, Any>
                databaseUsers = td["user_ids"] as HashMap<String, String>

                println("Database Users: $databaseUsers")
            }

            override fun onCancelled(p0: DatabaseError?) {

            }
        })

        val passwordField: EditText = findViewById(R.id.passwordField)
        passwordField.setOnEditorActionListener {v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Hides keyboard after user presses [ENTER] in password field
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)

                loginProcess()
                true
            } else {
                false
            }
        }

        setButtonListener(findViewById(R.id.loginBtn))
    }

    override fun onStart() {
        super.onStart()
        val user = firebaseAuth.currentUser
        if (user != null) {
            if (user.displayName in databaseUsers.keys) {
                onSignIn()
            }
        }
    }

    fun onClick(v: View) {
        when (v.id) {
            R.id.loginBtn ->  {
                loginProcess()
            }
        }
    }

    private fun loginProcess() {

        val userIDField: EditText = findViewById(R.id.usernameField)
        val passwordField: EditText = findViewById(R.id.passwordField)

        val userID = userIDField.text.toString().toLowerCase()
        val password = passwordField.text.toString()


        val emailVal = "^[_A-Za-z0-9-+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
        if (userID.matches(emailVal.toRegex())){
            signIn(userID, password)
        } else {
            if (userID in databaseUsers.keys) {
                signIn(databaseUsers[userID].toString(), password)
            } else {
                Toast.makeText(this, "User does not exist!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun setButtonListener(button: View) {
        val scaleFactor = 0.9f
        val alphaFactor = 0.8f
        button.setOnTouchListener({ v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.alpha = alphaFactor
                    v.scaleX = scaleFactor
                    v.scaleY = scaleFactor
                }
                MotionEvent.ACTION_UP -> {
                    v.alpha = 1f
                    v.scaleX = 1/scaleFactor
                    v.scaleY = 1/scaleFactor
                }
            }
            false
        })
    }

    private fun signIn(userID: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(userID, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        Toast.makeText(this, "Successfully Signed In!", Toast.LENGTH_SHORT).show()
                        onSignIn()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)

                        Toast.makeText(this, "Incorrect Password.",
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }

    private fun onSignIn() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        super.onBackPressed()
    }

    // TODO: Create "Sign Up" UI and implement this for new users.
    private fun createNewUser(email: String, password: String){
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success")
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }

}
