package com.example.posts

import android.os.Bundle
import android.view.ViewGroup
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

    private val androidLikesView by lazy { AndroidLikesView(this) }
    private val androidCommentView by lazy { AndroidCommentView(this) }
    private val androidNCommentsView by lazy { AndroidNCommentsView(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentView = androidNCommentsView
        val currentSource = mockNCommentsSource()

        setContentView(currentView, ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT))

        lifecycleScope.launch {
            currentSource.collect { currentView(it) }
        }
    }
}