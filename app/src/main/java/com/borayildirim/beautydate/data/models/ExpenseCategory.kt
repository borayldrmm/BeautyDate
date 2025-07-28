package com.borayildirim.beautydate.data.models

/**
 * Expense category enumeration for business expense management
 * Contains all major expense categories for beauty salon business
 * Memory efficient: enum-based approach with display names
 */
enum class ExpenseCategory(
    val displayName: String,
    val icon: String,
    val subcategories: List<String>
) {
    FIXED_EXPENSES(
        displayName = "💼 Sabit Giderler",
        icon = "💼",
        subcategories = listOf(
            "Kira",
            "Elektrik faturası",
            "Su faturası",
            "Doğalgaz / Isınma gideri",
            "İnternet & Telefon faturası",
            "Temizlik giderleri",
            "POS cihazı / Sanal pos komisyonları",
            "Muhasebe / Mali müşavir ücreti",
            "Sigorta (işyeri sigortası)",
            "Diğer"
        )
    ),
    
    PERSONNEL_EXPENSES(
        displayName = "👩‍🔧 Personel Giderleri",
        icon = "👩‍🔧",
        subcategories = listOf(
            "Personel maaşları",
            "SGK primleri ve vergiler",
            "Yemek / yemek kartı desteği",
            "Yol / ulaşım desteği",
            "Diğer"
        )
    ),
    
    CONSUMABLES_PRODUCTS(
        displayName = "🧴 Tüketim Malzemeleri ve Ürün Giderleri",
        icon = "🧴",
        subcategories = listOf(
            "Cilt bakım ürünleri",
            "Lazer cihazı sarf malzemeleri",
            "Manikür – pedikür malzemeleri",
            "Masaj yağları, losyonlar",
            "Tek kullanımlık ürünler",
            "Saç bakım ürünleri",
            "Diğer"
        )
    ),
    
    EQUIPMENT_EXPENSES(
        displayName = "🛠️ Demirbaş ve Ekipman Giderleri",
        icon = "🛠️",
        subcategories = listOf(
            "Bakım-onarım giderleri",
            "Yeni ekipman alımları",
            "Klima, ısıtıcı giderleri",
            "Bilgisayar, yazıcı, POS cihazı giderleri",
            "Diğer"
        )
    ),
    
    ADVERTISING_EXPENSES(
        displayName = "📣 Reklam Giderleri",
        icon = "📣",
        subcategories = listOf(
            "Reklam giderleri",
            "Diğer"
        )
    ),
    
    TAX_OFFICIAL_FEES(
        displayName = "🧾 Vergi ve Resmi Harçlar",
        icon = "🧾",
        subcategories = listOf(
            "Stopaj, KDV, gelir vergisi",
            "Belediye ruhsat harçları",
            "Çevre temizlik vergisi",
            "Diğer"
        )
    ),
    
    GENERAL_BUSINESS_EXPENSES(
        displayName = "🧺 Genel İşletme Giderleri",
        icon = "🧺",
        subcategories = listOf(
            "Kargo / kurye giderleri",
            "Misafir ikramları (çay, kahve, su, bisküvi vs.)",
            "Diğer"
        )
    );
    
    companion object {
        /**
         * Gets all subcategories for dropdown selection
         * Memory efficient: cached list computation
         */
        fun getAllSubcategories(): List<String> {
            return values().flatMap { it.subcategories }.distinct()
        }
        
        /**
         * Gets category by subcategory name
         * Business logic: find parent category from subcategory
         */
        fun getCategoryBySubcategory(subcategory: String): ExpenseCategory? {
            return values().find { category ->
                category.subcategories.contains(subcategory)
            }
        }
        
        /**
         * Gets formatted subcategories with category prefix
         * UI support: categorized subcategory display
         */
        fun getFormattedSubcategories(): List<Pair<ExpenseCategory, List<String>>> {
            return values().map { category ->
                category to category.subcategories
            }
        }
    }
} 