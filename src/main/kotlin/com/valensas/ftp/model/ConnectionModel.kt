package com.valensas.ftp.model

data class ConnectionModel(
    val connectionType: ConnectionType,
    val host: String,
    val port: Int,
    val username: String,
    val password: String? = null,
    val publicKey: String? = null,
    val privateKey: String? = null,
    val variant: ConnectionVariant? = null,
    val connectionTimeout: Int? = null,
    val retryCount: Int? = null,
)
