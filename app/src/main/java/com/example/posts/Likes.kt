package com.example.posts

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.view_likes.view.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlin.random.Random

class Likes private constructor(val amount: Int) {

    companion object {
        operator fun invoke(amount: Int): Likes? = if (amount >= 0) Likes(amount) else null
    }

    init {
        check(amount >= 0) { "Amount of likes cannot be less than 0" }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Likes

        if (amount != other.amount) return false

        return true
    }

    override fun hashCode(): Int {
        return amount
    }

    override fun toString(): String {
        return "Likes(amount=$amount)"
    }
}

interface LikesView : (Likes) -> Unit

interface LikesSource : () -> Flow<Likes>

class AndroidLikesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes),
    LikesView {

    private var likes: Likes? = null

    init {
        orientation = HORIZONTAL
        inflate(context, R.layout.view_likes, this)
    }

    override fun invoke(likes: Likes) = synchronized(this) {
        if (this.likes != likes) {
            this.likes = likes
            post { likesCountView.text = render(likes) }
        }
    }

    private fun render(likes: Likes?): CharSequence = when (val amount = likes?.amount) {
        null -> "0"
        in 0..999 -> amount.toString()
        in 1000..8999 -> "${amount / 1000}k+"
        else -> "9k++"
    }
}

class MockLikesSource(
    private val range: IntRange = 0..15000,
    private val delay: Long = 500L,
    private val random: Random = Random(delay)
) : LikesSource {
    override fun invoke(): Flow<Likes> = flow {
        while (currentCoroutineContext().isActive) {
            Likes(range.random(random))?.also { emit(it) }
            delay(delay)
        }
    }
}