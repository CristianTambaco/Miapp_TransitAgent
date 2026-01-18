package com.epn.transitagent.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.epn.transitagent.data.model.AccidentType
import com.epn.transitagent.utils.VibrationHelper
import com.epn.transitagent.viewmodel.AccidentFormViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AccidentFormScreen(
    viewModel: AccidentFormViewModel = viewModel()
) {
    val context = LocalContext.current
    val formState by viewModel.formState.collectAsState()
    val scrollState = rememberScrollState()
    
    // Permisos
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    
    // Foto URI temporal
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    
    // Launcher para tomar fotos
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            viewModel.addPhoto(photoUri!!)
        }
    }
    
    // Función para crear archivo temporal y tomar foto
    fun takePhoto() {
        val photoFile = File.createTempFile(
            "accident_photo_${System.currentTimeMillis()}",
            ".jpg",
            context.cacheDir
        )
        photoUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
        takePictureLauncher.launch(photoUri)
    }
    
    // Función para obtener ubicación
    fun getLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val cancellationTokenSource = CancellationTokenSource()
        
        try {
            // Primero intentar obtener la ubicación actual activamente
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.updateLocation(location.latitude, location.longitude)
                    Toast.makeText(context, "Ubicación obtenida correctamente", Toast.LENGTH_SHORT).show()
                } else {
                    // Si getCurrentLocation falla, intentar con lastLocation
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLocation ->
                        if (lastLocation != null) {
                            viewModel.updateLocation(lastLocation.latitude, lastLocation.longitude)
                            Toast.makeText(context, "Ubicación obtenida correctamente", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Active el GPS e intente nuevamente", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }.addOnFailureListener { e ->
                Toast.makeText(context, "Error al obtener ubicación: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } catch (e: SecurityException) {
            Toast.makeText(context, "Error de permisos de ubicación", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Date Picker
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    
    // Solicitar permisos al inicio
    LaunchedEffect(Unit) {
        permissionsState.launchMultiplePermissionRequest()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FORMULARIO REGISTRO DE ACCIDENTES DE TRÁNSITO") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. TIPO DE ACCIDENTE
            Text(
                text = "Seleccione el tipo de siniestro ocurrido:",
                style = MaterialTheme.typography.titleMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AccidentType.entries.forEach { type ->
                    FilterChip(
                        selected = formState.accidentType == type,
                        onClick = { viewModel.updateAccidentType(type) },
                        label = { Text(type.displayName) },
                        leadingIcon = if (formState.accidentType == type) {
                            { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                        } else null
                    )
                }
            }
            
            // 2. FECHA DEL SINIESTRO
            Text(
                text = "Complete los siguientes campos:",
                style = MaterialTheme.typography.titleMedium
            )
            OutlinedCard(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    //Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = formState.accidentDate?.let {
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
                        } ?: "Seleccione la fecha del siniestro"
                    )
                }
            }
            
            // 3. MATRÍCULA DEL AUTO
            OutlinedTextField(
                value = formState.licensePlate,
                onValueChange = { viewModel.updateLicensePlate(it.uppercase()) },
                label = { Text("Matrícula auto involucrado") },
                //leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // 4. NOMBRE DEL CONDUCTOR
            OutlinedTextField(
                value = formState.driverName,
                onValueChange = { viewModel.updateDriverName(it) },
                label = { Text("Nombre conductor") },
                //leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // 5. CÉDULA DEL CONDUCTOR
            OutlinedTextField(
                value = formState.driverIdNumber,
                onValueChange = { viewModel.updateDriverIdNumber(it) },
                label = { Text("Cédula conductor") },
                //leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // 6. OBSERVACIONES
            OutlinedTextField(
                value = formState.observations,
                onValueChange = { viewModel.updateObservations(it) },
                label = { Text("Observaciones") },
                //leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )
            
            // 7. FOTOGRAFÍAS
            Text(
                text = "Fotografías del siniestro",
                style = MaterialTheme.typography.titleMedium
            )

            Button(
                onClick = {
                    if (permissionsState.allPermissionsGranted) {
                        takePhoto()
                    } else {
                        permissionsState.launchMultiplePermissionRequest()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RectangleShape
            ) {
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tomar Foto")
            }


            if (formState.photos.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(formState.photos) { uri ->
                        Card(
                            modifier = Modifier.size(100.dp),
                            shape = RectangleShape
                        ) {
                            Box {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Fotografía",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { viewModel.removePhoto(uri) },
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Eliminar foto",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                    }
                }
                Text(
                    text = "${formState.photos.size} foto(s) capturada(s)",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            // 8. UBICACIÓN
            Text(
                text = "Ubicación del siniestro",
                style = MaterialTheme.typography.titleMedium
            )
            
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (formState.latitude != null && formState.longitude != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Latitud: ${String.format("%.6f", formState.latitude)}")
                                Text("Longitud: ${String.format("%.6f", formState.longitude)}")
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Spacer(modifier = Modifier.width(8.dp))
                            //Text("Ubicación no registrada")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (permissionsState.allPermissionsGranted) {
                                getLocation()
                            } else {
                                permissionsState.launchMultiplePermissionRequest()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RectangleShape
                    ) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Presione para obtener la ubicación actual")
                    }

                }
            }
            
            // Mensaje de campos requeridos
            //Text(
            //    text = "* Campos obligatorios",
            //    style = MaterialTheme.typography.bodySmall,
            //    color = MaterialTheme.colorScheme.outline
           //)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // BOTÓN GUARDAR
            Button(
                onClick = {
                    viewModel.saveReport {
                        // Vibrar por 5 segundos al guardar
                        VibrationHelper.vibrate(context, 5000L)
                        Toast.makeText(
                            context,
                            "Registro guardado exitosamente",
                            Toast.LENGTH_LONG
                        ).show()
                        viewModel.clearForm()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = viewModel.isFormValid(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                //Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("GUARDAR", style = MaterialTheme.typography.titleMedium)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            viewModel.updateAccidentDate(it)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
