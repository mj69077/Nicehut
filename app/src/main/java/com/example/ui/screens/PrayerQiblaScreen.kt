package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CalculationMethod
import com.example.data.model.PrayerTimesData
import com.example.ui.components.GlassCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PrayerQiblaScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val prayerData by viewModel.prayerTimes.collectAsState()
    val calculationMethod by viewModel.calculationMethod.collectAsState()
    val currentCity by viewModel.currentCity.collectAsState()
    val deviceHeading by viewModel.deviceHeading.collectAsState()
    val qiblaAngle by viewModel.qiblaAngle.collectAsState()

    var showCityDialog by remember { mutableStateOf(false) }
    var showMethodDialog by remember { mutableStateOf(false) }

    // Compass difference angle
    val compassRotation by animateFloatAsState(targetValue = -deviceHeading, label = "compass")
    val qiblaRelativeAngle = (qiblaAngle - deviceHeading + 360f) % 360f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(IslamicEmeraldDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showCityDialog = true },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x2EE2B84D))
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "تغيير المدينة",
                        tint = IslamicGoldPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "مواقيت الصلاة واتجاه القبلة",
                        style = MaterialTheme.typography.titleLarge,
                        color = IslamicGoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = IslamicGoldPrimary
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Qibla Compass Dial
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                contentPadding = 20.dp,
                backgroundColor = Color(0xFF0F2C20)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "زاوية القبلة: ${(qiblaAngle).toInt()}°",
                            style = MaterialTheme.typography.bodySmall,
                            color = IslamicMintLight
                        )
                        Text(
                            text = "بوصلة القبلة المشرفة",
                            style = MaterialTheme.typography.titleMedium,
                            color = IslamicGoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Compass Canvas Dial
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF133E2E),
                                        Color(0xFF0A2219),
                                        Color(0xFF06150F)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Rotating Dial
                        Canvas(modifier = Modifier.size(190.dp).rotate(compassRotation)) {
                            val center = Offset(size.width / 2, size.height / 2)
                            val radius = size.width / 2 - 8.dp.toPx()

                            // Outer Circle Ring
                            drawCircle(
                                color = IslamicBorderGold,
                                radius = radius,
                                center = center,
                                style = Stroke(width = 2.dp.toPx())
                            )

                            // Ticks around dial
                            for (i in 0 until 360 step 30) {
                                val rad = Math.toRadians(i.toDouble() - 90)
                                val tickLength = if (i % 90 == 0) 14.dp.toPx() else 8.dp.toPx()
                                val start = Offset(
                                    x = (center.x + (radius - tickLength) * cos(rad)).toFloat(),
                                    y = (center.y + (radius - tickLength) * sin(rad)).toFloat()
                                )
                                val end = Offset(
                                    x = (center.x + radius * cos(rad)).toFloat(),
                                    y = (center.y + radius * sin(rad)).toFloat()
                                )
                                drawLine(
                                    color = if (i % 90 == 0) IslamicGoldPrimary else Color(0x66E2B84D),
                                    start = start,
                                    end = end,
                                    strokeWidth = if (i % 90 == 0) 3.dp.toPx() else 1.5.dp.toPx()
                                )
                            }
                        }

                        // Qibla Pointer Indicator Needle pointing to Kaaba
                        Canvas(modifier = Modifier.size(170.dp).rotate(compassRotation + qiblaAngle)) {
                            val center = Offset(size.width / 2, size.height / 2)
                            val pointerLength = size.width / 2 - 12.dp.toPx()

                            // Needle Path pointing up (North-to-Qibla)
                            val needlePath = Path().apply {
                                moveTo(center.x, center.y - pointerLength)
                                lineTo(center.x - 12.dp.toPx(), center.y)
                                lineTo(center.x, center.y - 4.dp.toPx())
                                lineTo(center.x + 12.dp.toPx(), center.y)
                                close()
                            }
                            drawPath(needlePath, color = IslamicGoldPrimary)

                            // Kaaba Center Pin
                            drawCircle(color = IslamicGoldAccent, radius = 8.dp.toPx(), center = center)
                        }

                        // Center Kaaba Pin / Compass Pivot
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF04140E))
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = "مركز القبلة",
                                tint = IslamicGoldPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (kotlin.math.abs(qiblaRelativeAngle) < 5 || kotlin.math.abs(qiblaRelativeAngle - 360) < 5)
                            "🎯 أنت الآن متوجه مباشرة نحو القبلة الشريفة!"
                        else
                            "وجّه الهاتف حتى يستقر المؤشر الذهبي نحو اتجاه القبلة",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (kotlin.math.abs(qiblaRelativeAngle) < 5 || kotlin.math.abs(qiblaRelativeAngle - 360) < 5)
                            IslamicMintLight
                        else
                            IslamicTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Location & Calculation Method Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassCard(
                    modifier = Modifier.weight(1f).clickable { showCityDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = 12.dp,
                    backgroundColor = Color(0xFF10281F)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "المدينة الحالية", style = MaterialTheme.typography.labelSmall, color = IslamicTextMuted)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = currentCity, style = MaterialTheme.typography.titleSmall, color = IslamicGoldPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                GlassCard(
                    modifier = Modifier.weight(1f).clickable { showMethodDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = 12.dp,
                    backgroundColor = Color(0xFF10281F)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "طريقة الحساب", style = MaterialTheme.typography.labelSmall, color = IslamicTextMuted)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = calculationMethod.titleArabic, style = MaterialTheme.typography.titleSmall, color = IslamicGoldPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Full 5 Prayer Times Rows
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                contentPadding = 16.dp,
                backgroundColor = Color(0xFF0F2A1E)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "مواقيت اليوم (${prayerData.hijriDate})",
                        style = MaterialTheme.typography.titleMedium,
                        color = IslamicGoldPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    PrayerRowItem("صلاة الفجر", prayerData.fajr, prayerData.nextPrayerName == "الفجر")
                    PrayerRowItem("الشروق", prayerData.sunrise, prayerData.nextPrayerName == "الشروق")
                    PrayerRowItem("صلاة الظهر", prayerData.dhuhr, prayerData.nextPrayerName == "الظهر")
                    PrayerRowItem("صلاة العصر", prayerData.asr, prayerData.nextPrayerName == "العصر")
                    PrayerRowItem("صلاة المغرب", prayerData.maghrib, prayerData.nextPrayerName == "المغرب")
                    PrayerRowItem("صلاة العشاء", prayerData.isha, prayerData.nextPrayerName == "العشاء")
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // The Three Holy Mosques Virtues Card
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "المساجد الثلاثة وفضل الصلاة فيها",
                    style = MaterialTheme.typography.titleMedium,
                    color = IslamicGoldPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HolyMosqueMiniCard(
                        title = "المسجد الحرام",
                        subtitle = "١٠٠ ألف صلاة",
                        icon = Icons.Default.Apartment,
                        modifier = Modifier.weight(1f)
                    )
                    HolyMosqueMiniCard(
                        title = "المسجد النبوي",
                        subtitle = "ألف صلاة",
                        icon = Icons.Default.Mosque,
                        modifier = Modifier.weight(1f)
                    )
                    HolyMosqueMiniCard(
                        title = "المسجد الأقصى",
                        subtitle = "٥٠٠ صلاة",
                        icon = Icons.Default.Place,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // City Selector Dialog
    if (showCityDialog) {
        val popularCities = listOf(
            Triple("مكة المكرمة", 21.4225, 39.8262),
            Triple("المدينة المنورة", 24.4672, 39.6111),
            Triple("الرياض", 24.7136, 46.6753),
            Triple("جدة", 21.5433, 39.1728),
            Triple("القاهرة", 30.0444, 31.2357),
            Triple("القدس الشريف", 31.7683, 35.2137),
            Triple("دبي", 25.2048, 55.2708),
            Triple("أبوظبي", 24.4539, 54.3773),
            Triple("عمّان", 31.9454, 35.9284),
            Triple("الكويت", 29.3759, 47.9774),
            Triple("الدوحة", 25.2854, 51.5310),
            Triple("إسطنبول", 41.0082, 28.9784),
            Triple("الدار البيضاء", 33.5731, -7.5898)
        )

        AlertDialog(
            onDismissRequest = { showCityDialog = false },
            title = {
                Text(
                    text = "اختر مدينتك",
                    style = MaterialTheme.typography.titleMedium,
                    color = IslamicGoldPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp)) {
                    items(popularCities) { (city, lat, lng) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (city == currentCity) Color(0x33E2B84D) else Color.Transparent)
                                .clickable {
                                    viewModel.setLocation(city, lat, lng)
                                    showCityDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (city == currentCity) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = IslamicGoldPrimary, modifier = Modifier.size(18.dp))
                            } else {
                                Spacer(modifier = Modifier.size(18.dp))
                            }
                            Text(text = city, style = MaterialTheme.typography.bodyMedium, color = IslamicTextPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCityDialog = false }) {
                    Text("إغلاق", color = IslamicGoldPrimary)
                }
            },
            containerColor = Color(0xFF10281F)
        )
    }

    // Method Selector Dialog
    if (showMethodDialog) {
        AlertDialog(
            onDismissRequest = { showMethodDialog = false },
            title = {
                Text(
                    text = "اختر هيئة الحساب الفلكي",
                    style = MaterialTheme.typography.titleMedium,
                    color = IslamicGoldPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    CalculationMethod.values().forEach { method ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (method == calculationMethod) Color(0x33E2B84D) else Color.Transparent)
                                .clickable {
                                    viewModel.setCalculationMethod(method)
                                    showMethodDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = method == calculationMethod,
                                onClick = {
                                    viewModel.setCalculationMethod(method)
                                    showMethodDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = IslamicGoldPrimary)
                            )
                            Text(text = method.titleArabic, style = MaterialTheme.typography.bodyMedium, color = IslamicTextPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMethodDialog = false }) {
                    Text("إغلاق", color = IslamicGoldPrimary)
                }
            },
            containerColor = Color(0xFF10281F)
        )
    }
}

@Composable
private fun PrayerRowItem(
    name: String,
    time: String,
    isNext: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isNext) Color(0x33E2B84D) else Color(0x0FFFFFFF))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = time,
            style = MaterialTheme.typography.titleMedium,
            color = if (isNext) IslamicGoldLight else IslamicTextPrimary,
            fontWeight = FontWeight.Bold
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isNext) IslamicGoldPrimary else IslamicTextSecondary,
                fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal
            )
            if (isNext) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(IslamicGoldPrimary)
                )
            }
        }
    }
}

@Composable
private fun HolyMosqueMiniCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF09251B),
        border = BorderStroke(1.dp, Color(0x33E2B84D))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF051711)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = IslamicGoldPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = IslamicGoldLight,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = IslamicMintLight,
                fontSize = 9.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

