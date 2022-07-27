package com.aslaltuna.instafire

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.aslaltuna.instafire.PostsActivity.Companion.EXTRA_USER_DETAIL
import com.aslaltuna.instafire.databinding.ActivityProfileBinding
import com.aslaltuna.instafire.models.Post
import com.aslaltuna.instafire.models.User
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ProfileActivity"
        private const val RV_COLUMN_AMOUNT = 3
        const val EXTRA_POST_DETAIL = "EXTRA_POST_DETAIL"
    }

    private lateinit var binding: ActivityProfileBinding
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var adapter: ProfilePostsAdapter
    private lateinit var posts: MutableList<Post>
    private var userDetail: User? = null
    private var userToQuery: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        posts = mutableListOf()

        adapter = ProfilePostsAdapter(this, posts, object: ProfilePostsAdapter.RvInterface {
            override fun onItemClick(position: Int) {
                val intent = Intent(this@ProfileActivity, PostsActivity::class.java)
                intent.putExtra(EXTRA_POST_DETAIL, posts as ArrayList)
                intent.putExtra("position", position)
                startActivity(intent)
            }
        })

        binding.rvPosts.adapter = adapter
        binding.rvPosts.layoutManager = GridLayoutManager(this, RV_COLUMN_AMOUNT)

        firestoreDb = Firebase.firestore

        userToQuery = intent.getSerializableExtra(EXTRA_USER_DETAIL) as User?

        firestoreDb.collection("users")
            .document(Firebase.auth.currentUser?.uid as String)
            .get()
            .addOnSuccessListener { snapshot ->
                if (userToQuery != null) {
                    userDetail = userToQuery
                } else {
                    userDetail = snapshot.toObject(User::class.java)
                }

                bindUserInformation()
            }
            .addOnFailureListener { exception ->
                Log.i(TAG, "Failed to fetch signed in user", exception)
            }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_profile, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miSignout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun bindUserInformation() {
        firestoreDb.collection("posts")
            .whereEqualTo("user.username", userDetail?.username)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    binding.tvPosts.text = task.result.size().toString()
                }
            }

        binding.apply {
            tvUsername.text = userDetail?.username
            Glide.with(this@ProfileActivity).load(userDetail?.imageUrl).into(ivProfileImage)
            tvBio.text = userDetail?.bio
        }

        val postsReference = firestoreDb
            .collection("posts")
            .whereEqualTo("user.username", userDetail?.username)
            .orderBy("creation_time_ms", Query.Direction.DESCENDING)

        postsReference.addSnapshotListener { snapshot, exception ->
            if (snapshot == null || exception != null) {
                Log.i(TAG, "Exception when querying posts", exception)
                return@addSnapshotListener
            }
            val postList = snapshot.toObjects(Post::class.java)
            posts.clear()
            posts.addAll(postList)
            binding.rvPosts.adapter = adapter
            for (post in postList) {
                Log.i(TAG, "$post")
            }

            if (posts.isNotEmpty()) {
                binding.tvEmpty.visibility = View.GONE
            }
        }
    }
}
