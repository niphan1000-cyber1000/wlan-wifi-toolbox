package com.aistudio.wlanaitoolbox.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.aistudio.wlanaitoolbox.data.api.GeminiService
import com.aistudio.wlanaitoolbox.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress
import kotlin.random.Random

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiagnosticsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    // Wi-Fi Diagnostic State variables
    var ssid by remember { mutableStateOf("Enterprise_Staff_5G") }
    var rssi by remember { mutableStateOf(-65) } // dBm
    var noiseFloor by remember { mutableStateOf(-95) } // dBm
    var linkSpeed by remember { mutableStateOf(866) } // Mbps
    var frequency by remember { mutableStateOf(5180) } // MHz
    var channelWidth by remember { mutableStateOf("80 MHz") }
    
    // Live simulation toggle
    var isSimulating by remember { mutableStateOf(true) }
    
    // Derived values
    val snr = rssi - noiseFloor
    val channel = remember(frequency) {
        getChannelFromFrequency(frequency)
    }
    val band = remember(frequency) {
        getBandFromFrequency(frequency)
    }

    // Connection testing state
    var isTestingConnection by remember { mutableStateOf(false) }
    var testResultText by remember { mutableStateOf<String?>(null) }
    val pingMsList = remember { mutableStateListOf<Int>() }

    // AI Analysis state
    var isAnalyzingAI by remember { mutableStateOf(false) }
    var aiAnalysisResult by remember { mutableStateOf<String?>(null) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Location permission granted! Fetching live Wi-Fi.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permission denied. Using simulated/saved network values.", Toast.LENGTH_LONG).show()
        }
    }

    // Live update loop for simulation
    LaunchedEffect(isSimulating) {
        if (isSimulating) {
            while (true) {
                delay(2000)
                // Mutate values slightly for a highly realistic visual indicator
                rssi = (rssi + Random.nextInt(-3, 4)).coerceIn(-95, -30)
                noiseFloor = (noiseFloor + Random.nextInt(-1, 2)).coerceIn(-100, -85)
                linkSpeed = (linkSpeed + Random.nextInt(-50, 51)).coerceIn(150, 1200)
            }
        }
    }

    // Function to fetch real Wi-Fi info
    val fetchRealWifiInfo = {
        val permissionCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val connManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connManager.activeNetwork
            val capabilities = connManager.getNetworkCapabilities(activeNetwork)
            
            if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                @Suppress("DEPRECATION")
                val wifiInfo: WifiInfo? = wifiManager.connectionInfo

                if (wifiInfo != null) {
                    isSimulating = false
                    @Suppress("DEPRECATION")
                    val rawSsid = wifiInfo.ssid
                    ssid = if (rawSsid != "<unknown ssid>" && rawSsid.isNotEmpty()) {
                        rawSsid.replace("\"", "")
                    } else {
                        "WLAN-Local-AP"
                    }
                    rssi = wifiInfo.rssi
                    linkSpeed = wifiInfo.linkSpeed
                    frequency = wifiInfo.frequency
                    
                    channelWidth = if (wifiInfo.frequency > 5000) "80 MHz" else "20 MHz"
                    Toast.makeText(context, "Successfully loaded active Wi-Fi stats!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to read Wi-Fi details. Using manual data.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "No active Wi-Fi connection detected! Using simulator.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Scroll state for container
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateDarkBg)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Section Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "NETWORK DIAGNOSTICS",
                            style = MaterialTheme.typography.labelMedium,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Wi-Fi Signal Analyzer",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                    IconButton(
                        onClick = { fetchRealWifiInfo() },
                        modifier = Modifier
                            .background(SlateSurfaceVariant, RoundedCornerShape(12.dp))
                            .testTag("refresh_wifi_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Scan real WiFi",
                            tint = CyberCyan
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Simulation control banner
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SlateSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (isSimulating) Icons.Outlined.Info else Icons.Outlined.CheckCircle,
                        contentDescription = "Status",
                        tint = if (isSimulating) CyberAmber else CyberEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSimulating) "Simulation Mode Active (fluctuating signal)" else "Real Hardware Mode Connected",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isSimulating,
                        onCheckedChange = { isSimulating = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberAmber,
                            checkedTrackColor = CyberAmber.copy(alpha = 0.4f),
                            uncheckedThumbColor = CyberCyan,
                            uncheckedTrackColor = CyberCyan.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.scale(0.7f)
                    )
                }
            }
        }

        // Live Gauge Metrics Row (RSSI and SNR)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // RSSI Gauge Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(180.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Signal Strength (RSSI)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // RSSI Visual Indicator Circle
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(80.dp)
                            .drawBehindRssiRing(rssi)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$rssi",
                                style = MaterialTheme.typography.titleLarge,
                                color = getRssiColor(rssi),
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "dBm",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }

                    Text(
                        text = getRssiQualityText(rssi),
                        style = MaterialTheme.typography.bodyMedium,
                        color = getRssiColor(rssi),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // SNR Gauge Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(180.dp),
                colors = CardDefaults.cardColors(containerColor = SlateSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Signal Noise Ratio (SNR)",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )

                    // SNR Visual Indicator Ring
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(80.dp)
                            .drawBehindSnrRing(snr)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$snr",
                                style = MaterialTheme.typography.titleLarge,
                                color = getSnrColor(snr),
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "dB",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }

                    Text(
                        text = getSnrQualityText(snr),
                        style = MaterialTheme.typography.bodyMedium,
                        color = getSnrColor(snr),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Detailed WiFi Properties Grid
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "WiFi Parameter Details",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 2
                ) {
                    ParameterItem(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Wifi,
                        label = "SSID / AP",
                        value = ssid,
                        color = CyberCyan
                    )
                    ParameterItem(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Speed,
                        label = "Data Rate",
                        value = "$linkSpeed Mbps",
                        color = CyberEmerald
                    )
                    ParameterItem(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.CompassCalibration,
                        label = "Channel",
                        value = "$channel",
                        color = CyberPurple
                    )
                    ParameterItem(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.SettingsInputAntenna,
                        label = "Channel Width",
                        value = channelWidth,
                        color = CyberAmber
                    )
                    ParameterItem(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.NetworkWifi,
                        label = "Band",
                        value = band,
                        color = CyberCyan
                    )
                    ParameterItem(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Tune,
                        label = "Frequency",
                        value = "$frequency MHz",
                        color = CyberEmerald
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Editable Fields Panel
                Text(
                    text = "Edit Diagnostic Parameters manually:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = ssid,
                        onValueChange = { ssid = it },
                        label = { Text("SSID") },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SlateSurfaceVariant,
                            unfocusedContainerColor = SlateSurfaceVariant.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )
                    TextField(
                        value = rssi.toString(),
                        onValueChange = { rssi = it.toIntOrNull() ?: -60 },
                        label = { Text("RSSI") },
                        modifier = Modifier.weight(0.5f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SlateSurfaceVariant,
                            unfocusedContainerColor = SlateSurfaceVariant.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )
                    TextField(
                        value = noiseFloor.toString(),
                        onValueChange = { noiseFloor = it.toIntOrNull() ?: -95 },
                        label = { Text("Noise") },
                        modifier = Modifier.weight(0.5f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SlateSurfaceVariant,
                            unfocusedContainerColor = SlateSurfaceVariant.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )
                }
            }
        }

        // Connection Tester Panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Test connection",
                            tint = CyberEmerald
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Connection Live Test",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                isTestingConnection = true
                                testResultText = "Initializing DNS diagnostics..."
                                pingMsList.clear()
                                delay(800)
                                
                                testResultText = "Resolving google.com..."
                                val host = "8.8.8.8"
                                var successCount = 0
                                
                                for (i in 1..4) {
                                    testResultText = "Pinging Google DNS [$i/4]..."
                                    var rtt = 0L
                                    val isReachable = withContext(Dispatchers.IO) {
                                        try {
                                            val startTime = System.currentTimeMillis()
                                            val address = InetAddress.getByName(host)
                                            val reachable = address.isReachable(1000)
                                            val endTime = System.currentTimeMillis()
                                            rtt = endTime - startTime
                                            reachable
                                        } catch (e: Exception) {
                                            false
                                        }
                                    }
                                    if (isReachable) {
                                        successCount++
                                        pingMsList.add(rtt.toInt())
                                    }
                                    delay(500)
                                }
                                
                                val avgPing = if (pingMsList.isNotEmpty()) pingMsList.average().toInt() else 0
                                testResultText = """
                                    Ping Complete to 8.8.8.8:
                                    - Packets: Sent = 4, Received = $successCount, Lost = ${4 - successCount}
                                    - Average Latency: $avgPing ms
                                    - Packet Jitter: ${if (pingMsList.size > 1) (pingMsList.max() - pingMsList.min()) else 0} ms
                                    - Overall Connectivity: ${if (successCount >= 3) "EXCELLENT" else "DEGRADED"}
                                """.trimIndent()
                                isTestingConnection = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan,
                            contentColor = CyberPurple
                        ),
                        enabled = !isTestingConnection,
                        modifier = Modifier.testTag("test_connection_btn")
                    ) {
                        if (isTestingConnection) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = TextWhite)
                        } else {
                            Text("Test Link")
                        }
                    }
                }

                AnimatedVisibility(visible = testResultText != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .background(SlateDarkBg, RoundedCornerShape(8.dp))
                            .border(1.dp, SlateSurfaceVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = testResultText ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = CyberEmerald
                        )
                    }
                }
            }
        }

        // AI Diagnosis and Expert Ticket Generator
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SlateSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Toolbox",
                            tint = CyberPurple,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "AI Support Diagnosis",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Analyze signal & generate support ticket",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                    Button(
                        onClick = {
                            scope.launch {
                                isAnalyzingAI = true
                                aiAnalysisResult = null
                                
                                val prompt = """
                                    Analyze the following Wi-Fi signal diagnostic stats:
                                    - SSID: $ssid
                                    - RSSI (Signal strength): $rssi dBm
                                    - Noise Floor: $noiseFloor dBm
                                    - SNR (Signal-to-Noise Ratio): $snr dB
                                    - Link Speed: $linkSpeed Mbps
                                    - Frequency: $frequency MHz
                                    - Channel: $channel
                                    - Band: $band
                                    - Channel Width: $channelWidth
                                    
                                    Please output an expert analysis containing:
                                    1. **Signal Quality Evaluation** (evaluating SNR and RSSI specifically)
                                    2. **Interference Evaluation** (evaluate noise floor and channel choice)
                                    3. **Troubleshooting Recommendations** (actionable advice for the user)
                                    4. **Ready-to-Send Support Ticket** (formatted clearly for a support engineer to review).
                                """.trimIndent()

                                aiAnalysisResult = GeminiService.generateContent(
                                    prompt = prompt,
                                    systemInstruction = "You are an expert enterprise WiFi network analysis engine. You help network engineers troubleshoot field problems with clarity and professional precision."
                                )
                                isAnalyzingAI = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberPurple,
                            contentColor = CyberEmerald
                        ),
                        enabled = !isAnalyzingAI,
                        modifier = Modifier.testTag("ai_diagnose_button")
                    ) {
                        if (isAnalyzingAI) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = TextWhite)
                        } else {
                            Text("AI Analysis")
                        }
                    }
                }

                AnimatedVisibility(visible = isAnalyzingAI || aiAnalysisResult != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .background(SlateDarkBg, RoundedCornerShape(8.dp))
                            .border(1.dp, CyberPurple.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        if (isAnalyzingAI) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(color = CyberPurple, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Gemini is diagnosing your connection...", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "AI Diagnostic Report",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = CyberCyan,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = {
                                        aiAnalysisResult?.let {
                                            clipboardManager.setText(AnnotatedString(it))
                                            Toast.makeText(context, "Diagnostic ticket copied!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy to support", tint = CyberCyan, modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = aiAnalysisResult ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextWhite,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParameterItem(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(
        modifier = modifier
            .background(SlateSurfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .border(1.dp, SlateSurfaceVariant, RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
        }
    }
}

// --- Gauge Math and Graphics helpers ---
fun getChannelFromFrequency(freq: Int): Int {
    return when {
        freq in 2412..2484 -> (freq - 2407) / 5
        freq in 5170..5825 -> (freq - 5000) / 5
        freq in 5945..7125 -> (freq - 5940) / 5
        else -> 1
    }
}

fun getBandFromFrequency(freq: Int): String {
    return when {
        freq in 2400..2500 -> "2.4 GHz"
        freq in 4900..5900 -> "5 GHz"
        freq in 5900..7200 -> "6 GHz"
        else -> "Dual Band"
    }
}

fun getRssiColor(rssi: Int): Color {
    return when {
        rssi >= -60 -> GreenSignal
        rssi >= -75 -> OrangeSignal
        else -> RedSignal
    }
}

fun getRssiQualityText(rssi: Int): String {
    return when {
        rssi >= -50 -> "Excellent (AP Near)"
        rssi >= -67 -> "Very Good (Streaming/Gaming)"
        rssi >= -75 -> "Okay (Web Browsing)"
        rssi >= -85 -> "Weak (Unstable)"
        else -> "Extremely Poor (Dead Zone)"
    }
}

fun getSnrColor(snr: Int): Color {
    return when {
        snr >= 25 -> GreenSignal
        snr >= 15 -> OrangeSignal
        else -> RedSignal
    }
}

fun getSnrQualityText(snr: Int): String {
    return when {
        snr >= 30 -> "Pristine Network"
        snr >= 20 -> "Excellent Signal"
        snr >= 15 -> "Moderate Noise"
        snr >= 10 -> "Poor Connection"
        else -> "Heavy Interference"
    }
}

// Draw custom ring indicators in canvas behind rings
fun Modifier.drawBehindRssiRing(rssi: Int): Modifier = this.drawBehind {
    val progress = ((rssi + 100).toFloat() / 70f).coerceIn(0f, 1f)
    val color = getRssiColor(rssi)
    drawArc(
        color = SlateSurfaceVariant,
        startAngle = -225f,
        sweepAngle = 270f,
        useCenter = false,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
    )
    drawArc(
        color = color,
        startAngle = -225f,
        sweepAngle = 270f * progress,
        useCenter = false,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
    )
}

fun Modifier.drawBehindSnrRing(snr: Int): Modifier = this.drawBehind {
    val progress = (snr.toFloat() / 40f).coerceIn(0f, 1f)
    val color = getSnrColor(snr)
    drawArc(
        color = SlateSurfaceVariant,
        startAngle = -225f,
        sweepAngle = 270f,
        useCenter = false,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
    )
    drawArc(
        color = color,
        startAngle = -225f,
        sweepAngle = 270f * progress,
        useCenter = false,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
    )
}
