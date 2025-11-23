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
        val v = BigInteger(req.vHex, 16)
        users[req.username] = v

        log.info("Registered user={} with v={}", req.username, v.toString(16))

        return GenericResponse(true, "User registered")
    }


    // -----------------------------
    //  PRODUCTIVO
    @PostMapping("/start")
    fun startAuth(@RequestBody req: StartAuthRequest): StartAuthResponse {
        users[req.username] ?: return StartAuthResponse("")

        val t = BigInteger(req.tHex, 16)
        val c = SmallGroup.randomBigIntLessThan(SmallGroup.q)

        pending[req.username] = Pair(t, c)
        return StartAuthResponse(c.toString(16))
    }

    @PostMapping("/finish")
    fun finishAuth(@RequestBody req: FinishAuthRequest): GenericResponse {
        val v = users[req.username] ?: return GenericResponse(false, "Unknown user")
        val (tStored, c) = pending.remove(req.username)
            ?: return GenericResponse(false, "No auth started")

        val s = BigInteger(req.sHex, 16)

        val left = SmallGroup.g.modPow(s, SmallGroup.p)
        val right = tStored.multiply(v.modPow(c, SmallGroup.p)).mod(SmallGroup.p)

        return if (left == right)
            GenericResponse(true, "Authentication success")
        else
            GenericResponse(false, "Authentication FAILED")
    }

    // -----------------------------
    //  TEST (grupo chico)
    @PostMapping("/test/start")
    fun mockedStartAuth(@RequestBody req: StartAuthRequest): StartAuthResponse {
        users[req.username] ?: return StartAuthResponse("")

        val t = BigInteger(req.tHex, 16)
        val c = BigInteger("5")  // Challenge fijo

        pending[req.username] = Pair(t, c)
        return StartAuthResponse(c.toString(16))
    }

    @PostMapping("/test/finish")
    fun mockedFinishAuth(@RequestBody req: FinishAuthRequest): GenericResponse {
        val v = users[req.username] ?: return GenericResponse(false, "Unknown user")
        val (tStored, c) = pending.remove(req.username)
            ?: return GenericResponse(false, "No auth started")

        val s = BigInteger(req.sHex, 16)

        val left = MockedSmallGroup.g.modPow(s, MockedSmallGroup.p)
        val right = tStored.multiply(v.modPow(c, MockedSmallGroup.p)).mod(MockedSmallGroup.p)

        return if (left == right)
            GenericResponse(true, "Authentication success")
        else
            GenericResponse(false, "Authentication FAILED")
    }

}
