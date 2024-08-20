package com.valensas.ftp.model

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.ChannelSftp.LsEntry
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import org.apache.commons.net.ftp.FTPClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.util.UUID

class SFTPClient : FTPClient() {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private lateinit var session: Session
    private lateinit var channel: ChannelSftp

    fun connect(testConnection: ConnectionModel) {
        try {
            val jSch = JSch()
            jSch.addIdentity(
                UUID.randomUUID().toString(),
                testConnection.privateKey?.toByteArray(),
                testConnection.publicKey?.toByteArray(),
                null,
            )
            session = jSch.getSession(testConnection.username, testConnection.host, testConnection.port)
            session.setPassword(testConnection.password)
            session.setConfig("StrictHostKeyChecking", "no")
            session.connect()
            channel = session.openChannel("sftp") as ChannelSftp
            channel.connect()
        } catch (e: Exception) {
            logger.error("An error occurred while connecting to SFTP Server", e)
            throw e
        }
    }

    override fun retrieveFileStream(remoteFileName: String): InputStream = channel.get(remoteFileName)

    override fun deleteFile(remoteFileName: String): Boolean {
        channel.rm(remoteFileName)
        return true
    }

    fun listFilesAtPath(pathname: String): List<LsEntry> {
        val fileVector = channel.ls(pathname)
        return fileVector.map {
            it as LsEntry
        }
    }

    fun uploadFile(
        inputStream: InputStream,
        sftpPath: String,
    ) {
        channel.put(inputStream, sftpPath)
    }

    override fun isConnected(): Boolean = channel.isConnected

    override fun disconnect() {
        session.disconnect()
        channel.disconnect()
    }
}
