package com.afin.jauharnafissubmission1expert.features.story.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.afin.jauharnafissubmission1expert.core.utils.formatDate
import com.afin.jauharnafissubmission1expert.databinding.ItemStoryBinding
import com.afin.jauharnafissubmission1expert.features.story.domain.model.Story
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class StoryAdapter(
    private val onItemClick: (Story, ImageView) -> Unit
) : ListAdapter<Story, StoryAdapter.StoryViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StoryViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = getItem(position)
        holder.bind(story)
    }

    class StoryViewHolder(
        private val binding: ItemStoryBinding,
        private val onItemClick: (Story, ImageView) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(story: Story) {
            binding.apply {
                // Set user name
                tvItemName.text = story.name

                // Set avatar with first letter of name
                tvAvatar.text = story.name.firstOrNull()?.toString()?.uppercase() ?: "?"

                // Set date
                tvDate.text = story.createdAt.formatDate()

                // Set description preview
                tvDescription.text = story.description

                // Set transition name for shared element
                ivItemPhoto.transitionName = "story_image_${story.id}"

                // Load image with Glide
                Glide.with(itemView.context)
                    .load(story.photoUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .into(ivItemPhoto)

                // Set click listener with image view
                root.setOnClickListener {
                    onItemClick(story, ivItemPhoto)
                }
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Story>() {
            override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem == newItem
            }
        }
    }
}