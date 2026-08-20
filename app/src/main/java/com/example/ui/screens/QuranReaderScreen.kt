package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.OfflineData
import com.example.data.model.Ayah
import com.example.data.model.Surah
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel

@Composable
fun QuranReaderScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val selectedSurah by viewModel.selectedSurah.collectAsState()

    if (selectedSurah != null) {
        SurahDetailReaderView(
            surah = selectedSurah!!,
            viewModel = viewModel,
            onBack = { viewModel.closeSurahReader() }
        )
    } else {
        SurahListCatalogView(viewModel = viewModel, modifier = modifier)
    }
}

@Composable
private fun SurahListCatalogView(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.surahSearchQuery.collectAsState()
    var selectedFilter by remember { mutableStateOf("all") } // all, makkah, madinah, recommended

    val filteredSurahs = remember(searchQuery, selectedFilter) {
        OfflineData.all114Surahs.filter { surah ->
            val matchesSearch = searchQuery.isBlank() ||
                    surah.nameArabic.contains(searchQuery.trim()) ||
                    surah.nameEnglish.contains(searchQuery.trim(), ignoreCase = true) ||
                    surah.id.toString() == searchQuery.trim()

            val matchesFilter = when (selectedFilter) {
                "makkah" -> surah.revelationPlace == "makkah"
                "madinah" -> surah.revelationPlace == "madinah"
                "recommended" -> surah.id in listOf(1, 18, 36, 55, 56, 67, 112, 113, 114)
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IslamicEmeraldDark)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        // Title Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x2EE2B84D))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "١١٤ سورة",
                    style = MaterialTheme.typography.labelMedium,
                    color = IslamicGoldLight
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "فهرس القرآن الكريم",
                    style = MaterialTheme.typography.titleLarge,
                    color = IslamicGoldPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.AutoStories,
                    contentDescription = null,
                    tint = IslamicGoldPrimary
                )
            }
        }

        // Quran Hero Visual Banner Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF07241B),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4DE2B84D))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "القرآن الكريم كاملاً بالرسم العثماني",
                        style = MaterialTheme.typography.titleMedium,
                        color = IslamicGoldLight,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "تلاوات مرتلة لثمانية من كبار القراء مع التفسير والترجمة",
                        style = MaterialTheme.typography.bodySmall,
                        color = IslamicMintLight,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF03140E)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "المصحف الشريف",
                        tint = IslamicGoldPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSurahSearchQuery(it) },
            placeholder = { Text("ابحث عن سورة بالاسم أو الرقم...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "بحث", tint = IslamicGoldPrimary)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSurahSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "مسح", tint = IslamicTextMuted)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = IslamicGoldPrimary,
                unfocusedBorderColor = IslamicBorderGold,
                focusedContainerColor = Color(0xFF10281F),
                unfocusedContainerColor = Color(0xFF10281F)
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filters Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedFilter == "all",
                onClick = { selectedFilter = "all" },
                label = { Text("الكل") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = IslamicGoldPrimary,
                    selectedLabelColor = IslamicEmeraldDark
                )
            )
            FilterChip(
                selected = selectedFilter == "recommended",
                onClick = { selectedFilter = "recommended" },
                label = { Text("⭐ السور الفاضلة") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = IslamicGoldPrimary,
                    selectedLabelColor = IslamicEmeraldDark
                )
            )
            FilterChip(
                selected = selectedFilter == "makkah",
                onClick = { selectedFilter = "makkah" },
                label = { Text("مكية") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = IslamicGoldPrimary,
                    selectedLabelColor = IslamicEmeraldDark
                )
            )
            FilterChip(
                selected = selectedFilter == "madinah",
                onClick = { selectedFilter = "madinah" },
                label = { Text("مدنية") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = IslamicGoldPrimary,
                    selectedLabelColor = IslamicEmeraldDark
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Surah Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredSurahs, key = { it.id }) { surah ->
                SurahGridCard(
                    surah = surah,
                    onClick = { viewModel.openSurah(surah) }
                )
            }
        }
    }
}

@Composable
private fun SurahGridCard(
    surah: Surah,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        contentPadding = 12.dp,
        backgroundColor = Color(0xFF102A20)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Surah Number in Islamic star badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0x33E2B84D)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${surah.id}",
                    style = MaterialTheme.typography.labelMedium,
                    color = IslamicGoldPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "سورة ${surah.nameArabic}",
                    style = MaterialTheme.typography.titleSmall,
                    color = IslamicGoldLight,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${if (surah.revelationPlace == "makkah") "مكية" else "مدنية"} • ${surah.versesCount} آيات",
                    style = MaterialTheme.typography.bodySmall,
                    color = IslamicTextSecondary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun SurahDetailReaderView(
    surah: Surah,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val verses by viewModel.selectedSurahVerses.collectAsState()
    val tafsirMap by viewModel.selectedSurahTafsir.collectAsState()
    val isLoading by viewModel.isSurahLoading.collectAsState()
    val fontSize by viewModel.quranFontSize.collectAsState()
    val playbackState by viewModel.audioPlayer.playbackState.collectAsState()
    val context = LocalContext.current

    var selectedAyahForTafsir by remember { mutableStateOf<Ayah?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(IslamicEmeraldDark)
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0C241B))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "رجوع",
                    tint = IslamicGoldPrimary
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "سورة ${surah.nameArabic}",
                    style = MaterialTheme.typography.titleLarge,
                    color = IslamicGoldPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${if (surah.revelationPlace == "makkah") "مكية" else "مدنية"} • ${surah.versesCount} آية • صفحة ${surah.startPage}",
                    style = MaterialTheme.typography.bodySmall,
                    color = IslamicTextSecondary
                )
            }

            // Audio recitation trigger & font size controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        if (playbackState.currentSurah?.id == surah.id && playbackState.isPlaying) {
                            viewModel.audioPlayer.togglePlayPause()
                        } else {
                            viewModel.audioPlayer.playSurah(surah)
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (playbackState.currentSurah?.id == surah.id && playbackState.isPlaying)
                            Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                        contentDescription = "استماع للتلاوة",
                        tint = IslamicGoldPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Font size + / -
                IconButton(onClick = { viewModel.changeQuranFontSize(2) }, modifier = Modifier.size(32.dp)) {
                    Text("A+", color = IslamicGoldLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                IconButton(onClick = { viewModel.changeQuranFontSize(-2) }, modifier = Modifier.size(32.dp)) {
                    Text("A-", color = IslamicTextMuted, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = IslamicGoldPrimary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("جاري تحميل الآيات الكريمة...", color = IslamicTextSecondary)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
            ) {
                // Basmalah Header (except Surah At-Tawbah 9)
                if (surah.id != 9) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                style = MaterialTheme.typography.headlineMedium,
                                color = IslamicGoldPrimary,
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Verses List
                items(verses, key = { it.numberInSurah }) { ayah ->
                    AyahCard(
                        ayah = ayah,
                        surah = surah,
                        fontSize = fontSize,
                        tafsir = tafsirMap[ayah.numberInSurah] ?: "",
                        isSelectedForTafsir = selectedAyahForTafsir?.numberInSurah == ayah.numberInSurah,
                        onToggleTafsir = {
                            selectedAyahForTafsir = if (selectedAyahForTafsir?.numberInSurah == ayah.numberInSurah) null else ayah
                        },
                        onBookmark = { viewModel.addBookmark(surah, ayah) },
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("آية", "${ayah.textUthmani} ﴿${ayah.numberInSurah}﴾ [سورة ${surah.nameArabic}]")
                            clipboard.setPrimaryClip(clip)
                            viewModel.showNotification("تم النسخ", "تم نسخ الآية الكريمة إلى الحافظة")
                        },
                        onShare = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "${ayah.textUthmani} ﴿${ayah.numberInSurah}﴾\n[سورة ${surah.nameArabic}]\n— تطبيق الورد اليومي")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "مشاركة الآية"))
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun AyahCard(
    ayah: Ayah,
    surah: Surah,
    fontSize: Int,
    tafsir: String,
    isSelectedForTafsir: Boolean,
    onToggleTafsir: () -> Unit,
    onBookmark: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = if (isSelectedForTafsir) Color(0xFF133629) else Color(0xFF10281F),
        contentPadding = 14.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Verse Text with Arabic Ayah End Symbol ﴿X﴾
            Text(
                text = "${ayah.textUthmani} ﴿${ayah.numberInSurah}﴾",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize * 1.8).sp,
                    fontFamily = FontFamily.Serif
                ),
                color = IslamicTextPrimary,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Actions row (Tafsir, Bookmark, Copy, Share)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBookmark, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = "علامة مرجعية", tint = IslamicGoldPrimary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = IslamicTextSecondary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = IslamicTextSecondary, modifier = Modifier.size(16.dp))
                    }
                }

                TextButton(
                    onClick = onToggleTafsir,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = if (isSelectedForTafsir) Icons.Default.MenuBook else Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = IslamicMintLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSelectedForTafsir) "إخفاء التفسير" else "التفسير الميسر",
                        style = MaterialTheme.typography.bodySmall,
                        color = IslamicMintLight,
                        fontSize = 11.sp
                    )
                }
            }

            // Expandable Tafsir Block
            AnimatedVisibility(visible = isSelectedForTafsir) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x26000000))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "📖 التفسير الميسر:",
                        style = MaterialTheme.typography.labelSmall,
                        color = IslamicGoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (tafsir.isNotBlank()) tafsir else "جاري جلب تفسير الآية الكريمة أو لا يتوفر اتصال بالشبكة.",
                        style = MaterialTheme.typography.bodySmall,
                        color = IslamicTextSecondary,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Right
                    )
                }
            }
        }
    }
}
