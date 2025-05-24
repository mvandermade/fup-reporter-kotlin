package com.example.stamp.reporter

import org.springframework.modulith.core.ApplicationModules
import org.springframework.modulith.docs.Documenter
import kotlin.test.Test

class StampReporterModularityTest {
    var modules: ApplicationModules = ApplicationModules.of(StampReporterApplication::class.java)

    @Test
    fun verifiesModularStructure() {
        modules.verify()
    }

    @Test
    fun createModuleDocumentation() {
        Documenter(modules).writeDocumentation()
    }
}
