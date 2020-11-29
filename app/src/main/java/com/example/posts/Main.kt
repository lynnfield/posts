package com.example.posts

import android.os.Bundle
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val mockLikesSource = MockLikesSource()
    private val mockCommentSource = MockCommentSource()

    private val androidLikesView by lazy { AndroidLikesView(this) }
    private val androidCommentView by lazy { AndroidCommentView(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(androidCommentView, ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT))

        lifecycleScope.launch {
            mockCommentSource().collect { androidCommentView(it) }
        }
    }
}