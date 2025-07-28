package com.borayildirim.beautydate.data.models

/**
 * Service category enumeration
 * Categorizes services for better organization and filtering
 * Memory efficient: enum class for type safety
 */
enum class ServiceCategory {
    NAIL,           // 💅 Tırnak
    MASSAGE,        // 💆 Masaj  
    SKIN_CARE,      // ✨ Cilt Bakımı
    MAKEUP,         // 💄 Makyaj
    EPILATION,      // 🌸 Epilasyon/Lazer
    WELLNESS,       // 🎯 Zayıflama/Vücut
    EYEBROW_LASH;   // 👁️ Kaş & Kirpik
    
    /**
     * Returns Turkish display name for category
     * Memory efficient: when expression with string constants
     */
    fun getDisplayName(): String {
        return when (this) {
            NAIL -> "Tırnak"
            MASSAGE -> "Masaj"
            SKIN_CARE -> "Cilt Bakımı"
            MAKEUP -> "Makyaj"
            EPILATION -> "Epilasyon/Lazer"
            WELLNESS -> "Zayıflama/Vücut"
            EYEBROW_LASH -> "Kaş & Kirpik"
        }
    }
    
    /**
     * Returns category description
     */
    fun getDescription(): String {
        return when (this) {
            NAIL -> "Manikür, Pedikür, Nail Art işlemleri"
            MASSAGE -> "Klasik, Aromaterapi, Thai masajları"
            SKIN_CARE -> "Cilt temizliği, Anti-aging, Leke karşıtı"
            MAKEUP -> "Günlük, Gece, Gelin makyajları"
            EPILATION -> "Lazer, İğneli, Ağda epilasyon"
            WELLNESS -> "Zayıflama, Detoks, Vücut şekillendirme"
            EYEBROW_LASH -> "Kaş alımı, Laminasyon, Kirpik işlemleri"
        }
    }
    
    /**
     * Returns emoji icon for category
     * Memory efficient: cached emoji strings
     */
    fun getEmoji(): String {
        return when (this) {
            NAIL -> "💅"
            MASSAGE -> "💆"
            SKIN_CARE -> "✨"
            MAKEUP -> "💄"
            EPILATION -> "🌸"
            WELLNESS -> "🎯"
            EYEBROW_LASH -> "👁️"
        }
    }
}

/**
 * Service subcategory enumeration
 * Provides specific service types within each category
 * Memory efficient: enum class with category association
 */
enum class ServiceSubcategory(
    val category: ServiceCategory,
    val displayName: String,
    val defaultPrice: Double = 0.0
) {
    // Tırnak Kategorisi
    MANICURE(ServiceCategory.NAIL, "Manikür", 80.0),
    PEDICURE(ServiceCategory.NAIL, "Pedikür", 100.0),
    MANICURE_PEDICURE(ServiceCategory.NAIL, "Manikür + Pedikür", 150.0),
    PERMANENT_POLISH(ServiceCategory.NAIL, "Kalıcı Oje", 120.0),
    GEL_NAIL(ServiceCategory.NAIL, "Jel Tırnak", 150.0),
    PROSTHETIC_NAIL(ServiceCategory.NAIL, "Protez Tırnak", 200.0),
    NAIL_CARE(ServiceCategory.NAIL, "Tırnak Bakımı", 60.0),
    NAIL_ART(ServiceCategory.NAIL, "Tırnak Süsleme (Nail Art)", 80.0),
    
    // Masaj Kategorisi
    CLASSIC_MASSAGE(ServiceCategory.MASSAGE, "Klasik Masaj", 200.0),
    AROMATHERAPY_MASSAGE(ServiceCategory.MASSAGE, "Aromaterapi Masajı", 250.0),
    DEEP_TISSUE_MASSAGE(ServiceCategory.MASSAGE, "Derin Doku Masajı", 300.0),
    HOT_STONE_MASSAGE(ServiceCategory.MASSAGE, "Sıcak Taş Masajı", 350.0),
    THAI_MASSAGE(ServiceCategory.MASSAGE, "Thai Masajı", 400.0),
    REFLEXOLOGY(ServiceCategory.MASSAGE, "Refleksoloji", 180.0),
    MEDICAL_MASSAGE(ServiceCategory.MASSAGE, "Medikal Masaj", 280.0),
    
    // Cilt Bakımı Kategorisi
    DEEP_CLEANSING(ServiceCategory.SKIN_CARE, "Derinlemesine Cilt Temizliği", 150.0),
    ANTI_AGING_CARE(ServiceCategory.SKIN_CARE, "Anti-aging Bakım", 200.0),
    ANTI_SPOT_CARE(ServiceCategory.SKIN_CARE, "Leke Karşıtı Bakım", 180.0),
    MOISTURIZING_CARE(ServiceCategory.SKIN_CARE, "Nemlendirici Bakım", 120.0),
    BLACKHEAD_CLEANING(ServiceCategory.SKIN_CARE, "Siyah Nokta Temizliği", 100.0),
    SKIN_RENEWAL(ServiceCategory.SKIN_CARE, "Cilt Yenileme (Peeling)", 160.0),
    EYE_CARE(ServiceCategory.SKIN_CARE, "Göz Çevresi Bakımı", 80.0),
    
    // Makyaj Kategorisi
    DAILY_MAKEUP(ServiceCategory.MAKEUP, "Günlük Makyaj", 120.0),
    EVENING_MAKEUP(ServiceCategory.MAKEUP, "Gece Makyajı", 180.0),
    ENGAGEMENT_MAKEUP(ServiceCategory.MAKEUP, "Nişan Makyajı", 250.0),
    BRIDE_MAKEUP(ServiceCategory.MAKEUP, "Gelin Makyajı", 400.0),
    PROFESSIONAL_MAKEUP(ServiceCategory.MAKEUP, "Profesyonel Makyaj", 300.0),
    PERMANENT_MAKEUP(ServiceCategory.MAKEUP, "Kalıcı Makyaj", 500.0),
    
    // Epilasyon/Lazer Kategorisi
    REGIONAL_LASER(ServiceCategory.EPILATION, "Bölgesel Lazer Epilasyon", 300.0),
    FULL_BODY_LASER(ServiceCategory.EPILATION, "Tüm Vücut Lazer Epilasyon", 800.0),
    NEEDLE_EPILATION(ServiceCategory.EPILATION, "İğneli Epilasyon", 150.0),
    SUGAR_WAXING(ServiceCategory.EPILATION, "Şeker Ağda", 100.0),
    ROLL_ON_WAXING(ServiceCategory.EPILATION, "Roll-on Ağda", 80.0),
    SIR_WAXING(ServiceCategory.EPILATION, "Sir Ağda", 120.0),
    
    // Zayıflama/Vücut Kategorisi
    LYMPH_DRAINAGE(ServiceCategory.WELLNESS, "Lenf Drenaj", 200.0),
    CAVITATION(ServiceCategory.WELLNESS, "Kavitasyon", 300.0),
    RADIOFREQUENCY(ServiceCategory.WELLNESS, "Radyofrekans", 250.0),
    EMS_TREATMENT(ServiceCategory.WELLNESS, "EMS (Elektriksel Kas Stimülasyonu)", 180.0),
    CELLULITE_MASSAGE(ServiceCategory.WELLNESS, "Selülit Masajı", 150.0),
    BODY_TONING(ServiceCategory.WELLNESS, "Vücut Sıkılaştırma", 220.0),
    
    // Kaş & Kirpik Kategorisi
    EYEBROW_SHAPING(ServiceCategory.EYEBROW_LASH, "Kaş Alımı", 50.0),
    EYEBROW_LAMINATION(ServiceCategory.EYEBROW_LASH, "Kaş Laminasyonu", 120.0),
    EYEBROW_MICROBLADING(ServiceCategory.EYEBROW_LASH, "Kaş Kontürü (Microblading)", 400.0),
    LASH_LIFTING(ServiceCategory.EYEBROW_LASH, "Kirpik Lifting", 150.0),
    LASH_EXTENSION(ServiceCategory.EYEBROW_LASH, "Kirpik Takma", 300.0),
    PERMANENT_EYEBROW_COLOR(ServiceCategory.EYEBROW_LASH, "Kalıcı Kaş Renklendirme", 200.0);
    
    companion object {
        /**
         * Gets all subcategories for a specific category
         * Memory efficient: filtered list creation
         */
        fun getSubcategoriesForCategory(category: ServiceCategory): List<ServiceSubcategory> {
            return values().filter { it.category == category }
        }
        
        /**
         * Gets subcategory by display name
         * Memory efficient: single find operation
         */
        fun getByDisplayName(displayName: String): ServiceSubcategory? {
            return values().find { it.displayName == displayName }
        }
    }
} 