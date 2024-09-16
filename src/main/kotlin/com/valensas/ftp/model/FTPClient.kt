package com.valensas.ftp.model

import org.apache.commons.net.ftp.FTPClient

open class FTPClient : FTPClient() {
    open fun authAndConnect(connectionModel: ConnectionModel) {
        this.connect(connectionModel.host, connectionModel.port)
        this.login(connectionModel.username, connectionModel.password)
    }

    open fun listFilesAtPath(path: String): Any {
        return this.listFiles(path)
    }
}