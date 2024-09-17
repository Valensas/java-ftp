package com.valensas.ftp.factory

import com.valensas.ftp.model.ConnectionType
import com.valensas.ftp.model.ConnectionVariant
import com.valensas.ftp.model.FTPClient
import com.valensas.ftp.model.FTPSClient
import com.valensas.ftp.model.SFTPClient

class FtpClientFactory {
    fun createFtpClient(
        type: ConnectionType,
        variant: ConnectionVariant? = null,
    ): FTPClient =
        when (type) {
            ConnectionType.FTP -> FTPClient()
            ConnectionType.FTPS -> FTPSClient(variant == ConnectionVariant.Implicit)
            ConnectionType.SFTP -> SFTPClient()
        }
}
