import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dto.*
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

private const val BASE_URL = "http://127.0.0.1:9999"
private val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(HttpLoggingInterceptor(::println).apply { level = HttpLoggingInterceptor.Level.BASIC })
    .build()

fun main() {
    CoroutineScope(EmptyCoroutineContext).launch {
        try {
            val listPostsAndComments = getPosts().map { post ->
                async {
                    val comments = getComments(post.id).map { comment ->
                        CommentWithAuthor(comment, getAuthor(comment.authorId))
                    }
                    println(comments)
                    PostWithAuthorAndComments(post, getAuthor(post.authorId), comments)
                }
            }.awaitAll()
            println(listPostsAndComments)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    Thread.sleep(30_000)
}

suspend fun OkHttpClient.apiCall(url: String) = suspendCoroutine<Response> { continuation ->
    Request.Builder().url(url).build().let(::newCall).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            continuation.resumeWithException(e)
        }

        override fun onResponse(call: Call, response: Response) {
            continuation.resume(response)
        }
    })
}

suspend fun <T> makeRequest(url: String, okHttpClient: OkHttpClient, typeToken: TypeToken<T>): T =
    withContext(Dispatchers.IO) {
        okHttpClient.apiCall(url).let { response ->
            if (!response.isSuccessful) {
                throw  RuntimeException("Unsuccessful request")
            }
            val result = response.body ?: throw RuntimeException("No body")
            Gson().fromJson(result.charStream(), typeToken.type)
        }
    }
suspend fun getPosts() = makeRequest("$BASE_URL/api/posts", okHttpClient, object : TypeToken<List<Post>>() {})

suspend fun getComments(id: Long) =
    makeRequest("$BASE_URL/api/posts/$id/comments", okHttpClient, object : TypeToken<List<Comment>>() {})

suspend fun getAuthor(id: Long) = makeRequest("$BASE_URL/api/authors/$id", okHttpClient, object : TypeToken<Author>() {})