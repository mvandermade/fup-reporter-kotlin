package com.example.stamp.reporter

import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules
import org.springframework.modulith.docs.Documenter

class DocumentationTest {
    var modules: ApplicationModules = ApplicationModules.of(StampReporterApplication::class.java)

    @Test
    fun writeDocumentationSnippets() {
        Documenter(modules)
            .writeModulesAsPlantUml()
            .writeIndividualModulesAsPlantUml()

        println("Check build/spring-modulith-docs")
    }
}
