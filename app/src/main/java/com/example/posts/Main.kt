package com.example.posts

import android.os.Bundle
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {

    private val mockLikesSource = MockLikesSource()
    private val mockCommentSource = MockCommentSource()
    private val mockNCommentsSource = MockNCommentsSource(mockCommentSource)
    private val mockPostSource = MockPostSource(mockLikesSource, mockNCommentsSource)
    private val mockPostSourcesListSource = MockPostSourcesListSource(mockPostSource)

    private val androidLikesView by lazy { AndroidLikesView(this) }
    private val androidCommentView by lazy { AndroidCommentView(this) }
    private val androidNCommentsView by lazy { AndroidNCommentsView(this) }
    private val androidPostView by lazy { AndroidPostView(this) }
    private val androidPostSourcesListView by lazy { AndroidPostSourcesListView(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentView = androidPostSourcesListView
        val currentSource = mockPostSourcesListSource()

        setContentView(currentView, ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))

        lifecycleScope.launch {
            currentSource.collect { currentView(it) }
        }
    }
}