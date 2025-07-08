package com.afin.jauharnafissubmission1expert.features.story.presentation.list

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import com.afin.jauharnafissubmission1expert.core.data.repository.StoryRepository
import com.afin.jauharnafissubmission1expert.features.story.domain.model.Story
import com.afin.jauharnafissubmission1expert.features.story.presentation.adapter.StoryPagingAdapter
import com.afin.jauharnafissubmission1expert.utils.DataDummy
import com.afin.jauharnafissubmission1expert.utils.MainDispatcherRule
import com.afin.jauharnafissubmission1expert.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var storyRepository: StoryRepository

    private lateinit var mainViewModel: MainViewModel

    @Before
    fun setUp() {
        mainViewModel = MainViewModel(storyRepository)
    }

    @Test
    fun `when Get Story Should Not Null and Return Data`() = runTest {
        // Given
        val dummyStories = DataDummy.generateDummyStoryResponse()
        val data: PagingData<Story> = StoryPagingSource.snapshot(dummyStories)
        val expectedStories = flowOf(data)

        `when`(storyRepository.getStoriesWithPaging()).thenReturn(expectedStories)

        // When
        val actualStories: PagingData<Story> = mainViewModel.storiesPaging.getOrAwaitValue()

        // Then
        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryPagingAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualStories)

        // Assert
        assertNotNull(differ.snapshot())
        assertEquals(dummyStories.size, differ.snapshot().size)
        assertEquals(dummyStories[0], differ.snapshot()[0])
    }

    @Test
    fun `when Get Story Empty Should Return No Data`() = runTest {
        // Given
        val data: PagingData<Story> = PagingData.from(emptyList())
        val expectedStories = flowOf(data)

        `when`(storyRepository.getStoriesWithPaging()).thenReturn(expectedStories)

        // When
        val actualStories: PagingData<Story> = mainViewModel.storiesPaging.getOrAwaitValue()

        // Then
        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryPagingAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualStories)

        // Assert
        assertEquals(0, differ.snapshot().size)
    }
}

class StoryPagingSource : PagingSource<Int, LiveData<List<Story>>>() {
    companion object {
        fun snapshot(items: List<Story>): PagingData<Story> {
            return PagingData.from(items)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, LiveData<List<Story>>>): Int {
        return 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LiveData<List<Story>>> {
        return LoadResult.Page(emptyList(), 0, 1)
    }
}

val noopListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}