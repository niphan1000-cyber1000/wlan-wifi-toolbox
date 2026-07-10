package com.aistudio.wlanaitoolbox.util

import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt

object NetworkUtils {

    // --- 1. dBm and Watts converter ---
    fun dbmToWatts(dbm: Double): Double {
        return 10.0.pow((dbm - 30.0) / 10.0)
    }

    fun wattsToDbm(watts: Double): Double {
        if (watts <= 0.0) return -100.0 // prevent -infinity log
        return 10.0 * log10(watts) + 30.0
    }

    // --- 2. RF & WiFi Calculators ---
    
    // Antenna Length (wavelength)
    // Speed of light in vacuum = 299,792,458 m/s ≈ 300,000 km/s
    // Wavelength λ = c / f
    // Output is Wavelength (Full wave), 1/2 wave, 1/4 wave in cm and inches
    data class AntennaLengthResult(
        val fullWaveCm: Double,
        val halfWaveCm: Double,
        val quarterWaveCm: Double,
        val fullWaveInches: Double,
        val halfWaveInches: Double,
        val quarterWaveInches: Double
    )

    fun calculateAntennaLength(frequencyMhz: Double): AntennaLengthResult {
        if (frequencyMhz <= 0.0) return AntennaLengthResult(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        // Wavelength in cm = 30000 / frequency in MHz
        val fullCm = 29979.2458 / frequencyMhz
        val halfCm = fullCm / 2.0
        val quarterCm = fullCm / 4.0

        val cmToInches = 0.393701
        return AntennaLengthResult(
            fullWaveCm = fullCm,
            halfWaveCm = halfCm,
            quarterWaveCm = quarterCm,
            fullWaveInches = fullCm * cmToInches,
            halfWaveInches = halfCm * cmToInches,
            quarterWaveInches = quarterCm * cmToInches
        )
    }

    // EIRP calculation
    // EIRP (dBm) = Tx Power (dBm) - Cable Loss (dB) + Antenna Gain (dBi)
    fun calculateEirp(txPowerDbm: Double, cableLossDb: Double, antennaGainDbi: Double): Double {
        return txPowerDbm - cableLossDb + antennaGainDbi
    }

    // Free Space Path Loss (FSPL)
    // FSPL (dB) = 20 log10(d) + 20 log10(f) + 32.44 (for d in km, f in MHz)
    // Or: 20 log10(d) + 20 log10(f) - 27.55 (for d in meters, f in MHz)
    fun calculateFspl(distanceKm: Double, frequencyMhz: Double): Double {
        if (distanceKm <= 0.0 || frequencyMhz <= 0.0) return 0.0
        return 20.0 * log10(distanceKm) + 20.0 * log10(frequencyMhz) + 32.44
    }

    // Fresnel Zone maximum radius (at midpoint)
    // r = 8.65 * sqrt( d / f ) (where d is in km, f in GHz, output radius r in meters)
    fun calculateFresnelZoneMaxRadius(distanceKm: Double, frequencyGhz: Double): Double {
        if (distanceKm <= 0.0 || frequencyGhz <= 0.0) return 0.0
        return 8.65 * sqrt(distanceKm / frequencyGhz)
    }

    // Link Budget receiver signal level and margin
    // Rx Signal = Tx Power - Tx Cable Loss + Tx Ant Gain - Path Loss - Rx Cable Loss + Rx Ant Gain
    fun calculateRxSignal(
        txPowerDbm: Double,
        txCableLossDb: Double,
        txAntennaGainDbi: Double,
        pathLossDb: Double,
        rxCableLossDb: Double,
        rxAntennaGainDbi: Double
    ): Double {
        return txPowerDbm - txCableLossDb + txAntennaGainDbi - pathLossDb - rxCableLossDb + rxAntennaGainDbi
    }

    // --- 3. CIDR Subnet Calculator ---
    data class SubnetResult(
        val subnetMask: String,
        val networkAddress: String,
        val broadcastAddress: String,
        val firstUsable: String,
        val lastUsable: String,
        val usableHosts: Long,
        val binaryMask: String,
        val binaryIp: String
    )

    fun calculateSubnet(ipString: String, cidr: Int): SubnetResult? {
        try {
            val ipParts = ipString.split(".")
            if (ipParts.size != 4 || cidr !in 0..32) return null
            
            var ipInt = 0L
            for (i in 0..3) {
                val part = ipParts[i].toIntOrNull() ?: return null
                if (part !in 0..255) return null
                ipInt = (ipInt shl 8) or part.toLong()
            }

            val maskInt = if (cidr == 0) 0L else (-1L shl (32 - cidr)) and 0xFFFFFFFFL
            val networkInt = ipInt and maskInt
            val broadcastInt = networkInt or (maskInt.inv() and 0xFFFFFFFFL)

            val usableHosts = when (cidr) {
                32 -> 1L
                31 -> 2L
                else -> (2.0.pow(32 - cidr).toLong()) - 2
            }

            val firstUsableInt = when (cidr) {
                32 -> networkInt
                31 -> networkInt
                else -> networkInt + 1
            }

            val lastUsableInt = when (cidr) {
                32 -> networkInt
                31 -> broadcastInt
                else -> broadcastInt - 1
            }

            fun intToIp(ip: Long): String {
                return "${(ip shr 24) and 0xFF}.${(ip shr 16) and 0xFF}.${(ip shr 8) and 0xFF}.${ip and 0xFF}"
            }

            fun intToBinaryString(value: Long): String {
                val raw = LongArray(4)
                raw[0] = (value shr 24) and 0xFF
                raw[1] = (value shr 16) and 0xFF
                raw[2] = (value shr 8) and 0xFF
                raw[3] = value and 0xFF
                return raw.joinToString(".") { 
                    it.toString(2).padStart(8, '0') 
                }
            }

            return SubnetResult(
                subnetMask = intToIp(maskInt),
                networkAddress = intToIp(networkInt),
                broadcastAddress = intToIp(broadcastInt),
                firstUsable = if (usableHosts > 0) intToIp(firstUsableInt) else "N/A",
                lastUsable = if (usableHosts > 0) intToIp(lastUsableInt) else "N/A",
                usableHosts = usableHosts,
                binaryMask = intToBinaryString(maskInt),
                binaryIp = intToBinaryString(ipInt)
            )
        } catch (e: Exception) {
            return null
        }
    }

    // --- 4. Interactive NEMA Plug Local Decoder ---
    data class NemaPlugInfo(
        val designation: String,
        val voltage: String,
        val amperage: String,
        val characteristics: String,
        val isLocking: Boolean,
        val description: String
    )

    fun decodeNemaPlug(code: String): NemaPlugInfo? {
        val sanitized = code.uppercase().trim().replace(" ", "")
        // Examples: 5-15P, L5-30R, 14-50R, L6-20P, 1-15P
        val isLocking = sanitized.startsWith("L")
        val numberPart = if (isLocking) sanitized.substring(1) else sanitized
        
        // Find configuration parts: e.g. "5-15P" -> "5", "15", "P"
        val regex = """(\d+)-(\d+)([PR])""".toRegex()
        val matchResult = regex.find(numberPart) ?: return null
        
        val series = matchResult.groupValues[1].toIntOrNull() ?: return null
        val amps = matchResult.groupValues[2].toIntOrNull() ?: return null
        val typeChar = matchResult.groupValues[3] // P = Plug, R = Receptacle
        
        val typeWord = if (typeChar == "P") "Plug" else "Receptacle"
        val lockText = if (isLocking) "Twist-Locking " else "Non-Locking "

        val (volts, configDesc) = when (series) {
            1 -> "125V (Ungrounded)" to "2-pole, 2-wire ungrounded configuration (legacy standard)."
            2 -> "250V (Ungrounded)" to "2-pole, 2-wire ungrounded high voltage configuration (legacy)."
            5 -> "125V (Grounded)" to "2-pole, 3-wire grounded standard residential/commercial power."
            6 -> "250V (Grounded)" to "2-pole, 3-wire grounded utility/industrial high voltage."
            10 -> "125V/250V (Ungrounded)" to "3-pole, 3-wire dual-voltage ungrounded configuration (commonly dryer/stove before 1996)."
            14 -> "125V/250V (Grounded)" to "3-pole, 4-wire dual-voltage grounded configuration (modern heavy appliances)."
            15 -> "250V 3-Phase" to "3-pole, 4-wire grounded 3-phase power."
            18 -> "120V/208V 3-Phase Y" to "4-pole, 4-wire ungrounded 3-phase Y configuration."
            21 -> "120V/208V 3-Phase Y (Grounded)" to "4-pole, 5-wire grounded 3-phase Y configuration."
            else -> "Unknown Voltage" to "Standard NEMA design series $series."
        }

        return NemaPlugInfo(
            designation = "NEMA $sanitized",
            voltage = volts,
            amperage = "${amps}A",
            characteristics = "$lockText $typeWord",
            isLocking = isLocking,
            description = "$configDesc This standard defines a nominal ${amps}A capability operating at $volts."
        )
    }
}
