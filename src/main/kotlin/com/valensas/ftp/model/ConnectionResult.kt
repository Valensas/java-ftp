package com.valensas.ftp.model

data class ConnectionResult(
    val connected: Boolean,
    val retryCount: Int = 0,
    val errors: Set<Error> = emptySet(),
) {
    class Error(
        val description: String,
        val count: Int,
    )
}
