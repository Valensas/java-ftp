package com.valensas.ftp.model

import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import javax.naming.AuthenticationException

open class FTPClient : FTPClient() {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun authAndConnect(connectionModel: ConnectionModel): ConnectionResult {
        var retryCount = 0
        var connected = false
        val errors = mutableListOf<String>()

        repeat(connectionModel.retryBackoffDurationsInSecond.size + 1) {
            try {
                connectToServer(connectionModel)
                connected = true
                return@repeat
            } catch (e: AuthenticationException) {
                errors.add(e.localizedMessage)
                throw e
            } catch (e: Throwable) {
                val waitTime = connectionModel.retryBackoffDurationsInSecond.getOrNull(it)?.toLong() ?: return@repeat
                logger.error("Unable to connect ftp server. Error is: ", e)
                logger.info("Waiting {} seconds for next trial.", waitTime)
                retryCount += 1
                errors.add(e.localizedMessage)
                Thread.sleep(Duration.ofSeconds(waitTime).toSeconds())
            }
        }

        return ConnectionResult(
            connected = connected,
            retryCount = retryCount,
            errors = errors.groupBy { it }.map { ConnectionResult.Error(it.key, it.value.size) }.toSet(),
        )
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
        if (connectionModel.connectionType == ConnectionType.FTP) {
            when (connectionModel.connectionMode) {
                ConnectionMode.Active -> enterLocalActiveMode()
                ConnectionMode.Passive -> enterLocalPassiveMode()
                else -> enterLocalPassiveMode()
            }
        }
        this.setFileType(FTP.BINARY_FILE_TYPE)
    }
}
