package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.GeminiService
import com.example.ui.theme.*
import com.example.util.NetworkUtils
import kotlinx.coroutines.launch

@Composable
fun FieldRefScreen() {
    var activeSection by remember { mutableIntStateOf(0) }
    val sections = listOf(
        "Interactive Decoder",
        "Cable References",
        "Electrical Reference",
        "PoE Jobsite Guide"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateDarkBg)
    ) {
        // Tab-like pill buttons
        ScrollableTabRow(
            selectedTabIndex = activeSection,
            containerColor = SlateSurface,
            contentColor = CyberCyan,
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            sections.forEachIndexed { index, title ->
                Tab(
                    selected = activeSection == index,
                    onClick = { activeSection = index },
                    text = { Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                    selectedContentColor = CyberCyan,
                    unselectedContentColor = TextMuted,
                    modifier = Modifier.testTag("ref_tab_$index")
                )
            }
        }

        // Selected reference panel
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (activeSection) {
                0 -> InteractiveDecoderSection()
                1 -> CableReferenceSection()
                2 -> ElectricalReferenceSection()
                3 -> PoeReferenceSection()
            }
        }
    }
}

// --- 1. Interactive NEMA & Model Decoder with Gemini fallbacks ---
@Composable
fun InteractiveDecoderSection() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var plugCode by remember { mutableStateOf("L5-30P") }
    var decodedInfo by remember { mutableStateOf<NetworkUtils.NemaPlugInfo?>(null) }
    
    // AI Vendor Model Decoder States
    var vendorModelCode by remember { mutableStateOf("Cisco Catalyst 9120AXI") }
    var aiDecodeOutput by remember { mutableStateOf<String?>(null) }
    var isDecodingAI by remember { mutableStateOf(false) }

    // Run local decoder initially
    LaunchedEffect(plugCode) {
        decodedInfo = NetworkUtils.decodeNemaPlug(plugCode)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Power, contentDescription = "Nema", tint = CyberCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Interactive NEMA Plug Decoder",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }
            Text(
                text = "Type any NEMA standard plug code to break down its physical wiring layout, nominal voltage, load capacity, and locks.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            OutlinedTextField(
                value = plugCode,
                onValueChange = { plugCode = it },
                label = { Text("Enter NEMA Code (e.g. 5-15P, L6-30R, 14-50P)") },
                modifier = Modifier.fillMaxWidth().testTag("nema_code_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (decodedInfo != null) {
                val info = decodedInfo!!
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SlateSurfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .border(1.dp, SlateSurfaceVariant, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = info.designation,
                        style = MaterialTheme.typography.titleSmall,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Divider(color = SlateSurfaceVariant, thickness = 1.dp)

                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Nominal Voltage:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Text(info.voltage, style = MaterialTheme.typography.bodySmall, color = TextWhite, fontWeight = FontWeight.Bold)
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Current Rating (Amps):", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Text(info.amperage, style = MaterialTheme.typography.bodySmall, color = TextWhite, fontWeight = FontWeight.Bold)
                    }
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Wiring Connection:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Text(info.characteristics, style = MaterialTheme.typography.bodySmall, color = CyberEmerald, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = info.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            } else {
                Text(
                    text = "Unknown or incomplete NEMA designation code. Try: 5-15P, L5-30R, 14-50P.",
                    color = CyberAmber,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // AI Hardware / Vendor Model Decoder Card
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "AI Decoder", tint = CyberPurple)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI Hardware Model Decoder",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }
            Text(
                text = "Enter any field model number or serial (e.g., APC PDU, Cisco Access Points, CommScope cables) and let Gemini translate its specifications.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            OutlinedTextField(
                value = vendorModelCode,
                onValueChange = { vendorModelCode = it },
                label = { Text("Model / Vendor Code") },
                modifier = Modifier.fillMaxWidth().testTag("ai_vendor_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    scope.launch {
                        isDecodingAI = true
                        aiDecodeOutput = null
                        val prompt = """
                            Decode this network/electrical vendor model number: '$vendorModelCode'.
                            Please provide:
                            1. **Manufacturer & Category**: Who makes it and what is it?
                            2. **Key Physical Specifications**: Form factor, size, ports, power specs.
                            3. **Wireless/Wired Capabilities**: (If AP/Switch) Channel capabilities, Wi-Fi standard (e.g. Wi-Fi 6), throughput speeds, or physical media attenuation specs.
                            4. **Wiring pinout or installation notes** where relevant.
                        """.trimIndent()
                        
                        aiDecodeOutput = GeminiService.generateContent(
                            prompt = prompt,
                            systemInstruction = "You are a professional industrial site reference database. Translate model numbers to clear structured specs concisely."
                        )
                        isDecodingAI = false
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberPurple,
                    contentColor = CyberEmerald
                ),
                modifier = Modifier.fillMaxWidth().testTag("ai_decode_btn"),
                enabled = !isDecodingAI
            ) {
                if (isDecodingAI) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = TextWhite)
                } else {
                    Text("Decode with AI")
                }
            }

            AnimatedVisibility(visible = isDecodingAI || aiDecodeOutput != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .background(SlateDarkBg, RoundedCornerShape(8.dp))
                        .border(1.dp, CyberPurple.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    if (isDecodingAI) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            CircularProgressIndicator(color = CyberPurple, modifier = Modifier.size(24.dp))
                        }
                    } else {
                        Text(
                            text = "AI Translation Specs:",
                            style = MaterialTheme.typography.titleSmall,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Text(
                            text = aiDecodeOutput ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextWhite
                        )
                    }
                }
            }
        }
    }
}

// --- 2. Cable Reference Specifications Section ---
@Composable
fun CableReferenceSection() {
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ElectricalServices, contentDescription = "Cables", tint = CyberCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ethernet Category Standards",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }
            Text(
                text = "Maximum transmission speeds, typical frequencies, and maximum physical link distances.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CableSpecRow("Cat 5e", "1 Gbps", "100 MHz", "100 meters (Standard)")
                CableSpecRow("Cat 6", "10 Gbps", "250 MHz", "55 meters (10G) / 100m (1G)")
                CableSpecRow("Cat 6a", "10 Gbps", "500 MHz", "100 meters (Fully shielded)")
                CableSpecRow("Cat 7", "10 Gbps", "600 MHz", "100 meters (S/FTP shielding)")
                CableSpecRow("Cat 8", "40 Gbps", "2000 MHz", "30 meters (Data Centers)")
            }
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Coaxial Field Cable Reference",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CableSpecRow("RG-6", "75 Ω", "Broadband Cable TV / Satellite receivers", "High shielding, low loss.")
                CableSpecRow("RG-58", "50 Ω", "Legacy Ethernet / RF Antenna links", "Flexible, moderate loss.")
                CableSpecRow("RG-59", "75 Ω", "Analog CCTV Video distribution", "Lower bandwidth than RG-6.")
            }
        }
    }
}

@Composable
fun CableSpecRow(category: String, speed: String, frequency: String, limit: String) {
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
            Text(category, fontWeight = FontWeight.Bold, color = CyberCyan, style = MaterialTheme.typography.bodyMedium)
            Text(frequency, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(speed, fontWeight = FontWeight.Bold, color = TextWhite, style = MaterialTheme.typography.bodyMedium)
            Text(limit, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        }
    }
}

// --- 3. Electrical References Section ---
@Composable
fun ElectricalReferenceSection() {
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.TableChart, contentDescription = "Electrical", tint = CyberCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AWG Wire Size vs Max Ampacity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }
            Text(
                text = "National Electrical Code (NEC) typical current capacities for copper conductors.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ElectricalSpecRow("14 AWG", "15 Amps", "Standard residential lighting circuits")
                ElectricalSpecRow("12 AWG", "20 Amps", "Kitchen & general receptacle outlets")
                ElectricalSpecRow("10 AWG", "30 Amps", "Dryers, water heaters, small AC units")
                ElectricalSpecRow("8 AWG", "40-50 Amps", "Heavy-duty electric ranges, subpanels")
                ElectricalSpecRow("6 AWG", "55-65 Amps", "Large whole-house subpanels, large appliances")
            }
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "AC Phase Color Standard Mappings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Wire Type", fontWeight = FontWeight.Bold, color = TextWhite, style = MaterialTheme.typography.bodySmall)
                    Text("US Standard (120V/240V)", fontWeight = FontWeight.Bold, color = TextWhite, style = MaterialTheme.typography.bodySmall)
                    Text("EU/TH standard", fontWeight = FontWeight.Bold, color = TextWhite, style = MaterialTheme.typography.bodySmall)
                }
                Divider(color = SlateSurfaceVariant)
                PhaseColorRow("Phase (L1)", "Black", "Brown")
                PhaseColorRow("Phase (L2)", "Red", "Black")
                PhaseColorRow("Neutral (N)", "White", "Blue")
                PhaseColorRow("Ground (PE)", "Green / Bare", "Green with Yellow stripe")
            }
        }
    }
}

@Composable
fun ElectricalSpecRow(wire: String, current: String, usage: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SlateSurfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .border(1.dp, SlateSurfaceVariant, RoundedCornerShape(8.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(wire, fontWeight = FontWeight.Bold, color = CyberCyan, style = MaterialTheme.typography.bodyMedium)
            Text(usage, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        }
        Text(current, fontWeight = FontWeight.Bold, color = CyberEmerald, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun PhaseColorRow(type: String, us: String, eu: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(type, fontWeight = FontWeight.Bold, color = TextWhite, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(us, color = TextMuted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.2f))
        Text(eu, color = TextMuted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
    }
}

// --- 4. PoE References Section ---
@Composable
fun PoeReferenceSection() {
    Card(
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PowerInput, contentDescription = "PoE", tint = CyberCyan)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Power over Ethernet (PoE) Standards",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }
            Text(
                text = "IEEE standards, output power levels, and typical target hardware.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PoeSpecRow("PoE (802.3af)", "15.4 W", "Up to 350mA", "IP Cameras, basic VoIP phones")
                PoeSpecRow("PoE+ (802.3at)", "30.0 W", "Up to 600mA", "WLAN Access Points, PTZ Cameras")
                PoeSpecRow("PoE++ (802.3bt Type 3)", "60.0 W", "Up to 600mA per pair", "High-performance multi-band APs, video conference stations")
                PoeSpecRow("PoE++ (802.3bt Type 4)", "90.0 W", "Up to 960mA per pair", "Digital building lights, smart monitors, thin client terminals")
            }
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "RJ45 Ethernet Pinout Standards",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("T568A Standard", fontWeight = FontWeight.Bold, color = CyberCyan, style = MaterialTheme.typography.bodySmall)
                    Divider(color = CyberCyan, modifier = Modifier.padding(vertical = 4.dp))
                    PinoutList(listOf(
                        "1. White/Green", "2. Green", "3. White/Orange", "4. Blue",
                        "5. White/Blue", "6. Orange", "7. White/Brown", "8. Brown"
                    ))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("T568B Standard", fontWeight = FontWeight.Bold, color = CyberEmerald, style = MaterialTheme.typography.bodySmall)
                    Divider(color = CyberEmerald, modifier = Modifier.padding(vertical = 4.dp))
                    PinoutList(listOf(
                        "1. White/Orange", "2. Orange", "3. White/Green", "4. Blue",
                        "5. White/Blue", "6. Green", "7. White/Brown", "8. Brown"
                    ))
                }
            }
        }
    }
}

@Composable
fun PoeSpecRow(standard: String, power: String, current: String, usage: String) {
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
            Text(standard, fontWeight = FontWeight.Bold, color = CyberCyan, style = MaterialTheme.typography.bodyMedium)
            Text(usage, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(power, fontWeight = FontWeight.Bold, color = CyberEmerald, style = MaterialTheme.typography.bodyMedium)
            Text(current, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        }
    }
}

@Composable
fun PinoutList(pins: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        pins.forEach { pin ->
            Text(pin, style = MaterialTheme.typography.bodySmall, color = TextWhite)
        }
    }
}
