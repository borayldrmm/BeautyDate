package com.borayildirim.beautydate.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.borayildirim.beautydate.data.models.TutorialData
import com.borayildirim.beautydate.data.models.TutorialCategory
import com.borayildirim.beautydate.data.models.TutorialDifficulty
import com.borayildirim.beautydate.data.models.TutorialStep
import com.borayildirim.beautydate.data.models.TutorialActionType
import com.borayildirim.beautydate.viewmodels.TutorialViewModel
import com.borayildirim.beautydate.viewmodels.TutorialFilterType
import com.borayildirim.beautydate.viewmodels.state.TutorialStatistics
import com.borayildirim.beautydate.utils.ToastUtils
import kotlinx.coroutines.delay

/**
 * Tutorial main screen with comprehensive learning system - FIXED
 * Features: Category filtering, search, progress tracking, guided tours
 * Memory efficient: LazyColumn with minimal recomposition
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialScreen(
    tutorialViewModel: TutorialViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by tutorialViewModel.uiState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    // Handle error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            ToastUtils.showError(context, error)
            tutorialViewModel.clearError()
        }
    }
    
    // Handle completion messages  
    LaunchedEffect(uiState.completionMessage) {
        uiState.completionMessage?.let { message ->
            ToastUtils.showSuccess(context, message)
            delay(3000) // Show for 3 seconds
            tutorialViewModel.clearCompletionMessage()
        }
    }
    
    // Load recommended tutorials on first load
    LaunchedEffect(Unit) {
        tutorialViewModel.loadRecommendedTutorials()
    }
    
    if (uiState.isInTutorialMode) {
        // Show tutorial overlay when in tutorial mode
        TutorialOverlay(
            tutorialViewModel = tutorialViewModel,
            onExit = { tutorialViewModel.exitTutorial() }
        )
    } else {
        // Show main tutorial screen
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            TopAppBar(
                title = {
                    Text(
                        text = "📚 Rehber",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
            
            if (uiState.isLoading) {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tutorial'lar yükleniyor...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Progress Overview
                    item {
                        TutorialProgressCard(
                            statistics = uiState.tutorialStatistics,
                            onResetProgress = { tutorialViewModel.resetAllTutorialProgress() }
                        )
                    }
                    
                    // Search Bar
                    item {
                        TutorialSearchBar(
                            searchQuery = uiState.searchQuery,
                            onSearchQueryChange = { tutorialViewModel.updateSearchQuery(it) },
                            searchSuggestions = uiState.searchSuggestions
                        )
                    }
                    
                    // Filter Chips
                    item {
                        TutorialFilterSection(
                            selectedCategory = uiState.selectedCategory,
                            selectedFilterType = uiState.filterType,
                            onCategorySelected = { tutorialViewModel.selectCategory(it) },
                            onFilterTypeSelected = { tutorialViewModel.selectFilterType(it) }
                        )
                    }
                    
                    // Recommended Tutorials
                    if (uiState.personalizedRecommendations.isNotEmpty() && !uiState.hasActiveFilters) {
                        item {
                            RecommendedTutorialsSection(
                                recommendations = uiState.personalizedRecommendations,
                                onTutorialStart = { tutorialViewModel.startTutorial(it.id) }
                            )
                        }
                    }
                    
                    // Tutorial Categories or Filtered Results
                    if (uiState.hasActiveFilters) {
                        // Show filtered results
                        items(uiState.filteredTutorials) { tutorial ->
                            TutorialCard(
                                tutorial = tutorial,
                                onStart = { tutorialViewModel.startTutorial(tutorial.id) },
                                onReset = { tutorialViewModel.resetTutorialProgress(tutorial.id) }
                            )
                        }
                    } else {
                        // Show tutorials grouped by category
                        uiState.tutorialsByCategory.forEach { (category, tutorials) ->
                            item {
                                TutorialCategorySection(
                                    category = category,
                                    tutorials = tutorials,
                                    completionRate = uiState.getCategoryCompletionRate(category),
                                    onTutorialStart = { tutorialViewModel.startTutorial(it.id) },
                                    onTutorialReset = { tutorialViewModel.resetTutorialProgress(it.id) }
                                )
                            }
                        }
                    }
                    
                    // Empty state
                    if (!uiState.hasData && !uiState.isLoading) {
                        item {
                            EmptyTutorialState()
                        }
                    }
                    
                    // No search results
                    if (uiState.hasActiveFilters && uiState.filteredTutorials.isEmpty()) {
                        item {
                            NoSearchResultsState(
                                searchQuery = uiState.searchQuery,
                                onClearFilters = {
                                    tutorialViewModel.updateSearchQuery("")
                                    tutorialViewModel.selectCategory(null)
                                    tutorialViewModel.selectFilterType(TutorialFilterType.ALL)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tutorial progress overview card
 */
@Composable
private fun TutorialProgressCard(
    statistics: TutorialStatistics,
    onResetProgress: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(statistics.progressColor).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Öğrenme İlerlemen",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = statistics.progressLevelText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(statistics.progressColor)
                    )
                }
                
                IconButton(onClick = onResetProgress) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "İlerlemeyi Sıfırla",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress Bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = statistics.completionText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = statistics.formattedCompletionPercentage,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(statistics.progressColor)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = { (statistics.completionPercentage / 100).toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(statistics.progressColor),
                    trackColor = Color(statistics.progressColor).copy(alpha = 0.2f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Statistics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticItem(
                    icon = Icons.Default.CheckCircle,
                    value = statistics.completedTutorials.toString(),
                    label = "Tamamlanan",
                    color = Color(0xFF4CAF50)
                )
                StatisticItem(
                    icon = Icons.Default.PlayArrow,
                    value = statistics.inProgressTutorials.toString(),
                    label = "Devam Eden",
                    color = Color(0xFFFF9800)
                )
                StatisticItem(
                    icon = Icons.Default.School,
                    value = statistics.availableTutorials.toString(),
                    label = "Mevcut",
                    color = Color(0xFF2196F3)
                )
            }
        }
    }
}

/**
 * Individual statistic item
 */
@Composable
private fun StatisticItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Tutorial search bar with suggestions
 */
@Composable
private fun TutorialSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchSuggestions: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text("Tutorial ara...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Ara")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Temizle")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Search suggestions
            if (searchSuggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchSuggestions) { suggestion ->
                        SuggestionChip(
                            onClick = { onSearchQueryChange(suggestion) },
                            label = { Text(suggestion) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tutorial filter section
 */
@Composable
private fun TutorialFilterSection(
    selectedCategory: TutorialCategory?,
    selectedFilterType: TutorialFilterType,
    onCategorySelected: (TutorialCategory?) -> Unit,
    onFilterTypeSelected: (TutorialFilterType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Filtrele",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Filter Types
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(TutorialFilterType.values()) { filterType ->
                    FilterChip(
                        onClick = { onFilterTypeSelected(filterType) },
                        label = { Text(filterType.displayName) },
                        selected = filterType == selectedFilterType
                    )
                }
            }
            
            // Category Filters
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        onClick = { onCategorySelected(null) },
                        label = { Text("Tümü") },
                        selected = selectedCategory == null
                    )
                }
                items(TutorialCategory.values()) { category ->
                    FilterChip(
                        onClick = { onCategorySelected(category) },
                        label = { Text(category.displayName) },
                        selected = category == selectedCategory
                    )
                }
            }
        }
    }
}

/**
 * Recommended tutorials section
 */
@Composable
private fun RecommendedTutorialsSection(
    recommendations: List<TutorialData>,
    onTutorialStart: (TutorialData) -> Unit
) {
    Column {
        Text(
            text = "Senin İçin Önerilen",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            recommendations.forEach { tutorial ->
                RecommendedTutorialCard(
                    tutorial = tutorial,
                    onStart = { onTutorialStart(tutorial) }
                )
            }
        }
    }
}

/**
 * Recommended tutorial card
 */
@Composable
private fun RecommendedTutorialCard(
    tutorial: TutorialData,
    onStart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(tutorial.difficulty.color).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = tutorial.steps.firstOrNull()?.getIcon() ?: Icons.Default.School,
                    contentDescription = null,
                    tint = Color(tutorial.difficulty.color),
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = tutorial.difficulty.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(tutorial.difficulty.color)
                )
            }
            
            Text(
                text = tutorial.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = tutorial.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tutorial.durationText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Button(
                    onClick = onStart,
                    modifier = Modifier.size(width = 100.dp, height = 36.dp)
                ) {
                    Text("Başla", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

/**
 * Tutorial category section
 */
@Composable
private fun TutorialCategorySection(
    category: TutorialCategory,
    tutorials: List<TutorialData>,
    completionRate: Double,
    onTutorialStart: (TutorialData) -> Unit,
    onTutorialReset: (TutorialData) -> Unit
) {
    Column {
        // Category Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = category.displayName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = category.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Completion Badge
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (completionRate >= 100) Color(0xFF4CAF50) else Color(0xFF2196F3)
                )
            ) {
                Text(
                    text = "%.0f%%".format(completionRate),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Tutorial Cards
        tutorials.forEach { tutorial ->
            TutorialCard(
                tutorial = tutorial,
                onStart = { onTutorialStart(tutorial) },
                onReset = { onTutorialReset(tutorial) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * Individual tutorial card
 */
@Composable
private fun TutorialCard(
    tutorial: TutorialData,
    onStart: () -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (tutorial.isCompleted) {
                Color(0xFF4CAF50).copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(
                            imageVector = tutorial.steps.firstOrNull()?.getIcon() ?: Icons.Default.School,
                            contentDescription = null,
                            tint = Color(tutorial.difficulty.color),
                            modifier = Modifier.size(20.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = tutorial.difficulty.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(tutorial.difficulty.color)
                        )
                        
                        if (tutorial.isCompleted) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Tamamlandı",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    Text(
                        text = tutorial.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    
                    Text(
                        text = tutorial.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tutorial.completionText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = tutorial.durationText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Column {
                    Button(
                        onClick = onStart,
                        modifier = Modifier.size(width = 80.dp, height = 36.dp)
                    ) {
                        Text(
                            if (tutorial.isCompleted) "Tekrar" else "Başla",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    
                    if (tutorial.isCompleted) {
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(
                            onClick = onReset,
                            modifier = Modifier.size(width = 80.dp, height = 32.dp)
                        ) {
                            Text(
                                "Sıfırla",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
            
            // Progress bar for partially completed tutorials
            if (!tutorial.isCompleted && tutorial.completionPercentage > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { (tutorial.completionPercentage / 100).toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color(tutorial.difficulty.color),
                    trackColor = Color(tutorial.difficulty.color).copy(alpha = 0.2f)
                )
            }
        }
    }
}

/**
 * Tutorial overlay for guided tour - FIXED button positioning and navigation
 */
@Composable
private fun TutorialOverlay(
    tutorialViewModel: TutorialViewModel,
    onExit: () -> Unit
) {
    val uiState by tutorialViewModel.uiState.collectAsStateWithLifecycle()
    val currentStep = tutorialViewModel.getCurrentStep()
    
    // Full screen overlay with improved positioning
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)) // Increased opacity for better visibility
    ) {
        if (currentStep != null) {
            // Tutorial Step Card with better positioning
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp) // Reduced padding for better mobile fit
                    .fillMaxWidth()
                    .wrapContentHeight(), // Allow height to wrap content
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Progress indicator - IMPROVED
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = uiState.tutorialProgressText,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Text(
                                text = "${uiState.currentStepIndex + 1} / ${uiState.currentTutorial?.steps?.size ?: 0}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        IconButton(onClick = onExit) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Tutorial'ı Kapat",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Enhanced progress bar
                    LinearProgressIndicator(
                        progress = { (uiState.tutorialProgress / 100).toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .height(6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Step content - IMPROVED
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Card(
                            modifier = Modifier.size(40.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = currentStep.getIcon(),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentStep.title,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = currentStep.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.2
                            )
                            
                            // Action hint if available
                            if (currentStep.actionType != TutorialActionType.INFO) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "💡 ${currentStep.actionType.displayName} yapmanız gerekiyor",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // FIXED Navigation buttons with proper spacing and sizing
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Primary action row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Previous button - only show if not first step
                            if (tutorialViewModel.canGoToPreviousStep()) {
                                OutlinedButton(
                                    onClick = { tutorialViewModel.previousTutorialStep() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Default.ArrowBack,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Önceki")
                                }
                            }
                            
                            // Next/Complete button
                            Button(
                                onClick = { tutorialViewModel.nextTutorialStep() },
                                modifier = Modifier.weight(if (tutorialViewModel.canGoToPreviousStep()) 1f else 2f)
                            ) {
                                Text(
                                    if (uiState.isLastStep) "Tamamla" else "Sonraki"
                                )
                                if (!uiState.isLastStep) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        
                        // Secondary action row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            TextButton(
                                onClick = { tutorialViewModel.skipTutorial() }
                            ) {
                                Icon(
                                    Icons.Default.SkipNext,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tutorial'ı Atla")
                            }
                        }
                    }
                }
            }
        }
        
        // Tutorial completion celebration
        if (uiState.isLastStep && uiState.completionMessage != null) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Celebration,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = uiState.completionMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

/**
 * Empty state when no tutorials available
 */
@Composable
private fun EmptyTutorialState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Henüz Tutorial Yok",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Tutorial'lar yakında eklenecek. Uygulamayı keşfetmeye başlayabilirsiniz!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * No search results state
 */
@Composable
private fun NoSearchResultsState(
    searchQuery: String,
    onClearFilters: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Arama Sonucu Bulunamadı",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (searchQuery.isNotEmpty()) {
                    "'$searchQuery' için sonuç bulunamadı. Filtreleri temizleyip tekrar deneyin."
                } else {
                    "Seçilen filtrelere uygun tutorial bulunamadı."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(onClick = onClearFilters) {
                Text("Filtreleri Temizle")
            }
        }
    }
}