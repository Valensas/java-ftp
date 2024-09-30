package com.valensas.ftp.model

import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import javax.naming.AuthenticationException

open class FTPClient : FTPClient() {
    private var retryConnectionTimeouts: List<Int> = listOf(0)

    open fun authAndConnect(connectionModel: ConnectionModel) {
        run breaking@{
            retryConnectionTimeouts.forEach {
                try {
                    this.defaultTimeout = it
                    this.connect(connectionModel.host, connectionModel.port)
                    if (!this.login(connectionModel.username, connectionModel.password)) {
                        throw AuthenticationException("Authentication failed")
                    }
                    this.setFileType(FTP.BINARY_FILE_TYPE)
                    return@breaking
                } catch (e: AuthenticationException) {
                    throw e
                } catch (e: Throwable) {
                    return@forEach
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
}
