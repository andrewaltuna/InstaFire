package com.aslaltuna.instafire

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.aslaltuna.instafire.databinding.ItemPostBinding
import com.aslaltuna.instafire.databinding.ItemProfilePostBinding
import com.aslaltuna.instafire.models.Post
import com.bumptech.glide.Glide

class PostsAdapter(private val context: Context, private val posts: List<Post>, private val rvInterface: RvInterface) : RecyclerView.Adapter<PostsAdapter.ViewHolder>() {
    inner class ViewHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            binding.apply {
                tvUsername.text = post.user?.username
                tvDescription.text = post.description
                Glide.with(context).load(post.imageUrl).into(ivPost)
                Glide.with(context).load(post.user?.imageUrl).into(ivProfileImage)
                tvRelativeTime.text = DateUtils.getRelativeTimeSpanString(post.creationTimeMs)

                tvUsername.setOnClickListener {
                    rvInterface.onItemClick(posts.indexOf(post))
                }
                ivProfileImage.setOnClickListener {
                    rvInterface.onItemClick(posts.indexOf(post))
                }
            }
        }
    }

    interface RvInterface{
        fun onItemClick(position: Int)
        fun onItemLongClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(posts[position])
        holder.itemView.setOnLongClickListener {
            rvInterface.onItemLongClick(position)
            return@setOnLongClickListener true
        }
    }

    override fun getItemCount() = posts.size
}