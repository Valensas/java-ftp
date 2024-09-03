package com.valensas.ftp.model

data class ConnectionModel(
    val connectionType: ConnectionType,
    val host: String,
    val port: Int,
    val username: String,
    val password: String,
    val publicKey: String?,
    val privateKey: String?,
    val variant: ConnectionVariant?,
    val connectionTimout: Int?,
    val retryCount: Int?,
)
