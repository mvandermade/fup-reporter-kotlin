package com.example.postzegelreporter

import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules
import org.springframework.modulith.docs.Documenter

class DocumentationTest {
    var modules: ApplicationModules = ApplicationModules.of(PostzegelReporterApplication::class.java)

    @Test
    fun writeDocumentationSnippets() {
        Documenter(modules)
            .writeModulesAsPlantUml()
            .writeIndividualModulesAsPlantUml()

        println("Check build/spring-modulith-docs")
    }
}
