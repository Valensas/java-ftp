package com.valensas.ftp.server

import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.auth.password.PasswordAuthenticator
import org.apache.sshd.server.auth.pubkey.KeySetPublickeyAuthenticator
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.apache.sshd.server.session.ServerSession
import org.apache.sshd.sftp.server.SftpSubsystemFactory
import java.nio.file.Files
import java.nio.file.Path
import java.security.PublicKey

class EmbeddedSftpServer {
    private lateinit var sshServer: SshServer
    private lateinit var serverRoot: Path

    fun start(
        username: String,
        password: String? = null,
        host: String = "localhost",
        port: Int = 0,
        path: Path? = Files.createTempDirectory("ftp-test"),
        clientPublicKey: PublicKey? = null,
        algorithm: String = "RSA",
        keysize: Int = 2048
    ) {
        sshServer = SshServer.setUpDefaultServer()
        val fileSystemFactory = VirtualFileSystemFactory()
        path?.let {
            serverRoot = it
            fileSystemFactory.defaultHomeDir = serverRoot
            sshServer.fileSystemFactory = fileSystemFactory
        }
        clientPublicKey?.let {
            sshServer.publickeyAuthenticator = PublickeyAuthenticator { usernameTest, publicKey, serverSession ->
                isValid(publicKey, clientPublicKey)
            }
        }
        sshServer.keyPairProvider = setProvider(algorithm, keysize)
        sshServer.subsystemFactories = listOf(SftpSubsystemFactory())
        sshServer.port = port
        sshServer.host = host
        password.let {
            sshServer.passwordAuthenticator =
                PasswordAuthenticator { usernameTest: String, passwordTest: String, session: ServerSession? ->
                    usernameTest == username && passwordTest == password
                }
        }
        sshServer.start()
    }

    fun stop() {
        if (::sshServer.isInitialized) {
            sshServer.stop(true)
        }
    }

    fun getHost(): String = sshServer.host

    fun getPort(): Int = sshServer.port

    private fun setProvider(algorithm: String, keysize: Int): SimpleGeneratorHostKeyProvider {
        val provider = SimpleGeneratorHostKeyProvider()
        provider.algorithm = algorithm
        provider.keySize = keysize
        return provider
    }

    private fun isValid(pub1: PublicKey, pub2: PublicKey): Boolean {
        return pub1 == pub2
    }
}
