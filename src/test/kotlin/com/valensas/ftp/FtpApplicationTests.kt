package com.valensas.ftp

import com.valensas.ftp.factory.FtpClientFactory
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
import java.util.UUID
import javax.naming.AuthenticationException
import kotlin.test.assertEquals
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
                    6000,
                    null,
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
                    6000,
                    null,
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
                    6000,
                    null,
                )
            val client = ftpClientFactory.createFtpClient(ConnectionType.FTPS, ConnectionVariant.Explicit)
            client.authAndConnect(connectionModel)
            server.stop()
        }
    }

    @Test
    fun `Test sftp connection`() {
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
                    6000,
                    null,
                ),
            )
            assertTrue(client.isConnected)
            client.disconnect()
            server.stop()
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
                    null,
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
                null,
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
                null,
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
                    6000,
                    null,
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
                6000,
                null,
            )
        val client = ftpClientFactory.createFtpClient(ConnectionType.FTPS, ConnectionVariant.Explicit)
        client.retryConnectionTimeouts = listOf(1, 2, 3)
        val spyClient = spy(client)
        spyClient.authAndConnect(connectionModel)
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
                6000,
                null,
            )
        val client = ftpClientFactory.createFtpClient(ConnectionType.FTPS, ConnectionVariant.Explicit)
        client.retryConnectionTimeouts = listOf(1, 2, 3)
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
}
