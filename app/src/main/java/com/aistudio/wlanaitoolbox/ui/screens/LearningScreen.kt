package com.aistudio.wlanaitoolbox.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.wlanaitoolbox.ui.theme.*

@Composable
fun LearningScreen() {
    var activeSubPage by remember { mutableIntStateOf(0) }
    val subPages = listOf(
        "Standards & Glossary",
        "Field Cheat Sheets",
        "Interactive Spectrum"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateDarkBg)
    ) {
        ScrollableTabRow(
            selectedTabIndex = activeSubPage,
            containerColor = SlateSurface,
            contentColor = CyberCyan,
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            subPages.forEachIndexed { index, title ->
                Tab(
                    selected = activeSubPage == index,
                    onClick = { activeSubPage = index },
                    text = { Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                    selectedContentColor = CyberCyan,
                    unselectedContentColor = TextMuted,
                    modifier = Modifier.testTag("learn_tab_$index")
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (activeSubPage) {
                0 -> GlossaryAndStandardsView()
                1 -> LaminatedCheatSheetsView()
                2 -> InteractiveSpectrumCanvasView()
            }
        }
    }
}

// --- 1. WiFi Standards & Glossary ---
@Composable
fun GlossaryAndStandardsView() {
    var searchQuery by remember { mutableStateOf("") }
    val glossary = remember {
        listOf(
            GlossaryItem("BSSID", "Basic Service Set Identifier", "The physical MAC address of the wireless router or access point's radio card."),
            GlossaryItem("ESSID", "Extended Service Set Identifier", "The logical name of the wireless network (SSID) spans multiple physical Access Points."),
            GlossaryItem("MU-MIMO", "Multi-User MIMO", "Allows an AP to transmit spatial streams of data to multiple connected wireless devices simultaneously."),
            GlossaryItem("Beamforming", "Spatial Filtering", "A radio technology that focuses a signal in the direction of a specific receiving device, rather than broadcasting in all directions."),
            GlossaryItem("DFS", "Dynamic Frequency Selection", "A Wi-Fi channel allocation scheme that allows devices to share 5GHz spectrum with radar systems without causing interference."),
            GlossaryItem("OFDMA", "Orthogonal Frequency Division Multiple Access", "Subdivides Wi-Fi channels into smaller sub-carriers (Resource Units) to handle multiple low-throughput users efficiently."),
            GlossaryItem("EIRP", "Equivalent Isotropically Radiated Power", "The total effective power radiated by an antenna relative to an isotropic antenna."),
            GlossaryItem("SNR", "Signal to Noise Ratio", "The ratio of wireless signal power to background electrical noise power. Higher values mean clearer connection."),
            GlossaryItem("RSSI", "Received Signal Strength Indicator", "A logarithmic measurement of the power level received by a Wi-Fi client device in dBm.")
        )
    }

    val standards = remember {
        listOf(
            StandardMapping("802.11b", "Wi-Fi 1", "2.4 GHz", "11 Mbps", "DSSS modulation"),
            StandardMapping("802.11a", "Wi-Fi 2", "5.0 GHz", "54 Mbps", "OFDM introduced"),
            StandardMapping("802.11g", "Wi-Fi 3", "2.4 GHz", "54 Mbps", "OFDM on 2.4GHz"),
            StandardMapping("802.11n", "Wi-Fi 4", "2.4 / 5.0 GHz", "600 Mbps", "MIMO introduced"),
            StandardMapping("802.11ac", "Wi-Fi 5", "5.0 GHz", "6.9 Gbps", "256-QAM, MU-MIMO"),
            StandardMapping("802.11ax", "Wi-Fi 6 / 6E", "2.4 / 5 / 6 GHz", "9.6 Gbps", "OFDMA, 1024-QAM"),
            StandardMapping("802.11be", "Wi-Fi 7", "2.4 / 5 / 6 GHz", "46.1 Gbps", "320 MHz, 4096-QAM")
        )
    }

    val filteredGlossary = remember(searchQuery) {
        if (searchQuery.isEmpty()) {
            glossary
        } else {
            glossary.filter {
                it.term.contains(searchQuery, ignoreCase = true) || it.definition.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "IEEE vs Wi-Fi Alliance Standards",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                standards.forEach { std ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SlateSurfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .border(1.dp, SlateSurfaceVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(std.ieee, fontWeight = FontWeight.Bold, color = CyberCyan, style = MaterialTheme.typography.bodyMedium)
                            Text("Bands: ${std.bands}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(std.marketing, fontWeight = FontWeight.Black, color = CyberEmerald, style = MaterialTheme.typography.bodyMedium)
                            Text("Max speed: ${std.maxSpeed}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                    }
                }
            }
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Wi-Fi Glossary Database",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search wireless terms...") },
                modifier = Modifier.fillMaxWidth().testTag("glossary_search"),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search glossary") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredGlossary.forEach { item ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SlateSurfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .border(1.dp, SlateSurfaceVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(item.term, fontWeight = FontWeight.Bold, color = CyberCyan, style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("(${item.fullName})", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(item.definition, style = MaterialTheme.typography.bodySmall, color = TextWhite)
                    }
                }
            }
        }
    }
}

data class GlossaryItem(val term: String, val fullName: String, val definition: String)
data class StandardMapping(val ieee: String, val marketing: String, val bands: String, val maxSpeed: String, val notes: String)

// --- 2. Laminated Field Cheat Sheets ---
@Composable
fun LaminatedCheatSheetsView() {
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Calculate, contentDescription = "Math", tint = CyberCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "The RF Rule of 3s and 10s",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }
            Text(
                text = "Use these mental math rules to quickly estimate dBm gain and loss in the field without a scientific calculator.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                RuleRow("+3 dB", "Double the power in Watts (x2)", isGain = true)
                RuleRow("-3 dB", "Half the power in Watts (/2)", isGain = false)
                RuleRow("+10 dB", "Tenfold power increase in Watts (x10)", isGain = true)
                RuleRow("-10 dB", "Power decrease by a factor of ten in Watts (/10)", isGain = false)
            }
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WifiTethering, contentDescription = "Quality", tint = CyberCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "RSSI Signal Quality Cheat Sheet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RssiRangeRow("-30 dBm", "Amazing / Perfect", "Connected next to an Access Point.", CyberEmerald)
                RssiRangeRow("-67 dBm", "Minimum for Voice / Video", "Perfect for high-throughput streaming and video calls.", CyberEmerald)
                RssiRangeRow("-70 dBm", "Good / Minimum for Web", "Sufficient for standard web browsing, email and file sync.", CyberCyan)
                RssiRangeRow("-80 dBm", "Poor / Unstable", "Packet loss is highly probable. Voice calls degrade.", CyberAmber)
                RssiRangeRow("-90 dBm", "Unusable", "Client connection is drops frequently or fails altogether.", RedSignal)
            }
        }
    }
}

@Composable
fun RuleRow(dB: String, description: String, isGain: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SlateSurfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .border(1.dp, SlateSurfaceVariant, RoundedCornerShape(8.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dB,
            fontWeight = FontWeight.Black,
            color = if (isGain) CyberEmerald else RedSignal,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = TextWhite,
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun RssiRangeRow(range: String, quality: String, usage: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SlateSurfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .border(1.dp, SlateSurfaceVariant, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(range, fontWeight = FontWeight.Black, color = color, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Column(modifier = Modifier.weight(2.5f)) {
            Text(quality, fontWeight = FontWeight.Bold, color = TextWhite, style = MaterialTheme.typography.bodyMedium)
            Text(usage, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        }
    }
}

// --- 3. Breathtaking Custom Drawn Spectrum overlapping diagram ---
@Composable
fun InteractiveSpectrumCanvasView() {
    var selectedChannel by remember { mutableIntStateOf(6) }

    Card(
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Analytics, contentDescription = "Spectrum", tint = CyberCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "2.4 GHz Spectrum Overlaps",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }
            Text(
                text = "Tap a channel below to see its coverage. Channels 1, 6, and 11 are the only non-overlapping paths in the 2.4 GHz band.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            // Diagram Canvas Drawing
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(SlateDarkBg, RoundedCornerShape(12.dp))
                    .border(1.dp, SlateSurfaceVariant, RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    
                    // Draw base frequency line at bottom
                    drawLine(
                        color = SlateSurfaceVariant,
                        start = Offset(0f, height - 30f),
                        end = Offset(width, height - 30f),
                        strokeWidth = 2.dp.toPx()
                    )

                    // Helper to draw a single channel bell curve
                    fun drawChannelCurve(channelNum: Int, centerPx: Float, color: Color, isSelected: Boolean) {
                        val curveWidth = width * 0.28f
                        val path = Path().apply {
                            moveTo(centerPx - curveWidth / 2f, height - 30f)
                            cubicTo(
                                centerPx - curveWidth / 4f, height - 30f,
                                centerPx - curveWidth / 5f, 40f,
                                centerPx, 40f
                            )
                            cubicTo(
                                centerPx + curveWidth / 5f, 40f,
                                centerPx + curveWidth / 4f, height - 30f,
                                centerPx + curveWidth / 2f, height - 30f
                            )
                        }

                        // Fill translucent
                        drawPath(
                            path = path,
                            color = color.copy(alpha = if (isSelected) 0.3f else 0.08f)
                        )

                        // Draw boundary outline
                        drawPath(
                            path = path,
                            color = color.copy(alpha = if (isSelected) 1f else 0.4f),
                            style = Stroke(width = if (isSelected) 3.dp.toPx() else 1.5.dp.toPx())
                        )
                    }

                    // Draw curves for Channel 1, 6, 11 (the non-overlapping gold standards)
                    val px1 = width * 0.20f
                    val px6 = width * 0.50f
                    val px11 = width * 0.80f

                    drawChannelCurve(1, px1, CyberCyan, selectedChannel == 1)
                    drawChannelCurve(6, px6, CyberEmerald, selectedChannel == 6)
                    drawChannelCurve(11, px11, CyberPurple, selectedChannel == 11)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Channel Selector chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(1, 6, 11).forEach { chNum ->
                    FilterChip(
                        selected = selectedChannel == chNum,
                        onClick = { selectedChannel = chNum },
                        label = { Text("Channel $chNum") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (chNum) {
                                1 -> CyberCyan.copy(alpha = 0.2f)
                                6 -> CyberEmerald.copy(alpha = 0.2f)
                                else -> CyberPurple.copy(alpha = 0.2f)
                            },
                            selectedLabelColor = when (chNum) {
                                1 -> CyberCyan
                                6 -> CyberEmerald
                                else -> CyberPurple
                            }
                        ),
                        modifier = Modifier.testTag("spectrum_chip_$chNum")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Informative analysis of selected channel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SlateSurfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "Spectrum Evaluation:",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (selectedChannel) {
                        1 -> "Channel 1 centers around 2.412 GHz. To avoid overlapping interference, do not use Channels 2, 3, 4, or 5 in the same physical space."
                        6 -> "Channel 6 centers around 2.437 GHz. This is the absolute middle of the 2.4 GHz spectrum band. Avoid using channels 2-10 nearby."
                        else -> "Channel 11 centers around 2.462 GHz. This is the top non-overlapping channel in the US. Do not run Channels 7-10 nearby."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }
    }
}
