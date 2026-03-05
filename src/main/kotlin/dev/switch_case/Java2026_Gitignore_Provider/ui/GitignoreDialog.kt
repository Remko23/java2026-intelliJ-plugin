package dev.switch_case.Java2026_Gitignore_Provider.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.SearchTextField
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import dev.switch_case.Java2026_Gitignore_Provider.GitignoreBundle
import dev.switch_case.Java2026_Gitignore_Provider.services.GitignoreTemplate
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import javax.swing.*
import javax.swing.event.DocumentEvent

class GitignoreDialog(
    project: Project,
    private val allTemplates: List<GitignoreTemplate>,
    private val recommendedNames: List<String>
) : DialogWrapper(project, true) {

    private val checkBoxMap = linkedMapOf<GitignoreTemplate, JCheckBox>()
    private val previewArea = JTextArea()
    private val checkBoxPanel = JPanel()

    init {
        title = GitignoreBundle.message("dialog.title")
        init()
    }

    override fun createCenterPanel(): JComponent {
        val mainPanel = JPanel(BorderLayout())
        mainPanel.preferredSize = Dimension(800, 500)

        val leftPanel = JPanel(BorderLayout(0, 8))
        leftPanel.border = BorderFactory.createEmptyBorder(0, 0, 0, 8)

        val searchField = SearchTextField(false)
        searchField.textEditor.emptyText.setText(GitignoreBundle.message("dialog.search.placeholder"))
        searchField.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                filterTemplates(searchField.text)
            }
        })
        leftPanel.add(searchField, BorderLayout.NORTH)

        checkBoxPanel.layout = BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS)
        buildCheckBoxList("")
        val scrollPane = JBScrollPane(checkBoxPanel)
        scrollPane.horizontalScrollBarPolicy = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        leftPanel.add(scrollPane, BorderLayout.CENTER)

        val rightPanel = JPanel(BorderLayout(0, 8))
        val previewLabel = JBLabel(GitignoreBundle.message("dialog.preview.header"))
        previewLabel.font = previewLabel.font.deriveFont(Font.BOLD)
        rightPanel.add(previewLabel, BorderLayout.NORTH)

        previewArea.isEditable = false
        previewArea.font = Font("Monospaced", Font.PLAIN, 12)
        previewArea.lineWrap = true
        previewArea.wrapStyleWord = true
        val previewScroll = JBScrollPane(previewArea)
        rightPanel.add(previewScroll, BorderLayout.CENTER)

        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel)
        splitPane.dividerLocation = 300
        splitPane.resizeWeight = 0.4

        mainPanel.add(splitPane, BorderLayout.CENTER)

        updatePreview()

        return mainPanel
    }

    private fun buildCheckBoxList(filter: String) {
        checkBoxPanel.removeAll()
        checkBoxMap.clear()

        val lowerFilter = filter.lowercase()

        val recommended = allTemplates.filter { it.displayName in recommendedNames }
        val others = allTemplates.filter { it.displayName !in recommendedNames }

        val filteredRecommended = recommended.filter {
            lowerFilter.isBlank() || it.displayName.lowercase().contains(lowerFilter)
        }
        if (filteredRecommended.isNotEmpty()) {
            val header = JBLabel("▸ ${GitignoreBundle.message("dialog.templates.header")} ${GitignoreBundle.message("dialog.recommended")}")
            header.font = header.font.deriveFont(Font.BOLD, 13f)
            header.border = BorderFactory.createEmptyBorder(4, 4, 4, 4)
            header.alignmentX = Component.LEFT_ALIGNMENT
            checkBoxPanel.add(header)

            for (template in filteredRecommended) {
                addCheckBox(template, checked = true)
            }
        }

        val filteredOthers = others.filter {
            lowerFilter.isBlank() || it.displayName.lowercase().contains(lowerFilter)
        }
        if (filteredOthers.isNotEmpty()) {
            val header = JBLabel("▸ ${GitignoreBundle.message("dialog.templates.header")}")
            header.font = header.font.deriveFont(Font.BOLD, 13f)
            header.border = BorderFactory.createEmptyBorder(12, 4, 4, 4)
            header.alignmentX = Component.LEFT_ALIGNMENT
            checkBoxPanel.add(header)

            for (template in filteredOthers.sortedBy { it.displayName }) {
                addCheckBox(template, checked = false)
            }
        }

        checkBoxPanel.revalidate()
        checkBoxPanel.repaint()
    }

    private fun addCheckBox(template: GitignoreTemplate, checked: Boolean) {
        val checkBox = JCheckBox(template.displayName, checked)
        checkBox.border = BorderFactory.createEmptyBorder(2, 16, 2, 4)
        checkBox.alignmentX = Component.LEFT_ALIGNMENT
        checkBox.addActionListener { updatePreview() }
        checkBoxMap[template] = checkBox
        checkBoxPanel.add(checkBox)
    }

    private fun filterTemplates(query: String) {
        buildCheckBoxList(query)
        updatePreview()
    }

    private fun updatePreview() {
        val selected = getSelectedTemplates()
        if (selected.isEmpty()) {
            previewArea.text = "# Select templates from the list to preview the .gitignore content"
        } else {
            previewArea.text = selected.joinToString("\n\n") { template ->
                "### ${template.displayName} ###\n${template.content.trimEnd()}"
            } + "\n"
        }
        previewArea.caretPosition = 0
    }

    fun getSelectedTemplates(): List<GitignoreTemplate> {
        return checkBoxMap.entries
            .filter { (_, checkBox) -> checkBox.isSelected }
            .map { (template, _) -> template }
    }
}
