package com.aslaltuna.instafire

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.aslaltuna.instafire.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
    }

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firestoreDb: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val auth = Firebase.auth
//        if (auth.currentUser != null) {
//            goPostsActivity()
//        }
        firestoreDb = Firebase.firestore

        firestoreDb.collection("users")
            .document("${Firebase.auth.currentUser?.uid}")
            .get()
            .addOnCompleteListener { userTask ->
                if (userTask.isSuccessful) {
                    // Profile exists and there is a logged in user
                    if (userTask.result.data != null && auth.currentUser != null) {
                        goPostsActivity()
                        Log.i(TAG, "Profile exists and there is a logged in user")
                        finish()
                    } else if (auth.currentUser != null) {
                        goProfileActivity()
                        finish()
                    }
                }
            }


        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Email/password cannot be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            binding.btnLogin.isEnabled = false

            // Firebase authentication
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
//                    Toast.makeText(this, "Login success!", Toast.LENGTH_SHORT).show()

                    // Check if profile has been created before
                    firestoreDb.collection("users")
                        .document("${Firebase.auth.currentUser?.uid}")
                        .get()
                        .addOnCompleteListener { userTask ->
                            if (userTask.isSuccessful) {
                                // Profile already exists
                                if (userTask.result.data != null) {
                                    goPostsActivity()
                                } else {
                                    goProfileActivity()
                                }
                                finish()
                            }
                        }
                } else {
                    Log.i(TAG, "signIn failed", task.exception)
                    Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
                    binding.tvInvalidLogin.visibility = View.VISIBLE
                    binding.btnLogin.isEnabled = true
                }
            }
        }

        binding.btnSignUp.setOnClickListener {
            Log.i(TAG, "tvSignup Clicked")
            goSignUpActivity()
        }
    }

    private fun goProfileActivity() {
        val intent = Intent(this, UpdateProfileActivity::class.java)
        startActivity(intent)
    }

    private fun goSignUpActivity() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }

    private fun goPostsActivity() {
        Log.i(TAG, "goPostsActivity")
        val intent = Intent(this, PostsActivity::class.java)
        startActivity(intent)
    }
}