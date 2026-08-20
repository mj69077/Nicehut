package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.OfflineData
import com.example.data.model.AthkarCategory
import com.example.data.model.AthkarItem
import com.example.data.model.TasbihRecord
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

enum class BeadMaterial(val title: String, val primaryColor: Color, val accentColor: Color, val bgGradient: List<Color>) {
    GOLD("ذهب عيار ٢٤", Color(0xFFFFD700), Color(0xFFFFA500), listOf(Color(0xFF4A3B00), Color(0xFF1E1700))),
    EMERALD("زمرد أندلسي", Color(0xFF00E676), Color(0xFF00B0FF), listOf(Color(0xFF00381B), Color(0xFF00170A))),
    RUBY("عقيق يماني", Color(0xFFFF5252), Color(0xFFFFD700), listOf(Color(0xFF450A0A), Color(0xFF1A0000))),
    PEARL("لؤلؤ ملكي", Color(0xFFECEFF1), Color(0xFF80DEEA), listOf(Color(0xFF263238), Color(0xFF10171A)))
}

@Composable
fun AthkarTasbihScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableStateOf(0) } // 0: Athkar, 1: Digital Tasbih, 2: 99 Names

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IslamicEmeraldDark)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        // Section Tabs Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0C241B))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TabButton(
                title = "أسماء الله الحسنى",
                isSelected = selectedSection == 2,
                onClick = { selectedSection = 2 },
                modifier = Modifier.weight(1f)
            )
            TabButton(
                title = "المسبحة الملكية",
                isSelected = selectedSection == 1,
                onClick = { selectedSection = 1 },
                modifier = Modifier.weight(1f)
            )
            TabButton(
                title = "الأذكار اليومية",
                isSelected = selectedSection == 0,
                onClick = { selectedSection = 0 },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (selectedSection) {
            0 -> AthkarSectionView(viewModel = viewModel)
            1 -> RoyalDigitalTasbihSectionView(viewModel = viewModel)
            2 -> AsmaAllahSectionView(viewModel = viewModel)
        }
    }
}

@Composable
private fun TabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) IslamicGoldPrimary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) IslamicEmeraldDark else IslamicTextSecondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun AthkarSectionView(viewModel: MainViewModel) {
    val categories = AthkarCategory.values()
    var selectedCat by remember { mutableStateOf(AthkarCategory.MORNING) }
    val athkarList by viewModel.getAthkarForCategory(selectedCat).collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize()) {
        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { cat ->
                FilterChip(
                    selected = cat == selectedCat,
                    onClick = { selectedCat = cat },
                    label = { Text(cat.displayName, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IslamicGoldPrimary,
                        selectedLabelColor = IslamicEmeraldDark,
                        containerColor = Color(0xFF0C241B),
                        labelColor = IslamicTextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = cat == selectedCat,
                        borderColor = Color(0xFF1E4D3B),
                        selectedBorderColor = IslamicGoldPrimary
                    )
                )
            }
        }

        // Athkar Banner with Glowing Lantern
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF07241B),
            border = BorderStroke(1.dp, Color(0x33E2B84D))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "« أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ »",
                        style = MaterialTheme.typography.bodyMedium,
                        color = IslamicGoldLight,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "حصن المسلم وأذكار اليوم والليلة من الكتاب والسنة",
                        style = MaterialTheme.typography.bodySmall,
                        color = IslamicMintLight,
                        fontSize = 10.sp
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF04140E)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "فضل الذكر",
                        tint = IslamicGoldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Athkar List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(athkarList, key = { it.id }) { item ->
                AthkarItemCard(
                    item = item,
                    onTap = { viewModel.incrementAthkar(item) }
                )
            }
        }
    }
}

@Composable
private fun AthkarItemCard(
    item: AthkarItem,
    onTap: () -> Unit
) {
    val context = LocalContext.current

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (item.isCompleted) Color(0xFF071F17) else Color(0xFF0B291F),
        border = BorderStroke(
            1.dp,
            if (item.isCompleted) Color(0xFF1E5B42) else Color(0x33E2B84D)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Category & Share/Copy
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x22E2B84D)
                ) {
                    Text(
                        text = item.category.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = IslamicGoldLight,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        val clip = ClipData.newPlainText("Thikr", item.arabicText)
                        clipboard?.setPrimaryClip(clip)
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "نسخ الذكر",
                        tint = IslamicGoldLight,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Arabic Text
            Text(
                text = "« ${item.arabicText} »",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 17.sp,
                    lineHeight = 28.sp,
                    fontFamily = FontFamily.Serif
                ),
                color = if (item.isCompleted) IslamicTextMuted else IslamicTextPrimary,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            if (item.reward.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "✨ ${item.reward}",
                    style = MaterialTheme.typography.bodySmall,
                    color = IslamicGoldLight,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Counter & Tap Trigger
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "— ${item.source}",
                    style = MaterialTheme.typography.bodySmall,
                    color = IslamicTextMuted,
                    fontSize = 11.sp
                )

                // Big Counter Touch Button
                Button(
                    onClick = onTap,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (item.isCompleted) Color(0xFF1B5E20) else IslamicGoldPrimary,
                        contentColor = if (item.isCompleted) Color.White else IslamicEmeraldDark
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    if (item.isCompleted) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تم الإنجاز (${item.countTarget})", fontWeight = FontWeight.Bold)
                    } else {
                        Text(
                            text = "${item.currentCount} / ${item.countTarget}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoyalDigitalTasbihSectionView(viewModel: MainViewModel) {
    val tasbihList by viewModel.tasbihCounters.collectAsState()
    val activeId by viewModel.activeTasbihId.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedMaterial by remember { mutableStateOf(BeadMaterial.GOLD) }

    val activeTasbih = remember(tasbihList, activeId) {
        tasbihList.find { it.id == activeId } ?: tasbihList.firstOrNull() ?: TasbihRecord(title = "سُبْحَانَ اللَّهِ", targetCount = 33)
    }

    // Scale animation on tap
    val scale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 90.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Material Selector & Presets Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0x2EE2B84D))
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة ذكر", tint = IslamicGoldPrimary, modifier = Modifier.size(20.dp))
            }

            LazyRow(
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(tasbihList, key = { it.id }) { item ->
                    FilterChip(
                        selected = item.id == activeTasbih.id,
                        onClick = { viewModel.selectTasbih(item.id) },
                        label = { Text(item.title, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IslamicGoldPrimary,
                            selectedLabelColor = IslamicEmeraldDark
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bead Material Switcher Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0A2218))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BeadMaterial.values().forEach { mat ->
                val isSelected = mat == selectedMaterial
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) mat.primaryColor.copy(alpha = 0.25f) else Color.Transparent,
                    border = if (isSelected) BorderStroke(1.dp, mat.primaryColor) else null,
                    modifier = Modifier
                        .clickable { selectedMaterial = mat }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = mat.title,
                        fontSize = 11.sp,
                        color = if (isSelected) mat.primaryColor else IslamicTextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Ultra Luxury Interactive Misbaha Canvas & Touch Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(32.dp))
                .pointerInput(activeTasbih.id) {
                    detectTapGestures(
                        onTap = {
                            coroutineScope.launch {
                                scale.animateTo(0.94f, spring(stiffness = Spring.StiffnessHigh))
                                scale.animateTo(1f, spring(stiffness = Spring.StiffnessMedium))
                            }
                            viewModel.incrementActiveTasbih(activeTasbih)
                        }
                    )
                },
            color = Color(0xFF071D15),
            border = BorderStroke(2.dp, Brush.linearGradient(listOf(selectedMaterial.primaryColor, Color(0xFF0F3B2C))))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scale.value),
                contentAlignment = Alignment.Center
            ) {
                // Royal Circular Bead Wheel Drawing
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.minDimension * 0.38f
                    val numBeads = 33

                    // Draw outer subtle glowing track
                    drawCircle(
                        color = selectedMaterial.primaryColor.copy(alpha = 0.15f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Draw the 33 beads in a circular loop
                    val currentPos = activeTasbih.currentCount % numBeads
                    for (i in 0 until numBeads) {
                        val angle = (i * 2 * Math.PI / numBeads) - (Math.PI / 2)
                        val bx = (center.x + radius * cos(angle)).toFloat()
                        val by = (center.y + radius * sin(angle)).toFloat()

                        val isPassed = i <= currentPos
                        val beadRadius = if (i == currentPos) 8.dp.toPx() else 5.5.dp.toPx()
                        val beadColor = if (isPassed) selectedMaterial.primaryColor else Color(0x44FFFFFF)

                        drawCircle(
                            color = beadColor,
                            radius = beadRadius,
                            center = Offset(bx, by)
                        )
                    }
                }

                // Center Counter Info
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = activeTasbih.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = selectedMaterial.primaryColor,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Serif
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "${activeTasbih.currentCount}",
                        fontSize = 54.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "هدف الدورة: ${activeTasbih.targetCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = IslamicGoldLight
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "✨ المجموع الكلي: ${activeTasbih.totalAllTime} • الدورات: ${activeTasbih.totalRounds}",
                        fontSize = 11.sp,
                        color = IslamicMintLight
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "« انقر في أي مكان للتسبيح »",
                        fontSize = 10.sp,
                        color = IslamicTextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Reset & Goal Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { viewModel.resetTasbih(activeTasbih.id) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF9A9A)),
                border = BorderStroke(1.dp, Color(0x66EF9A9A))
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("تصفير العداد", fontSize = 11.sp)
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F3628),
                border = BorderStroke(1.dp, Color(0x44E2B84D))
            ) {
                Text(
                    text = "🌱 ذكر مستمر",
                    fontSize = 11.sp,
                    color = IslamicGoldLight,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var target by remember { mutableStateOf("33") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "إضافة تسبيح مخصص",
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
                        label = { Text("نص الذكر (مثال: حَسْبُنَا اللَّهُ وَنِعْمَ الْوَكِيلُ)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = target,
                        onValueChange = { target = it },
                        label = { Text("عدد المرات في كل دورة") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.addNewTasbih(title, target.toIntOrNull() ?: 33)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGoldPrimary)
                ) {
                    Text("إضافة", color = IslamicEmeraldDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("إلغاء", color = IslamicTextMuted)
                }
            },
            containerColor = Color(0xFF10281F)
        )
    }
}

@Composable
private fun AsmaAllahSectionView(viewModel: MainViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    val allNames = remember { OfflineData.asmaAllahAlHusna }
    val filtered = remember(searchQuery) {
        if (searchQuery.isBlank()) allNames
        else allNames.filter { it.nameArabic.contains(searchQuery) || it.meaningArabic.contains(searchQuery) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("ابحث في أسماء الله الـ 99...", fontSize = 12.sp, color = IslamicTextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = IslamicGoldPrimary) },
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

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filtered, key = { it.number }) { asma ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0A2B20),
                    border = BorderStroke(1.dp, Color(0x33E2B84D)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0x33E2B84D),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${asma.number}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IslamicGoldLight,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = asma.nameArabic,
                            style = MaterialTheme.typography.titleMedium,
                            color = IslamicGoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = asma.meaningArabic,
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicTextSecondary,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}
