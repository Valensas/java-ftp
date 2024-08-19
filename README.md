# Java ftp
This library facilitates the creation of various FTP clients, including FTP, FTPS, and SFTP. It also provides an embedded server classes that supports all three types of FTP.

## Usage

### Clients
For creating ftp clients you can use FTPClientFactory. While creating ftps client you have two option implicit or explicit. If you do not give variant then by default ftps client will create connection explicitly.
```kotlin
  val ftpClientFactory = FtpClientFactory()
  val ftpClient = ftpClientFactory.createFtpClient(ConnectionType.FTP)
  val ftpsClient = ftpClientFactory.createFtpClient(ConnectionType.FTPS, ConnectionVariant.Implicit) // If you do not provide variant by default it will be explicit
  val sftpClient = ftpClientFactory.createFtpClient(ConnectionType.SFTP) as SFTPClient
```

### Embedded Servers
There is two embedded server classes: EmbeddedFtpServer and EmbeddedSftpServer.

* You can use EmbeddedFtpServer for both ftp and ftps. 
```kotlin
// Ftp server
val server = EmbeddedFtpServer()
server.start("username", "password", ConnectionType.FTP)
server.stop()

// Ftps server
val server = EmbeddedFtpServer()
server.start("username", "password", ConnectionType.FTPS, isImplicit = true, certificatePath = "path")
server.stop()
```
* For sftp connection you can use EmbeddedSftpServer()
```kotlin
val server = EmbeddedSftpServer()
server.start("username", "password")
server.stop()
```
