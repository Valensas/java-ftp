package com.valensas.ftp.hints

import com.valensas.nativesupport.hints.HintUtils
import org.slf4j.LoggerFactory
import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.data.util.TypeScanner

class CustomRuntimeHintsRegistrar : RuntimeHintsRegistrar {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun registerHints(
        hints: RuntimeHints,
        classLoader: ClassLoader?,
    ) {
        val packages = listOf("com.valensas.ftp.model")

        logger.info(
            "Setting reflection hints for classes in packages: {}",
            packages.joinToString(", "),
        )

        TypeScanner
            .typeScanner(requireNotNull(classLoader))
            .scanPackages(packages)
            .forEach { clazz ->
                hints
                    .reflection()
                    .registerType(clazz, *MemberCategory.entries.toTypedArray())

                HintUtils.registerSerializationHints(
                    hints,
                    clazz,
                    classLoader,
                )
            }
    }
}
