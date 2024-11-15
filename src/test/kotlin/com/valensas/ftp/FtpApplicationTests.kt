package com.valensas.ftp

import com.valensas.ftp.factory.FtpClientFactory
import com.valensas.ftp.model.ConnectionMode
import com.valensas.ftp.model.ConnectionModel
import com.valensas.ftp.model.ConnectionType
import com.valensas.ftp.model.ConnectionVariant
import com.valensas.ftp.model.SFTPClient
import com.valensas.ftp.server.EmbeddedFtpServer
import com.valensas.ftp.server.EmbeddedSftpServer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.springframework.boot.test.context.SpringBootTest
import java.net.ServerSocket
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.util.Base64
import java.util.UUID
import javax.naming.AuthenticationException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
class FtpApplicationTests {
    private val ftpClientFactory = FtpClientFactory()

    @Test
    fun `Test ftp connection`() {
        assertDoesNotThrow {
            val server = EmbeddedFtpServer()
            server.start("username", "password", ConnectionType.FTP, port = getRandomFreePort())
            val client = ftpClientFactory.createFtpClient(ConnectionType.FTP)
            val connectionModel =
                ConnectionModel(
                    ConnectionType.SFTP,
                    "localhost",
                    server.getPort(),
                    "username",
                    "password",
                    Fake.privateKey(),
                    null,
                    ConnectionMode.Passive,
                    6000,
                )
            client.authAndConnect(connectionModel)
            server.stop()
        }
    }

    @Test
    fun `Test ftps connection implicit`() {
        assertDoesNotThrow {
            val server = EmbeddedFtpServer()
            server.start(
                "username",
                "password",
                ConnectionType.FTPS,
                isImplicit = true,
                certificatePath = "src/test/resources/ftps-test-cert.jks",
                port = getRandomFreePort(),
            )
            val connectionModel =
                ConnectionModel(
                    ConnectionType.FTPS,
                    server.getHost(),
                    server.getPort(),
                    "username",
                    "password",
                    Fake.privateKey(),
                    null,
                    ConnectionMode.Passive,
                    6000,
                )
            val client = ftpClientFactory.createFtpClient(ConnectionType.FTPS, ConnectionVariant.Implicit)
            client.authAndConnect(connectionModel)
            server.stop()
        }
    }

    @Test
    fun `Test ftps connection explicit`() {
        assertDoesNotThrow {
            val server = EmbeddedFtpServer()
            server.start(
                "username",
                "password",
                ConnectionType.FTPS,
                isImplicit = false,
                certificatePath = "src/test/resources/ftps-test-cert.jks",
                port = getRandomFreePort(),
            )
            val connectionModel =
                ConnectionModel(
                    ConnectionType.FTPS,
                    server.getHost(),
                    server.getPort(),
                    "username",
                    "password",
                    Fake.privateKey(),
                    null,
                    ConnectionMode.Passive,
                    6000,
                )
            val client = ftpClientFactory.createFtpClient(ConnectionType.FTPS, ConnectionVariant.Explicit)
            client.authAndConnect(connectionModel)
            server.stop()
        }
    }

    @Test
    fun `Test sftp connection with RSA`() {
        assertDoesNotThrow {
            val keys = generatePublicKey()
            val server = EmbeddedSftpServer()
            server.start("username", null, clientPublicKey = keys.public, algorithm = "RSA")
            val client = ftpClientFactory.createFtpClient(ConnectionType.SFTP) as SFTPClient
            client.authAndConnect(
                ConnectionModel(
                    ConnectionType.SFTP,
                    server.getHost(),
                    server.getPort(),
                    "username",
                    null,
                    privateKey = keys.private.toPEM(),
                    null,
                    ConnectionMode.Passive,
                    6000,
                ),
            )
            assertTrue(client.isConnected)
            client.disconnect()
            server.stop()
        }
    }

    @Test
    fun `Test sftp connection with DSA`() {
        assertDoesNotThrow {
            val keys = generatePublicKey(keySize = 1024, algorithm = "DSA")
            val server = EmbeddedSftpServer()
            server.start("username", null, clientPublicKey = keys.public, algorithm = "DSA", keySize = 1024)
            val client = ftpClientFactory.createFtpClient(ConnectionType.SFTP) as SFTPClient
            client.authAndConnect(
                ConnectionModel(
                    ConnectionType.SFTP,
                    server.getHost(),
                    server.getPort(),
                    "username",
                    null,
                    privateKey = keys.private.toPEM(),
                    null,
                    ConnectionMode.Passive,
                    6000,
                ),
            )
            assertTrue(client.isConnected)
            client.disconnect()
            server.stop()
        }
    }

    @Test
    fun `Test should fail when sftp connection with DSA with key size greater than 1024`() {
        assertThrows<Exception> {
            val keys = generatePublicKey(keySize = 2048, algorithm = "DSA")
            val server = EmbeddedSftpServer()
            server.start("username", null, clientPublicKey = keys.public, algorithm = "DSA", keySize = 2048)
            val client = ftpClientFactory.createFtpClient(ConnectionType.SFTP) as SFTPClient
            client.authAndConnect(
                ConnectionModel(
                    ConnectionType.SFTP,
                    server.getHost(),
                    server.getPort(),
                    "username",
                    null,
                    privateKey = keys.private.toPEM(),
                    null,
                    ConnectionMode.Passive,
                    6000,
                ),
            )
            assertTrue(client.isConnected)
            client.disconnect()
            server.stop()
        }
    }

    @Test
    fun `Can not connect to sftp with invalid private key`() {
        assertDoesNotThrow {
            val keys = generatePublicKey()
            val server = EmbeddedSftpServer()
            server.start("username", null, clientPublicKey = keys.public)
            val client = ftpClientFactory.createFtpClient(ConnectionType.SFTP) as SFTPClient
            val connectionResult =
                client.authAndConnect(
                    ConnectionModel(
                        ConnectionType.SFTP,
                        server.getHost(),
                        server.getPort(),
                        "username",
                        null,
                        Fake.privateKey(),
                        null,
                        ConnectionMode.Passive,
                        6000,
                    ),
                )
            assertFalse(connectionResult.connected)
        }
    }

    @Test
    fun `Test if fail sftp connection can be handled`() {
        assertThrows<Exception> {
            val server = EmbeddedSftpServer()
            server.start("username", "password")
            val client = ftpClientFactory.createFtpClient(ConnectionType.SFTP) as SFTPClient
            client.authAndConnect(
                ConnectionModel(
                    ConnectionType.SFTP,
                    server.getHost(),
                    server.getPort(),
                    "wrongusername",
                    "password",
                    Fake.privateKey(),
                    null,
                    null,
                    6000,
                ),
            )
            assertTrue(client.isConnected)
            client.disconnect()
            server.stop()
        }
    }

    @Test
    fun `Upload and list file`() {
        val server = EmbeddedSftpServer()
        server.start("username", "password")
        val client = ftpClientFactory.createFtpClient(ConnectionType.SFTP) as SFTPClient
        val fileContent = "This is a test file content."
        val inputStream = fileContent.byteInputStream()
        client.authAndConnect(
            ConnectionModel(
                ConnectionType.SFTP,
                server.getHost(),
                server.getPort(),
                "username",
                "password",
                Fake.privateKey(),
                null,
                null,
                6000,
            ),
        )
        val fileName = UUID.randomUUID().toString()
        client.storeFile(fileName, inputStream)
        val files = client.listFilesInfo(".")
        assertEquals(1, files.size)
        assertEquals(fileName, files.keys.first())
        client.deleteFile(fileName)
        server.stop()
    }

    @Test
    fun `Upload and delete file`() {
        val server = EmbeddedSftpServer()
        server.start("username", "password")
        val client = ftpClientFactory.createFtpClient(ConnectionType.SFTP) as SFTPClient
        val fileContent = "This is a test file content."
        val inputStream = fileContent.byteInputStream()
        client.authAndConnect(
            ConnectionModel(
                ConnectionType.SFTP,
                server.getHost(),
                server.getPort(),
                "username",
                "password",
                Fake.privateKey(),
                null,
                null,
                6000,
            ),
        )
        val fileName = UUID.randomUUID().toString()
        client.storeFile(fileName, inputStream)
        val stream = client.retrieveFileStream(fileName)
        assertNotNull(stream)
        client.deleteFile(fileName)
        assertThrows<Exception> {
            client.retrieveFileStream(fileName)
        }
        assertThrows<Exception> {
            client.deleteFile(fileName)
        }
        server.stop()
    }

    @Test
    fun `Test sftp directories function`() {
        assertDoesNotThrow {
            val server = EmbeddedSftpServer()
            server.start("username", "password")
            val client = ftpClientFactory.createFtpClient(ConnectionType.SFTP) as SFTPClient
            client.authAndConnect(
                ConnectionModel(
                    ConnectionType.SFTP,
                    server.getHost(),
                    server.getPort(),
                    "username",
                    "password",
                    Fake.privateKey(),
                    null,
                    ConnectionMode.Passive,
                    6000,
                ),
            )
            assertTrue(client.isConnected)
            assertNotNull(client.listDirectories("."))
            client.disconnect()
            server.stop()
        }
    }

    @Test
    fun `Test retry connection`() {
        val server = EmbeddedFtpServer()
        val port = getRandomFreePort()
        server.start(
            "username",
            "password",
            ConnectionType.FTPS,
            isImplicit = false,
            certificatePath = "src/test/resources/ftps-test-cert.jks",
            port = port,
        )
        val connectionModel =
            ConnectionModel(
                ConnectionType.FTPS,
                server.getHost(),
                getRandomFreePort(),
                "username",
                "password",
                Fake.privateKey(),
                null,
                ConnectionMode.Passive,
                6000,
                retryBackoffDurationsInSecond = listOf(1000, 2000, 3000),
            )
        val client = ftpClientFactory.createFtpClient(ConnectionType.FTPS, ConnectionVariant.Explicit)
        val spyClient = spy(client)
        val connectionResult = spyClient.authAndConnect(connectionModel)
        assertFalse(connectionResult.connected)
        assertEquals("Connection refused", connectionResult.errors.first().description)

        verify(spyClient, times(4)).connectToServer(connectionModel)
    }

    @Test
    fun `Retry connection should not handle authentication exception`() {
        val server = EmbeddedFtpServer()
        val port = getRandomFreePort()
        server.start(
            "username",
            "password",
            ConnectionType.FTPS,
            isImplicit = false,
            certificatePath = "src/test/resources/ftps-test-cert.jks",
            port = port,
        )
        val connectionModel =
            ConnectionModel(
                ConnectionType.FTPS,
                server.getHost(),
                server.getPort(),
                "wrongusername",
                "wrongpassword",
                Fake.privateKey(),
                null,
                ConnectionMode.Passive,
                6000,
                retryBackoffDurationsInSecond = listOf(1000, 2000, 3000),
            )
        val client = ftpClientFactory.createFtpClient(ConnectionType.FTPS, ConnectionVariant.Explicit)
        val spyClient = spy(client)
        assertThrows<AuthenticationException> {
            spyClient.authAndConnect(connectionModel)
        }
        verify(spyClient, times(1)).connectToServer(connectionModel)
    }

    private fun getRandomFreePort(): Int {
        ServerSocket(0).use { serverSocket ->
            return serverSocket.localPort
        }
    }

    private fun generatePublicKey(
        keySize: Int = 2048,
        algorithm: String = "RSA",
    ): KeyPair {
        val generator = KeyPairGenerator.getInstance(algorithm)
        generator.initialize(keySize)
        return generator.generateKeyPair()
    }

    fun PrivateKey.toPEM(): String {
        val base64Key = Base64.getEncoder().encodeToString(this.encoded)
        return "-----BEGIN PRIVATE KEY-----\n${base64Key.chunked(64).joinToString("\n")}\n-----END PRIVATE KEY-----"
    }
}
