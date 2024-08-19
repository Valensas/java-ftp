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
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertTrue

@SpringBootTest
class FtpApplicationTests {
    private val ftpClientFactory = FtpClientFactory()

    @Test
    fun `Test ftp connection`() {
        assertDoesNotThrow {
            val server = EmbeddedFtpServer()
            server.start("username", "password", ConnectionType.FTP)
            val client = ftpClientFactory.createFtpClient(ConnectionType.FTP)
            client.connect("localhost", server.getPort())
            client.login("username", "password")
            server.stop()
        }
    }

    @Test
    fun `Test ftps connection`() {
        assertDoesNotThrow {
            val server = EmbeddedFtpServer()
            server.start("username", "password", ConnectionType.FTPS, isImplicit = true, certificatePath = "src/test/resources/ftps-test-cert.jks")
            val client = ftpClientFactory.createFtpClient(ConnectionType.FTPS, ConnectionVariant.Implicit)
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
                    ConnectionType.FTPS,
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
                    ConnectionType.FTPS,
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
}
