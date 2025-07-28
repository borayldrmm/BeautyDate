package com.borayildirim.beautydate.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.borayildirim.beautydate.data.models.Service
import com.borayildirim.beautydate.data.models.ServiceCategory
import com.borayildirim.beautydate.data.models.ServiceSubcategory
import com.borayildirim.beautydate.viewmodels.ServiceViewModel
import com.borayildirim.beautydate.screens.components.ServiceCategorySelector
import com.borayildirim.beautydate.screens.components.ServiceSubcategorySelector
import androidx.compose.ui.graphics.Color
import com.borayildirim.beautydate.viewmodels.AuthViewModel

/**
 * Edit service screen with form inputs pre-filled with existing service data
 * Memory efficient: form state management with minimal object creation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditServiceScreen(
    serviceId: String,
    onNavigateBack: () -> Unit,
    serviceViewModel: ServiceViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var selectedCategory by remember { mutableStateOf<ServiceCategory?>(null) }
    var selectedSubcategory by remember { mutableStateOf<ServiceSubcategory?>(null) }
    var serviceName by remember { mutableStateOf("") }
    var servicePrice by remember { mutableStateOf("") }
    var serviceDescription by remember { mutableStateOf("") }
    var showCategorySelector by remember { mutableStateOf(false) }
    var showSubcategorySelector by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var currentService by remember { mutableStateOf<Service?>(null) }
    
    val uiState by serviceViewModel.uiState.collectAsState()
    val businessName by authViewModel.businessName.collectAsState()
    
    // Load service data
    LaunchedEffect(serviceId) {
        if (serviceId.isNotEmpty()) {
            val service = serviceViewModel.getServiceById(serviceId)
            service?.let {
                currentService = it
                serviceName = it.name
                servicePrice = it.price.toInt().toString()
                serviceDescription = it.description
                selectedCategory = it.category
                selectedSubcategory = it.subcategory
            }
        }
    }
    
    LaunchedEffect(selectedCategory) {
        // Reset subcategory when category changes
        if (selectedCategory != currentService?.category) {
            selectedSubcategory = null
        }
    }
    
    // Handle success/error messages
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Hizmeti Düzenle",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        // Update service
                        val price = servicePrice.toDoubleOrNull() ?: 0.0
                        if (serviceName.isNotBlank() && price > 0 && currentService != null) {
                            val updatedService = currentService!!.copy(
                                name = serviceName.trim(),
                                price = price,
                                category = selectedCategory ?: ServiceCategory.NAIL,
                                subcategory = selectedSubcategory,
                                description = serviceDescription.trim()
                            )
                            serviceViewModel.updateService(updatedService)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)), // Yeşil
                    enabled = serviceName.isNotBlank() && servicePrice.isNotBlank() && !isLoading
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Güncelle")
                }
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)) // Kırmızı
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("İptal Et")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (currentService == null) {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Business name header
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "İşletme Adı",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = businessName,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    // Category selection
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Hizmet Türü",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { showCategorySelector = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (selectedCategory != null) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                )
                            ) {
                                if (selectedCategory != null) {
                                    Text(
                                        text = "${selectedCategory!!.getEmoji()} ${selectedCategory!!.getDisplayName()}",
                                        fontWeight = FontWeight.Medium
                                    )
                                } else {
                                    Text("Kategori Seçin")
                                }
                            }
                        }
                    }
                    
                    // Service name input
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Hizmet Adı",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = serviceName,
                                onValueChange = { serviceName = it },
                                placeholder = { Text("Hizmet adını girin") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }
                    
                    // Service price input
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Fiyat (₺)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = servicePrice,
                                onValueChange = { servicePrice = it },
                                placeholder = { Text("Fiyat girin") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                suffix = { Text("₺") }
                            )
                        }
                    }
                    
                    // Service description input
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Açıklama (Opsiyonel)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = serviceDescription,
                                onValueChange = { serviceDescription = it },
                                placeholder = { Text("Hizmet açıklaması girin") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 5
                            )
                        }
                    }
                }
            }
        }
        
        // Category selector bottom sheet
        if (showCategorySelector) {
            ServiceCategorySelector(
                onCategorySelected = { category ->
                    selectedCategory = category
                    showCategorySelector = false
                },
                onDismiss = { showCategorySelector = false }
            )
        }
        
        // Subcategory selector bottom sheet
        if (showSubcategorySelector && selectedCategory != null) {
            ServiceSubcategorySelector(
                category = selectedCategory!!,
                onSubcategorySelected = { subcategory ->
                    selectedSubcategory = subcategory
                    showSubcategorySelector = false
                },
                onDismiss = { showSubcategorySelector = false }
            )
        }
        
        // Error message
        if (uiState.errorMessage != null) {
            LaunchedEffect(uiState.errorMessage) {
                // Show error message (could be a snackbar or toast)
            }
        }
    }
} 