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
import org.jetbrains.annotations.Nullable;

import static java.lang.String.format;


public class GoToDeclarationHandler implements GotoDeclarationHandler {

    @Override
    public PsiElement @Nullable [] getGotoDeclarationTargets(@Nullable PsiElement sourceElement, int offset, Editor editor) {
        Project project = editor.getProject();
        if (sourceElement == null) return null;
        var text = sourceElement.getText();
        var node = sourceElement.getNode();
        String type = "";
        if (node != null) {
            type = node.getElementType().toString();
        }
        var language = sourceElement.getLanguage();


        text = text.replace("/", "\\");
        text = text.replace("source-code", "");
        text = "C:\\Users\\aleks\\Projects\\pipeline"+text;
        VirtualFile virtualFile = LocalFileSystem.getInstance()
                .findFileByPath(text);
        System.out.println(format("Clicked element: %s. Type: %s. Language: %s", text, type, language));

        ApplicationManager.getApplication().invokeLater(() -> {
            if (virtualFile != null) {
                // 2. Open it in the editor
                FileEditorManager.getInstance(project).openFile(virtualFile, true);
            }
        });
        return null;
    }
}