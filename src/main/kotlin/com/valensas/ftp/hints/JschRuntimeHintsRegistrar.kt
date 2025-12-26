package com.valensas.ftp.hints

import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar

class JschRuntimeHintsRegistrar : RuntimeHintsRegistrar {
    override fun registerHints(
        hints: RuntimeHints,
        classLoader: ClassLoader?,
    ) {
        val reflection = hints.reflection()

        hints.resources().registerPattern("com/jcraft/jsch/**")

        listOf(
            "javax.crypto.Cipher",
            "javax.crypto.Mac",
            "javax.crypto.KeyAgreement",
            "java.security.MessageDigest",
            "java.security.KeyPairGenerator",
            "java.security.Signature",
        ).forEach {
            reflection.registerTypeIfPresent(
                classLoader,
                it,
                ::configureForReflection,
            )
        }

        val jschClasses =
            listOf(
                "com.jcraft.jsch.JSch",
                "com.jcraft.jsch.Session",
                "com.jcraft.jsch.ChannelSftp",
                "com.jcraft.jsch.UserAuth",
                "com.jcraft.jsch.UserAuthPassword",
                "com.jcraft.jsch.UserAuthPublicKey",
                "com.jcraft.jsch.UserAuthKeyboardInteractive",
                "com.jcraft.jsch.UserAuthNone",
                "com.jcraft.jsch.UserAuthGSSAPIWithMIC",
                "com.jcraft.jsch.jce.DH",
                "com.jcraft.jsch.jce.Random",
                "com.jcraft.jsch.jce.AES128CBC",
                "com.jcraft.jsch.jce.AES256CBC",
                "com.jcraft.jsch.jce.AES128CTR",
                "com.jcraft.jsch.jce.TripleDESCBC",
                "com.jcraft.jsch.jce.BlowfishCBC",
                "com.jcraft.jsch.jce.HMACSHA1",
                "com.jcraft.jsch.jce.HMACSHA256",
                "com.jcraft.jsch.jce.HMACMD5",
                "com.jcraft.jsch.jce.SHA1",
                "com.jcraft.jsch.jce.SHA256",
                "com.jcraft.jsch.jce.MD5",
                "com.jcraft.jsch.jce.SignatureRSA",
                "com.jcraft.jsch.jce.SignatureDSA",
                "com.jcraft.jsch.jce.KeyPairGenRSA",
                "com.jcraft.jsch.jce.KeyPairGenDSA",
                "com.jcraft.jsch.DHG1",
                "com.jcraft.jsch.DHG14",
                "com.jcraft.jsch.DHGEX",
                "com.jcraft.jsch.DHGEX256",
            )

        jschClasses.forEach {
            reflection.registerTypeIfPresent(
                classLoader,
                it,
                ::configureForReflection,
            )
        }
    }

    private fun configureForReflection(typeHint: org.springframework.aot.hint.TypeHint.Builder) {
        typeHint.withMembers(
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_DECLARED_METHODS,
            MemberCategory.DECLARED_FIELDS,
        )
    }
}
