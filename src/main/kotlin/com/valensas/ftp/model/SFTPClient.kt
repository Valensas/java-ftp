package com.valensas.ftp.model

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.ChannelSftp.LsEntry
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import org.apache.commons.net.ftp.FTPFile
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.util.UUID

class SFTPClient : FTPClient() {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private lateinit var session: Session
    private lateinit var channel: ChannelSftp

    override fun connectToServer(connectionModel: ConnectionModel) {
        val jSch = JSch()

        connectionModel.privateKey?.let {
            jSch.addIdentity(UUID.randomUUID().toString(), it.toByteArray(), null, null)
        }
        session = jSch.getSession(connectionModel.username, connectionModel.host, connectionModel.port)
        connectionModel.connectionTimeout?.let {
            session.timeout = it
        }
        connectionModel.password?.let {
            session.setPassword(it)
        }
        session.setConfig("StrictHostKeyChecking", connectionModel.strictHostKeyChecking)
        session.connect()
        channel = session.openChannel("sftp") as ChannelSftp
        channel.connect()
    }

    override fun retrieveFileStream(remoteFileName: String): InputStream = channel.get(remoteFileName)

    override fun deleteFile(remoteFileName: String): Boolean =
        try {
            channel.rm(remoteFileName)
            true
        } catch (e: Exception) {
            logger.warn("Can not delete $remoteFileName file.", e)
            throw e
        }

    override fun rename(
        from: String?,
        to: String?,
    ): Boolean =
        try {
            channel.rename(from, to)
            true
        } catch (e: Exception) {
            logger.warn("Can not move file from $from to $to.", e)
            throw e
        }

    override fun storeFile(
        remote: String?,
        local: InputStream?,
    ): Boolean {
        try {
            channel.put(local, remote)
            return true
        } catch (e: Exception) {
            logger.warn("Error happened while uploading file", e)
            throw e
        }
    }

    override fun listFilesInfo(path: String): Map<String, Long> {
        val fileVector = channel.ls(path)
        return fileVector
            .map { it as LsEntry }
            .filter { !it.attrs.isDir }
            .associate { it.filename to it.attrs.size }
    }

    override fun listDirectoryInfo(path: String): Map<String, Long> {
        val fileVector = channel.ls(path)
        return fileVector
            .map { it as LsEntry }
            .filter { it.attrs.isDir && it.filename != "." && it.filename != ".." }
            .associate { it.filename to it.attrs.size }
    }

    override fun completePendingCommand(): Boolean = true

    override fun isConnected(): Boolean = channel.isConnected || session.isConnected

    override fun disconnect() {
        if (::channel.isInitialized) {
            logger.debug("Disconnecting from sftp channel.")
            channel.disconnect()
        }
        if (::session.isInitialized) {
            logger.debug("Disconnecting from sftp session.")
            session.disconnect()
        }
    }

    override fun listDirectories(parent: String): Array<FTPFile> {
        val fileVector = channel.ls(parent)
        return fileVector
            .map { it as LsEntry }
            .filter { it.attrs.isDir }
            .map {
                val ftpFile = FTPFile()
                ftpFile.name = it.filename
                ftpFile
            }.toTypedArray()
    }

    fun setTimeout(timeout: Int) {
        if (::session.isInitialized) {
            session.timeout = timeout
        }
    }

    override fun makeDirectory(pathname: String): Boolean {
        try {
            val directories = pathname.split("/")
            var current = "/"
            directories.forEach {
                val dirs = listDirectoryInfo(current)
                current = current + it + "/"
                if (dirs.containsKey(it)) return@forEach
                channel.mkdir(current.removeSuffix("/"))
            }
            return true
        } catch (e: Exception) {
            logger.warn("Error happened while creating directory", e)
            throw e
        }
    }
}
