package com.example.zkp.groups

import java.math.BigInteger
import java.security.SecureRandom

object SmallGroup {
    val p: BigInteger = BigInteger("13407807929942597099574024998205846127479365820592393377723561443721764030073546976801874298166903427690031858186486050853753882811946569946433649006084171")
    val q: BigInteger = BigInteger("FFFFFFFFFFFFFFFFFFFFFFFF99DEF836146BC9B1B4D22831", 16)
    val g: BigInteger = BigInteger("5")
    private val rnd = SecureRandom()

    fun randomBigIntLessThan(n: BigInteger): BigInteger {
        var x: BigInteger
        do {
            x = BigInteger(n.bitLength(), rnd)
        } while (x >= n || x == BigInteger.ZERO)
        return x
    }
}