package com.example.zkp.groups

import java.math.BigInteger
import java.security.SecureRandom

object MockedSmallGroup {

    // Grupo pequeño para pruebas reproducibles
    // p = 23, grupo multiplicativo mod 23 tiene orden 22
    val p = BigInteger("23")
    val q = BigInteger("22")
    val g = BigInteger("5")

    private val rnd = SecureRandom()

    // Función incluida solo para mantener compatibilidad con SmallGroup
    fun randomBigIntLessThan(n: BigInteger): BigInteger {
        var x: BigInteger
        do {
            x = BigInteger(n.bitLength(), rnd)
        } while (x >= n || x == BigInteger.ZERO)
        return x
    }
}
