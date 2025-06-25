package com.afin.jauharnafissubmission1expert.features.story.presentation.detail

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.afin.jauharnafissubmission1expert.core.utils.Result
import com.afin.jauharnafissubmission1expert.core.utils.ViewModelFactory
import com.afin.jauharnafissubmission1expert.core.utils.formatDate
import com.afin.jauharnafissubmission1expert.databinding.ActivityDetailBinding
import com.afin.jauharnafissubmission1expert.features.story.domain.model.Story
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class DetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STORY = "extra_story"
        const val EXTRA_STORY_ID = "extra_story_id"
    }

    private lateinit var binding: ActivityDetailBinding
    private val viewModel: DetailViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    private var currentStory: Story? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()
        setupAction()
        loadStoryData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun setupAction() {
        // Share button
        binding.btnShare.setOnClickListener {
            shareStory()
        }
    }

    private fun loadStoryData() {
        val story = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_STORY, Story::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_STORY)
        }

        if (story != null) {
            // Display story
            currentStory = story
            displayStory(story)
        } else {
            // Load from API jika ID passed
            val storyId = intent.getStringExtra(EXTRA_STORY_ID)
            if (storyId != null) {
                loadStoryFromApi(storyId)
            } else {
                // kalau no data, finish activity
                finish()
            }
        }
    }

    private fun loadStoryFromApi(storyId: String) {
        viewModel.getStoryDetail(storyId).observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    showLoading(true)
                }

                is Result.Success -> {
                    showLoading(false)
                    currentStory = result.data
                    displayStory(result.data)
                }

                is Result.Error -> {
                    showLoading(false)
                    finish()
                }
            }
        }
    }

    private fun displayStory(story: Story) {
        binding.apply {
            // Load image
            Glide.with(this@DetailActivity)
                .load(story.photoUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .centerCrop()
                .into(ivDetailPhoto)

            ivDetailPhoto.setOnClickListener {
                val intent = Intent(this@DetailActivity, FullImageActivity::class.java)
                intent.putExtra(FullImageActivity.EXTRA_IMAGE_URL, story.photoUrl)
                startActivity(intent)
            }

            // Set user info
            tvDetailName.text = story.name
            tvAvatar.text = story.name.firstOrNull()?.toString()?.uppercase() ?: "?"
            tvDate.text = story.createdAt.formatDate()

            // Set description
            tvDetailDescription.text = story.description

            // Set location jika tersedia
            if (story.lat != null && story.lon != null) {
                llLocation.visibility = View.VISIBLE
                tvLocation.text = "Lat: ${story.lat}, Lon: ${story.lon}"
            } else {
                llLocation.visibility = View.GONE
            }
        }
    }

    private fun shareStory() {
        currentStory?.let { story ->
            val shareText = buildString {
                append("Check out this story from ${story.name}!\n\n")
                append("\"${story.description}\"\n\n")
                append("Shared from Dicoding Story App")
            }

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }

            startActivity(Intent.createChooser(shareIntent, "Share Story"))
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // exit animation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(
                OVERRIDE_TRANSITION_CLOSE,
                0,
                android.R.anim.fade_out
            )
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, android.R.anim.fade_out)
        }
    }
}