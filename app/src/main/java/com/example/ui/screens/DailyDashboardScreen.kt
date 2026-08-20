package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.OfflineData
import com.example.data.model.*
import com.example.ui.components.GlassCard
import com.example.ui.components.IslamicHeader
import com.example.ui.components.PrayerCountdownCard
import com.example.ui.components.QuranProgressCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel

@Composable
fun DailyDashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.todayTasks.collectAsState()
    val quranProgress by viewModel.quranProgress.collectAsState()
    val prayerData by viewModel.prayerTimes.collectAsState()
    val todayHadith by viewModel.todayHadith.collectAsState()
    val todayFatwa by viewModel.todayFatwa.collectAsState()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showKhatmahDialog by remember { mutableStateOf(false) }
    var showAsmaAllahDialog by remember { mutableStateOf(false) }
    var showZakatDialog by remember { mutableStateOf(false) }

    val completedCount = tasks.count { it.isCompleted }
    val totalCount = tasks.size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(IslamicEmeraldDark),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // 1. Top Header with Hijri Date & Streak & Quick Triggers
        item {
            IslamicHeader(
                prayerData = prayerData,
                completedTasksCount = completedCount,
                totalTasksCount = totalCount,
                onOpenAsmaAllah = { showAsmaAllahDialog = true },
                onOpenZakatCalculator = { showZakatDialog = true }
            )
        }

        // 2. Next Prayer Countdown Card
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                PrayerCountdownCard(
                    prayerData = prayerData,
                    onNavigateToPrayer = { viewModel.setTab(AppTab.PRAYER) }
                )
            }
        }

        // 3. Quran Daily Wird Card
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                QuranProgressCard(
                    progress = quranProgress,
                    onContinueReading = {
                        val surah = OfflineData.all114Surahs.find { it.id == quranProgress.currentSurahId }
                            ?: OfflineData.all114Surahs.first()
                        viewModel.openSurah(surah)
                        viewModel.setTab(AppTab.QURAN)
                    },
                    onOpenPlanDialog = { showKhatmahDialog = true }
                )
            }
        }

        // 4. Quick Shortcuts Row
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "الوصول السريع والخدمات",
                    style = MaterialTheme.typography.titleMedium,
                    color = IslamicGoldPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionCard(
                        title = "أذكار الصباح",
                        icon = Icons.Default.WbSunny,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setAthkarCategory(AthkarCategory.MORNING)
                            viewModel.setTab(AppTab.ATHKAR)
                        }
                    )
                    QuickActionCard(
                        title = "أذكار المساء",
                        icon = Icons.Default.NightsStay,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setAthkarCategory(AthkarCategory.EVENING)
                            viewModel.setTab(AppTab.ATHKAR)
                        }
                    )
                    QuickActionCard(
                        title = "المسبحة",
                        icon = Icons.Default.TouchApp,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setTab(AppTab.ATHKAR)
                        }
                    )
                    QuickActionCard(
                        title = "الأدعية",
                        icon = Icons.Default.VolunteerActivism,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setTab(AppTab.DUAS)
                        }
                    )
                    QuickActionCard(
                        title = "الفتاوى",
                        icon = Icons.Default.MenuBook,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.setTab(AppTab.FATWAS)
                        }
                    )
                }
            }
        }

        // 5. Daily Tasks Section Header
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { showAddTaskDialog = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = IslamicGoldPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "إضافة مهمة",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "مهام العبادات اليومية",
                        style = MaterialTheme.typography.titleMedium,
                        color = IslamicGoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = IslamicGoldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 6. Tasks List
        items(tasks, key = { it.id }) { task ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                DailyTaskItemCard(
                    task = task,
                    onToggle = { viewModel.toggleTask(task) },
                    onIncrement = { viewModel.incrementTask(task) },
                    onDelete = { viewModel.deleteTask(task.id) }
                )
            }
        }

        // 7. Hadith of the Day Card
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = Color(0xFF0F2C20)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✨ من وصايا الحبيب ﷺ",
                                style = MaterialTheme.typography.labelLarge,
                                color = IslamicGoldPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = IslamicGoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "« ${todayHadith.text} »",
                            style = MaterialTheme.typography.bodyLarge,
                            color = IslamicTextPrimary,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            lineHeight = 28.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "— ${todayHadith.narrator} [${todayHadith.source}]",
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicGoldLight,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // 8. Daily Fatwa Showcase Card
        if (todayFatwa != null) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    todayFatwa?.let { fatwa ->
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            backgroundColor = Color(0xFF07261B)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.HelpOutline,
                                            contentDescription = null,
                                            tint = IslamicGoldPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "📖 فتوى وحكم اليوم",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = IslamicGoldPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF144D38)
                                    ) {
                                        Text(
                                            text = fatwa.category.displayName,
                                            fontSize = 10.sp,
                                            color = IslamicGoldLight,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "س: ${fatwa.question}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 22.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = fatwa.summary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = IslamicTextSecondary,
                                    maxLines = 2,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "المفتي: ${fatwa.scholar}",
                                        fontSize = 11.sp,
                                        color = IslamicGoldLight
                                    )

                                    Button(
                                        onClick = {
                                            viewModel.setFatwaCategory(fatwa.category)
                                            viewModel.setTab(AppTab.FATWAS)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = IslamicGoldPrimary,
                                            contentColor = IslamicEmeraldDark
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("التفاصيل والفتاوى", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Custom Task Dialog
    if (showAddTaskDialog) {
        AddCustomTaskDialog(
            onAddTask = { title, category, target, desc ->
                viewModel.addNewTask(title, category, target, desc)
                showAddTaskDialog = false
            },
            onDismiss = { showAddTaskDialog = false }
        )
    }

    // Khatmah Plan Dialog
    if (showKhatmahDialog) {
        KhatmahPlanDialog(
            currentProgress = quranProgress,
            onSavePlan = { days, pages ->
                viewModel.updateKhatmahPlan(days, pages)
                showKhatmahDialog = false
            },
            onDismiss = { showKhatmahDialog = false }
        )
    }

    // 99 Names of Allah Dialog
    if (showAsmaAllahDialog) {
        AsmaAllahDialog(
            onDismiss = { showAsmaAllahDialog = false },
            viewModel = viewModel
        )
    }

    // Smart Zakat Calculator Dialog
    if (showZakatDialog) {
        ZakatCalculatorDialog(
            onDismiss = { showZakatDialog = false },
            viewModel = viewModel
        )
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        contentPadding = 10.dp,
        backgroundColor = Color(0xFF10281F)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0x2EE2B84D)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = IslamicGoldPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = IslamicTextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun DailyTaskItemCard(
    task: DailyTask,
    onToggle: () -> Unit,
    onIncrement: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = if (task.isCompleted) Color(0xFF0D251C) else Color(0xFF112B21),
        borderColor = if (task.isCompleted) IslamicMintDark else IslamicBorderGold,
        contentPadding = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox / Completion indicator
            IconButton(
                onClick = onToggle,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (task.isCompleted) "منجز" else "غير منجز",
                    tint = if (task.isCompleted) IslamicMintLight else IslamicGoldPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Task info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onToggle)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (task.isCompleted) IslamicTextMuted else IslamicTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = IslamicTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Target counter increment button if multiple counts
            if (task.targetCount > 1) {
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = onIncrement,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (task.isCompleted) Color(0x225DD9A9) else Color(0x33E2B84D)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "${task.currentCount}/${task.targetCount} +",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (task.isCompleted) IslamicMintLight else IslamicGoldLight,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Delete button for custom tasks
            if (!task.isDefault) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "حذف المهمة",
                        tint = Color(0x99E57373),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddCustomTaskDialog(
    onAddTask: (title: String, category: TaskCategory, target: Int, description: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(TaskCategory.QURAN) }
    var targetCount by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "إضافة مهمة عبادة يومية",
                style = MaterialTheme.typography.titleMedium,
                color = IslamicGoldPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان المهمة (مثال: سورة يس)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("وصف أو فضل (اختياري)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = targetCount,
                    onValueChange = { targetCount = it },
                    label = { Text("عدد المرات المطلوب يومياً") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val target = targetCount.toIntOrNull() ?: 1
                        onAddTask(title, selectedCategory, target, description)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary)
            ) {
                Text("إضافة المهمة", color = IslamicEmeraldDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = IslamicTextMuted)
            }
        },
        containerColor = Color(0xFF10281F)
    )
}
