package com.example.posts

import android.os.Bundle
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
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

    private val androidLikesView by lazy { AndroidLikesView(this) }
    private val androidCommentView by lazy { AndroidCommentView(this) }
    private val androidNCommentsView by lazy { AndroidNCommentsView(this) }
    private val androidPostView by lazy { AndroidPostView(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentView = androidPostView
        val currentSource = mockPostSource()

        setContentView(currentView, ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT))

        lifecycleScope.launch {
            currentSource.collect { currentView(it) }
        }
    }
}