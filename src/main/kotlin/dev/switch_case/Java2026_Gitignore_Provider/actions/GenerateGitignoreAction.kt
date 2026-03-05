package dev.switch_case.Java2026_Gitignore_Provider.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import dev.switch_case.Java2026_Gitignore_Provider.analysis.ProjectAnalyzer
import dev.switch_case.Java2026_Gitignore_Provider.services.GitignoreTemplateService
import dev.switch_case.Java2026_Gitignore_Provider.ui.GitignoreDialog

class GenerateGitignoreAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val templateService = service<GitignoreTemplateService>()
        val allTemplates = templateService.getAvailableTemplates()

        val analyzer = ProjectAnalyzer()
        val recommendedNames = analyzer.analyzeProject(project)

        val dialog = GitignoreDialog(project, allTemplates, recommendedNames)
        if (dialog.showAndGet()) {
            val selectedTemplates = dialog.getSelectedTemplates()
            if (selectedTemplates.isNotEmpty()) {
                val writer = GitignoreFileWriter()
                val content = writer.generateContent(selectedTemplates)
                writer.writeGitignore(project, content)
            }
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}
