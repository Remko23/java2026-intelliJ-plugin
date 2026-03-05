package dev.switch_case.Java2026_Gitignore_Provider.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger

data class GitignoreTemplate(
    val name: String,
    val category: String,
    val resourcePath: String,
    val content: String
) {
    val displayName: String
        get() = if (category == "Global") "Global/$name" else name
}

@Service(Service.Level.APP)
class GitignoreTemplateService {

    private val templates: List<GitignoreTemplate> by lazy { loadTemplates() }

    fun getAvailableTemplates(): List<GitignoreTemplate> = templates

    private fun loadTemplates(): List<GitignoreTemplate> {
        val result = mutableListOf<GitignoreTemplate>()

        val languageTemplates = listOf(
            "Android", "Go", "Gradle", "Java", "Kotlin", "Maven",
            "Node", "Python", "Ruby", "Rust", "Scala", "Swift"
        )
        for (name in languageTemplates) {
            val resourcePath = "gitignore_templates/$name.gitignore"
            val content = loadResource(resourcePath)
            if (content != null) {
                result.add(GitignoreTemplate(name, "Language", resourcePath, content))
            }
        }

        val globalTemplates = listOf("JetBrains", "Linux", "macOS", "Windows")
        for (name in globalTemplates) {
            val resourcePath = "gitignore_templates/Global/$name.gitignore"
            val content = loadResource(resourcePath)
            if (content != null) {
                result.add(GitignoreTemplate(name, "Global", resourcePath, content))
            }
        }

        thisLogger().info("Loaded ${result.size} gitignore templates")
        return result
    }

    private fun loadResource(path: String): String? {
        return try {
            javaClass.classLoader?.getResourceAsStream(path)?.bufferedReader()?.readText()
        } catch (e: Exception) {
            thisLogger().warn("Failed to load gitignore template: $path", e)
            null
        }
    }
}
