package org.alex.concourserjetbrains;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

public class GoToDeclarationHandler implements GotoDeclarationHandler {

    private static final Logger log = LoggerFactory.getLogger(GoToDeclarationHandler.class);
    private static final String INLINE_PYTHON_RUN = "python3";
    private static final List<String> PYTHON_RESOLVABLE_PARAMS = List.of("SCRIPT_PATH", "run", "py", INLINE_PYTHON_RUN);
    private static final List<String> YAML_RESOLVABLE_PARAMS = List.of("file", "PATH", "FILE");


    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(@Nullable PsiElement sourceElement, int offset, Editor editor) {
        Project project = editor.getProject();

        if (project == null) {
            log.info("Project was null");
            return null;
        }
        Configuration configuration = ConfigurationService.get(editor.getProject()).getConfig();
        String clickedLine = getClickedLine(editor.getDocument(), offset);
        if (!isClickedLineResolvable(clickedLine)) {
            return null;
        }

        if (sourceElement == null) return null;

        var clickedContent = sourceElement.getText();
        System.out.println("ALEX: Clicked content: "+clickedContent);

        String fullPath = resolvePath(clickedLine, clickedContent, configuration);
        System.out.println("ALEX: Full path: "+fullPath);
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

    private String getClickedLine(Document document, int offset) {
        int lineNumber = document.getLineNumber(offset);
        int lineStart = document.getLineStartOffset(lineNumber);
        int lineEnd = document.getLineEndOffset(lineNumber);
        return document.getText(new TextRange(lineStart, lineEnd));
    }

    private boolean isClickedLineResolvable(String clickedLine) {
        List<String> allParams = new ArrayList<>();
        allParams.addAll(YAML_RESOLVABLE_PARAMS);
        allParams.addAll(PYTHON_RESOLVABLE_PARAMS);
        for (String param : allParams) {
            if (clickedLine.contains(param)) {
                return true;
            }
        }
        return false;
    }

    private String resolvePath(String clickedLine, String relativePath, Configuration configuration) {
        if (isPythonPath(clickedLine)) {
            return resolvePythonPath(relativePath, configuration);
        }
        return resolveFilePath(relativePath, configuration);
    }

    private boolean isPythonPath(String path) {
        for (String param: PYTHON_RESOLVABLE_PARAMS) {
            if (path.contains(param)) {
                return true;
            }
        }
        return false;
    }

    private String resolvePythonPath(String relativePath, Configuration configuration) {
        if (relativePath.startsWith(INLINE_PYTHON_RUN)) {
            relativePath = relativePath.replaceFirst(format("%s -m", INLINE_PYTHON_RUN), "");
        }
        String normalisedPath = relativePath.replace(".", "/");
        normalisedPath = normalisedPath.trim();
        normalisedPath += ".py";
        return resolveFilePath(normalisedPath, configuration);
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