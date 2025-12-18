package com.valensas.ftp.autoconfigure

import com.valensas.ftp.hints.CustomRuntimeHintsRegistrar
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.ImportRuntimeHints

@Configuration
@ImportRuntimeHints(CustomRuntimeHintsRegistrar::class)
class RuntimeHintsAutoConfiguration
