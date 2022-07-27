package com.aslaltuna.instafire

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aslaltuna.instafire.ProfileActivity.Companion.EXTRA_POST_DETAIL
import com.aslaltuna.instafire.databinding.ActivityPostsBinding
import com.aslaltuna.instafire.models.Post
import com.aslaltuna.instafire.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

open class PostsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_USERNAME = "EXTRA_USERNAME"
        const val EXTRA_USER_DETAIL = "EXTRA_USER_DETAIL"
        private const val TAG = "PostsActivity"
    }

    private lateinit var binding: ActivityPostsBinding
    private lateinit var firestoreDb: FirebaseFirestore
    private lateinit var posts: MutableList<Post>
    private lateinit var adapter: PostsAdapter

    private var signedInUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        posts = mutableListOf()

        val userPosts = intent.getSerializableExtra(EXTRA_POST_DETAIL)

        if (userPosts != null) {
            posts.addAll(userPosts as List<Post>)
        }

        adapter = PostsAdapter(this, posts, object: PostsAdapter.RvInterface {
            override fun onItemClick(position: Int) {
                val intent = Intent(this@PostsActivity, ProfileActivity::class.java)
                intent.putExtra(EXTRA_USER_DETAIL, posts[position].user)
                startActivity(intent)
            }

            override fun onItemLongClick(position: Int) {
//                if (posts[position].user?.username == signedInUser?.username) {
//                    showAlertDialog(position)
//                }
            }
        })

        binding.apply {
            rvPosts.adapter = adapter
            rvPosts.layoutManager = LinearLayoutManager(this@PostsActivity)
            rvPosts.addItemDecoration(DividerItemDecoration(rvPosts.context, DividerItemDecoration.VERTICAL))
        }

        // Readying user data for PROFILE ACTIVITY
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

        if (userPosts == null) {
            supportActionBar?.title = "My Posts"
            val postsReference = firestoreDb
                .collection("posts")
                .limit(20)
                .orderBy("creation_time_ms", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, exception ->
                    if (snapshot == null || exception != null) {
                        return@addSnapshotListener
                    }
                    val postList = snapshot.toObjects(Post::class.java)
                    posts.clear()
                    posts.addAll(postList)
                    adapter.notifyDataSetChanged()
                    for (post in postList) {
                        Log.i(TAG, "$post")
                }
            }
        } else {
            val position = intent.getSerializableExtra("position") as Int
            binding.rvPosts.smoothScrollToPosition(position)
            Log.i(TAG, "Scrolling~ $position")
        }

        binding.fabCreate.setOnClickListener {
            val intent = Intent(this, PostCreateActivity::class.java)
            startActivity(intent)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_posts, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miProfile -> goProfileActivity()
        }
        return super.onOptionsItemSelected(item)
    }

    // TODO: Delete posts functionality
//    private fun showAlertDialog(position: Int) {
//
////        val view = LayoutInflater.from(this).inflate(R.layout.dialogue_delete_post, null)
//
//        val dialog = AlertDialog.Builder(this)
//            .setTitle("Delete confirmation")
//            .setMessage("Are you sure you want to delete this post?")
//            .setNegativeButton("Cancel", null)
//            .setPositiveButton("Delete") { _,_ ->
//                firestoreDb.collection("posts")
//                    .document(posts[position])
//            }
//    }

    private fun goProfileActivity() {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra(EXTRA_USERNAME, signedInUser?.username)
        startActivity(intent)
    }
}