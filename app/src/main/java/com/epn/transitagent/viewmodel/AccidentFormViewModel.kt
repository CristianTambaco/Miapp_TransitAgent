package com.epn.transitagent.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.epn.transitagent.data.model.AccidentReport
import com.epn.transitagent.data.model.AccidentType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel para manejar el estado del formulario de registro de accidentes
 */
class AccidentFormViewModel : ViewModel() {
    
    private val _formState = MutableStateFlow(AccidentReport())
    val formState: StateFlow<AccidentReport> = _formState.asStateFlow()
    
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()
    
    fun updateAccidentType(type: AccidentType) {
        _formState.update { it.copy(accidentType = type) }
    }
    
    fun updateAccidentDate(date: Long) {
        _formState.update { it.copy(accidentDate = date) }
    }
    
    fun updateLicensePlate(plate: String) {
        _formState.update { it.copy(licensePlate = plate) }
    }
    
    fun updateDriverName(name: String) {
        _formState.update { it.copy(driverName = name) }
    }
    
    fun updateDriverIdNumber(idNumber: String) {
        _formState.update { it.copy(driverIdNumber = idNumber) }
    }
    
    fun updateObservations(observations: String) {
        _formState.update { it.copy(observations = observations) }
    }
    
    fun addPhoto(uri: Uri) {
        _formState.update { it.copy(photos = it.photos + uri) }
    }
    
    fun removePhoto(uri: Uri) {
        _formState.update { it.copy(photos = it.photos - uri) }
    }
    
    fun updateLocation(latitude: Double, longitude: Double) {
        _formState.update { it.copy(latitude = latitude, longitude = longitude) }
    }
    
    /**
     * Guarda el reporte de accidente
     * @param onSaveComplete Callback que se ejecuta al completar el guardado
     */
    fun saveReport(onSaveComplete: () -> Unit) {
        _isSaving.value = true
        // Aquí se implementaría la lógica de guardado (Room, Firebase, etc.)
        // Por ahora solo simulamos el guardado
        onSaveComplete()
        _isSaving.value = false
    }
    
    /**
     * Valida que el formulario tenga los campos requeridos
     */
    fun isFormValid(): Boolean {
        val state = _formState.value
        return state.accidentType != null &&
               state.accidentDate != null &&
               state.licensePlate.isNotBlank() &&
               state.driverName.isNotBlank() &&
               state.driverIdNumber.isNotBlank()
    }
    
    /**
     * Limpia el formulario para un nuevo registro
     */
    fun clearForm() {
        _formState.value = AccidentReport()
    }
}
