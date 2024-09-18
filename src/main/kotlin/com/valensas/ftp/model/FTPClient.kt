package com.valensas.ftp.model

import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient

open class FTPClient : FTPClient() {
    open fun authAndConnect(connectionModel: ConnectionModel) {
        this.connect(connectionModel.host, connectionModel.port)
        this.login(connectionModel.username, connectionModel.password)
        this.setFileType(FTP.BINARY_FILE_TYPE)
    }

    open fun listFilesInfo(path: String): Map<String, Long> {
        val filesInfo =
            this.listFiles(path).map {
                it.name to it.size
            }
        return filesInfo.toMap()
    }
}
