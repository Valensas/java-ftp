package com.valensas.ftp.model

import org.apache.commons.net.ftp.FTPSClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.SocketException
import javax.net.ssl.KeyManager
import javax.net.ssl.SSLException
import javax.net.ssl.TrustManager

class FTPSClient(
    private val isImplicit: Boolean = false,
) : FTPClient() {
    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val ftpsClient = FTPSClient(isImplicit)

    var trustManager: TrustManager
        get() = ftpsClient.trustManager
        set(value) {
            ftpsClient.trustManager = value
        }

    var authValue: String
        get() = ftpsClient.authValue
        set(value) {
            ftpsClient.authValue = value
        }

    var isEndpointCheckingEnabled: Boolean
        get() = ftpsClient.isEndpointCheckingEnabled
        set(value) {
            ftpsClient.isEndpointCheckingEnabled = value
        }

    var useClientMode: Boolean
        get() = ftpsClient.useClientMode
        set(value) {
            ftpsClient.useClientMode = value
        }

    var needClientAuth: Boolean
        get() = ftpsClient.needClientAuth
        set(value) {
            ftpsClient.needClientAuth = value
        }

    var wantClientAuth: Boolean
        get() = ftpsClient.wantClientAuth
        set(value) {
            ftpsClient.wantClientAuth = value
        }

    var protocols: Array<String>
        get() = ftpsClient.enabledProtocols
        set(value) {
            ftpsClient.enabledProtocols = value
        }

    fun setKeyManager(keyManager: KeyManager?) {
        ftpsClient.setKeyManager(keyManager)
    }

    @Throws(IOException::class, SocketException::class)
    override fun authAndConnect(connectionModel: ConnectionModel) {
        try {
            with(ftpsClient) {
                connect(connectionModel.host, connectionModel.port)
                login(connectionModel.username, connectionModel.password)
            }
        } catch (e: Throwable) {
            logger.error("An error occurred while connecting to the FTPS server", e)
            throw e
        }
    }

    @Throws(IOException::class)
    override fun disconnect() {
        try {
            ftpsClient.disconnect()
        } catch (e: Throwable) {
            logger.error("An error occurred while disconnecting from the FTPS server", e)
            throw e
        }
    }

    @Throws(IOException::class)
    override fun sendCommand(
        command: String,
        args: String?,
    ): Int =
        try {
            ftpsClient.sendCommand(command, args)
        } catch (e: Throwable) {
            logger.error("An error occurred while sending command $command to the FTPS server", e)
            throw e
        }

    @Throws(IOException::class, SSLException::class)
    fun parseADATReply(reply: String): ByteArray = ftpsClient.parseADATReply(reply)

    @Throws(IOException::class, SSLException::class)
    fun parsePBSZ(pbsz: Long): Long = ftpsClient.parsePBSZ(pbsz)

    @Throws(IOException::class)
    fun execAUTH(mechanism: String): Int = ftpsClient.execAUTH(mechanism)

    @Throws(IOException::class)
    fun execCCC(): Int = ftpsClient.execCCC()

    @Throws(IOException::class)
    fun execCONF(data: ByteArray): Int = ftpsClient.execCONF(data)

    @Throws(IOException::class)
    fun execENC(data: ByteArray): Int = ftpsClient.execENC(data)

    @Throws(IOException::class)
    fun execMIC(data: ByteArray): Int = ftpsClient.execMIC(data)

    @Throws(IOException::class, SSLException::class)
    fun execPBSZ(pbsz: Long) {
        ftpsClient.execPBSZ(pbsz)
    }

    @Throws(IOException::class, SSLException::class)
    fun execPROT(prot: String) {
        ftpsClient.execPROT(prot)
    }
}
