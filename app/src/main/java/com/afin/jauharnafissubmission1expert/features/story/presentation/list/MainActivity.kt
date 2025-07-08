package com.afin.jauharnafissubmission1expert.features.story.presentation.list

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.afin.jauharnafissubmission1expert.R
import com.afin.jauharnafissubmission1expert.core.utils.EventObserver
import com.afin.jauharnafissubmission1expert.core.utils.ViewModelFactory
import com.afin.jauharnafissubmission1expert.core.utils.showToast
import com.afin.jauharnafissubmission1expert.databinding.ActivityMainBinding
import com.afin.jauharnafissubmission1expert.features.auth.presentation.login.LoginActivity
import com.afin.jauharnafissubmission1expert.features.story.presentation.add.AddStoryActivity
import com.afin.jauharnafissubmission1expert.features.story.presentation.adapter.LoadingStateAdapter
import com.afin.jauharnafissubmission1expert.features.story.presentation.adapter.StoryPagingAdapter
import com.afin.jauharnafissubmission1expert.features.story.presentation.detail.DetailActivity
import kotlinx.coroutines.launch
import androidx.paging.LoadState

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var storyPagingAdapter: StoryPagingAdapter

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

    // Tidak perlu lagi onResume untuk refresh stories karena Paging otomatis handle data lifecycle-aware

    private fun setupRecyclerView() {
        storyPagingAdapter = StoryPagingAdapter { story, imageView ->
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra(DetailActivity.EXTRA_STORY, story)
            }

            val options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(this, imageView, "story_image")
            startActivity(intent, options.toBundle())
        }

        binding.rvStories.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = storyPagingAdapter.withLoadStateFooter(
                footer = LoadingStateAdapter { storyPagingAdapter.retry() }
            )
            setHasFixedSize(true)
        }

        // Handle loading, error, empty state dari Paging
        storyPagingAdapter.addLoadStateListener { loadState ->
            // Tampilkan progress bar ketika sedang memuat data
            binding.progressBar.visibility = if (loadState.source.refresh is LoadState.Loading) {
                View.VISIBLE
            } else {
                View.GONE
            }

            // Tampilkan data jika berhasil
            binding.rvStories.visibility = if (loadState.source.refresh is LoadState.NotLoading) {
                View.VISIBLE
            } else {
                View.GONE
            }

            // Tampilkan state kosong kalau datanya habis
            val isListEmpty = loadState.source.refresh is LoadState.NotLoading &&
            storyPagingAdapter.itemCount == 0
            showEmpty(isListEmpty)

            // Handle jika terjadi error
            val errorState = loadState.source.append as? LoadState.Error
                ?: loadState.source.prepend as? LoadState.Error
                ?: loadState.source.refresh as? LoadState.Error
            errorState?.let {
                showToast(it.error.message ?: "Terjadi kesalahan saat memuat data")
            }
        }
    }

    private fun setupAction() {
        // Refresh manual lewat swipe gesture
        binding.swipeRefresh.setOnRefreshListener {
            storyPagingAdapter.refresh()
            binding.swipeRefresh.isRefreshing = false
        }

        binding.actionLogout.setOnClickListener {
            showLogoutDialog()
        }

        binding.fabAddStory.setOnClickListener {
            startActivity(Intent(this, AddStoryActivity::class.java))
        }
    }

    private fun setupObservers() {
        // Observe data dari ViewModel dan kirim ke adapter Paging
        viewModel.storiesPaging.observe(this) { pagingData ->
            lifecycleScope.launch {
                storyPagingAdapter.submitData(pagingData)
            }
        }

        // Observe event logout
        viewModel.logoutEvent.observe(this, EventObserver {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        })
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
