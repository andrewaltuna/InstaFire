package com.aslaltuna.instafire

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.aslaltuna.instafire.databinding.ActivityUpdateProfileBinding
import com.aslaltuna.instafire.models.Post
import com.aslaltuna.instafire.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class UpdateProfileActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "UpdateProfileActivity"
    }

    private lateinit var binding: ActivityUpdateProfileBinding
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private var usernameSignUp: String? = null
    private var photoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storageReference = Firebase.storage.reference
        firestoreDb = Firebase.firestore

        binding.btnSubmit.setOnClickListener {
            doesUsernameExist()
        }

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK || it.data == null) {
                return@registerForActivityResult
            }

            photoUri = it.data?.data
            Log.i(TAG, "photoUri $photoUri")
            binding.ivProfileImage.setImageURI(photoUri)

        }

        binding.btnImagePicker.setOnClickListener {
            val imagePickerIntent = Intent(Intent.ACTION_GET_CONTENT)
            imagePickerIntent.type = "image/*"
            Log.i(TAG, "btnPickImage")
            imagePickerLauncher.launch(imagePickerIntent)
        }

    }

    private fun doesUsernameExist() {
        if (binding.etUsername.text.isEmpty() || binding.etAge.text.isEmpty() || binding.etBio.text.isEmpty()) {
            return
        }

        usernameSignUp = binding.etUsername.text.toString()

        if (photoUri == null) {
            photoUri = Uri.parse("android.resource://com.aslaltuna.instafire/drawable/empty_profile")
        }

        // Query DB for inputted username
        firestoreDb.collection("posts")
            .whereEqualTo("user.username", usernameSignUp)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (task.result.documents.isEmpty()) {
                        updateProfile()
                    }
                }
            }
    }

    private fun updateProfile() {
        binding.btnSubmit.isEnabled = false
        val photoUploadUri = photoUri as Uri
        val filePath = "profile_images/${Firebase.auth.currentUser?.uid}.jpg"
        Log.i(TAG, "${Firebase.auth.currentUser?.uid}")
        val photoReference = storageReference.child(filePath)
        // Upload photo to Firebase Storage
        photoReference.putFile(photoUploadUri)
            .continueWithTask { uploadTask ->
                Log.i(TAG, "Uploaded bytes: ${uploadTask.result.bytesTransferred}")
                // Retrieve image url of the uploaded image
                photoReference.downloadUrl
            }.continueWithTask { downloadUrlTask ->
                // Create a user object with the image URL
                val user = User(
                    binding.etUsername.text.toString(),
                    binding.etAge.text.toString().toInt(),
                    binding.etBio.text.toString(),
                    downloadUrlTask.result.toString())

                firestoreDb.collection("users").document("${Firebase.auth.currentUser?.uid}").set(user)
            }.addOnCompleteListener { postCreationTask ->
                if (!postCreationTask.isSuccessful) {
                    Log.e(TAG, "Exception while attempting to save post", postCreationTask.exception)
                    Toast.makeText(this, "Failed to save post", Toast.LENGTH_SHORT).show()
                    binding.btnSubmit.isEnabled = true
                }
                Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()
                val profileIntent = Intent(this, PostsActivity::class.java)
                startActivity(profileIntent)
                finish()
            }
    }
}