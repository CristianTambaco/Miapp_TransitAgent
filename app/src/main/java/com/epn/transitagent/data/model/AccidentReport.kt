package com.epn.transitagent.data.model

import android.net.Uri

/**
 * Enum que representa los tipos de accidentes de tránsito
 */
enum class AccidentType(val displayName: String) {
    CHOQUE("Choque"),
    COLISION("Colisión"),
    ATROPELLO("Atropello")
}

/**
 * Data class que representa un reporte de accidente de tránsito
 */
data class AccidentReport(
    val accidentType: AccidentType? = null,
    val accidentDate: Long? = null,           // Timestamp de la fecha del siniestro
    val licensePlate: String = "",             // Matrícula del auto involucrado
    val driverName: String = "",               // Nombre del conductor
    val driverIdNumber: String = "",           // Cédula del conductor
    val observations: String = "",             // Observaciones del accidente
    val photos: List<Uri> = emptyList(),       // URIs de las fotografías capturadas
    val latitude: Double? = null,              // Latitud GPS del lugar del siniestro
    val longitude: Double? = null              // Longitud GPS del lugar del siniestro
)
