package com.borayildirim.beautydate.screens.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.borayildirim.beautydate.data.cityDistrictMap
import com.borayildirim.beautydate.viewmodels.state.AuthUiState
import com.borayildirim.beautydate.viewmodels.actions.AuthActions
import java.text.Collator
import java.util.Locale

/**
 * Contact form section for registration
 * Handles business name, city, district, address selection with simple dropdowns
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactFormSection(
    uiState: AuthUiState,
    authActions: AuthActions,
    selectedCity: String,
    onCitySelected: (String) -> Unit,
    selectedDistrict: String,
    onDistrictSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Turkish collator for proper alphabetical sorting with Turkish characters
    val turkishCollator = Collator.getInstance(Locale.forLanguageTag("tr-TR"))
    
    val cityList = cityDistrictMap.keys.toList().sortedWith(turkishCollator)
    val districtList = cityDistrictMap[selectedCity]?.sortedWith(turkishCollator) ?: emptyList()
    
    // State for dropdown expansion
    var cityDropdownExpanded by remember { mutableStateOf(false) }
    var districtDropdownExpanded by remember { mutableStateOf(false) }
    
    // Clear district when city changes
    LaunchedEffect(selectedCity) {
        if (selectedDistrict.isNotEmpty() && !districtList.contains(selectedDistrict)) {
            onDistrictSelected("")
        }
    }
    
    Column(modifier = modifier) {
        // Business name field (moved from RegisterFormSection)
        OutlinedTextField(
            value = uiState.businessName,
            onValueChange = { authActions.updateBusinessName(it) },
            label = { Text("İşletme Adı *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // City dropdown
        ExposedDropdownMenuBox(
            expanded = cityDropdownExpanded,
            onExpandedChange = { cityDropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedCity,
                onValueChange = { },
                readOnly = true,
                label = { Text("İl *") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityDropdownExpanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )
            
            ExposedDropdownMenu(
                expanded = cityDropdownExpanded,
                onDismissRequest = { cityDropdownExpanded = false }
            ) {
                cityList.forEach { city ->
                    DropdownMenuItem(
                        text = { Text(city) },
                        onClick = {
                            onCitySelected(city)
                            onDistrictSelected("") // Clear district when city changes
                            cityDropdownExpanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // District dropdown
        ExposedDropdownMenuBox(
            expanded = districtDropdownExpanded,
            onExpandedChange = { districtDropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedDistrict,
                onValueChange = { },
                readOnly = true,
                label = { Text("İlçe *") },
                placeholder = { Text("Önce il seçiniz") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = districtDropdownExpanded)
                },
                enabled = selectedCity.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
            )

            ExposedDropdownMenu(
                expanded = districtDropdownExpanded,
                onDismissRequest = { districtDropdownExpanded = false }
            ) {
                districtList.forEach { district ->
                    DropdownMenuItem(
                        text = { Text(district) },
                        onClick = {
                            onDistrictSelected(district)
                            districtDropdownExpanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Address field
        OutlinedTextField(
            value = uiState.address,
            onValueChange = { authActions.updateAddress(it) },
            label = { Text("Adres") },
            placeholder = { Text("Mahalle, sokak, bina no, iç kapı no") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 3
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Tax number field
        OutlinedTextField(
            value = uiState.taxNumber,
            onValueChange = { authActions.updateTaxNumber(it) },
            label = { Text("Vergi Numarası (Opsiyonel)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}