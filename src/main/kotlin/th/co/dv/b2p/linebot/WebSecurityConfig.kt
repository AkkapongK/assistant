package th.co.dv.b2p.linebot

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class WebSecurityConfig {

    @GetMapping("/test")
    fun test(): String {
        println("........ Test service ........")
        return "{\"success\": true}"
    }
}