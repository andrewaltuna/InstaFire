package com.aslaltuna.instafire

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.aslaltuna.instafire.databinding.ActivitySignUpBinding
import com.aslaltuna.instafire.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class SignUpActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "SignUpActivity"
    }

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.btnSignUp.setOnClickListener {
            createUserAndLogin()
        }
    }

    private fun createUserAndLogin() {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "Email/password cannot be empty", Toast.LENGTH_LONG).show()
            return
        }
        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_LONG).show()
            return
        }

        binding.btnSignUp.isEnabled = false

        // Firebase user creation
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Firebase authentication
                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login success!", Toast.LENGTH_SHORT).show()
                        goUpdateProfileActivity()
                    } else {
                        Log.i(TAG, "signIn failed", task.exception)
                        Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()
                        binding.tvInvalidLogin.visibility = View.VISIBLE
                        binding.btnSignUp.isEnabled = true

                        goUpdateProfileActivity()
                    }
                }
            } else {
                Toast.makeText(this, "Sign up failed", Toast.LENGTH_SHORT).show()
                binding.btnSignUp.isEnabled = true
            }
        }
    }

    private fun goUpdateProfileActivity() {
        val intent = Intent(this, UpdateProfileActivity::class.java)
        startActivity(intent)
        finish()
    }


}