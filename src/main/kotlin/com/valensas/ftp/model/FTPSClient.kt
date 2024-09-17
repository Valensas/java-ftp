package com.valensas.ftp.model

import org.apache.commons.net.ftp.FTPSClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.SocketException

class FTPSClient(
    private val isImplicit: Boolean = false,
) : FTPClient() {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val ftpsClient = FTPSClient(isImplicit)

    @Throws(IOException::class, SocketException::class)
    override fun authAndConnect(connectionModel: ConnectionModel) {
        try {
            ftpsClient.connect(connectionModel.host, connectionModel.port)
            ftpsClient.login(connectionModel.username, connectionModel.password)
        } catch (e: Throwable) {
            logger.error("An error occured while connecting to FTPS server", e)
            throw e
        }
    }

    @Throws(IOException::class)
    override fun disconnect() {
        try {
            ftpsClient.disconnect()
        } catch (e: Throwable) {
            logger.error("An error occured while disconnecting from FTPS server", e)
            throw e
        }
    }

    @Throws(IOException::class)
    override fun sendCommand(
        command: String,
        args: String?,
    ): Int {
        try {
            return ftpsClient.sendCommand(command, args)
        } catch (e: Throwable) {
            logger.error("An error occured while sending command $command from FTPS server", e)
            throw e
        }
    }
}
