package com.example.zkp

import com.example.zkp.groups.MockedSmallGroup
import com.example.zkp.groups.SmallGroup
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*
import java.math.BigInteger
import java.util.concurrent.ConcurrentHashMap

@RestController
@RequestMapping("auth")
class LoginController {

    private val users = ConcurrentHashMap<String, BigInteger>()
    private val pending = ConcurrentHashMap<String, Pair<BigInteger, BigInteger>>()
    private val log = LoggerFactory.getLogger(LoginController::class.java)

    @PostMapping("/register")
    fun register(@RequestBody req: RegisterRequest): GenericResponse {
        val v = BigInteger(req.verifierHex, 16)
        users[req.username] = v

        log.info("REGISTER: user={} stored v={}", req.username, v.toString(16))

        return GenericResponse(true, "User registered")
    }

    // -----------------------------
    // PRODUCTIVO - devuelve SmallGroup
    @GetMapping("/vars")
    fun getVars(): Map<String, String> {
        return mapOf(
            "primeModulusP" to SmallGroup.primeModulusP.toString(),
            "subgroupOrderQ" to SmallGroup.subgroupOrderQ.toString(),
            "generatorG" to SmallGroup.generatorG.toString()
        )
    }

    @PostMapping("/start")
    fun startAuth(@RequestBody req: StartAuthRequest): StartAuthResponse {
        if (!users.containsKey(req.username)) {
            log.warn("START: unknown user={}", req.username)
            return StartAuthResponse("")
        }

        val t = BigInteger(req.commitmentHex, 16)
        val c = SmallGroup.randomBigIntLessThan(SmallGroup.subgroupOrderQ)

        pending[req.username] = Pair(t, c)

        log.info("START: user={} t={} c={}", req.username, t.toString(16), c.toString(16))
        return StartAuthResponse(c.toString(16))
    }

    @PostMapping("/finish")
    fun finishAuth(@RequestBody req: FinishAuthRequest): GenericResponse {
        val v = users[req.username] ?: return GenericResponse(false, "Unknown user")
        val (tStored, c) = pending.remove(req.username)
            ?: return GenericResponse(false, "No auth started")

        val s = BigInteger(req.responseHex, 16)

        log.info("FINISH: user={} received_s={} tStored={} c={}", req.username, s.toString(16), tStored.toString(16), c.toString(16))

        val left = SmallGroup.generatorG.modPow(s, SmallGroup.primeModulusP)
        val right = tStored.multiply(v.modPow(c, SmallGroup.primeModulusP)).mod(SmallGroup.primeModulusP)

        log.info("FINISH: user={} left={} right={}", req.username, left.toString(16), right.toString(16))

        return if (left == right)
            GenericResponse(true, "Authentication success")
        else
            GenericResponse(false, "Authentication FAILED")
    }

    // -----------------------------
    // TEST (grupo chico)
    @GetMapping("/test/vars")
    fun getMockedVars(): Map<String, String> {
        return mapOf(
            "primeModulusP" to MockedSmallGroup.primeModulusP.toString(),
            "subgroupOrderQ" to MockedSmallGroup.subgroupOrderQ.toString(),
            "generatorG" to MockedSmallGroup.generatorG.toString()
        )
    }

    @PostMapping("/test/start")
    fun mockedStartAuth(@RequestBody req: StartAuthRequest): StartAuthResponse {
        if (!users.containsKey(req.username)) {
            log.warn("TEST START: unknown user={}", req.username)
            return StartAuthResponse("")
        }

        val t = BigInteger(req.commitmentHex, 16)
        val c = BigInteger("5")

        pending[req.username] = Pair(t, c)

        log.info("TEST START: user={} t={} c={}", req.username, t.toString(16), c.toString(16))
        return StartAuthResponse(c.toString(16))
    }

    @PostMapping("/test/finish")
    fun mockedFinishAuth(@RequestBody req: FinishAuthRequest): GenericResponse {
        val v = users[req.username] ?: return GenericResponse(false, "Unknown user")
        val (tStored, c) = pending.remove(req.username)
            ?: return GenericResponse(false, "No auth started")

        val s = BigInteger(req.responseHex, 16)

        log.info("TEST FINISH: user={} received_s={} tStored={} c={}", req.username, s.toString(16), tStored.toString(16), c.toString(16))

        val left = MockedSmallGroup.generatorG.modPow(s, MockedSmallGroup.primeModulusP)
        val right = tStored.multiply(v.modPow(c, MockedSmallGroup.primeModulusP)).mod(MockedSmallGroup.primeModulusP)

        log.info("TEST FINISH: user={} left={} right={}", req.username, left.toString(16), right.toString(16))

        return if (left == right)
            GenericResponse(true, "Authentication success")
        else
            GenericResponse(false, "Authentication FAILED")
    }

}
