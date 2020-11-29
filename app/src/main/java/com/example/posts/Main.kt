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

    private val androidLikesView by lazy { AndroidLikesView(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(androidLikesView, ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT))

        lifecycleScope.launch {
            mockLikesSource().collect { androidLikesView(it) }
        }
    }
}