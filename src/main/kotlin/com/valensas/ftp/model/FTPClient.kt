package com.valensas.ftp.model

import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.naming.AuthenticationException

open class FTPClient : FTPClient() {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private var _retryConnectionTimeouts: List<Int> = listOf(0)
    var retryConnectionTimeouts: List<Int>
        get() = _retryConnectionTimeouts
        set(value) {
            _retryConnectionTimeouts = value
        }

    fun authAndConnect(connectionModel: ConnectionModel) {
        run breaking@{
            retryConnectionTimeouts.forEach {
                try {
                    this.connectTimeout = it
                    connectToServer(connectionModel)
                    return@breaking
                } catch (e: AuthenticationException) {
                    throw e
                } catch (e: Throwable) {
                    logger.error("Error connecting to server", e)
                }
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
