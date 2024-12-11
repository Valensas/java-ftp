package com.valensas.ftp.model

data class ConnectionModel(
    val connectionName: String,
    val connectionType: ConnectionType,
    val host: String,
    val port: Int,
    val username: String,
    val password: String? = null,
    val privateKey: String? = null,
    val variant: ConnectionVariant? = null,
    val connectionMode: ConnectionMode? = null,
    val connectionTimeout: Int? = null,
    val strictHostKeyChecking: String = "no",
    val retryBackoffDurationsInSecond: List<Int> = listOf(0),
)
