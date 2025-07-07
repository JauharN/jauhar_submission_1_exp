package com.afin.jauharnafissubmission1expert.features.story.presentation.list

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.afin.jauharnafissubmission1expert.R
import com.afin.jauharnafissubmission1expert.core.utils.EventObserver
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

        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.setPadding(statusBar.left, statusBar.top, statusBar.right, statusBar.bottom)
            insets
        }

        setupRecyclerView()
        setupAction()
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshStories()
    }

    private fun setupRecyclerView() {
        storyAdapter = StoryAdapter { story, imageView ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra(DetailActivity.EXTRA_STORY, story)

            val options = androidx.core.app.ActivityOptionsCompat
                .makeSceneTransitionAnimation(this, imageView, "story_image")

            startActivity(intent, options.toBundle())
        }

        binding.rvStories.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = storyAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupAction() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshStories()
        }

        binding.actionLogout.setOnClickListener {
            showLogoutDialog()
        }

        binding.fabAddStory.setOnClickListener {
            startActivity(Intent(this, AddStoryActivity::class.java))
        }
    }

    private fun setupObservers() {
        // Observe initial stories
        viewModel.stories.observe(this) { result ->
            handleStoryResult(result)
        }

        // Observe refresh stories
        viewModel.refreshStories.observe(this) { result ->
            handleStoryResult(result, isRefreshing = true)
        }

        // Observe logout event
        viewModel.logoutEvent.observe(this, EventObserver {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        })
    }

    private fun handleStoryResult(
        result: Result<List<com.afin.jauharnafissubmission1expert.features.story.domain.model.Story>>,
        isRefreshing: Boolean = false
    ) {
        when (result) {
            is Result.Loading -> {
                if (!isRefreshing) showLoading(true)
                binding.swipeRefresh.isRefreshing = true
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
                if (storyAdapter.currentList.isEmpty()) {
                    showEmpty(true)
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showEmpty(isEmpty: Boolean) {
        binding.llEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvStories.visibility = if (!isEmpty) View.VISIBLE else View.GONE
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
    }
}