package com.moyritm.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moyritm.app.data.AppDatabase
import com.moyritm.app.ui.theme.MyRitmTheme
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database = AppDatabase.getInstance(applicationContext)

        setContent {
            MyRitmTheme {
                MyRitmApp(database)
            }
        }
    }
}

private data class AppTab(
    val title: String,
    val symbol: String,
    val description: String
)

private val appTabs = listOf(
    AppTab("Главная", "⌂", "Сводка дня"),
    AppTab("Стики", "◉", "Время и вид стика"),
    AppTab("Лекарства", "＋", "Расписание и отметки приёма"),
    AppTab("Питание", "♡", "Продукты и КБЖУ"),
    AppTab("Вода", "◍", "Стаканы и бутылки")
)

@Composable
private fun MyRitmApp(database: AppDatabase) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }

    val zone = rememberSaveable { ZoneId.systemDefault() }
    val todayStart = rememberSaveable {
        LocalDate.now().atStartOfDay(zone).toInstant().toEpochMilli()
    }
    val tomorrowStart = rememberSaveable {
        LocalDate.now().plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    }

    val sticks by database.stickDao()
        .observeBetween(todayStart, tomorrowStart)
        .collectAsState(initial = emptyList())
    val medications by database.medicationDao()
        .observeActive()
        .collectAsState(initial = emptyList())
    val intakes by database.medicationDao()
        .observeIntakesBetween(todayStart, tomorrowStart)
        .collectAsState(initial = emptyList())
    val foodEntries by database.foodDao()
        .observeEntriesBetween(todayStart, tomorrowStart)
        .collectAsState(initial = emptyList())
    val waterEntries by database.waterDao()
        .observeBetween(todayStart, tomorrowStart)
        .collectAsState(initial = emptyList())

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = Color(0xFFE9EEE3)) {
                appTabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Text(tab.symbol, fontSize = 20.sp) },
                        label = { Text(tab.title, fontSize = 10.sp) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Header()

            if (selectedTab == 0) {
                HomeScreen(
                    sticksCount = sticks.size,
                    medicationsTaken = intakes.count { it.status == "TAKEN" },
                    medicationsTotal = intakes.size,
                    activeMedications = medications.size,
                    foodKcal = foodEntries.sumOf { it.energyKcal },
                    waterMl = waterEntries.sumOf { it.amountMl },
                    onOpenTab = { selectedTab = it }
                )
            } else {
                SectionPlaceholder(tab = appTabs[selectedTab])
            }
        }
    }
}

@Composable
private fun Header() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 22.dp, top = 24.dp, bottom = 12.dp)
    ) {
        Text(
            text = "Мой ритм",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        val date = LocalDate.now().format(
            DateTimeFormatter.ofPattern("d MMMM", Locale("ru"))
        )
        Text(
            text = "Сегодня, " + date,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HomeScreen(
    sticksCount: Int,
    medicationsTaken: Int,
    medicationsTotal: Int,
    activeMedications: Int,
    foodKcal: Double,
    waterMl: Int,
    onOpenTab: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Твой день в одном месте",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Стики",
                value = sticksCount.toString(),
                detail = "записей сегодня",
                onClick = { onOpenTab(1) }
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Лекарства",
                value = medicationsTaken.toString() + "/" + medicationsTotal,
                detail = "приёмов отмечено",
                onClick = { onOpenTab(2) }
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Питание",
                value = String.format(Locale.getDefault(), "%.0f", foodKcal),
                detail = "ккал за день",
                onClick = { onOpenTab(3) }
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                title = "Вода",
                value = waterMl.toString(),
                detail = "мл за день",
                onClick = { onOpenTab(4) }
            )
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Спокойно, шаг за шагом",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Активных лекарств: " + activeMedications +
                        ". Все записи будут храниться на этом устройстве.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            text = "Быстрый переход",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 4.dp)
        )
        appTabs.drop(1).forEachIndexed { index, tab ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenTab(index + 1) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(tab.symbol, fontSize = 22.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(tab.title, fontWeight = FontWeight.SemiBold)
                        Text(
                            tab.description,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text("›", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier,
    title: String,
    value: String,
    detail: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = CardDefaults.shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = value,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = detail,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SectionPlaceholder(tab: AppTab) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = tab.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = tab.description,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Text(
                text = "Основа раздела подготовлена. На следующем этапе добавим формы ввода, " +
                    "редактирование записей и напоминания. Пока данные не добавляются через этот экран.",
                modifier = Modifier.padding(18.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
