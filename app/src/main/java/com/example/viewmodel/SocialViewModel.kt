package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.database.*
import com.example.data.repository.SocialRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import kotlin.random.Random

// --- Retrofit Data Classes for Gemini REST API ---
@JsonClass(generateAdapter = true)
data class Part(val text: String? = null)

@JsonClass(generateAdapter = true)
data class Content(val parts: List<Part>)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(val contents: List<Content>)

@JsonClass(generateAdapter = true)
data class Candidate(val content: Content)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(val candidates: List<Candidate>)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

class SocialViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SocialRepository
    
    val allPosts: StateFlow<List<PostEntity>>
    val profile: StateFlow<CreatorProfileEntity?>
    val withdrawals: StateFlow<List<WithdrawalEntity>>

    // Live feedback strings
    private val _aiGenerationState = MutableStateFlow<String?>(null)
    val aiGenerationState: StateFlow<String?> = _aiGenerationState.asStateFlow()

    private val _trafficFeedback = MutableStateFlow<String?>(null)
    val trafficFeedback: StateFlow<String?> = _trafficFeedback.asStateFlow()

    private val _isSimulating = MutableStateFlow(false)
    val isSimulating: StateFlow<Boolean> = _isSimulating.asStateFlow()

    // Retrofit service for Gemini REST API
    private val geminiService: GeminiApiService by lazy {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val moshi = Moshi.Builder().build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }

    init {
        val database = SocialDatabase.getDatabase(application)
        val socialDao = database.socialDao()
        repository = SocialRepository(socialDao)

        // Setup States with subscriber flow
        allPosts = repository.allPosts
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        profile = repository.profile
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        withdrawals = repository.allWithdrawals
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Populate database if empty
        viewModelScope.launch {
            checkAndPrepopulateData()
            startContinuousViewsSimulator()
        }
    }

    private suspend fun checkAndPrepopulateData() {
        // 1. Setup default user profile if not present
        val currentProfile = repository.getProfileSync()
        if (currentProfile == null) {
            val defaultUser = CreatorProfileEntity(
                username = "awadhesh_nishad",
                fullName = "Awadhesh Nishad",
                bio = "Content Creator & Dev 🚀 | Earning from viral views! 💻💸",
                avatarUrl = "https://picsum.photos/id/1025/300/300",
                walletBalance = 45.80, // Start with ₹45.80 INR
                totalEarnings = 45.80,
                ratePerView = 0.20 // ₹0.20 per view
            )
            repository.insertProfile(defaultUser)
        }

        // 2. Setup mock posts in database if none are present
        val posts = repository.allPosts.first()
        if (posts.isEmpty()) {
            val initialPosts = listOf(
                PostEntity(
                    username = "tech_pioneer",
                    userFullName = "Tech Pioneer",
                    avatarUrl = "https://picsum.photos/id/1012/300/300",
                    imageUrl = "https://picsum.photos/id/180/600/400",
                    caption = "Just finished setting up my workspace for 2026! Jetpack Compose makes UI styling incredibly smooth! 💻🔥",
                    likesCount = 245,
                    isLikedByMe = false,
                    viewsCount = 1250,
                    location = "San Francisco, CA",
                    isUserPost = false,
                    timestamp = System.currentTimeMillis() - 3600000 * 2
                ),
                PostEntity(
                    username = "foodie_vibes",
                    userFullName = "Rohan Sharma",
                    avatarUrl = "https://picsum.photos/id/64/300/300",
                    imageUrl = "https://picsum.photos/id/292/600/400",
                    caption = "Sunday morning homemade waffles with organic maple syrup and fresh blueberries! Cooking is therapeutic 🧇🍓🧁",
                    likesCount = 890,
                    isLikedByMe = true,
                    viewsCount = 5400,
                    location = "Mumbai, India",
                    isUserPost = false,
                    timestamp = System.currentTimeMillis() - 3600000 * 5
                ),
                PostEntity(
                    username = "nature_whisperer",
                    userFullName = "Shreya Sen",
                    avatarUrl = "https://picsum.photos/id/338/300/300",
                    imageUrl = "https://picsum.photos/id/10/600/400",
                    caption = "A quiet sunrise at the misty hills of Munnar. Connecting back with mother nature is always magical. 🌄🍃⛰️",
                    likesCount = 512,
                    isLikedByMe = false,
                    viewsCount = 3120,
                    location = "Kerala, India",
                    isUserPost = false,
                    timestamp = System.currentTimeMillis() - 3600000 * 10
                ),
                PostEntity(
                    username = "gym_beast",
                    userFullName = "Kabir Yadav",
                    avatarUrl = "https://picsum.photos/id/447/300/300",
                    imageUrl = "https://picsum.photos/id/26/600/400",
                    caption = "No shortcut to consistency! Focused arms day today. What are you training? 💪🏋️‍♀️🚴‍♂️",
                    likesCount = 1045,
                    isLikedByMe = false,
                    viewsCount = 12050,
                    location = "Delhi, India",
                    isUserPost = false,
                    timestamp = System.currentTimeMillis() - 3600000 * 18
                )
            )

            for (p in initialPosts) {
                val pId = repository.createPost(p).toInt()
                // insert standard mock comments for these
                if (p.username == "tech_pioneer") {
                    repository.addComment(CommentEntity(postId = pId, username = "anshika_dev", text = "This workspace looks futuristic!"))
                    repository.addComment(CommentEntity(postId = pId, username = "coding_ninja", text = "Compose is indeed brilliant."))
                } else if (p.username == "foodie_vibes") {
                    repository.addComment(CommentEntity(postId = pId, username = "awadhesh_nishad", text = "Waffles look delicious! Recipe please?"))
                    repository.addComment(CommentEntity(postId = pId, username = "hungry_soul", text = "Now I need waffles immediately!"))
                }
            }
        }
    }

    // --- Actions ---

    // Toggle post like
    fun toggleLike(post: PostEntity) {
        viewModelScope.launch {
            val isLiked = !post.isLikedByMe
            val likesDiff = if (isLiked) 1 else -1
            val updatedPost = post.copy(
                isLikedByMe = isLiked,
                likesCount = (post.likesCount + likesDiff).coerceAtLeast(0)
            )
            repository.updatePost(updatedPost)
        }
    }

    // Add comments to post
    fun addComment(postId: Int, username: String, text: String) {
        viewModelScope.launch {
            if (text.isNotBlank()) {
                val comment = CommentEntity(
                    postId = postId,
                    username = username,
                    text = text,
                    timestamp = System.currentTimeMillis()
                )
                repository.addComment(comment)
            }
        }
    }

    fun getComments(postId: Int): Flow<List<CommentEntity>> {
        return repository.getCommentsForPost(postId)
    }

    // Create New Post and award 1 unit of views initially
    fun addNewPost(caption: String, category: String, customImageUrl: String? = null) {
        viewModelScope.launch {
            val userProfile = repository.getProfileSync() ?: return@launch
            
            // Map categories to high-quality Unsplash or Picsum photo IDs to look spectacular!
            // Picsum category matches that have high-aesthetic landscape and compositions
            val picsumId = when (category.lowercase()) {
                "tech" -> "119" // beautiful retro iMac setup
                "travel" -> "28" // forest highway
                "food" -> "429" // food/fruit plate
                "fitness" -> "319" // bike athlete
                "fashion" -> "338" // girl in coat
                "nature" -> "49" // ocean forest
                else -> "237" // dog
            }

            val finalImageUrl = customImageUrl ?: "https://picsum.photos/id/$picsumId/620/420"
            val locations = listOf("Mumbai, India", "Bengaluru, India", "Noida", "Pune", "Goa Beach", "Kashmir")
            val randLocation = locations.random()

            val newPost = PostEntity(
                username = userProfile.username,
                userFullName = userProfile.fullName,
                avatarUrl = userProfile.avatarUrl,
                imageUrl = finalImageUrl,
                caption = caption,
                likesCount = 0,
                isLikedByMe = false,
                viewsCount = 10, // initial views
                earningsEarned = 10 * userProfile.ratePerView,
                location = randLocation,
                isUserPost = true,
                timestamp = System.currentTimeMillis()
            )

            val pId = repository.createPost(newPost).toInt()
            
            // Add automated welcome comment on user's new post
            delay(1200)
            repository.addComment(CommentEntity(
                postId = pId,
                username = "system_bot",
                text = "Congratulations awadhesh_nishad! Your post is live. Share it with friends to get viral traffic! 🚀📊",
                timestamp = System.currentTimeMillis()
            ))

            // Update user balance for initial views
            val extraBalance = 10 * userProfile.ratePerView
            val updatedProfile = userProfile.copy(
                walletBalance = userProfile.walletBalance + extraBalance,
                totalEarnings = userProfile.totalEarnings + extraBalance
            )
            repository.updateProfile(updatedProfile)
        }
    }

    // --- Viral Simulation Engine ---
    fun runViralSimulation() {
        viewModelScope.launch {
            if (_isSimulating.value) return@launch
            _isSimulating.value = true
            _trafficFeedback.value = "Initiating Promotion... 📡"
            
            // Step 1: Simulating traffic delivery
            delay(1000)
            _trafficFeedback.value = "Distributing across Social Feed... 🌐"
            delay(1000)
            _trafficFeedback.value = "Engagement Spike! Creators Reacting... 🔥"
            
            // Fetch posts to see what are user posts
            val currentPosts = allPosts.value
            val userPosts = currentPosts.filter { it.isUserPost }
            val profileSync = repository.getProfileSync() ?: return@launch

            if (userPosts.isEmpty()) {
                _trafficFeedback.value = "Create your first Post to get viral traffic! 📝"
                delay(3000)
                _trafficFeedback.value = null
                _isSimulating.value = false
                return@launch
            }

            // Distribute 250 - 500 total simulation views among user posts
            var totalAddedViews = 0
            val rate = profileSync.ratePerView
            
            for (post in userPosts) {
                val viewsAdded = Random.nextInt(75, 150)
                totalAddedViews += viewsAdded
                
                val newPostViews = post.viewsCount + viewsAdded
                val earningsAdded = viewsAdded * rate
                val newPostEarnings = post.earningsEarned + earningsAdded
                
                val updatedPost = post.copy(
                    viewsCount = newPostViews,
                    earningsEarned = newPostEarnings,
                    likesCount = post.likesCount + Random.nextInt(10, 30)
                )
                repository.updatePost(updatedPost)

                // Add a random comment as engagement
                val commentators = listOf("varun_sharma", "kavita_mehta", "neha_shastri_9", "rahul_vlogs", "tech_expert_2")
                val botComments = listOf(
                    "This is super creative! Love the vibe structure 👏💞",
                    "Aesthetic looks so premium! Keep shining!",
                    "Aapki posts bilkul gazab hoti hain! Superb!",
                    "Views are crazy on this, totally deserved! 🏆💸",
                    "Such a lovely capture. Visual storytelling at its finest!"
                )
                repository.addComment(CommentEntity(
                    postId = post.id,
                    username = commentators.random(),
                    text = botComments.random(),
                    timestamp = System.currentTimeMillis()
                ))
            }

            val totalAddedEarnings = totalAddedViews * rate
            val finalProfile = profileSync.copy(
                walletBalance = profileSync.walletBalance + totalAddedEarnings,
                totalEarnings = profileSync.totalEarnings + totalAddedEarnings
            )
            repository.updateProfile(finalProfile)

            _trafficFeedback.value = "Simulation Success! +$totalAddedViews Views (+₹${String.format("%.2f", totalAddedEarnings)}) 🎉"
            delay(4000)
            _trafficFeedback.value = null
            _isSimulating.value = false
        }
    }

    // Step-by-step continuous simulator to run in background (adds 1-3 views occasionally to user posts)
    private fun startContinuousViewsSimulator() {
        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                delay(12000) // update every 12 seconds
                val userPosts = repository.allPosts.first().filter { it.isUserPost }
                val profileSync = repository.getProfileSync()
                if (userPosts.isNotEmpty() && profileSync != null) {
                    val targetPost = userPosts.random()
                    val addedViews = Random.nextInt(1, 4)
                    val rate = profileSync.ratePerView
                    
                    val updatedPost = targetPost.copy(
                        viewsCount = targetPost.viewsCount + addedViews,
                        earningsEarned = targetPost.earningsEarned + (addedViews * rate)
                    )
                    repository.updatePost(updatedPost)

                    val updatedProfile = profileSync.copy(
                        walletBalance = profileSync.walletBalance + (addedViews * rate),
                        totalEarnings = profileSync.totalEarnings + (addedViews * rate)
                    )
                    repository.updateProfile(updatedProfile)
                }
            }
        }
    }

    // --- Wallet Withdrawal request ---
    fun requestWithdrawal(upiId: String, amount: Double, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val userProfile = repository.getProfileSync() ?: return@launch
            
            if (amount < 1.0) {
                onError("Please enter a valid amount (Minimum ₹1)!")
                return@launch
            }
            if (userProfile.walletBalance < amount) {
                onError("Insufficient balance! You have ₹${String.format("%.2f", userProfile.walletBalance)}")
                return@launch
            }
            if (!upiId.contains("@") || upiId.length < 5) {
                onError("Please enter a valid UPI ID (e.g., username@upi)")
                return@launch
            }

            // Deduct from Balance
            val newBalance = userProfile.walletBalance - amount
            val updatedProfile = userProfile.copy(walletBalance = newBalance)
            repository.updateProfile(updatedProfile)

            // Insert "Pending" withdrawal
            val withdrawal = WithdrawalEntity(
                upiId = upiId,
                amount = amount,
                status = "Pending",
                timestamp = System.currentTimeMillis()
            )
            repository.requestWithdrawal(withdrawal)
            onSuccess()

            // Simulate instant verification process
            delay(6000) // 6 seconds wait
            val list = withdrawals.value
            val target = list.firstOrNull { it.upiId == upiId && it.amount == amount && it.status == "Pending" }
            if (target != null) {
                val approvedTarget = target.copy(status = "Approved")
                // Need to use dao update which our repository can call since we can save it
                // We'll update via standard insert on-conflict replace which is the requested DAO implementation
                repository.requestWithdrawal(approvedTarget)
            }
        }
    }

    // --- Gemini AI Caption API --
    fun generateAICaption(theme: String, userDescription: String, onGenerated: (String) -> Unit) {
        viewModelScope.launch {
            _aiGenerationState.value = "Generating premium caption with Gemini API... 🧠"
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                // FALLBACK generator when key is the placeholder
                delay(1500)
                val fallbackCaptions = listOf(
                    "Living life in high definition! Category: $theme 🌟✨ #Visuals #CreatorLife #VibeClean",
                    "Chasing perfect moments and building dreams. Today's theme is: $userDescription #Trending #InstaAesthetic",
                    "A glimpse of my modern story. Every day is a new canvas 🎨📈 #SocialEarnings #M3Style #Influencer",
                    "Capturing details that matter. Loving the digital universe under $theme! 💎🚀 #InstaEarn #Viral #Life"
                )
                onGenerated(fallbackCaptions.random())
                _aiGenerationState.value = null
                return@launch
            }

            try {
                val promptText = "Write a highly engaging, catchy Instagram post caption. Theme is: $theme. User's seed idea: $userDescription. Include 3 trending hashtags, look modern, emoji-rich, and keep it under 30 words."
                
                val req = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = promptText))))
                )
                
                val response = withContext(Dispatchers.IO) {
                    geminiService.generateContent(apiKey, req)
                }
                
                val textResponse = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (textResponse != null) {
                    onGenerated(textResponse.trim())
                } else {
                    onGenerated("Sparking creative energy in every single corner. Theme: $theme. #InstaLifestyle #InstaEarn")
                }
            } catch (e: Exception) {
                onGenerated("Vibe check: $theme lifestyle in full swing! ✨ #InstaGlow #CreatorHub")
            } finally {
                _aiGenerationState.value = null
            }
        }
    }
}
