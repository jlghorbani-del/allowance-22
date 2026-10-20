package com.example.allowance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

data class Level(val name: String, val male: Int, val female: Int) // female = -1 یعنی مجاز نیست
data class Factor(val title: String, val levels: List<Level>)

val factors = listOf(
    Factor("ایستادن", listOf(Level("ندارد", 0, 0), Level("کار ایستاده", 2, 4))),
    Factor("وضعیت بدنی", listOf(Level("عادی", 0, 0), Level("کمی نامناسب", 0, 1),
        Level("نامناسب (خمیده)", 2, 3), Level("خیلی نامناسب (درازکش/کشیده)", 7, 7))),
    Factor("وزن بار / نیرو", listOf(Level("ندارد", 0, 0), Level("2.5 کیلوگرم", 0, 1),
        Level("5 کیلوگرم", 1, 2), Level("7.5 کیلوگرم", 2, 3), Level("10 کیلوگرم", 3, 4),
        Level("12.5 کیلوگرم", 4, 6), Level("15 کیلوگرم", 5, 8), Level("17.5 کیلوگرم", 7, 10),
        Level("20 کیلوگرم", 9, 13), Level("22.5 کیلوگرم", 11, 16), Level("25 کیلوگرم", 13, 20),
        Level("30 کیلوگرم", 17, -1), Level("33.5 کیلوگرم", 22, -1))),
    Factor("روشنایی", listOf(Level("کافی", 0, 0), Level("کمی کمتر از حد توصیه", 0, 0),
        Level("خیلی کمتر از حد توصیه", 2, 2), Level("کاملاً ناکافی", 5, 5))),
    Factor("شرایط جوی (دما)", listOf(Level("عادی", 0, 0), Level("گرم", 2, 2),
        Level("داغ", 3, 3), Level("خیلی داغ", 5, 5), Level("بسیار شدید", 10, 10))),
    Factor("دقت چشمی", listOf(Level("نسبتاً دقیق", 0, 0), Level("دقیق", 2, 2),
        Level("خیلی دقیق", 5, 5))),
    Factor("سر و صدا", listOf(Level("پیوسته", 0, 0), Level("متناوب - بلند", 2, 2),
        Level("متناوب - خیلی بلند", 5, 5), Level("زیر و بلند", 5, 5))),
    Factor("فشار ذهنی", listOf(Level("ندارد", 0, 0), Level("نسبتاً پیچیده", 1, 1),
        Level("پیچیده", 4, 4), Level("خیلی پیچیده", 8, 8))),
    Factor("یکنواختی ذهنی", listOf(Level("کم", 0, 0), Level("متوسط", 1, 1), Level("زیاد", 4, 4))),
    Factor("خسته‌کنندگی جسمی", listOf(Level("نسبتاً خسته‌کننده", 0, 0),
        Level("خسته‌کننده", 2, 1), Level("خیلی خسته‌کننده", 5, 2)))
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MaterialTheme { Surface(Modifier.fillMaxSize()) { AllowanceScreen() } }
            }
        }
    }
}

fun String.num(): Double = this.replace("٫", ".").replace(",", ".").toDoubleOrNull() ?: 0.0

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllowanceScreen() {
    var isMale by remember { mutableStateOf(true) }
    val selected = remember { mutableStateListOf(*Array(factors.size) { 0 }) }
    var absorbed by remember { mutableStateOf("0") }

    var unavoidable by remember { mutableStateOf("100") }
    var available by remember { mutableStateOf("2100") }

    var cycle by remember { mutableStateOf("30") }
    var manual by remember { mutableStateOf("5") }
    var rating by remember { mutableStateOf("100") }
    var cavities by remember { mutableStateOf("2") }
    var setup by remember { mutableStateOf("60") }
    var batch by remember { mutableStateOf("5000") }
    var scrap by remember { mutableStateOf("2") }
    var shift by remember { mutableStateOf("8") }

    // ---------- محاسبات ----------
    val fixed = if (isMale) 9 else 11
    var notAllowed = false
    val variable = factors.indices.sumOf { i ->
        val lv = factors[i].levels[selected[i]]
        val v = if (isMale) lv.male else lv.female
        if (v < 0) { notAllowed = true; 0 } else v
    }
    val opAllow = ((fixed + variable - absorbed.num()) / 100.0).coerceAtLeast(0.0)

    val avail = available.num()
    val p = if (avail > 0) (unavoidable.num() / avail).coerceIn(0.0, 0.99) else 0.0
    val machAllow = p / (1 - p)

    val k = cavities.num().coerceAtLeast(1.0)
    val q = batch.num().coerceAtLeast(1.0)
    val s = (scrap.num() / 100.0).coerceIn(0.0, 0.99)
    val stCycle = cycle.num() * (1 + machAllow) +
            manual.num() * rating.num() / 100.0 * (1 + opAllow)
    val stPart = (stCycle / k + setup.num() * 60.0 / q) / (1 - s)
    val perShift = if (stPart > 0) shift.num() * 3600.0 / stPart else 0.0

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("محاسبه الوانس و زمان استاندارد - تزریق پلاستیک",
            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        Section("۱. الوانس اپراتور (ILO)")
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("جنسیت:  ")
            FilterChip(isMale, { isMale = true }, { Text("مرد") })
            Spacer(Modifier.width(8.dp))
            FilterChip(!isMale, { isMale = false }, { Text("زن") })
        }
        factors.forEachIndexed { i, f ->
            FactorDropdown(f, selected[i], isMale) { selected[i] = it }
        }
        NumField("الوانس جذب‌شده در سیکل (%)", absorbed) { absorbed = it }
        if (notAllowed) Text("⚠️ این وزن برای اپراتور زن مجاز نیست.", color = Color.Red)
        ResultLine("الوانس ثابت", "$fixed %")
        ResultLine("الوانس متغیر", "$variable %")
        ResultLine("الوانس کل اپراتور", "%.1f %%".format(opAllow * 100))

        Section("۲. الوانس ماشین")
        NumField("توقفات اجتناب‌ناپذیر (دقیقه)", unavoidable) { unavoidable = it }
        NumField("زمان در دسترس ماشین (دقیقه)", available) { available = it }
        ResultLine("سهم توقف (p)", "%.1f %%".format(p * 100))
        ResultLine("الوانس ماشین", "%.1f %%".format(machAllow * 100))

        Section("۳. زمان استاندارد")
        NumField("زمان سیکل ماشین (ثانیه)", cycle) { cycle = it }
        NumField("کار دستی خارج از سیکل (ثانیه)", manual) { manual = it }
        NumField("ضریب عملکرد (%)", rating) { rating = it }
        NumField("تعداد کویتی", cavities) { cavities = it }
        NumField("زمان تعویض قالب (دقیقه)", setup) { setup = it }
        NumField("اندازه سری (قطعه)", batch) { batch = it }
        NumField("ضایعات (%)", scrap) { scrap = it }
        NumField("طول شیفت (ساعت)", shift) { shift = it }

        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE2EFDA))) {
            Column(Modifier.padding(12.dp)) {
                ResultLine("زمان استاندارد هر سیکل", "%.2f ثانیه".format(stCycle))
                ResultLine("زمان استاندارد هر قطعه سالم", "%.2f ثانیه".format(stPart))
                ResultLine("ظرفیت در شیفت", "%,.0f قطعه".format(perShift))
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun Section(title: String) {
    Spacer(Modifier.height(8.dp))
    Text(title, style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    HorizontalDivider()
}

@Composable
fun NumField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label) },
        singleLine = true, modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
}

@Composable
fun ResultLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label); Text(value, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FactorDropdown(f: Factor, index: Int, isMale: Boolean, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val lv = f.levels[index]
    val v = if (isMale) lv.male else lv.female
    Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text("${f.title}: ${lv.name}  (${if (v < 0) "مجاز نیست" else "$v%"})")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            f.levels.forEachIndexed { i, l ->
                val lvVal = if (isMale) l.male else l.female
                DropdownMenuItem(
                    text = { Text("${l.name}  (${if (lvVal < 0) "-" else "$lvVal%"})") },
                    onClick = { onSelect(i); expanded = false }
                )
            }
        }
    }
}
