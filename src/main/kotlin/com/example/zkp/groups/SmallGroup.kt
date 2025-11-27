package com.example.zkp.groups

import java.math.BigInteger
import java.security.SecureRandom

object SmallGroup {
    val subgroupOrderQ = BigInteger("8202267310775019161")
    val primeModulusP  = BigInteger("16404534621550038323")
    val generatorG = BigInteger("4")

    private val rnd = SecureRandom()

    fun randomBigIntLessThan(n: BigInteger): BigInteger {
        var x: BigInteger
        do {
            x = BigInteger(n.bitLength(), rnd)
        } while (x >= n || x == BigInteger.ZERO)
        return x
    }
}