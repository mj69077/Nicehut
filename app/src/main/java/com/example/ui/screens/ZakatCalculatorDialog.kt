package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ZakatCalculatorDialog(
    onDismiss: () -> Unit,
    viewModel: MainViewModel
) {
    var cashAmount by remember { mutableStateOf("") }
    var gold24Grams by remember { mutableStateOf("") }
    var gold21Grams by remember { mutableStateOf("") }
    var silverGrams by remember { mutableStateOf("") }
    var tradeGoods by remember { mutableStateOf("") }
    var stocksAmount by remember { mutableStateOf("") }
    var debtsDue by remember { mutableStateOf("") }

    // Standard assumed gold price per gram in USD/local unit (can be adjusted)
    var goldPricePerGram by remember { mutableDoubleStateOf(75.0) }
    var silverPricePerGram by remember { mutableDoubleStateOf(0.95) }

    val cashVal = cashAmount.toDoubleOrNull() ?: 0.0
    val gold24Val = (gold24Grams.toDoubleOrNull() ?: 0.0) * goldPricePerGram
    val gold21Val = (gold21Grams.toDoubleOrNull() ?: 0.0) * (goldPricePerGram * (21.0 / 24.0))
    val silverVal = (silverGrams.toDoubleOrNull() ?: 0.0) * silverPricePerGram
    val tradeVal = tradeGoods.toDoubleOrNull() ?: 0.0
    val stocksVal = stocksAmount.toDoubleOrNull() ?: 0.0
    val debtsVal = debtsDue.toDoubleOrNull() ?: 0.0

    val totalZakatableAssets = (cashVal + gold24Val + gold21Val + silverVal + tradeVal + stocksVal - debtsVal).coerceAtLeast(0.0)
    val goldNisabThreshold = 85.0 * goldPricePerGram // 85 grams of gold
    val reachesNisab = totalZakatableAssets >= goldNisabThreshold

    val zakatPayable = if (reachesNisab) totalZakatableAssets * 0.025 else 0.0

    val numberFormatter = remember {
        NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 0
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .clip(RoundedCornerShape(28.dp)),
            color = Color(0xFF041812),
            border = BorderStroke(1.5.dp, IslamicGoldPrimary)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(38.dp)
                                .background(Color(0xFF0C2E22), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = IslamicGoldPrimary)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "💰 حاسبة الزكاة الذكية الشاملة",
                                style = MaterialTheme.typography.titleLarge,
                                color = IslamicGoldLight,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "احسب زكاتك الشرعية بدقة وفق النصاب والأصول",
                                style = MaterialTheme.typography.bodySmall,
                                color = IslamicTextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Box(modifier = Modifier.size(38.dp))
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Nisab & Result Card
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF0A3326),
                        border = BorderStroke(1.5.dp, IslamicGoldPrimary)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "مقدار الزكاة الواجبة إخراجها (٢.٥٪)",
                                fontSize = 13.sp,
                                color = IslamicGoldLight,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${numberFormatter.format(zakatPayable)} ريال / عملتك",
                                fontSize = 26.sp,
                                color = if (reachesNisab) IslamicGoldAccent else Color.White,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (reachesNisab) Color(0xFF1B5E20) else Color(0xFF5D4037)
                                ) {
                                    Text(
                                        text = if (reachesNisab) "✓ بلغ النصاب وحال عليه الحول" else "لم يبلغ نصاب الذهب (٨٥ جرام)",
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "مجموع أموالك الخاضعة للزكاة: ${numberFormatter.format(totalZakatableAssets)} | قيمة النصاب: ${numberFormatter.format(goldNisabThreshold)}",
                                fontSize = 11.sp,
                                color = IslamicTextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Inputs Section
                item {
                    Text(
                        text = "١. الأموال والمدخرات النقدية والودائع",
                        fontSize = 13.sp,
                        color = IslamicGoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ZakatInputField(
                        value = cashAmount,
                        onValueChange = { cashAmount = it },
                        label = "السيولة النقدية في البنك أو الخزينة",
                        placeholder = "0"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "٢. الذهب والفضة (المدخر أو عيار التجارة والكنز)",
                        fontSize = 13.sp,
                        color = IslamicGoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ZakatInputField(
                            value = gold24Grams,
                            onValueChange = { gold24Grams = it },
                            label = "ذهب عيار 24 (جرام)",
                            placeholder = "0",
                            modifier = Modifier.weight(1f)
                        )
                        ZakatInputField(
                            value = gold21Grams,
                            onValueChange = { gold21Grams = it },
                            label = "ذهب عيار 21 (جرام)",
                            placeholder = "0",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    ZakatInputField(
                        value = silverGrams,
                        onValueChange = { silverGrams = it },
                        label = "الفضة الخالصة (جرام)",
                        placeholder = "0"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "٣. عروض التجارة والأسهم الاستثمارية",
                        fontSize = 13.sp,
                        color = IslamicGoldPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ZakatInputField(
                            value = tradeGoods,
                            onValueChange = { tradeGoods = it },
                            label = "بضائع وسلع التجارة (بسعر البيع)",
                            placeholder = "0",
                            modifier = Modifier.weight(1f)
                        )
                        ZakatInputField(
                            value = stocksAmount,
                            onValueChange = { stocksAmount = it },
                            label = "أسهم للتجارة والمضاربة",
                            placeholder = "0",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "٤. يُخصم: الديون والالتزامات المستحقة حالاً",
                        fontSize = 13.sp,
                        color = Color(0xFFEF9A9A),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ZakatInputField(
                        value = debtsDue,
                        onValueChange = { debtsDue = it },
                        label = "ديون واجبة السداد الآن",
                        placeholder = "0"
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 8 Eligible Recipients Info
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF072118),
                        border = BorderStroke(1.dp, Color(0xFF1B4E3C))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "﴿ إِنَّمَا الصَّدَقَاتُ لِلْفُقَرَاءِ وَالْمَسَاكِينِ... ﴾",
                                fontSize = 13.sp,
                                color = IslamicGoldLight,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "مصارف الزكاة الثمانية: الفقراء، المساكين، العاملين عليها، المؤلفة قلوبهم، في الرقاب، الغارمين، في سبيل الله، وابن السبيل.",
                                fontSize = 11.sp,
                                color = IslamicTextSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ZakatInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            if (input.all { it.isDigit() || it == '.' }) {
                onValueChange(input)
            }
        },
        label = { Text(label, fontSize = 11.sp) },
        placeholder = { Text(placeholder, fontSize = 12.sp, color = IslamicTextSecondary) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF08261E),
            unfocusedContainerColor = Color(0xFF061E17),
            focusedBorderColor = IslamicGoldPrimary,
            unfocusedBorderColor = Color(0xFF1D5A46),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = IslamicGoldPrimary,
            unfocusedLabelColor = IslamicTextSecondary
        ),
        modifier = modifier.fillMaxWidth()
    )
}
