package dev.switch_case.Java2026_Gitignore_Provider.actions

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import dev.switch_case.Java2026_Gitignore_Provider.GitignoreBundle
import dev.switch_case.Java2026_Gitignore_Provider.services.GitignoreTemplate

class GitignoreFileWriter {
    fun generateContent(templates: List<GitignoreTemplate>): String {
        return templates.joinToString("\n\n") { template ->
            "### ${template.displayName} ###\n${template.content.trimEnd()}"
        } + "\n"
    }

    fun writeGitignore(project: Project, content: String) {
        val projectDir = project.guessProjectDir()
        if (projectDir == null) {
            thisLogger().warn("Could not determine project directory")
            return
        }

        WriteCommandAction.runWriteCommandAction(project, "Generate .gitignore", null, {
            try {
                val existingFile = projectDir.findChild(".gitignore")

                if (existingFile != null) {
                    val existingContent = String(existingFile.contentsToByteArray(), Charsets.UTF_8)
                    val existingLines = existingContent.lines()
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .toSet()

                    val newLines = content.lines().filter { line ->
                        val trimmed = line.trim()
                        trimmed.isBlank() ||
                                trimmed.startsWith("###") ||
                                trimmed !in existingLines
                    }

                    if (newLines.any { it.trim().isNotBlank() && !it.trim().startsWith("###") }) {
                        val separator = if (existingContent.endsWith("\n")) "\n" else "\n\n"
                        val appendContent = separator + newLines.joinToString("\n")
                        existingFile.setBinaryContent(
                            (existingContent + appendContent).toByteArray(Charsets.UTF_8)
                        )
                        showNotification(project, GitignoreBundle.message("notification.updated"))
                    }
                } else {
                    val newFile = projectDir.createChildData(this, ".gitignore")
                    newFile.setBinaryContent(content.toByteArray(Charsets.UTF_8))
                    showNotification(project, GitignoreBundle.message("notification.success"))
                }
            } catch (e: Exception) {
                thisLogger().error("Failed to write .gitignore", e)
            }
        })
    }

    private fun showNotification(project: Project, message: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Gitignore Generator")
            .createNotification(message, NotificationType.INFORMATION)
            .notify(project)
    }
}
