package com.aistudio.wlanaitoolbox.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.aistudio.wlanaitoolbox.ui.theme.*
import com.aistudio.wlanaitoolbox.util.NetworkUtils
import java.util.Locale

@Composable
fun CalculatorsScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "Antenna Length",
        "EIRP",
        "Path Loss (FSPL)",
        "Link Budget",
        "Fresnel Zone",
        "dBm <-> Watts"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateDarkBg)
    ) {
        // Tab row header
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = SlateSurface,
            contentColor = CyberCyan,
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                    selectedContentColor = CyberCyan,
                    unselectedContentColor = TextMuted,
                    modifier = Modifier.testTag("calc_tab_$index")
                )
            }
        }

        // Active calculator container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedTab) {
                0 -> AntennaLengthCalculatorView()
                1 -> EirpCalculatorView()
                2 -> FsplCalculatorView()
                3 -> LinkBudgetCalculatorView()
                4 -> FresnelZoneCalculatorView()
                5 -> DbmWattsCalculatorView()
            }
        }
    }
}

// --- 1. Antenna Length Calculator ---
@Composable
fun AntennaLengthCalculatorView() {
    var freqText by remember { mutableStateOf("2442") } // Default center of 2.4GHz
    val freq = freqText.toDoubleOrNull() ?: 0.0
    val result = NetworkUtils.calculateAntennaLength(freq)

    CalculatorContainer(
        title = "Wavelength & Antenna Dimension Calculator",
        description = "Calculate optimal physical antenna elements based on radio frequency wavelength λ = c / f."
    ) {
        OutlinedTextField(
            value = freqText,
            onValueChange = { freqText = it },
            label = { Text("Frequency (MHz)") },
            placeholder = { Text("e.g. 2442 or 5500") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberCyan,
                unfocusedBorderColor = SlateSurfaceVariant,
                focusedLabelColor = CyberCyan
            ),
            modifier = Modifier.fillMaxWidth().testTag("antenna_freq_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Calculated Physical Elements",
            style = MaterialTheme.typography.titleSmall,
            color = CyberCyan,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        ResultRow(
            label = "Full Wavelength (1λ)",
            valueCm = String.format(Locale.US, "%.2f cm", result.fullWaveCm),
            valueInch = String.format(Locale.US, "%.2f in", result.fullWaveInches)
        )
        ResultRow(
            label = "Half-Wave Dipole (1/2λ)",
            valueCm = String.format(Locale.US, "%.2f cm", result.halfWaveCm),
            valueInch = String.format(Locale.US, "%.2f in", result.halfWaveInches),
            highlighted = true
        )
        ResultRow(
            label = "Quarter-Wave Monopole (1/4λ)",
            valueCm = String.format(Locale.US, "%.2f cm", result.quarterWaveCm),
            valueInch = String.format(Locale.US, "%.2f in", result.quarterWaveInches)
        )
    }
}

// --- 2. EIRP Calculator ---
@Composable
fun EirpCalculatorView() {
    var txPowerText by remember { mutableStateOf("20") } // dBm
    var cableLossText by remember { mutableStateOf("1.5") } // dB
    var antGainText by remember { mutableStateOf("5.0") } // dBi

    val tx = txPowerText.toDoubleOrNull() ?: 0.0
    val loss = cableLossText.toDoubleOrNull() ?: 0.0
    val gain = antGainText.toDoubleOrNull() ?: 0.0

    val eirpDbm = NetworkUtils.calculateEirp(tx, loss, gain)
    val eirpWatts = NetworkUtils.dbmToWatts(eirpDbm)

    CalculatorContainer(
        title = "EIRP Calculator",
        description = "Equivalent Isotropically Radiated Power (EIRP) calculates the actual output radiated power of an antenna system."
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = txPowerText,
                onValueChange = { txPowerText = it },
                label = { Text("Transmitter Power (dBm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("eirp_tx_input")
            )
            OutlinedTextField(
                value = cableLossText,
                onValueChange = { cableLossText = it },
                label = { Text("Coaxial Cable & Connector Loss (dB)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = antGainText,
                onValueChange = { antGainText = it },
                label = { Text("Antenna Gain (dBi)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberCyan.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Radiated Output (EIRP)",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = String.format(Locale.US, "%.2f dBm", eirpDbm),
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextWhite,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = String.format(Locale.US, "≈ %.4f Watts", eirpWatts),
                        style = MaterialTheme.typography.bodyMedium,
                        color = CyberEmerald,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// --- 3. Free Space Path Loss (FSPL) Calculator ---
@Composable
fun FsplCalculatorView() {
    var distanceText by remember { mutableStateOf("0.5") } // Default 500m / 0.5km
    var freqText by remember { mutableStateOf("2400") } // Default 2400MHz

    val distance = distanceText.toDoubleOrNull() ?: 0.0
    val freq = freqText.toDoubleOrNull() ?: 0.0
    val fspl = NetworkUtils.calculateFspl(distance, freq)

    CalculatorContainer(
        title = "Free-Space Path Loss (FSPL)",
        description = "Calculate the signal attenuation (loss) that occurs over a direct line-of-sight path through free space."
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = distanceText,
                onValueChange = { distanceText = it },
                label = { Text("Distance (km)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f).testTag("fspl_dist_input")
            )
            OutlinedTextField(
                value = freqText,
                onValueChange = { freqText = it },
                label = { Text("Frequency (MHz)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberCyan.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Total Path Loss (FSPL)",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = String.format(Locale.US, "%.2f dB", fspl),
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextWhite,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Signal strength decreases by this value over the specified distance without any obstacles.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

// --- 4. Link Budget Calculator ---
@Composable
fun LinkBudgetCalculatorView() {
    var txPower by remember { mutableStateOf("20.0") } // dBm
    var txAntGain by remember { mutableStateOf("6.0") } // dBi
    var txLoss by remember { mutableStateOf("1.5") } // dB
    var pathLoss by remember { mutableStateOf("80.0") } // dB (usually FSPL)
    var rxLoss by remember { mutableStateOf("1.0") } // dB
    var rxAntGain by remember { mutableStateOf("6.0") } // dBi
    var rxSens by remember { mutableStateOf("-75.0") } // dBm (receiver sensitivity)

    val txP = txPower.toDoubleOrNull() ?: 0.0
    val txG = txAntGain.toDoubleOrNull() ?: 0.0
    val txL = txLoss.toDoubleOrNull() ?: 0.0
    val pL = pathLoss.toDoubleOrNull() ?: 0.0
    val rxL = rxLoss.toDoubleOrNull() ?: 0.0
    val rxG = rxAntGain.toDoubleOrNull() ?: 0.0
    val sens = rxSens.toDoubleOrNull() ?: -80.0

    val rxSignal = NetworkUtils.calculateRxSignal(txP, txL, txG, pL, rxL, rxG)
    val margin = rxSignal - sens

    CalculatorContainer(
        title = "Link Budget & Fade Margin",
        description = "Calculate the total received power at the antenna and the Fade Margin above receiver noise threshold."
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = txPower, onValueChange = { txPower = it }, label = { Text("Tx Power (dBm)") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = txAntGain, onValueChange = { txAntGain = it }, label = { Text("Tx Ant Gain (dBi)") }, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = txLoss, onValueChange = { txLoss = it }, label = { Text("Tx Cab Loss (dB)") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = pathLoss, onValueChange = { pathLoss = it }, label = { Text("Path Loss (FSPL dB)") }, modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = rxLoss, onValueChange = { rxLoss = it }, label = { Text("Rx Cab Loss (dB)") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = rxAntGain, onValueChange = { rxAntGain = it }, label = { Text("Rx Ant Gain (dBi)") }, modifier = Modifier.weight(1f))
            }
            OutlinedTextField(value = rxSens, onValueChange = { rxSens = it }, label = { Text("Rx Sensitivity Threshold (dBm)") }, modifier = Modifier.fillMaxWidth())
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (margin >= 15.0) CyberEmerald.copy(alpha = 0.1f) else if (margin >= 0.0) CyberAmber.copy(alpha = 0.1f) else RedSignal.copy(alpha = 0.1f),
                    RoundedCornerShape(12.dp)
                )
                .border(
                    1.dp,
                    if (margin >= 15.0) CyberEmerald.copy(alpha = 0.4f) else if (margin >= 0.0) CyberAmber.copy(alpha = 0.4f) else RedSignal.copy(alpha = 0.4f),
                    RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Received Signal Level",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Fade Margin",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = String.format(Locale.US, "%.2f dBm", rxSignal),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextWhite,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = String.format(Locale.US, "%+.2f dB", margin),
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (margin >= 15.0) CyberEmerald else if (margin >= 0) CyberAmber else RedSignal,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = when {
                        margin >= 20.0 -> "Robust Link: Exceptional margin for rain fade, atmospheric and obstacle loss."
                        margin >= 10.0 -> "Healthy Link: Suitable for enterprise field networks."
                        margin >= 0.0 -> "Unstable Link: High vulnerability to interference and weather variations."
                        else -> "Link Failure: Received signal level is below receiver sensitivity."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextWhite.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// --- 5. Fresnel Zone Calculator ---
@Composable
fun FresnelZoneCalculatorView() {
    var totalDistanceText by remember { mutableStateOf("2.0") } // Total km
    var frequencyText by remember { mutableStateOf("5.8") } // GHz

    val dist = totalDistanceText.toDoubleOrNull() ?: 0.0
    val freq = frequencyText.toDoubleOrNull() ?: 0.0
    val maxRadius = NetworkUtils.calculateFresnelZoneMaxRadius(dist, freq)
    val clearance60 = maxRadius * 0.60

    CalculatorContainer(
        title = "First Fresnel Zone Radius",
        description = "Calculate the maximum radius of the first Fresnel zone at link midpoint to determine line-of-sight obstacle clearance requirements."
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = totalDistanceText,
                onValueChange = { totalDistanceText = it },
                label = { Text("Total Distance (km)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f).testTag("fresnel_dist_input")
            )
            OutlinedTextField(
                value = frequencyText,
                onValueChange = { frequencyText = it },
                label = { Text("Frequency (GHz)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = SlateSurfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Midpoint Fresnel Clearance Specs",
                    style = MaterialTheme.typography.titleSmall,
                    color = CyberCyan,
                    fontWeight = FontWeight.Bold
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Max Zone Radius (100%):", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                    Text(String.format(Locale.US, "%.2f meters", maxRadius), color = TextWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Min Clearance Req (60%):", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                    Text(String.format(Locale.US, "%.2f meters", clearance60), color = CyberAmber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
                
                Divider(color = SlateSurfaceVariant, thickness = 1.dp)

                Text(
                    text = "Pro Tip: For a robust RF link, the inner 60% of the first Fresnel zone (the core $clearance60 m area) must be entirely free of terrain, building, or vegetation obstruction.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }
    }
}

// --- 6. dBm to Watts Converter ---
@Composable
fun DbmWattsCalculatorView() {
    var dbmInput by remember { mutableStateOf("30") } // default 1W
    var wattsInput by remember { mutableStateOf("1.0") }

    CalculatorContainer(
        title = "dBm to Watts Conversions",
        description = "Instantly convert logarithmic dBm values to linear power in Watts and vice versa. Values update instantly as you type."
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // dBm to Watts card
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceVariant.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Logarithmic Power (dBm) ➔ Linear Power (Watts)",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = dbmInput,
                            onValueChange = {
                                dbmInput = it
                                val dVal = it.toDoubleOrNull()
                                if (dVal != null) {
                                    val wVal = NetworkUtils.dbmToWatts(dVal)
                                    wattsInput = String.format(Locale.US, "%.5f", wVal)
                                }
                            },
                            label = { Text("Power in dBm") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("dbm_val_input")
                        )
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "equals", tint = TextMuted)
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Output Watts", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(
                                text = "${wattsInput.trimEnd('0').trimEnd('.')} W",
                                style = MaterialTheme.typography.titleMedium,
                                color = CyberEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Watts to dBm card
            Card(
                colors = CardDefaults.cardColors(containerColor = SlateSurfaceVariant.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Linear Power (Watts) ➔ Logarithmic Power (dBm)",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberPurple,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = wattsInput,
                            onValueChange = {
                                wattsInput = it
                                val wVal = it.toDoubleOrNull()
                                if (wVal != null && wVal > 0) {
                                    val dVal = NetworkUtils.wattsToDbm(wVal)
                                    dbmInput = String.format(Locale.US, "%.2f", dVal)
                                }
                            },
                            label = { Text("Power in Watts") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "equals", tint = TextMuted)
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Output dBm", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(
                                text = "${dbmInput} dBm",
                                style = MaterialTheme.typography.titleMedium,
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Common UI Components ---
@Composable
fun CalculatorContainer(
    title: String,
    description: String,
    content: @Composable ColumnScope.() -> Unit
) {
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
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            content()
        }
    }
}

@Composable
fun ResultRow(
    label: String,
    valueCm: String,
    valueInch: String,
    highlighted: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (highlighted) CyberCyan.copy(alpha = 0.1f) else Color.Transparent
            )
            .border(
                1.dp,
                if (highlighted) CyberCyan.copy(alpha = 0.3f) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (highlighted) CyberCyan else TextWhite,
                fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Normal
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = valueCm,
                style = MaterialTheme.typography.bodyMedium,
                color = TextWhite,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = valueInch,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}
