package com.example.zkp

data class RegisterRequest(val username: String, val vHex: String)
data class GenericResponse(val ok: Boolean, val message: String)

data class StartAuthRequest(val username: String, val tHex: String)
data class StartAuthResponse(val challengeHex: String)

data class FinishAuthRequest(val username: String, val sHex: String, val tHex: String)