package com.example.postzegelreporter

import org.springframework.modulith.core.ApplicationModules
import org.springframework.modulith.docs.Documenter
import kotlin.test.Test

class PostzegelReporterModularityTest {
    var modules: ApplicationModules = ApplicationModules.of(PostzegelReporterApplication::class.java)

    @Test
    fun verifiesModularStructure() {
        modules.verify()
    }

    @Test
    fun createModuleDocumentation() {
        Documenter(modules).writeDocumentation()
    }
}
