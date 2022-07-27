package com.aslaltuna.instafire

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import com.aslaltuna.instafire.ProfileActivity.Companion.EXTRA_POST_DETAIL
import com.aslaltuna.instafire.databinding.ActivityProfileBinding
import com.aslaltuna.instafire.databinding.ItemPostBinding
import com.aslaltuna.instafire.models.Post
import com.bumptech.glide.Glide

class PostDetailActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "PostDetailActivity"
    }

    private lateinit var binding: ItemPostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ItemPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var post = intent.getSerializableExtra(EXTRA_POST_DETAIL)

        if (post != null) {
            post = post as Post
            binding.apply {
                tvUsername.text = post.user?.username
                tvDescription.text = post.description
                Glide.with(this@PostDetailActivity).load(post.imageUrl).into(ivPost)
                Glide.with(this@PostDetailActivity).load(post.user?.imageUrl).into(ivProfileImage)
                tvRelativeTime.text = DateUtils.getRelativeTimeSpanString(post.creationTimeMs)

            }
        } else {
            Log.i(TAG, "post is null")
        }



    }


}