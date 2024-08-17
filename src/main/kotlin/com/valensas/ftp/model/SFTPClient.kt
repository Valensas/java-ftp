package com.valensas.ftp.model

import com.jcraft.jsch.Channel
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import org.apache.commons.net.ftp.FTPClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.UUID

class SFTPClient : FTPClient() {

    private val logger: Logger = LoggerFactory.getLogger(javaClass)


    private lateinit var session: Session
    private lateinit var channel: Channel

    fun connect(testConnection: ConnectionModel) {
        try {
            val jSch = JSch()
            jSch.addIdentity(UUID.randomUUID().toString(), testConnection.privateKey?.toByteArray(), testConnection.publicKey?.toByteArray(), null)
            session = jSch.getSession(testConnection.username, testConnection.host, testConnection.port)
            session.setPassword(testConnection.password)
            session.setConfig("StrictHostKeyChecking", "no")
            session.connect()
            channel = session.openChannel("sftp")
            channel.connect()
        } catch (e: Exception) {
            logger.error("An error occurred while connecting to SFTP Server", e)
            throw e
        }
    }

    override fun isConnected(): Boolean = channel.isConnected

    override fun disconnect() {
        session.disconnect()
        channel.disconnect()
    }
}