package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.database.CommentEntity
import com.example.data.database.PostEntity
import com.example.data.database.CreatorProfileEntity
import com.example.viewmodel.SocialViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// --- Color Tokens (Professional Polish Theme) ---
val InstagramGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF833AB4), // Purple
        Color(0xFFFD1D1D), // Red
        Color(0xFFF77737), // Orange
        Color(0xFFFCAF45)  // Yellow
    )
)

val GoldenEarningGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFFD700),
        Color(0xFFFFA500),
        Color(0xFFFF8C00)
    )
)

val AppLightBackground = Color(0xFFF8F9FF)
val CardBackgroundLight = Color.White
val MainDarkText = Color(0xFF191C1E)
val SecondaryGrayText = Color(0xFF44474E)
val BrandBlue = Color(0xFF005AC1)
val LightBlueContainer = Color(0xFFD3E4FF)
val SupportBorderColor = Color(0xFFC4C6CF)
val SoftHighlightBlue = Color(0xFFE7F0FF)
val DeepDarkBlueText = Color(0xFF001D36)
val EarningGreen = Color(0xFF34A853)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialAppUI(viewModel: SocialViewModel) {
    val posts by viewModel.allPosts.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val withdrawals by viewModel.withdrawals.collectAsStateWithLifecycle()
    val aiGenerationState by viewModel.aiGenerationState.collectAsStateWithLifecycle()
    val trafficFeedback by viewModel.trafficFeedback.collectAsStateWithLifecycle()
    val isSimulating by viewModel.isSimulating.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("home") }
    var selectedPostForComments by remember { mutableStateOf<PostEntity?>(null) }
    var showActivePromotionDialog by remember { mutableStateOf(false) }

    // Scaffold with beautiful professional light theme and safe insets
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "InstaEarn",
                            fontFamily = FontFamily.Cursive,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandBlue,
                            fontStyle = FontStyle.Italic
                        )
                        Box(
                            modifier = Modifier
                                .background(GoldenEarningGradient, CircleShape)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "MONETIZED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                },
                actions = {
                    // Quick stats pill representing balance in light professional palette
                    profile?.let { p ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .testTag("wallet_balance_pill")
                                .padding(end = 12.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(LightBlueContainer)
                                .clickable { activeTab = "profile" }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = "Wallet Balance",
                                tint = EarningGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "₹${String.format("%.2f", p.walletBalance)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DeepDarkBlueText
                            )
                        }
                    }

                    IconButton(
                        onClick = { showActivePromotionDialog = true },
                        modifier = Modifier.testTag("promo_quick_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Simulate Traffic",
                            tint = Color(0xFFFFA500)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = MainDarkText,
                    navigationIconContentColor = MainDarkText,
                    actionIconContentColor = MainDarkText
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = activeTab == "home",
                    onClick = { activeTab = "home" },
                    icon = { Icon(imageVector = if (activeTab == "home") Icons.Filled.Home else Icons.Outlined.Home, contentDescription = "Home") },
                    label = { Text("Feed", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DeepDarkBlueText,
                        unselectedIconColor = SecondaryGrayText,
                        selectedTextColor = DeepDarkBlueText,
                        unselectedTextColor = SecondaryGrayText,
                        indicatorColor = LightBlueContainer
                    ),
                    modifier = Modifier.testTag("nav_feed_tab")
                )
                NavigationBarItem(
                    selected = activeTab == "explore",
                    onClick = { activeTab = "explore" },
                    icon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Explore") },
                    label = { Text("Explore", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DeepDarkBlueText,
                        unselectedIconColor = SecondaryGrayText,
                        selectedTextColor = DeepDarkBlueText,
                        unselectedTextColor = SecondaryGrayText,
                        indicatorColor = LightBlueContainer
                    ),
                    modifier = Modifier.testTag("nav_explore_tab")
                )
                NavigationBarItem(
                    selected = activeTab == "create",
                    onClick = { activeTab = "create" },
                    icon = { Icon(imageVector = Icons.Default.AddBox, contentDescription = "Create Post") },
                    label = { Text("Create AI", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DeepDarkBlueText,
                        unselectedIconColor = SecondaryGrayText,
                        selectedTextColor = DeepDarkBlueText,
                        unselectedTextColor = SecondaryGrayText,
                        indicatorColor = LightBlueContainer
                    ),
                    modifier = Modifier.testTag("nav_create_tab")
                )
                NavigationBarItem(
                    selected = activeTab == "profile",
                    onClick = { activeTab = "profile" },
                    icon = { Icon(imageVector = if (activeTab == "profile") Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle, contentDescription = "Profile Wallet") },
                    label = { Text("Studio 🪙", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DeepDarkBlueText,
                        unselectedIconColor = SecondaryGrayText,
                        selectedTextColor = DeepDarkBlueText,
                        unselectedTextColor = SecondaryGrayText,
                        indicatorColor = LightBlueContainer
                    ),
                    modifier = Modifier.testTag("nav_profile_tab")
                )
            }
        },
        containerColor = AppLightBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen router
            when (activeTab) {
                "home" -> HomeFeedScreen(
                    posts = posts,
                    onLikeClicked = { viewModel.toggleLike(it) },
                    onCommentsClicked = { selectedPostForComments = it }
                )
                "explore" -> ExploreScreen(
                    posts = posts,
                    onPostClicked = { selectedPostForComments = it }
                )
                "create" -> CreatePostScreen(
                    viewModel = viewModel,
                    fallbackState = aiGenerationState,
                    onPostCreated = { activeTab = "home" }
                )
                "profile" -> ProfileStudioScreen(
                    viewModel = viewModel,
                    profile = profile,
                    withdrawals = withdrawals,
                    isSimulating = isSimulating,
                    trafficFeedback = trafficFeedback
                )
            }

            // Global comments detail drawer (as dynamic sheet/dialog)
            selectedPostForComments?.let { post ->
                CommentsDialog(
                    post = post,
                    viewModel = viewModel,
                    onDismiss = { selectedPostForComments = null }
                )
            }

            // Global prompt Traffic viral simulation dialog
            if (showActivePromotionDialog) {
                QuickPromotionDetailsDialog(
                    viewModel = viewModel,
                    isSimulating = isSimulating,
                    trafficFeedback = trafficFeedback,
                    onDismiss = { showActivePromotionDialog = false }
                )
            }
        }
    }
}

// --- SCREEN 1: HOME FEED ---
@Composable
fun HomeFeedScreen(
    posts: List<PostEntity>,
    onLikeClicked: (PostEntity) -> Unit,
    onCommentsClicked: (PostEntity) -> Unit
) {
    if (posts.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.GridOn,
                    contentDescription = "No Posts",
                    tint = SecondaryGrayText,
                    modifier = Modifier.size(72.dp)
                )
                Text(
                    text = "No Creator Posts Yet!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MainDarkText
                )
                Text(
                    text = "Head over to Create AI tab and craft the first viral masterpiece! 🚀",
                    textAlign = TextAlign.Center,
                    color = SecondaryGrayText,
                    fontSize = 14.sp
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Instagram-like Stories bar showing Monetized Creators
            item {
                ActiveCreatorsStoryBar()
            }

            // Post feed card items
            items(posts, key = { it.id }) { post ->
                PostFeedCard(
                    post = post,
                    onLikeClicked = { onLikeClicked(post) },
                    onCommentsClicked = { onCommentsClicked(post) }
                )
            }
        }
    }
}

@Composable
fun ActiveCreatorsStoryBar() {
    val creators = listOf(
        Pair("awadhesh_nishad", EarningGreen), // Green represents current user
        Pair("tech_pioneer", Color(0xFFC13584)),
        Pair("foodie_vibes", Color(0xFFFD1D1D)),
        Pair("nature_whisperer", Color(0xFF833AB4)),
        Pair("gym_beast", Color(0xFFFCAF45)),
        Pair("artist_glow", Color(0xFF03DAC6))
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FiberManualRecord,
                contentDescription = "Live Meter",
                tint = EarningGreen,
                modifier = Modifier
                    .size(12.dp)
                    .padding(end = 4.dp)
            )
            Text(
                text = "Earning Real-Time Creators",
                fontSize = 12.sp,
                color = SecondaryGrayText,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            creators.forEach { creator ->
                val initials = creator.first.take(2).uppercase()
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(72.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .border(width = 2.5.dp, brush = InstagramGradient, shape = CircleShape)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        // If it's the current user, show specialized avatar initials
                        if (creator.first == "awadhesh_nishad") {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(GoldenEarningGradient),
                                contentAlignment = Alignment.Center
                            ) {
                                  Text(
                                    text = "ME",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black
                                )
                            }
                        } else {
                            Text(
                                text = initials,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MainDarkText
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = creator.first,
                        fontSize = 10.sp,
                        color = MainDarkText,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        Divider(color = Color(0xFFE1E2E9), thickness = 1.dp)
    }
}

@Composable
fun PostFeedCard(
    post: PostEntity,
    onLikeClicked: () -> Unit,
    onCommentsClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackgroundLight),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE1E2E9))
    ) {
        Column {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Profile Avatar circle
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(brush = if (post.isUserPost) GoldenEarningGradient else androidx.compose.ui.graphics.SolidColor(Color(0xFF333333))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.username.take(2).uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (post.isUserPost) Color.Black else Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = post.username,
                                fontWeight = FontWeight.Bold,
                                color = MainDarkText,
                                fontSize = 14.sp
                            )
                            if (post.isUserPost || post.likesCount > 500) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified Partner",
                                    tint = if (post.isUserPost) Color(0xFFFFD700) else BrandBlue,
                                    modifier = Modifier
                                        .size(15.dp)
                                        .padding(start = 3.dp)
                                )
                            }
                        }
                        Text(
                            text = post.location,
                            fontSize = 11.sp,
                            color = SecondaryGrayText
                        )
                    }
                }

                // Earning stat badge displayed top-right
                val formattedEarn = String.format("%.2f", post.earningsEarned)
                val badgeBg = if (post.isUserPost) SoftHighlightBlue else Color(0xFFF1F3F4)
                val badgeText = if (post.isUserPost) EarningGreen else SecondaryGrayText

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(badgeBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = "Earning badge",
                            tint = if (post.isUserPost) EarningGreen else Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (post.isUserPost) "Earned: ₹$formattedEarn" else "RPM Match",
                            fontSize = 11.sp,
                            color = badgeText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Post Visual Image containing gorgeous canvas gradients with full-bleed fallback
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFF1F3F9), Color(0xFFE1E2E9))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // If it's a simulated or manual post, CoilAsyncImage loads high quality Picsum
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(post.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Post Media Content",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = android.R.drawable.stat_notify_error),
                    onSuccess = { /* image loaded successfully */ }
                )

                // Beautiful category background overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.3f))
                            )
                        )
                )
            }

            // Interactive Engagement Buttons Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Like button + scale animations
                    IconButton(
                        onClick = onLikeClicked,
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("like_button_${post.id}")
                    ) {
                        Icon(
                            imageVector = if (post.isLikedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like icon",
                            tint = if (post.isLikedByMe) Color.Red else MainDarkText
                        )
                    }

                    // Comment button
                    IconButton(
                        onClick = onCommentsClicked,
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("comment_button_${post.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Comment button icon",
                            tint = MainDarkText
                        )
                    }

                    // Share button
                    IconButton(
                        onClick = { /* share simulation */ },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share post icon",
                            tint = MainDarkText
                        )
                    }
                }

                // Interactive Views counter
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Views icon",
                        tint = SecondaryGrayText,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "${post.viewsCount} views",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryGrayText
                    )
                }
            }

            // Caption Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "${post.username} ",
                        fontWeight = FontWeight.Bold,
                        color = MainDarkText,
                        fontSize = 13.sp
                    )
                    Text(
                        text = post.caption,
                        color = MainDarkText,
                        fontSize = 13.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Clicking this prompts Comments Bottom panel
                Text(
                    text = "View all comments",
                    color = SecondaryGrayText,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clickable { onCommentsClicked() }
                        .padding(vertical = 4.dp),
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.None
                )

                // Timestamp showing how old this post is
                val minutesAgo = (System.currentTimeMillis() - post.timestamp) / 60000
                val dateStr = when {
                    minutesAgo < 1 -> "Just now"
                    minutesAgo < 60 -> "$minutesAgo minutes ago"
                    minutesAgo < 1440 -> "${minutesAgo / 60} hours ago"
                    else -> "${minutesAgo / 1440} days ago"
                }

                Text(
                    text = dateStr,
                    fontSize = 10.sp,
                    color = SecondaryGrayText,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}


// --- SCREEN 2: EXPLORE ---
@Composable
fun ExploreScreen(
    posts: List<PostEntity>,
    onPostClicked: (PostEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredPosts = posts.filter {
        it.caption.contains(searchQuery, ignoreCase = true) ||
        it.username.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Aesthetic search bar in professional styling
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("explore_search_bar"),
            placeholder = { Text("Search Monetized Creators or Tags...", color = SecondaryGrayText) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = SecondaryGrayText) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandBlue,
                unfocusedBorderColor = SupportBorderColor,
                focusedTextColor = MainDarkText,
                unfocusedTextColor = MainDarkText,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )

        if (filteredPosts.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No creators matched '$searchQuery'",
                    color = SecondaryGrayText,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredPosts) { post ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFE1E2E9))
                            .clickable { onPostClicked(post) }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(post.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Explore media image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = android.R.drawable.stat_notify_error),
                            placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
                        )

                        // If user post, show gold earning badge indicator
                        if (post.isUserPost) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(GoldenEarningGradient)
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = "Monetized",
                                    tint = Color.Black,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }

                        // Bottom visibility layout details on hover simulation
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.4f))))
                                .padding(4.dp),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Visibility, contentDescription = "Views", tint = Color.White, modifier = Modifier.size(11.dp))
                                Text("${post.viewsCount}", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}


// --- SCREEN 3: CREATE POST ---
@Composable
fun CreatePostScreen(
    viewModel: SocialViewModel,
    fallbackState: String?,
    onPostCreated: () -> Unit
) {
    var seedDescription by remember { mutableStateOf("") }
    var captionText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tech") }
    val categories = listOf("Tech", "Travel", "Nature", "Art", "Food", "Fashion")

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Create AI Monetized Post",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            color = MainDarkText
        )

        Text(
            text = "Select a content category to load dynamic media layouts, then enter details below to write AI-optimized descriptions that trigger maximum impressions!",
            color = SecondaryGrayText,
            fontSize = 13.sp
        )

        // Category Cards chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = cat == selectedCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(brush = if (isSelected) androidx.compose.ui.graphics.SolidColor(BrandBlue) else androidx.compose.ui.graphics.SolidColor(Color(0xFFE1E2E9)))
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) Color.White else SecondaryGrayText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        // Seeds input for Gemini
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackgroundLight),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, SupportBorderColor)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Gemini API Assist",
                        tint = BrandBlue
                    )
                    Text(
                        text = "AI Assist (Gemini Studio)",
                        fontWeight = FontWeight.Bold,
                        color = MainDarkText,
                        fontSize = 14.sp
                    )
                }

                OutlinedTextField(
                    value = seedDescription,
                    onValueChange = { seedDescription = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("seed_input_field"),
                    label = { Text("What is this post about? (e.g., retro iMac setup, misty beach)", color = SecondaryGrayText) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandBlue,
                        unfocusedBorderColor = SupportBorderColor,
                        focusedLabelColor = BrandBlue,
                        unfocusedLabelColor = SecondaryGrayText,
                        focusedTextColor = MainDarkText,
                        unfocusedTextColor = MainDarkText,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = false,
                    maxLines = 2
                )

                Button(
                    onClick = {
                        if (seedDescription.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Enter a short description first for Gemini!")
                            }
                        } else {
                            viewModel.generateAICaption(selectedCategory, seedDescription) { caption ->
                                captionText = caption
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_caption_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Draft AI Caption with Gemini API ✨", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                // AI Progress Loader Status
                fallbackState?.let { status ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = BrandBlue)
                        Text(text = status, fontSize = 11.sp, color = BrandBlue, fontStyle = FontStyle.Italic)
                    }
                }
            }
        }

        // Final caption editor
        OutlinedTextField(
            value = captionText,
            onValueChange = { captionText = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .testTag("caption_input_field"),
            label = { Text("Final caption with hashtags...", color = SecondaryGrayText) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandBlue,
                unfocusedBorderColor = SupportBorderColor,
                focusedTextColor = MainDarkText,
                unfocusedTextColor = MainDarkText,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedLabelColor = BrandBlue,
                unfocusedLabelColor = SecondaryGrayText
            )
        )

        Button(
            onClick = {
                if (captionText.isBlank()) {
                    scope.launch {
                        snackbarHostState.showSnackbar("Please enter a caption (or generate with AI) first!")
                    }
                } else {
                    viewModel.addNewPost(captionText, selectedCategory)
                    scope.launch {
                        snackbarHostState.showSnackbar("Masterpiece Created! Generating impressions... 📈💸")
                        onPostCreated()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("publish_post_button"),
            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Publish Monetized Post 🚀",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp
            )
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}


// --- SCREEN 4: PROFILE / CREATOR STUDIO ---
@Composable
fun ProfileStudioScreen(
    viewModel: SocialViewModel,
    profile: CreatorProfileEntity?,
    withdrawals: List<com.example.data.database.WithdrawalEntity>,
    isSimulating: Boolean,
    trafficFeedback: String?
) {
    var upiIdInput by remember { mutableStateOf("") }
    var withdrawAmountInput by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (profile == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrandBlue)
            }
        } else {
            // Profile Header
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackgroundLight),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SupportBorderColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User Avatar
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(GoldenEarningGradient)
                                .padding(3.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "AN",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandBlue
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = profile.fullName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MainDarkText
                                )
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified Gold Partner",
                                    tint = BrandBlue,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .padding(start = 4.dp)
                                )
                            }
                            Text(
                                text = "@${profile.username}",
                                fontSize = 13.sp,
                                color = SecondaryGrayText
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = profile.bio,
                                fontSize = 12.sp,
                                color = MainDarkText
                            )
                        }
                    }

                    Divider(color = Color(0xFFE1E2E9))

                    // Earning Dashboard Statistics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(text = "MONETIZATION RATE", fontSize = 10.sp, color = SecondaryGrayText)
                            Text(text = "₹${String.format("%.2f", profile.ratePerView)}/view", fontSize = 15.sp, fontWeight = FontWeight.Black, color = EarningGreen)
                        }
                        VerticalDivider(modifier = Modifier.height(30.dp), color = Color(0xFFE1E2E9))
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(text = "TOTAL EARNED", fontSize = 10.sp, color = SecondaryGrayText)
                            Text(text = "₹${String.format("%.2f", profile.totalEarnings)}", fontSize = 15.sp, fontWeight = FontWeight.Black, color = BrandBlue)
                        }
                    }
                }
            }

            // Wallet/Withdraw Balance Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SoftHighlightBlue), // modern luxury outline
                border = BorderStroke(1.dp, BrandBlue.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "CREATOR WALLET balance",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlue,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "₹${String.format("%.2f", profile.walletBalance)}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = DeepDarkBlueText
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BrandBlue)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "INR CASH",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    Divider(color = BrandBlue.copy(alpha = 0.2f))

                    // UPI Withdrawal Panel
                    Text(
                        text = "Instant UPI Bank Cashout",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = DeepDarkBlueText
                    )

                    OutlinedTextField(
                        value = upiIdInput,
                        onValueChange = { upiIdInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upi_id_field"),
                        placeholder = { Text("Enter UPI ID (e.g. nishad@paytm)", color = SecondaryGrayText, fontSize = 13.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = SupportBorderColor,
                            focusedTextColor = MainDarkText,
                            unfocusedTextColor = MainDarkText,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = withdrawAmountInput,
                            onValueChange = { withdrawAmountInput = it },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("withdraw_amount_field"),
                            placeholder = { Text("Amount (₹)", color = SecondaryGrayText, fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandBlue,
                                unfocusedBorderColor = SupportBorderColor,
                                focusedTextColor = MainDarkText,
                                unfocusedTextColor = MainDarkText,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                val amtStr = withdrawAmountInput.toDoubleOrNull()
                                if (amtStr == null) {
                                    scope.launch { snackbarHostState.showSnackbar("Enter a valid numerical amount!") }
                                } else {
                                    viewModel.requestWithdrawal(
                                        upiId = upiIdInput,
                                        amount = amtStr,
                                        onSuccess = {
                                            withdrawAmountInput = ""
                                            scope.launch { snackbarHostState.showSnackbar("Cashout Request Sent! Processing... 💸🏛️") }
                                        },
                                        onError = { error ->
                                            scope.launch { snackbarHostState.showSnackbar(error) }
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("submit_withdraw_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Withdraw 🏦", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Text(
                        text = "Transactions are verified instantly. Minimum payout: ₹1.",
                        fontSize = 11.sp,
                        color = SecondaryGrayText,
                        fontStyle = FontStyle.Italic
                    )
                }
            }

            // TRAFFIC simulator block
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7E6)),
                border = BorderStroke(1.dp, Color(0xFFFFA500).copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "TRAFFIC BOOSTER & VIRAL GENERATOR",
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = Color(0xFF9E6400),
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "Increase post views automatically! Clicking the booster below will send simulated organic traffic, hearts, and monetize directly into your wallet.",
                        color = SecondaryGrayText,
                        fontSize = 12.sp
                    )

                    Button(
                        onClick = { viewModel.runViralSimulation() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("boost_views_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500)),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isSimulating
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = Color.Black)
                            Text(
                                text = if (isSimulating) "Boosting Traffic Live..." else "Boost Post & Go Viral 🎉",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                              )
                          }
                      }

                    // Feed traffic log response
                    trafficFeedback?.let { log ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFF3CD))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = log,
                                fontSize = 12.sp,
                                color = Color(0xFF856404),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Withdrawal History Statements
            Text(
                text = "Transaction History",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MainDarkText
            )

            if (withdrawals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No withdrawals registered yet. Accumulate views and request cashouts!",
                        color = SecondaryGrayText,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackgroundLight),
                    border = BorderStroke(1.dp, SupportBorderColor)
                ) {
                    Column {
                        withdrawals.forEachIndexed { index, w ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(text = "Payout via UPI", fontWeight = FontWeight.Bold, color = MainDarkText, fontSize = 13.sp)
                                    Text(text = w.upiId, color = SecondaryGrayText, fontSize = 11.sp)
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "₹${String.format("%.2f", w.amount)}",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MainDarkText,
                                        fontSize = 14.sp
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (w.status == "Approved") Color(0xFFD4EDDA) else Color(0xFFFFF3CD))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = w.status,
                                            fontSize = 9.sp,
                                            color = if (w.status == "Approved") Color(0xFF155724) else Color(0xFF856404),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            if (index < withdrawals.lastIndex) {
                                Divider(color = Color(0xFFE1E2E9))
                            }
                        }
                    }
                }
            }
        }
        SnackbarHost(hostState = snackbarHostState)
    }
}


// --- ACCESSORY COMPONENT A: COMMENTS BOTTOM DIALOG ---
@Composable
fun CommentsDialog(
    post: PostEntity,
    viewModel: SocialViewModel,
    onDismiss: () -> Unit
) {
    val comments by viewModel.getComments(post.id).collectAsStateWithLifecycle(initialValue = emptyList())
    var textInput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(top = 48.dp)
                .testTag("comments_dialog_${post.id}"),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, SupportBorderColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Comments (${comments.size})",
                        fontWeight = FontWeight.Bold,
                        color = MainDarkText,
                        fontSize = 16.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "CloseComments", tint = MainDarkText)
                    }
                }

                Divider(color = Color(0xFFE1E2E9), modifier = Modifier.padding(vertical = 8.dp))

                // Post header caption reference
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE1E2E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(post.username.take(2).uppercase(), color = MainDarkText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(post.username, fontWeight = FontWeight.Bold, color = MainDarkText, fontSize = 13.sp)
                        Text(post.caption, color = SecondaryGrayText, fontSize = 13.sp)
                    }
                }

                Divider(color = Color(0xFFE1E2E9), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))

                // Comments scrolling feed
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (comments.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No comments yet. Share your thoughts below!", color = SecondaryGrayText, fontSize = 13.sp)
                            }
                        }
                    } else {
                        items(comments) { comment ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE1E2E9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(comment.username.take(2).uppercase(), color = MainDarkText, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = comment.username, fontWeight = FontWeight.Bold, color = MainDarkText, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        val minsAgo = (System.currentTimeMillis() - comment.timestamp) / 60000
                                        Text(
                                            text = if (minsAgo < 1) "now" else "${minsAgo}m",
                                            color = SecondaryGrayText,
                                            fontSize = 9.sp
                                        )
                                    }
                                    Text(text = comment.text, color = SecondaryGrayText, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                // Add comment input tray
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_comment_input"),
                        placeholder = { Text("Add comment as awadhesh_nishad...", color = SecondaryGrayText, fontSize = 13.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandBlue,
                            unfocusedBorderColor = SupportBorderColor,
                            focusedTextColor = MainDarkText,
                            unfocusedTextColor = MainDarkText,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp)
                    )

                    Button(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                viewModel.addComment(post.id, "awadhesh_nishad", textInput)
                                textInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.testTag("submit_comment_button")
                    ) {
                        Text("Post", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


// --- ACCESSORY COMPONENT B: METRIC TRAFFIC PROMOTION DIALOG ---
@Composable
fun QuickPromotionDetailsDialog(
    viewModel: SocialViewModel,
    isSimulating: Boolean,
    trafficFeedback: String?,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.padding(16.dp),
            border = BorderStroke(1.dp, SupportBorderColor)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = BrandBlue,
                    modifier = Modifier.size(52.dp)
                )

                Text(
                    text = "Impression Monetizer",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MainDarkText,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Aapki published posts par views and traffic simulate karne ka option. Har extra view par ₹0.20 ki earning user account wallet me instantly credit hogi! ✨💸",
                    textAlign = TextAlign.Center,
                    color = SecondaryGrayText,
                    fontSize = 13.sp
                )

                Button(
                    onClick = { viewModel.runViralSimulation() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("promotion_launch_button"),
                    enabled = !isSimulating
                ) {
                    Text(
                        text = if (isSimulating) "Boosting Now..." else "Boost Traffic & Go Viral 🎉",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                }

                trafficFeedback?.let { log ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF3CD))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = log,
                            color = Color(0xFF856404),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                TextButton(onClick = onDismiss) {
                    Text("Close Panel", color = BrandBlue, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun VerticalDivider(
    modifier: Modifier = Modifier,
    color: Color = DividerDefaults.color,
    thickness: androidx.compose.ui.unit.Dp = DividerDefaults.Thickness
) {
    Box(
        modifier
            .width(thickness)
            .fillMaxHeight()
            .background(color = color)
    )
}
