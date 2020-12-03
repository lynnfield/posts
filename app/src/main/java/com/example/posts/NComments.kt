package com.example.posts

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.children
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.random.Random

typealias NComments = List<Comment>

interface NCommentsView : (NComments) -> Unit

interface NCommentsSource : () -> Flow<NComments>

class AndroidNCommentsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr),
    NCommentsView {

    init {
        orientation = VERTICAL
        removeAllViews()

        if (isInEditMode) {
            render(
                listOf(
                    Comment.invoke("first value")!!,
                    Comment.invoke("second value")!!,
                    Comment.invoke("third value")!!
                )
            )
        }
    }

    private var nComments: NComments = emptyList()

    override fun invoke(nComments: NComments) = synchronized(this) {
        if (this.nComments != nComments) {
            this.nComments = nComments
            post { render(nComments) }
        }
    }

    private fun render(nComments: NComments) {
        // todo may be too slow for large sequences
        updateChildCount(nComments.size)
        children.filterIsInstance<CommentView>()
            .zip(nComments.asSequence())
            .forEach { (show, comment) -> show(comment) }
    }

    private fun updateChildCount(size: Int) {
        val i = size - childCount
        when {
            i > 0 -> repeat(i) { addView(AndroidCommentView(context)) }
            i < 0 -> repeat(-i) { removeViewAt(childCount - 1) }
        }
    }
}

@ExperimentalCoroutinesApi
class MockNCommentsSource(
    private val createComment: CommentSource,
    private val delay: Long = 3000L,
    private val random: Random = Random(delay),
    private val commentsCounts: IntRange = 0..10
) : NCommentsSource {
    override fun invoke(): Flow<NComments> = channelFlow {
        while (currentCoroutineContext().isActive) {
            val commentsCount = commentsCounts.random(random)
            val commentsSources = generateSequence { createComment() }.take(commentsCount)
            val job = launch { commentsSources.combineResultsToList().collect { send(it) } }
            delay(delay)
            job.cancel()
        }
    }

    private fun <T> Sequence<Flow<T>>.combineResultsToList(): Flow<List<T>> =
        fold(flowOf(emptyList())) { flows, flow -> flows.combine(flow) { comments, comment -> comments + comment } }
}