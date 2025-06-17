package com.afin.jauharnafissubmission1expert.features.story.presentation.list

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.afin.jauharnafissubmission1expert.R
import com.afin.jauharnafissubmission1expert.core.utils.Result
import com.afin.jauharnafissubmission1expert.core.utils.ViewModelFactory
import com.afin.jauharnafissubmission1expert.core.utils.showToast
import com.afin.jauharnafissubmission1expert.databinding.ActivityMainBinding
import com.afin.jauharnafissubmission1expert.features.auth.presentation.login.LoginActivity
import com.afin.jauharnafissubmission1expert.features.story.presentation.adapter.StoryAdapter
import com.afin.jauharnafissubmission1expert.features.story.presentation.add.AddStoryActivity
import com.afin.jauharnafissubmission1expert.features.story.presentation.detail.DetailActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var storyAdapter: StoryAdapter

    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup window insets
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupAction()
        observeData()

        // Load stories
        viewModel.getStories()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning from AddStoryActivity
        viewModel.getStories()
    }

    private fun setupRecyclerView() {
        storyAdapter = StoryAdapter { story, imageView ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra(DetailActivity.EXTRA_STORY, story)

            val options = androidx.core.app.ActivityOptionsCompat
                .makeSceneTransitionAnimation(
                    this,
                    imageView,
                    "story_image"
                )

            startActivity(intent, options.toBundle())
        }

        binding.rvStories.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = storyAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupAction() {
        // Swipe refresh
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.getStories()
        }

        // Logout button
        binding.actionLogout.setOnClickListener {
            showLogoutDialog()
        }

        // FAB Add Story
        binding.fabAddStory.setOnClickListener {
            val intent = Intent(this, AddStoryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeData() {
        viewModel.getStories().observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    showLoading(true)
                    showEmpty(false)
                }

                is Result.Success -> {
                    showLoading(false)
                    binding.swipeRefresh.isRefreshing = false

                    if (result.data.isEmpty()) {
                        showEmpty(true)
                        storyAdapter.submitList(emptyList())
                    } else {
                        showEmpty(false)
                        storyAdapter.submitList(result.data)
                    }
                }

                is Result.Error -> {
                    showLoading(false)
                    binding.swipeRefresh.isRefreshing = false
                    showToast(result.message)

                    // Show empty state if no data
                    if (storyAdapter.currentList.isEmpty()) {
                        showEmpty(true)
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showEmpty(isEmpty: Boolean) {
        binding.llEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvStories.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.logout))
            setMessage(getString(R.string.logout_confirmation))
            setPositiveButton(getString(R.string.yes)) { _, _ ->
                performLogout()
            }
            setNegativeButton(getString(R.string.no), null)
            create()
            show()
        }
    }

    private fun performLogout() {
        viewModel.logout()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}