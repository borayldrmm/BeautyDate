package com.borayildirim.beautydate.data.models

import com.google.firebase.Timestamp

/**
 * User data model representing a user in the BeautyDate app
 * Contains business-related fields collected during registration and profile management
 */
data class User(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val createdAt: Timestamp? = null,
    val emailVerified: Boolean = false,
    // Business details - editable in profile
    val businessName: String = "",
    val phoneNumber: String = "",
    val address: String = "",
    val taxNumber: String = "",
    // Terms and Privacy Policy acceptance
    val acceptedTerms: Boolean = false,
    val termsAcceptedAt: Timestamp? = null
) 