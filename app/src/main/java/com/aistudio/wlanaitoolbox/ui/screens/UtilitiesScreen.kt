package com.aistudio.wlanaitoolbox.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.wlanaitoolbox.ui.theme.*
import com.aistudio.wlanaitoolbox.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.util.Locale

@Composable
fun UtilitiesScreen() {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val subTabs = listOf(
        "Ping Terminal",
        "DNS Lookup",
        "Subnet Calc",
        "HTTP Codes"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SlateDarkBg)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = SlateSurface,
            contentColor = CyberCyan,
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            subTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSubTab == index,
                    onClick = { selectedSubTab = index },
                    text = { Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp) },
                    selectedContentColor = CyberCyan,
                    unselectedContentColor = TextMuted,
                    modifier = Modifier.testTag("util_tab_$index")
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
            when (selectedSubTab) {
                0 -> PingTerminalView()
                1 -> DnsLookupView()
                2 -> SubnetCalculatorView()
                3 -> HttpCodesExplorerView()
            }
        }
    }
}

// --- 1. Real Ping Terminal with Process Executor ---
@Composable
fun PingTerminalView() {
    var hostInput by remember { mutableStateOf("1.1.1.1") }
    val terminalOutput = remember { mutableStateListOf<String>() }
    var isRunningPing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Card(
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "ICMP Ping Utility",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Text(
                text = "Runs native ICMP pings in a background terminal. Real-time RTT results.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = hostInput,
                    onValueChange = { hostInput = it },
                    label = { Text("Target Host / IP") },
                    modifier = Modifier.weight(1f).testTag("ping_host_input"),
                    singleLine = true
                )
                Button(
                    onClick = {
                        scope.launch {
                            isRunningPing = true
                            terminalOutput.clear()
                            terminalOutput.add("${'$'} ping -c 4 $hostInput")
                            
                            withContext(Dispatchers.IO) {
                                try {
                                    val process = Runtime.getRuntime().exec("ping -c 4 -W 2 $hostInput")
                                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                                    var line: String?
                                    while (reader.readLine().also { line = it } != null) {
                                        val outputLine = line ?: ""
                                        withContext(Dispatchers.Main) {
                                            terminalOutput.add(outputLine)
                                        }
                                    }
                                    val errorReader = BufferedReader(InputStreamReader(process.errorStream))
                                    while (errorReader.readLine().also { line = it } != null) {
                                        val errLine = line ?: ""
                                        withContext(Dispatchers.Main) {
                                            terminalOutput.add("[Error] $errLine")
                                        }
                                    }
                                    process.waitFor()
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        terminalOutput.add("Execution Error: ${e.localizedMessage}")
                                    }
                                }
                            }
                            isRunningPing = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = CyberPurple
                    ),
                    enabled = !isRunningPing && hostInput.isNotEmpty(),
                    modifier = Modifier.testTag("ping_start_btn")
                ) {
                    if (isRunningPing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = TextWhite)
                    } else {
                        Text("Ping")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Terminal Output", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            Spacer(modifier = Modifier.height(6.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(SlateDarkBg, RoundedCornerShape(8.dp))
                    .border(1.dp, SlateSurfaceVariant, RoundedCornerShape(8.dp))
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (terminalOutput.isEmpty()) {
                    Text(
                        text = "No diagnostic session active. Press 'Ping' to begin.",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted
                    )
                } else {
                    terminalOutput.forEach { line ->
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = if (line.startsWith("$")) CyberCyan else if (line.contains("error", ignoreCase = true) || line.startsWith("[Error]")) RedSignal else CyberEmerald
                        )
                    }
                }
            }
        }
    }
}

// --- 2. Live DNS Resolver ---
@Composable
fun DnsLookupView() {
    var dnsHost by remember { mutableStateOf("google.com") }
    var dnsOutput by remember { mutableStateOf<String?>(null) }
    var isResolvingDns by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Card(
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Live DNS Record Lookup",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Text(
                text = "Resolve domain names to active IPv4 and IPv6 addresses on-demand.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = dnsHost,
                    onValueChange = { dnsHost = it },
                    label = { Text("Domain Name") },
                    modifier = Modifier.weight(1f).testTag("dns_input"),
                    singleLine = true
                )
                Button(
                    onClick = {
                        scope.launch {
                            isResolvingDns = true
                            dnsOutput = null
                            delay(400)
                            
                            val resolved = withContext(Dispatchers.IO) {
                                try {
                                    val addresses = InetAddress.getAllByName(dnsHost)
                                    val sb = StringBuilder()
                                    sb.append("DNS Resolution for: $dnsHost\n")
                                    sb.append("Total Records Found: ${addresses.size}\n\n")
                                    addresses.forEachIndexed { idx, addr ->
                                        val type = if (addr.address.size == 4) "A (IPv4)" else "AAAA (IPv6)"
                                        sb.append("Record [${idx + 1}]: $type\n")
                                        sb.append("- Host Address: ${addr.hostAddress}\n")
                                        sb.append("- Fully Qualified: ${addr.canonicalHostName}\n\n")
                                    }
                                    sb.toString()
                                } catch (e: Exception) {
                                    "Error: Failed to resolve '$dnsHost'. Details: ${e.localizedMessage}"
                                }
                            }
                            dnsOutput = resolved
                            isResolvingDns = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = CyberPurple
                    ),
                    enabled = !isResolvingDns && dnsHost.isNotEmpty(),
                    modifier = Modifier.testTag("dns_start_btn")
                ) {
                    if (isResolvingDns) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = TextWhite)
                    } else {
                        Text("Lookup")
                    }
                }
            }

            AnimatedVisibility(visible = dnsOutput != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .background(SlateDarkBg, RoundedCornerShape(8.dp))
                        .border(1.dp, SlateSurfaceVariant, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = dnsOutput ?: "",
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberCyan
                    )
                }
            }
        }
    }
}

// --- 3. Subnet / CIDR Calculator View ---
@Composable
fun SubnetCalculatorView() {
    var ipInput by remember { mutableStateOf("192.168.1.100") }
    var cidrInput by remember { mutableStateOf("24") }
    var result by remember { mutableStateOf<NetworkUtils.SubnetResult?>(null) }

    // Re-calculate live on input change
    LaunchedEffect(ipInput, cidrInput) {
        val cidrInt = cidrInput.toIntOrNull() ?: 24
        result = NetworkUtils.calculateSubnet(ipInput, cidrInt)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "CIDR Subnet Calculator",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Text(
                text = "Calculate network address boundaries, usable host limits, masks, and binary representation.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = ipInput,
                    onValueChange = { ipInput = it },
                    label = { Text("IPv4 Address") },
                    modifier = Modifier.weight(1f).testTag("subnet_ip_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = cidrInput,
                    onValueChange = { cidrInput = it },
                    label = { Text("CIDR (0-32)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.5f).testTag("subnet_cidr_input"),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (result != null) {
                val res = result!!
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SubnetFieldRow("Subnet Mask", res.subnetMask)
                    SubnetFieldRow("Network IP Address", res.networkAddress, highlight = true)
                    SubnetFieldRow("First Usable Host IP", res.firstUsable)
                    SubnetFieldRow("Last Usable Host IP", res.lastUsable)
                    SubnetFieldRow("Broadcast IP Address", res.broadcastAddress)
                    SubnetFieldRow("Total Usable Hosts", String.format(Locale.US, "%,d", res.usableHosts), color = CyberEmerald)
                    
                    Divider(color = SlateSurfaceVariant, modifier = Modifier.padding(vertical = 4.dp))
                    
                    Text("Binary Bitmask Mappings", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SlateDarkBg, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text("IP:   ${res.binaryIp}", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall, color = TextWhite)
                        Text("Mask: ${res.binaryMask}", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall, color = CyberCyan)
                    }
                }
            } else {
                Text(
                    text = "Invalid IP address or CIDR mask range (0-32).",
                    color = RedSignal,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SubnetFieldRow(label: String, value: String, highlight: Boolean = false, color: androidx.compose.ui.graphics.Color = TextWhite) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (highlight) CyberCyan.copy(alpha = 0.08f) else androidx.compose.ui.graphics.Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = if (highlight) CyberCyan else color, fontWeight = FontWeight.Bold)
    }
}

// --- 4. HTTP Status Codes Explorer ---
@Composable
fun HttpCodesExplorerView() {
    var searchQuery by remember { mutableStateOf("") }
    val codes = remember {
        listOf(
            HttpCodeInfo("100", "Continue", "The server has received the request headers and the client should proceed."),
            HttpCodeInfo("101", "Switching Protocols", "The requester has asked the server to switch protocols."),
            HttpCodeInfo("200", "OK", "Standard response for successful HTTP requests."),
            HttpCodeInfo("201", "Created", "The request has been fulfilled, resulting in the creation of a new resource."),
            HttpCodeInfo("204", "No Content", "The server successfully processed the request, but is not returning any content."),
            HttpCodeInfo("301", "Moved Permanently", "This and all future requests should be directed to the given URI."),
            HttpCodeInfo("302", "Found", "Tells the client to look at another temporary URI."),
            HttpCodeInfo("304", "Not Modified", "Indicates that the resource has not been modified since the last request."),
            HttpCodeInfo("400", "Bad Request", "The server cannot or will not process the request due to an apparent client error."),
            HttpCodeInfo("401", "Unauthorized", "Similar to 403 Forbidden but specifically for use when authentication is required."),
            HttpCodeInfo("403", "Forbidden", "The request was valid, but the server is refusing action. The user might not have permissions."),
            HttpCodeInfo("404", "Not Found", "The requested resource could not be found but may be available in the future."),
            HttpCodeInfo("405", "Method Not Allowed", "A request method is not supported for the requested resource."),
            HttpCodeInfo("429", "Too Many Requests", "The user has sent too many requests in a given amount of time (rate limiting)."),
            HttpCodeInfo("500", "Internal Server Error", "A generic error message, given when an unexpected condition was encountered."),
            HttpCodeInfo("502", "Bad Gateway", "The server was acting as a gateway or proxy and received an invalid response from upstream."),
            HttpCodeInfo("503", "Service Unavailable", "The server cannot handle the request (normally temporary overloading or maintenance).")
        )
    }

    val filteredCodes = remember(searchQuery) {
        if (searchQuery.isEmpty()) {
            codes
        } else {
            codes.filter {
                it.code.contains(searchQuery) || it.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SlateSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "HTTP Response Status Codes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by code or status text (e.g. 404)") },
                modifier = Modifier.fillMaxWidth().testTag("http_search_input"),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredCodes.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SlateSurfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .border(1.dp, SlateSurfaceVariant, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(getHttpCodeBg(item.code))
                        ) {
                            Text(
                                item.code,
                                color = TextWhite,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(item.name, fontWeight = FontWeight.Bold, color = TextWhite, style = MaterialTheme.typography.bodyMedium)
                            Text(item.description, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                    }
                }
            }
        }
    }
}

data class HttpCodeInfo(val code: String, val name: String, val description: String)

fun getHttpCodeBg(code: String): androidx.compose.ui.graphics.Color {
    return when {
        code.startsWith("1") -> CyberPurple
        code.startsWith("2") -> CyberEmerald
        code.startsWith("3") -> CyberCyan
        code.startsWith("4") -> RedSignal
        else -> CyberAmber
    }
}
