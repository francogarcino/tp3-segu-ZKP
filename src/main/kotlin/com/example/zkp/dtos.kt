package com.example.zkp

data class RegisterRequest(
    val username: String,
    val verifierHex: String   // v = g^x mod p
)

data class GenericResponse(
    val ok: Boolean,
    val message: String
)

data class StartAuthRequest(
    val username: String,
    val commitmentHex: String   // t = g^r mod p
)

data class StartAuthResponse(
    val challengeHex: String    // c
)

data class FinishAuthRequest(
    val username: String,
    val responseHex: String,     // s = r + c*x mod q
    val commitmentHex: String    // t again (client echoes it)
)
