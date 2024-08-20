package com.valensas.ftp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FtpApplication

fun main(args: Array<String>) {
    runApplication<FtpApplication>(*args)
}
