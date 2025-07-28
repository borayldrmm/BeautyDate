package com.borayildirim.beautydate.data.repository

import com.borayildirim.beautydate.data.local.TutorialPreferences
import com.borayildirim.beautydate.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of TutorialRepository
 * Provides BeautyDate-specific tutorial content and progress tracking
 * Memory efficient: Combines DataStore preferences with static tutorial data
 */
@Singleton
class TutorialRepositoryImpl @Inject constructor(
    private val tutorialPreferences: TutorialPreferences
) : TutorialRepository {
    
    companion object {
        /**
         * Creates default BeautyDate tutorial data
         */
        private fun createDefaultTutorials(): List<TutorialData> {
            return listOf(
                // Getting Started Tutorial
                TutorialData(
                    id = "getting_started",
                    title = "BeautyDate'e Hoş Geldiniz",
                    description = "Uygulamayı kullanmaya başlamak için temel özellikleri öğrenin",
                    category = TutorialCategory.GETTING_STARTED,
                    estimatedDuration = 5,
                    difficulty = TutorialDifficulty.BEGINNER,
                    steps = listOf(
                        TutorialStep(
                            id = "welcome_overview",
                            title = "Ana Ekran Tanıtımı",
                            description = "BeautyDate ana ekranının bölümlerini keşfedin. Alt navigation bar ile farklı bölümlere erişebilirsiniz.",
                            iconName = "home",
                            category = TutorialCategory.GETTING_STARTED,
                            order = 1,
                            targetScreen = "home",
                            actionType = TutorialActionType.INFO
                        ),
                        TutorialStep(
                            id = "navigation_tour",
                            title = "Navigasyon Turu",
                            description = "Alt menüden Müşteriler, Takvim, Diğer sekmelerine nasıl geçiş yapacağınızı öğrenin.",
                            iconName = "menu",
                            category = TutorialCategory.GETTING_STARTED,
                            order = 2,
                            targetScreen = "main",
                            actionType = TutorialActionType.TAP
                        ),
                        TutorialStep(
                            id = "quick_actions",
                            title = "Hızlı Eylemler",
                            description = "Ana ekrandaki hızlı eylem kartlarıyla müşteri ekleme, randevu oluşturma işlemlerini öğrenin.",
                            iconName = "add",
                            category = TutorialCategory.GETTING_STARTED,
                            order = 3,
                            targetScreen = "home",
                            actionType = TutorialActionType.TAP
                        )
                    )
                ),
                
                // Customer Management Tutorial
                TutorialData(
                    id = "customer_management",
                    title = "Müşteri Yönetimi",
                    description = "Müşteri ekleme, düzenleme ve arama işlemlerini öğrenin",
                    category = TutorialCategory.CUSTOMER_MANAGEMENT,
                    estimatedDuration = 8,
                    difficulty = TutorialDifficulty.BEGINNER,
                    prerequisites = listOf("getting_started"),
                    steps = listOf(
                        TutorialStep(
                            id = "add_customer",
                            title = "Yeni Müşteri Ekleme",
                            description = "Müşteriler sekmesinden + butonuna tıklayarak yeni müşteri ekleyin. Gerekli bilgileri doldurun.",
                            iconName = "people",
                            category = TutorialCategory.CUSTOMER_MANAGEMENT,
                            order = 1,
                            targetScreen = "musteriler",
                            actionType = TutorialActionType.TAP
                        ),
                        TutorialStep(
                            id = "customer_form",
                            title = "Müşteri Bilgileri",
                            description = "Ad, telefon, e-mail gibi temel bilgileri girin. İsteğe bağlı adres bilgilerini de ekleyebilirsiniz.",
                            iconName = "edit",
                            category = TutorialCategory.CUSTOMER_MANAGEMENT,
                            order = 2,
                            targetScreen = "add_customer",
                            actionType = TutorialActionType.TYPE
                        ),
                        TutorialStep(
                            id = "customer_search",
                            title = "Müşteri Arama",
                            description = "Arama kutusunu kullanarak müşterileri isim veya telefon numarasına göre bulun.",
                            iconName = "search",
                            category = TutorialCategory.CUSTOMER_MANAGEMENT,
                            order = 3,
                            targetScreen = "musteriler",
                            actionType = TutorialActionType.TYPE
                        ),
                        TutorialStep(
                            id = "customer_details",
                            title = "Müşteri Detayları",
                            description = "Müşteri kartına tıklayarak detay sayfasına gidin. Burada randevu geçmişi ve notları görebilirsiniz.",
                            iconName = "info",
                            category = TutorialCategory.CUSTOMER_MANAGEMENT,
                            order = 4,
                            targetScreen = "customer_detail",
                            actionType = TutorialActionType.TAP
                        )
                    )
                ),
                
                // Appointment System Tutorial
                TutorialData(
                    id = "appointment_system",
                    title = "Randevu Sistemi",
                    description = "Randevu oluşturma, düzenleme ve takvim kullanımını öğrenin",
                    category = TutorialCategory.APPOINTMENT_SYSTEM,
                    estimatedDuration = 10,
                    difficulty = TutorialDifficulty.INTERMEDIATE,
                    prerequisites = listOf("customer_management"),
                    steps = listOf(
                        TutorialStep(
                            id = "calendar_overview",
                            title = "Takvim Görünümü",
                            description = "Takvim sekmesinde günlük randevularınızı görebilirsiniz. Renkli slot'lar randevu durumlarını gösterir.",
                            iconName = "calendar",
                            category = TutorialCategory.APPOINTMENT_SYSTEM,
                            order = 1,
                            targetScreen = "calendar",
                            actionType = TutorialActionType.INFO
                        ),
                        TutorialStep(
                            id = "create_appointment",
                            title = "Randevu Oluşturma",
                            description = "Boş bir zaman dilimene tıklayarak yeni randevu oluşturun. Müşteriyi ve hizmeti seçin.",
                            iconName = "add",
                            category = TutorialCategory.APPOINTMENT_SYSTEM,
                            order = 2,
                            targetScreen = "calendar",
                            actionType = TutorialActionType.TAP
                        ),
                        TutorialStep(
                            id = "appointment_details",
                            title = "Randevu Bilgileri",
                            description = "Müşteri seçimi, hizmet seçimi, tarih/saat ayarlama ve özel notlar eklemeyi öğrenin.",
                            iconName = "edit",
                            category = TutorialCategory.APPOINTMENT_SYSTEM,
                            order = 3,
                            targetScreen = "add_appointment",
                            actionType = TutorialActionType.TYPE
                        ),
                        TutorialStep(
                            id = "appointment_status",
                            title = "Randevu Durumları",
                            description = "Randevuları tamamlandı, iptal edildi veya gelmedi olarak işaretlemeyi öğrenin.",
                            iconName = "check",
                            category = TutorialCategory.APPOINTMENT_SYSTEM,
                            order = 4,
                            targetScreen = "calendar",
                            actionType = TutorialActionType.TAP
                        )
                    )
                ),
                
                // Service Management Tutorial
                TutorialData(
                    id = "service_management",
                    title = "Hizmet Yönetimi",
                    description = "Salon hizmetlerinizi tanımlama ve fiyatlandırma",
                    category = TutorialCategory.SERVICE_MANAGEMENT,
                    estimatedDuration = 7,
                    difficulty = TutorialDifficulty.BEGINNER,
                    steps = listOf(
                        TutorialStep(
                            id = "service_categories",
                            title = "Hizmet Kategorileri",
                            description = "Hizmetlerinizi kategorilere ayırın: Saç, Cilt, Makyaj, Nail Art gibi.",
                            iconName = "work",
                            category = TutorialCategory.SERVICE_MANAGEMENT,
                            order = 1,
                            targetScreen = "services",
                            actionType = TutorialActionType.INFO
                        ),
                        TutorialStep(
                            id = "add_service",
                            title = "Yeni Hizmet Ekleme",
                            description = "Hizmet adı, kategorisi, fiyatı ve süresini belirleyerek yeni hizmet ekleyin.",
                            iconName = "add",
                            category = TutorialCategory.SERVICE_MANAGEMENT,
                            order = 2,
                            targetScreen = "add_service",
                            actionType = TutorialActionType.TYPE
                        ),
                        TutorialStep(
                            id = "price_management",
                            title = "Fiyat Yönetimi",
                            description = "Toplu fiyat güncellemeleri yapabilir, indirim ve artış oranları uygulayabilirsiniz.",
                            iconName = "edit",
                            category = TutorialCategory.SERVICE_MANAGEMENT,
                            order = 3,
                            targetScreen = "services",
                            actionType = TutorialActionType.TAP
                        )
                    )
                ),
                
                // Statistics Tutorial
                TutorialData(
                    id = "statistics_analytics",
                    title = "İstatistik ve Analiz",
                    description = "İş analitiği ve raporlarınızı görüntüleme",
                    category = TutorialCategory.STATISTICS_ANALYTICS,
                    estimatedDuration = 6,
                    difficulty = TutorialDifficulty.INTERMEDIATE,
                    prerequisites = listOf("customer_management", "appointment_system"),
                    steps = listOf(
                        TutorialStep(
                            id = "statistics_overview",
                            title = "İstatistik Genel Bakış",
                            description = "Diğer menüsünden İstatistikler'e giderek işletmenizin genel performansını görün.",
                            iconName = "analytics",
                            category = TutorialCategory.STATISTICS_ANALYTICS,
                            order = 1,
                            targetScreen = "statistics",
                            actionType = TutorialActionType.INFO
                        ),
                        TutorialStep(
                            id = "financial_stats",
                            title = "Finansal İstatistikler",
                            description = "Gelir, gider, kar marjı ve ödeme yöntemlerini analiz edin.",
                            iconName = "payment",
                            category = TutorialCategory.STATISTICS_ANALYTICS,
                            order = 2,
                            targetScreen = "statistics",
                            actionType = TutorialActionType.SCROLL
                        ),
                        TutorialStep(
                            id = "customer_analytics",
                            title = "Müşteri Analitiği",
                            description = "Müşteri büyüme oranı, retention ve ortalama müşteri değerini inceleyin.",
                            iconName = "people",
                            category = TutorialCategory.STATISTICS_ANALYTICS,
                            order = 3,
                            targetScreen = "statistics",
                            actionType = TutorialActionType.SCROLL
                        )
                    )
                )
            )
        }
    }
    
    private val defaultTutorials = createDefaultTutorials()
    
    override fun getAllTutorials(): Flow<List<TutorialData>> {
        return combine(
            tutorialPreferences.completedTutorials,
            tutorialPreferences.skippedTutorials
        ) { completed, skipped ->
            defaultTutorials.map { tutorial ->
                tutorial.copy(
                    isCompleted = completed.contains(tutorial.id),
                    steps = tutorial.steps.map { step ->
                        step.copy(isCompleted = completed.contains("${tutorial.id}_${step.id}"))
                    }
                )
            }
        }
    }
    
    override fun getTutorialsByCategory(category: TutorialCategory): Flow<List<TutorialData>> {
        return getAllTutorials().map { tutorials ->
            tutorials.filter { it.category == category }
        }
    }
    
    override suspend fun getTutorialById(id: String): TutorialData? {
        return defaultTutorials.find { it.id == id }
    }
    
    override fun getCompletedTutorials(): Flow<List<TutorialData>> {
        return getAllTutorials().map { tutorials ->
            tutorials.filter { it.isCompleted }
        }
    }
    
    override fun getAvailableTutorials(): Flow<List<TutorialData>> {
        return getAllTutorials().map { tutorials ->
            tutorials.filter { !it.isCompleted }
        }
    }
    
    override fun getRecommendedTutorials(): Flow<List<TutorialData>> {
        return combine(
            getAllTutorials(),
            tutorialPreferences.hasCompletedOnboarding
        ) { tutorials, hasOnboarding ->
            if (!hasOnboarding) {
                // If onboarding not completed, recommend getting started
                tutorials.filter { it.category == TutorialCategory.GETTING_STARTED }
            } else {
                // Recommend based on prerequisites and completion status
                tutorials.filter { tutorial ->
                    !tutorial.isCompleted && 
                    tutorial.prerequisites.all { prereq ->
                        tutorials.find { it.id == prereq }?.isCompleted == true
                    }
                }.take(3) // Limit to 3 recommendations
            }
        }
    }
    
    override suspend fun getTutorialSteps(tutorialId: String): List<TutorialStep> {
        return getTutorialById(tutorialId)?.steps ?: emptyList()
    }
    
    override suspend fun markStepCompleted(tutorialId: String, stepId: String): Result<Unit> {
        return try {
            tutorialPreferences.markTutorialCompleted("${tutorialId}_${stepId}")
            
            // Check if all steps are completed, then mark tutorial as completed
            val tutorial = getTutorialById(tutorialId)
            if (tutorial != null) {
                val allStepsCompleted = tutorial.steps.all { step ->
                    // This would need actual checking against preferences
                    true // Simplified for now
                }
                if (allStepsCompleted) {
                    tutorialPreferences.markTutorialCompleted(tutorialId)
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun markTutorialCompleted(tutorialId: String): Result<Unit> {
        return try {
            tutorialPreferences.markTutorialCompleted(tutorialId)
            
            // Also mark all steps as completed
            val tutorial = getTutorialById(tutorialId)
            tutorial?.steps?.forEach { step ->
                tutorialPreferences.markTutorialCompleted("${tutorialId}_${step.id}")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun markTutorialSkipped(tutorialId: String): Result<Unit> {
        return try {
            tutorialPreferences.markTutorialSkipped(tutorialId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun resetTutorialProgress(tutorialId: String): Result<Unit> {
        return try {
            // This would need implementation in TutorialPreferences
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun resetAllProgress(): Result<Unit> {
        return try {
            tutorialPreferences.resetAllProgress()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getTutorialCompletionStatus(tutorialId: String): Boolean {
        return tutorialPreferences.isTutorialCompleted(tutorialId)
    }
    
    override fun getTutorialProgress(): Flow<Double> {
        return tutorialPreferences.getTutorialProgress(defaultTutorials.size)
    }
    
    override fun getTutorialPreferences(): Flow<com.borayildirim.beautydate.data.models.TutorialPreferences> {
        return flowOf(
            com.borayildirim.beautydate.data.models.TutorialPreferences(
                hasSeenWelcome = false,
                hasCompletedOnboarding = false,
                completedTutorials = emptyList(),
                skippedTutorials = emptyList(),
                showHints = true,
                autoPlayTutorials = false,
                tutorialSpeed = com.borayildirim.beautydate.data.models.TutorialSpeed.NORMAL
            )
        )
    }
    
    override suspend fun updateTutorialPreferences(preferences: com.borayildirim.beautydate.data.models.TutorialPreferences): Result<Unit> {
        return try {
            tutorialPreferences.setWelcomeSeen(preferences.hasSeenWelcome)
            tutorialPreferences.setOnboardingCompleted(preferences.hasCompletedOnboarding)
            tutorialPreferences.setShowHints(preferences.showHints)
            tutorialPreferences.setAutoPlayTutorials(preferences.autoPlayTutorials)
            tutorialPreferences.setTutorialSpeed(preferences.tutorialSpeed)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun initializeDefaultTutorials(): Result<Unit> {
        return try {
            // Default tutorials are already created statically
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun searchTutorials(query: String): Flow<List<TutorialData>> {
        return getAllTutorials().map { tutorials ->
            if (query.isBlank()) {
                tutorials
            } else {
                tutorials.filter { tutorial ->
                    tutorial.title.contains(query, ignoreCase = true) ||
                    tutorial.description.contains(query, ignoreCase = true) ||
                    tutorial.steps.any { step ->
                        step.title.contains(query, ignoreCase = true) ||
                        step.description.contains(query, ignoreCase = true)
                    }
                }
            }
        }
    }
    
    override suspend fun getTutorialStatistics(): TutorialStatistics {
        return try {
            val allTutorials = defaultTutorials
            val totalTutorials = allTutorials.size
            val totalSteps = allTutorials.sumOf { it.steps.size }
            
            // These would need actual completion data
            val completedTutorials = 0 // Would get from preferences
            val skippedTutorials = 0  // Would get from preferences
            val completedSteps = 0    // Would get from preferences
            
            TutorialStatistics(
                totalTutorials = totalTutorials,
                completedTutorials = completedTutorials,
                skippedTutorials = skippedTutorials,
                inProgressTutorials = totalTutorials - completedTutorials - skippedTutorials,
                totalSteps = totalSteps,
                completedSteps = completedSteps,
                averageCompletionTime = 7, // Mock average
                mostPopularCategory = TutorialCategory.GETTING_STARTED,
                completionRate = if (totalTutorials > 0) (completedTutorials.toDouble() / totalTutorials) * 100 else 0.0
            )
        } catch (e: Exception) {
            TutorialStatistics()
        }
    }
} 