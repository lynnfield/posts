package com.example.posts

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import coil.load
import coil.request.Disposable
import kotlinx.android.synthetic.main.post_view.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import java.net.URL
import kotlin.random.Random

data class Post(
    val title: Title,
    val image: URL,
    val likes: Likes,
    val comments: NComments
) {
    class Title private constructor(val value: String) {
        companion object {
            operator fun invoke(value: String): Title? = value.takeIf(String::isNotBlank)?.let(::Title)
        }

        init {
            check(value.isNotBlank()) { "value of Title cannot be blank" }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Title

            if (value != other.value) return false

            return true
        }

        override fun hashCode(): Int {
            return value.hashCode()
        }

        override fun toString(): String {
            return "Title(value='$value')"
        }
    }
}

interface PostView : (Post) -> Unit

interface PostSource : () -> Flow<Post>

class AndroidPostView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes),
    PostView {

    private val maxCommentsAmount: Int

    init {
        inflate(context, R.layout.post_view, this)

        val typedArray =
            context.theme.obtainStyledAttributes(attrs, R.styleable.AndroidPostView, defStyleAttr, defStyleRes)

        maxCommentsAmount = typedArray.getInt(R.styleable.AndroidPostView_maxCommentsAmount, 3)

        typedArray.recycle()
    }

    private var post: Post? = null
    private var loading: Disposable? = null

    override fun invoke(post: Post) = synchronized(this) {
        if (this.post != post) {
            this.post = post
            post {
                titleView.text = render(post.title)
                loading?.dispose()
                loading = imageView.load(post.image.toURI().toString())
                this.likesView(post.likes)
                this.nCommentsView(post.comments.take(maxCommentsAmount))
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        loading?.dispose()
    }

    private fun render(title: Post.Title): CharSequence = title.value
}

@ExperimentalCoroutinesApi
class MockPostSource(
    private val likesSource: LikesSource,
    private val nCommentsSource: NCommentsSource,
    private val titles: List<Post.Title> = listOf(
        Post.Title("Simple title")!!,
        Post.Title("Short")!!,
        Post.Title("Very very long title text")!!,
        Post.Title("Fancy title")!!,
        Post.Title("Summer")!!,
        Post.Title("Winter")!!,
        Post.Title("Genovich")!!,
        Post.Title("JetBrains")!!,
        Post.Title("Android")!!,
    ),
    private val images: List<URL> = listOf(
        URL("https://images.unsplash.com/photo-1605350635510-f389c4ec13d6?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixlib=rb-1.2.1&q=80&w=800"),
        URL("https://images.unsplash.com/photo-1605781645799-c9c7d820b4ac?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixlib=rb-1.2.1&q=80&w=800"),
        URL("https://images.unsplash.com/photo-1604817467386-977905bcbfcc?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixlib=rb-1.2.1&q=80&w=800"),
        URL("https://images.unsplash.com/photo-1604600087734-6021a87222e5?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixlib=rb-1.2.1&q=80&w=800"),
        URL("https://images.unsplash.com/photo-1605012755891-ba8df642d374?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixlib=rb-1.2.1&q=80&w=800"),
        URL("https://images.unsplash.com/photo-1604509780907-fee4b4f3a51f?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixlib=rb-1.2.1&q=80&w=800"),
        URL("https://images.unsplash.com/photo-1606191027641-dd583bbf4597?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixlib=rb-1.2.1&q=80&w=800"),
        URL("https://images.unsplash.com/photo-1605271553729-d0fab5f2c03a?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixlib=rb-1.2.1&q=80&w=800"),
        URL("https://images.unsplash.com/photo-1605026284432-b98a666caf5f?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&h=600&ixlib=rb-1.2.1&q=80&w=800"),
    ),
    private val delay: Long = 5000L,
    private val random: Random = Random(delay),
) : PostSource {
    override fun invoke(): Flow<Post> = channelFlow {
        while (currentCoroutineContext().isActive) {
            val title = titles.random(random)
            val url = images.random(random)

            val job = launch {
                likesSource()
                    .combine(nCommentsSource()) { likes, comments -> Post(title, url, likes, comments) }
                    .collect { send(it) }
            }

            delay(delay)
            job.cancel()
        }
    }
}