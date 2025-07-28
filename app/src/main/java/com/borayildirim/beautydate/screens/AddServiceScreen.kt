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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalContext
import com.borayildirim.beautydate.utils.ToastUtils

/**
 * Add service screen with form inputs
 * Memory efficient: form state management with minimal object creation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServiceScreen(
    onNavigateBack: () -> Unit,
    onNavigateToServiceList: () -> Unit = {},
    serviceViewModel: ServiceViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var selectedCategory by remember { mutableStateOf<ServiceCategory?>(null) }
    var selectedSubcategory by remember { mutableStateOf<ServiceSubcategory?>(null) }
    var serviceName by remember { mutableStateOf("") }
    var servicePrice by remember { mutableStateOf("") }
    // serviceDuration kaldırıldı
    var serviceDescription by remember { mutableStateOf("") }
    var showCategorySelector by remember { mutableStateOf(false) }
    var showSubcategorySelector by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    val uiState by serviceViewModel.uiState.collectAsState()
    val businessName by authViewModel.businessName.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(selectedCategory) {
        // Reset subcategory when category changes
        selectedSubcategory = null
    }
    
    LaunchedEffect(selectedSubcategory) {
        // Auto-fill form when subcategory is selected
        selectedSubcategory?.let { subcategory ->
            if (serviceName.isEmpty()) {
                serviceName = subcategory.displayName
            }
            if (servicePrice.isEmpty()) {
                servicePrice = subcategory.defaultPrice.toInt().toString()
            }
            // serviceDuration = subcategory.defaultDuration.toString() // Removed
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Yeni Hizmet Ekle",
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
                // actions kaldırıldı
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
                        // Kaydet işlemi
                        val price = servicePrice.toDoubleOrNull() ?: 0.0
                        if (serviceName.isNotBlank() && price > 0) {
                            val service = Service(
                                name = serviceName.trim(),
                                price = price,
                                category = selectedCategory ?: ServiceCategory.NAIL,
                                subcategory = selectedSubcategory,
                                description = serviceDescription.trim(),
                                businessId = "" // ViewModel'den alınacak
                            )
                            serviceViewModel.addService(service)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)), // Yeşil
                    enabled = serviceName.isNotBlank() && servicePrice.isNotBlank()
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Kaydet")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
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
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hizmet Türü",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = { showCategorySelector = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedCategory != null) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ),
                        shape = RoundedCornerShape(8.dp)
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
                    
                    // Subcategory selection (if category is selected)
                    if (selectedCategory != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { showSubcategorySelector = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selectedSubcategory != null) {
                                    MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (selectedSubcategory != null) {
                                Text(
                                    text = selectedSubcategory!!.displayName,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Text("Hizmet Seçin (Opsiyonel)")
                            }
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
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hizmet Adı",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = serviceName,
                        onValueChange = { serviceName = it },
                        placeholder = { Text("Hizmet adını girin") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
            
            // Service price input
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Fiyat",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = servicePrice,
                        onValueChange = { servicePrice = it },
                        placeholder = { Text("Fiyat girin") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        suffix = { Text("₺", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
            
            // Service description input
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Açıklama",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = serviceDescription,
                        onValueChange = { serviceDescription = it },
                        placeholder = { Text("Hizmet açıklaması girin (opsiyonel)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(8.dp)
                    )
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
        
        // Success message handling with toast and navigation to service list
        LaunchedEffect(uiState.successMessage) {
            uiState.successMessage?.let { message ->
                // Show toast message
                ToastUtils.showSuccess(context, message)
                
                // Clear the success message
                serviceViewModel.clearSuccessMessage()
                
                // Navigate to Service List (Hizmetlerimiz)
                onNavigateToServiceList()
            }
        }
        
        // Error message handling
        LaunchedEffect(uiState.errorMessage) {
            uiState.errorMessage?.let { message ->
                ToastUtils.showError(context, message)
                serviceViewModel.clearError()
            }
        }
    }
} 