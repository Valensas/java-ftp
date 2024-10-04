package com.valensas.ftp.model

import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import javax.naming.AuthenticationException

open class FTPClient : FTPClient() {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    var retryBackoffDurations: List<Long> = listOf(0)

    fun authAndConnect(connectionModel: ConnectionModel) {
        retryBackoffDurations.forEach {
            try {
                connectToServer(connectionModel)
                return@authAndConnect
            } catch (e: AuthenticationException) {
                throw e
            } catch (e: Throwable) {
                logger.error("Error connecting to server", e)
                Thread.sleep(Duration.ofMillis(it))
            }
        }
    }

    open fun listFilesInfo(path: String): Map<String, Long> {
        val filesInfo =
            this.listFiles(path).map {
                it.name to it.size
            }
        return filesInfo.toMap()
    }

    open fun connectToServer(connectionModel: ConnectionModel) {
        this.connect(connectionModel.host, connectionModel.port)
        if (!this.login(connectionModel.username, connectionModel.password)) {
            throw AuthenticationException("Authentication failed")
        }
        this.setFileType(FTP.BINARY_FILE_TYPE)
    }
}
