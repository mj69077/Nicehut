package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioPlaybackState
import com.example.data.model.Reciter
import com.example.data.network.QuranApiService
import com.example.ui.theme.*

@Composable
fun AudioPlayerBar(
    state: AudioPlaybackState,
    onTogglePlayPause: () -> Unit,
    onStop: () -> Unit,
    onSelectReciter: (Reciter) -> Unit,
    modifier: Modifier = Modifier
) {
    var showReciterDialog by remember { mutableStateOf(false) }

    if (state.currentSurah == null) return

    val progress = if (state.durationMs > 0) {
        state.currentPositionMs.toFloat() / state.durationMs.toFloat()
    } else 0f

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = Color(0xFF0C241B),
        contentPadding = 12.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Close / Stop button
                IconButton(
                    onClick = onStop,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = IslamicTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Controls & Reciter Selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Play/Pause button
                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(IslamicGoldPrimary)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = IslamicEmeraldDark,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (state.isPlaying) "إيقاف مؤقت" else "تشغيل",
                                tint = IslamicEmeraldDark,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Surah & Reciter info
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.clickable { showReciterDialog = true }
                    ) {
                        Text(
                            text = "تلاوة سورة ${state.currentSurah.nameArabic}",
                            style = MaterialTheme.typography.labelLarge,
                            color = IslamicGoldLight,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "القارئ: ${state.currentReciter.nameArabic} (تغيير)",
                                style = MaterialTheme.typography.bodySmall,
                                color = IslamicMintLight,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = IslamicMintLight,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = IslamicGoldPrimary,
                trackColor = Color(0x22FFFFFF)
            )
        }
    }

    if (showReciterDialog) {
        AlertDialog(
            onDismissRequest = { showReciterDialog = false },
            title = {
                Text(
                    text = "اختر القارئ المفضل",
                    style = MaterialTheme.typography.titleMedium,
                    color = IslamicGoldPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    QuranApiService.availableReciters.forEach { reciter ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (reciter.id == state.currentReciter.id) Color(0x33E2B84D) else Color.Transparent)
                                .clickable {
                                    onSelectReciter(reciter)
                                    showReciterDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (reciter.id == state.currentReciter.id) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = IslamicGoldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Spacer(modifier = Modifier.size(18.dp))
                            }
                            Text(
                                text = reciter.nameArabic,
                                style = MaterialTheme.typography.bodyMedium,
                                color = IslamicTextPrimary
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showReciterDialog = false }) {
                    Text("إغلاق", color = IslamicGoldPrimary)
                }
            },
            containerColor = Color(0xFF10281F)
        )
    }
}
