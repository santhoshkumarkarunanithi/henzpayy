package com.henzpay.alerts

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private var tick by mutableIntStateOf(0)
    private val refresh = { runOnUiThread { tick++ } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PaymentStore.listeners.add(refresh)
        setContent { MaterialTheme { Screen() } }
    }
    override fun onResume() { super.onResume(); tick++ }
    override fun onDestroy() { PaymentStore.listeners.remove(refresh); super.onDestroy() }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen() {
        val t = tick
        val enabled = NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
        val list = remember(t) { PaymentStore.all(this) }
        val dayStart = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis
        val today = list.filter { it.time >= dayStart }
        var voice by remember { mutableStateOf(PaymentStore.voice(this)) }
        val fmt = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

        Scaffold(topBar = { TopAppBar(title = { Text("HenzPay Pay Alerts") }) }) { pad ->
            Column(Modifier.padding(pad).padding(16.dp)) {
                if (!enabled) {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Allow notification access", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text("HenzPay reads ONLY payment notifications from PhonePe, Google Pay, Paytm and BHIM to show them in one place. Nothing leaves your phone.")
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }) { Text("Agree & open settings") }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Today's collection")
                        Text("₹%.2f".format(today.sumOf { it.amount }), style = MaterialTheme.typography.headlineLarge)
                        Text("${today.size} payments")
                    }
                }
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Voice alert", Modifier.weight(1f))
                    Switch(checked = voice, onCheckedChange = { voice = it; PaymentStore.setVoice(this@MainActivity, it) })
                }
                LazyColumn {
                    items(list) { p ->
                        ListItem(
                            headlineContent = { Text("₹%.2f  •  ${p.source}".format(p.amount)) },
                            supportingContent = { Text("${p.sender}  •  ${fmt.format(Date(p.time))}") }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
