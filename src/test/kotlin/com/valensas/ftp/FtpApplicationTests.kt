package com.valensas.ftp

import com.valensas.ftp.factory.FtpClientFactory
import com.valensas.ftp.model.ConnectionModel
import com.valensas.ftp.model.ConnectionType
import com.valensas.ftp.model.ConnectionVariant
import com.valensas.ftp.model.SFTPClient
import com.valensas.ftp.server.EmbeddedFtpServer
import com.valensas.ftp.server.EmbeddedSftpServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import java.net.ServerSocket
import java.nio.file.Files
import java.util.UUID
import kotlin.io.path.Path
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@SpringBootTest
class FtpApplicationTests {
    private val ftpClientFactory = FtpClientFactory()

    companion object {
        @BeforeAll
        @JvmStatic
        fun createDir() {
            Files.createDirectories(Path("src/test/resources/files/"))
        }

        @AfterAll
        @JvmStatic
        fun cleanup() {
            Files.deleteIfExists(Path("src/test/resources/files/"))
        }
    }

    @Test
    fun `Test ftp connection`() {
        assertDoesNotThrow {
            val server = EmbeddedFtpServer()
            server.start("username", "password", ConnectionType.FTP, port = getRandomFreePort())
            val client = ftpClientFactory.createFtpClient(ConnectionType.FTP)
            client.connect("localhost", server.getPort())
            client.login("username", "password")
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
            val client = ftpClientFactory.createFtpClient(ConnectionType.FTPS, ConnectionVariant.Implicit)
            client.connect("localhost", server.getPort())
            client.login("username", "password")
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
            val client = ftpClientFactory.createFtpClient(ConnectionType.FTPS, ConnectionVariant.Explicit)
            client.connect("localhost", server.getPort())
            client.login("username", "password")
            server.stop()
        }
    }

    @Test
    fun `Test sftp connection`() {
        assertDoesNotThrow {
            val server = EmbeddedSftpServer()
            server.start("username", "password")
            val client = ftpClientFactory.createFtpClient(ConnectionType.SFTP) as SFTPClient
            client.connect(
                ConnectionModel(
                    ConnectionType.SFTP,
                    server.getHost(),
                    server.getPort(),
                    "username",
                    "password",
                    Fake.publicKey(),
                    Fake.privateKey(),
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
            client.connect(
                ConnectionModel(
                    ConnectionType.SFTP,
                    server.getHost(),
                    server.getPort(),
                    "wrongusername",
                    "password",
                    Fake.publicKey(),
                    Fake.privateKey(),
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
        client.connect(
            ConnectionModel(
                ConnectionType.SFTP,
                server.getHost(),
                server.getPort(),
                "username",
                "password",
                Fake.publicKey(),
                Fake.privateKey(),
                null,
            ),
        )
        val pathname = "src/test/resources/files/"
        val fileName = UUID.randomUUID().toString()
        client.uploadFile(inputStream, pathname + fileName)
        val files = client.listFilesAtPath(pathname)
        assertEquals(fileName, files[files.size - 1].filename)
        client.deleteFile(pathname + fileName)
        server.stop()
    }

    @Test
    fun `Upload and delete file`() {
        val server = EmbeddedSftpServer()
        server.start("username", "password")
        val client = ftpClientFactory.createFtpClient(ConnectionType.SFTP) as SFTPClient
        val fileContent = "This is a test file content."
        val inputStream = fileContent.byteInputStream()
        client.connect(
            ConnectionModel(
                ConnectionType.SFTP,
                server.getHost(),
                server.getPort(),
                "username",
                "password",
                Fake.publicKey(),
                Fake.privateKey(),
                null,
            ),
        )
        val pathname = "src/test/resources/files/"
        val fileName = UUID.randomUUID().toString()
        client.uploadFile(inputStream, pathname + fileName)
        val stream = client.retrieveFileStream(pathname + fileName)
        assertNotNull(stream)
        client.deleteFile(pathname + fileName)
        assertThrows<Exception> {
            client.retrieveFileStream(pathname + fileName)
        }
        assertFalse(client.deleteFile(pathname + fileName))
        server.stop()
    }

    private fun getRandomFreePort(): Int {
        ServerSocket(0).use { serverSocket ->
            return serverSocket.localPort
        }
    }
}
