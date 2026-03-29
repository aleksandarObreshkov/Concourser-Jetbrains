package org.alex.concourserjetbrains;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import groovy.util.logging.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.Map;

import static java.lang.String.format;

public class GoToDeclarationHandler implements GotoDeclarationHandler {

    private static final Logger log = LoggerFactory.getLogger(GoToDeclarationHandler.class);

    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(@Nullable PsiElement sourceElement, int offset, Editor editor) {
        Project project = editor.getProject();

        if (project == null) {
            log.info("Project was null");
            return null;
        }
        Configuration configuration = ConfigurationService.get(editor.getProject()).getConfig();

        if (sourceElement == null) return null;
        var text = sourceElement.getText();
        String fullPath = resolveFilePath(text, configuration);
        VirtualFile virtualFile = LocalFileSystem.getInstance()
                .findFileByPath(fullPath);

        ApplicationManager.getApplication().invokeLater(() -> {
            if (virtualFile != null) {
                // 2. Open it in the editor
                FileEditorManager.getInstance(project).openFile(virtualFile, true);
            }
        });
        return null;
    }

    private String resolveFilePath(String relativePath, Configuration configuration) {
        Pair<String, String> pathPair = getResourcePathForRelativePath(relativePath, configuration.resources);
        relativePath = relativePath.replaceFirst(pathPair.getKey(), "");
        String[] remainderParts = relativePath.split("/");

        return Paths.get(pathPair.getValue(), remainderParts).toString();
    }

    private Pair<String, String> getResourcePathForRelativePath(String relativePath, Map<String, String> resources) {
        for (Map.Entry<String, String> pair : resources.entrySet()) {
            if (relativePath.startsWith(pair.getKey())) {
                return Pair.of(pair.getKey(), pair.getValue());
            }
        }

        throw new IllegalArgumentException(format("Relative path %s is not resolvable from 'resources'", relativePath));
    }
}