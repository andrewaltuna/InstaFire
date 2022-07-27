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
import com.aslaltuna.instafire.PostsActivity.Companion.EXTRA_USERNAME
import com.aslaltuna.instafire.databinding.ActivityPostCreateBinding
import com.aslaltuna.instafire.models.Post
import com.aslaltuna.instafire.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class PostCreateActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "PostCreateActivity"
    }

    private lateinit var binding: ActivityPostCreateBinding
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private var photoUri: Uri? = null
    private var signedInUser: User? = null
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storageReference = Firebase.storage.reference

        firestoreDb = Firebase.firestore
        firestoreDb.collection("users")
            .document(Firebase.auth.currentUser?.uid as String)
            .get()
            .addOnSuccessListener { snapshot ->
                signedInUser = snapshot.toObject(User::class.java)
                Log.i(TAG, "Signed in user: $signedInUser")
            }
            .addOnFailureListener { exception ->
                Log.i(TAG, "Failed to fetch signed in user", exception)
            }

        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != Activity.RESULT_OK || it.data == null) {
                return@registerForActivityResult
            }

            photoUri = it.data?.data
            Log.i(TAG, "photoUri $photoUri")
            binding.ivImagePreview.setImageURI(photoUri)

        }

        binding.btnPickImage.setOnClickListener {


            val imagePickerIntent = Intent(Intent.ACTION_GET_CONTENT)
            imagePickerIntent.type = "image/*"

//            if (imagePickerIntent.resolveActivity(packageManager) != null) {
                Log.i(TAG, "btnPickImage")
                imagePickerLauncher.launch(imagePickerIntent)
//            }
        }

        binding.btnSubmit.setOnClickListener {
            handleSubmitButton()
        }

    }

    private fun handleSubmitButton() {
        if (photoUri == null || binding.etDescription.text.isBlank()) {
            Toast.makeText(this, "Image and description cannot be left empty", Toast.LENGTH_LONG).show()
            return
        }

        if (signedInUser == null) {
            Toast.makeText(this, "Please try again in a few seconds", Toast.LENGTH_LONG).show()
            return
        }

        binding.btnSubmit.isEnabled = false

        val photoUploadUri = photoUri as Uri
        val filePath = "images/${System.currentTimeMillis()}-photo.jpg"
        val photoReference = storageReference.child(filePath)
        // Upload photo to Firebase Storage
        photoReference.putFile(photoUploadUri)
            .continueWithTask { uploadTask ->
                Log.i(TAG, "Uploaded bytes: ${uploadTask.result.bytesTransferred}")
                // Retrieve image url of the uploaded image
                photoReference.downloadUrl
            }.continueWithTask { downloadUrlTask ->
                // Create a post object with the image URL
                val post = Post(
                    binding.etDescription.text.toString(),
                    downloadUrlTask.result.toString(),
                    System.currentTimeMillis(),
                    signedInUser)

                firestoreDb.collection("posts").add(post)
            }.addOnCompleteListener { postCreationTask ->
                if (!postCreationTask.isSuccessful) {
                    Log.e(TAG, "Exception while attempting to save post", postCreationTask.exception)
                    Toast.makeText(this, "Failed to save post", Toast.LENGTH_SHORT).show()
                    binding.btnSubmit.isEnabled = true
                }

                binding.etDescription.text.clear()
                binding.ivImagePreview.setImageResource(0)
                Toast.makeText(this, "Posted!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, PostsActivity::class.java)
                startActivity(intent)
                finish()
            }
    }
}