package dev.switch_case.Java2026_Gitignore_Provider.analysis

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem

class ProjectAnalyzer {

    companion object {
        private val FILE_TO_TEMPLATES: Map<String, List<String>> = mapOf(
            "build.gradle" to listOf("Gradle", "Java"),
            "build.gradle.kts" to listOf("Gradle", "Kotlin"),
            "pom.xml" to listOf("Maven", "Java"),
            "package.json" to listOf("Node"),
            "requirements.txt" to listOf("Python"),
            "setup.py" to listOf("Python"),
            "pyproject.toml" to listOf("Python"),
            "Pipfile" to listOf("Python"),
            "Cargo.toml" to listOf("Rust"),
            "go.mod" to listOf("Go"),
            "build.sbt" to listOf("Scala"),
            "Package.swift" to listOf("Swift"),
            "Gemfile" to listOf("Ruby"),
            "AndroidManifest.xml" to listOf("Android"),
        )
    }

    fun analyzeProject(project: Project): List<String> {
        val recommended = mutableSetOf<String>()

        recommended.add("Global/JetBrains")

        val basePath = project.basePath ?: return recommended.toList()
        val projectDir = LocalFileSystem.getInstance().findFileByPath(basePath)
            ?: return recommended.toList()


        for ((fileName, templates) in FILE_TO_TEMPLATES) {
            if (projectDir.findChild(fileName) != null) {
                recommended.addAll(templates)
            }
            if (fileName == "AndroidManifest.xml") {
                val appSrcMain = projectDir.findChild("app")
                    ?.findChild("src")
                    ?.findChild("main")
                if (appSrcMain?.findChild(fileName) != null) {
                    recommended.addAll(templates)
                }
            }
        }

        return recommended.toList().sorted()
    }
}
