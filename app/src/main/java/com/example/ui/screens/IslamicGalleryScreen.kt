package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.IslamicGalleryData
import com.example.data.model.GalleryCategory
import com.example.data.model.IslamicArtwork
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun IslamicGalleryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<GalleryCategory?>(null) }
    var selectedArtwork by remember { mutableStateOf<IslamicArtwork?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val allArtworks = remember { IslamicGalleryData.allArtworks }
    val filteredArtworks = remember(selectedCategory, searchQuery) {
        allArtworks.filter { item ->
            val matchCat = selectedCategory == null || item.category == selectedCategory
            val matchSearch = searchQuery.isBlank() ||
                    item.title.contains(searchQuery.trim()) ||
                    item.subtitle.contains(searchQuery.trim()) ||
                    item.location.contains(searchQuery.trim()) ||
                    item.tags.any { it.contains(searchQuery.trim()) }
            matchCat && matchSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IslamicEmeraldDark)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0x33E2B84D),
                border = BorderStroke(1.dp, Color(0x66E2B84D))
            ) {
                Text(
                    text = "${allArtworks.size} لوحات ومعالم",
                    style = MaterialTheme.typography.labelSmall,
                    color = IslamicGoldLight,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "المعرض والمعالم الإسلامية",
                    style = MaterialTheme.typography.titleLarge,
                    color = IslamicGoldPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Collections,
                    contentDescription = null,
                    tint = IslamicGoldPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("ابحث عن معالم، مساجد، خلفيات...", fontSize = 12.sp, color = IslamicTextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = IslamicGoldPrimary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = IslamicTextSecondary)
                    }
                }
            },
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF09241B),
                unfocusedContainerColor = Color(0xFF071C15),
                focusedBorderColor = IslamicGoldPrimary,
                unfocusedBorderColor = Color(0xFF1C523F),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("الكل", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IslamicGoldPrimary,
                        selectedLabelColor = IslamicEmeraldDark,
                        containerColor = Color(0xFF0C241B),
                        labelColor = IslamicTextSecondary
                    )
                )
            }
            items(GalleryCategory.values()) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat.title, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IslamicGoldPrimary,
                        selectedLabelColor = IslamicEmeraldDark,
                        containerColor = Color(0xFF0C241B),
                        labelColor = IslamicTextSecondary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Grid of Artworks
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 120.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredArtworks, key = { it.id }) { artwork ->
                ArtworkCard(
                    artwork = artwork,
                    onClick = { selectedArtwork = artwork }
                )
            }
        }
    }

    // Fullscreen Artwork Modal Dialog
    if (selectedArtwork != null) {
        ArtworkDetailDialog(
            artwork = selectedArtwork!!,
            onDismiss = { selectedArtwork = null },
            viewModel = viewModel
        )
    }
}

@Composable
private fun ArtworkCard(
    artwork: IslamicArtwork,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF09261D),
        border = BorderStroke(1.dp, Color(0x33E2B84D)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column {
            // Image Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color(0xFF04140E)),
                contentAlignment = Alignment.Center
            ) {
                // AsyncImage with local vector fallback
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(artwork.imageUrl)
                        .crossfade(true)
                        .error(artwork.localDrawableRes)
                        .placeholder(artwork.localDrawableRes)
                        .build(),
                    contentDescription = artwork.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Category Tag overlay
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xCC041812),
                    border = BorderStroke(0.5.dp, IslamicGoldPrimary),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    Text(
                        text = artwork.category.title,
                        fontSize = 9.sp,
                        color = IslamicGoldLight,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Info Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(
                    text = artwork.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = IslamicGoldPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = artwork.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = IslamicTextMuted,
                    fontSize = 10.sp,
                    maxLines = 1,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ArtworkDetailDialog(
    artwork: IslamicArtwork,
    onDismiss: () -> Unit,
    viewModel: MainViewModel
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .clip(RoundedCornerShape(26.dp)),
            color = Color(0xFF041812),
            border = BorderStroke(1.5.dp, IslamicGoldPrimary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF0C2E22), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = IslamicGoldPrimary)
                    }

                    Text(
                        text = "معالم الحضارة الإسلامية",
                        style = MaterialTheme.typography.titleMedium,
                        color = IslamicGoldPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "${artwork.title}\n${artwork.subtitle}\n\n${artwork.description}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "مشاركة المعلم الإسلامي"))
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFF0C2E22), CircleShape)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = IslamicGoldLight)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // Big Image
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, IslamicGoldPrimary)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(artwork.imageUrl)
                                        .crossfade(true)
                                        .error(artwork.localDrawableRes)
                                        .placeholder(artwork.localDrawableRes)
                                        .build(),
                                    contentDescription = artwork.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Gradient overlay at bottom
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(70.dp)
                                        .align(Alignment.BottomCenter)
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color.Transparent, Color(0xCC041812))
                                            )
                                        )
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = artwork.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = IslamicGoldLight,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "📍 ${artwork.location}",
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicMintLight,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "« ${artwork.subtitle} »",
                            style = MaterialTheme.typography.bodyMedium,
                            color = IslamicGoldAccent,
                            fontFamily = FontFamily.Serif,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF0A2B20),
                            border = BorderStroke(1.dp, Color(0x33E2B84D))
                        ) {
                            Text(
                                text = artwork.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFECEFF1),
                                lineHeight = 24.sp,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        // Tags Row
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(artwork.tags) { tag ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0x22E2B84D),
                                    border = BorderStroke(0.5.dp, Color(0x66E2B84D))
                                ) {
                                    Text(
                                        text = "#$tag",
                                        fontSize = 11.sp,
                                        color = IslamicGoldLight,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            val clip = ClipData.newPlainText("Islamic Landmark", "${artwork.title}\n${artwork.location}\n${artwork.description}")
                            clipboard?.setPrimaryClip(clip)
                            viewModel.showNotification("تم النسخ", "تم نسخ معلومات ${artwork.title}")
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = IslamicEmeraldDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("نسخ التفاصيل", color = IslamicEmeraldDark, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = IslamicGoldLight),
                        border = BorderStroke(1.dp, Color(0x66E2B84D)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إغلاق")
                    }
                }
            }
        }
    }
}
