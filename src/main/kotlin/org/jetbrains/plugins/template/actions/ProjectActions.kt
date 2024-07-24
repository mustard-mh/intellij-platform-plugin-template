package org.jetbrains.plugins.template.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.task.ProjectTaskManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.concurrency.Promise
import org.jetbrains.concurrency.await

class ProjectBuildAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: throw Exception("Project not found")
        performBuildAction("ProjectBuildAction", project) {
            it.buildAllModules()
        }
    }
}

class ProjectRebuildAllModulesAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: throw Exception("Project not found")
        performBuildAction("ProjectRebuildAllModulesAction", project) {
            it.rebuildAllModules()
        }
    }
}

private fun performBuildAction(
    key: String,
    project: Project,
    buildAction: (ProjectTaskManager) -> Promise<ProjectTaskManager.Result>
) {
    GlobalScope.launch {
        try {
            thisLogger().warn("$key: action triggered >>>>>>>>>>>>>>>>")
            val result = buildAction(ProjectTaskManager.getInstance(project)).await()
            if (result.hasErrors()) {
                thisLogger().error("$key: build failed with errors <<<<<<<<<<<<<<")
            } else {
                thisLogger().warn("$key: build succeeded <<<<<<<<<<<<<<")
            }
        } catch (ex: Exception) {
            thisLogger().error("$key: build failed with exception <<<<<<<<<<<<<<", ex)
        }
    }
}