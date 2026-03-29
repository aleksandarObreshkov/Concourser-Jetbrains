package org.alex.concourserjetbrains

import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import java.io.File

class ConfigurationLoader : ProjectActivity {
    override suspend fun execute(project: Project) {
        val configFile = File(project.basePath, "concourser.json")
        if (!configFile.exists()) return

        try {
            val raw = configFile.readText()
            val mapper = ObjectMapper()
            val configuration = mapper.readValue(raw, Configuration::class.java)
            ConfigurationService.get(project).config = configuration
        } catch (e: Exception) {
            println("Error while loading configuration from ${configFile.absolutePath}: ${e.message}")
        }
    }
}