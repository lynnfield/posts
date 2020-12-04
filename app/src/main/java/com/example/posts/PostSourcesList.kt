package com.example.posts

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

typealias PostSourcesList = List<Flow<Post>>

interface PostSourcesListView : (PostSourcesList) -> Unit

interface PostSourcesListSource : () -> Flow<PostSourcesList>

class AndroidPostSourcesListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr),
    PostSourcesListView {

    init {
        adapter = Adapter()
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    override fun invoke(postSourcesList: PostSourcesList) {
        post {
            with(adapter as Adapter) {
                items = postSourcesList
                notifyDataSetChanged()
            }
        }
    }

    class Adapter : RecyclerView.Adapter<ViewHolder>() {

        var items: PostSourcesList = emptyList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(AndroidPostView(parent.context).apply {
                layoutParams =
                    ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            })

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.coroutineContext.cancelChildren()
            holder.launch { item.collect { holder.postView(it) } }
        }

        override fun getItemCount(): Int = items.size

        override fun onViewDetachedFromWindow(holder: ViewHolder) {
            super.onViewDetachedFromWindow(holder)
            holder.coroutineContext.cancelChildren()
        }
    }

    class ViewHolder(
        val postView: AndroidPostView
    ) : RecyclerView.ViewHolder(postView),
        CoroutineScope by CoroutineScope(Job())
}

class MockPostSourcesListSource(
    private val postSource: PostSource,
    private val amounts: IntRange = 0..100,
    private val delay: Long = 10000L,
    private val random: Random = Random(delay)
) : PostSourcesListSource {
    override fun invoke(): Flow<PostSourcesList> = flow {
        while (currentCoroutineContext().isActive) {
            emit(
                generateSequence(postSource)
                    .take(amounts.random(random))
                    .toList()
            )

            delay(delay)
        }
    }
}