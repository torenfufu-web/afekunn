package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.viewmodel.CafeViewModel

sealed class CafeTab(val route: String, val title: String) {
    object Chat : CafeTab("chat", "マスターと対話 ☕️")
    object Notepad : CafeTab("notes", "レシピノート 📝")
    object Calculator : CafeTab("calc", "黒板電卓 🧮")
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen(
    viewModel: CafeViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf<CafeTab>(CafeTab.Chat) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding() // Safe drawing under status bar
    ) {
        // --- 1. Top Cafe Header ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(130.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.img_cafe_banner),
                    contentDescription = "カフェの店先イラスト",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Dark elegant overlay scrim to read text clearly
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = "Cafe AI ✨",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "マスターの温もりと、便利な黒板ツールがある場所",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // --- 2. Custom Chalkboard Menu Tab Switcher ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabs = listOf(CafeTab.Chat, CafeTab.Notepad, CafeTab.Calculator)
                tabs.forEach { tab ->
                    val isSelected = activeTab == tab
                    val tabBg = if (isSelected) {
                        MaterialTheme.colorScheme.primary // Cozy Espresso for selected tab
                    } else {
                        Color.Transparent
                    }
                    val tabTextCol = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(tabBg)
                            .clickable { activeTab = tab }
                            .padding(vertical = 10.dp)
                            .testTag("tab_${tab.route}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.title,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = tabTextCol
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- 3. Body Content with Cozy Fade & Slide Animation ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn(animationSpec = spring(stiffness = 500f)) + slideInHorizontally(
                        animationSpec = spring(stiffness = 500f),
                        initialOffsetX = { fullWidth -> if (targetState.route == "chat") -fullWidth else fullWidth }
                    ) with fadeOut(animationSpec = spring(stiffness = 500f)) + slideOutHorizontally(
                        animationSpec = spring(stiffness = 500f),
                        targetOffsetX = { fullWidth -> if (targetState.route == "chat") fullWidth else -fullWidth }
                    )
                },
                label = "tabTransition"
            ) { state ->
                when (state) {
                    CafeTab.Chat -> ChatScreen(viewModel = viewModel)
                    CafeTab.Notepad -> MemoScreen(viewModel = viewModel)
                    CafeTab.Calculator -> CalculatorScreen(viewModel = viewModel)
                }
            }
        }
    }
}
