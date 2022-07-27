package com.aslaltuna.instafire

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.aslaltuna.instafire.databinding.ItemProfilePostBinding
import com.aslaltuna.instafire.models.Post
import com.bumptech.glide.Glide
import kotlin.math.min

class ProfilePostsAdapter(private val context: Context, private val posts: List<Post>, private val rvInterface: RvInterface): RecyclerView.Adapter<ProfilePostsAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemProfilePostBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            Glide.with(context).load(post.imageUrl).into(binding.ivPost)
        }
    }

    interface RvInterface {
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val cellSide = parent.width / 3

        val binding = ItemProfilePostBinding.inflate(LayoutInflater.from(context), parent, false)
        val layoutParams = binding.ivPost.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.width = cellSide
        layoutParams.height = cellSide

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(posts[position])

        holder.itemView.setOnClickListener {
            rvInterface.onItemClick(position)
        }
    }

    override fun getItemCount() = posts.size
}
