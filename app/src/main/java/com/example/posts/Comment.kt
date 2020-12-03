package com.example.posts

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlin.random.Random

class Comment private constructor(val text: String) {

    companion object {
        operator fun invoke(text: String): Comment? =
            text.takeIf(String::isNotBlank)?.let(::Comment)
    }

    init {
        check(text.isNotBlank()) { "text should not be empty" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Comment

        if (text != other.text) return false

        return true
    }

    override fun hashCode(): Int {
        return text.hashCode()
    }

    override fun toString(): String {
        return "Comment(text='$text')"
    }
}

interface CommentView : (Comment) -> Unit

interface CommentSource : () -> Flow<Comment>

class AndroidCommentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr), CommentView {

    init {
        if (isInEditMode) {
            render(Comment.invoke("stub comment")!!)
        }
    }

    private var comment: Comment? = null

    override fun invoke(comment: Comment) = synchronized(this) {
        if (this.comment != comment) {
            this.comment = comment
            post { render(comment) }
        }
    }

    private fun render(comment: Comment) {
        text = comment.asText()
    }

    private fun Comment.asText(): CharSequence = text
}

class MockCommentSource(
    private val values: List<String> = listOf(
        "Short message",
        "Very long message lorem ipsum sit dolor amen",
        "Very long message lorem ipsum sit dolor amen\nVery long message lorem ipsum sit dolor amen"
    ),
    private val delay: Long = 500L,
    private val random: Random = Random(delay)
) : CommentSource {
    override fun invoke(): Flow<Comment> = flow {
        while (currentCoroutineContext().isActive) {
            Comment(values.random(random))?.also { emit(it) }
            delay(delay)
        }
    }
}