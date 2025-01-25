package com.olufemithompson.codetori;

import com.google.gson.Gson;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.olufemithompson.codetori.dto.ExplainRequest;
import com.olufemithompson.codetori.dto.ExplainResponse;
import net.sourceforge.plantuml.SourceStringReader;
import net.sourceforge.plantuml.bpm.Col;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;


public class FunctionExplanationAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .callTimeout(60, TimeUnit.SECONDS)
            .build();

    private final Gson gson = new Gson();

    @Override
    public void actionPerformed(@NotNull final AnActionEvent e) {

        // Access the editor and project
        final Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);

        // Access the PSI file (parsed Java code structure)
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (!(psiFile instanceof PsiJavaFile)) {
            Messages.showErrorDialog("This action only works on Java files.", "Error");
            return;
        }

        // Get the caret position
        int caretOffset = editor.getCaretModel().getOffset();

        // Find the method at the caret position
        PsiElement elementAtCaret = psiFile.findElementAt(caretOffset);
        PsiMethod method = findParentMethod(elementAtCaret);

        if (method == null) {
            Messages.showErrorDialog("No method found at the caret position.", "Error");
            return;
        }

        // Show "Loading..." dialog while making the API request
        SwingUtilities.invokeLater(() -> {
            UMLDialog umlDialog = new UMLDialog(method.getName(), null);


            new Thread(() -> {
                try {
                    // Make API call to get UML using Gson
                    ExplainResponse explainResponse = callExplainAPI(method.getText());

                    // Render UML diagram as an image
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    SourceStringReader reader = new SourceStringReader(explainResponse.getUml());
                    reader.generateImage(outputStream);
                    byte[] imageData = outputStream.toByteArray();

                    // Update the dialog with UML diagram
                    SwingUtilities.invokeLater(() -> {
                        umlDialog.updateContent(method.getName(), imageData);
                    });

                } catch (Exception ex) {
                    // Handle errors and close the loading dialog
                    SwingUtilities.invokeLater(() -> {
                        umlDialog.close(DialogWrapper.OK_EXIT_CODE);
                        Messages.showErrorDialog("Failed to fetch UML: " + ex.getMessage(), "Error");
                    });
                }
            }).start();
            umlDialog.show();
        });
    }

    /**
     * Sets visibility and enables this action menu item if:
     * <ul>
     *   <li>a project is open</li>
     *   <li>an editor is active</li>
     * </ul>
     *
     * @param e Event related to this action
     */
    @Override
    public void update(@NotNull final AnActionEvent e) {
        // Get required data keys
        final Project project = e.getProject();
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        // Set visibility only in case of existing project and editor
        e.getPresentation().setEnabledAndVisible(project != null && editor != null);
    }


    /**
     * Finds the nearest parent method for the given element.
     *
     * @param element The PSI element at the caret position.
     * @return The enclosing PsiMethod or null if not found.
     */
    private PsiMethod findParentMethod(PsiElement element) {
        while (element != null && !(element instanceof PsiMethod)) {
            element = element.getParent();
        }
        return (PsiMethod) element;
    }


    /**
     * Makes a POST request to the API to get the UML using Java objects for request and response.
     *
     * @param functionCode The function code to send in the API request.
     * @return The UML response object from the API.
     * @throws IOException If an error occurs during the API call.
     */
    private ExplainResponse callExplainAPI(String functionCode) throws IOException {
        String apiUrl = "http://23.92.20.245:3000/explain";
        // Create the request object
        ExplainRequest requestBody = new ExplainRequest(functionCode);

        // Convert the request object to JSON using Gson
        String jsonPayload = gson.toJson(requestBody);
        // Create the request body
        RequestBody body = RequestBody.create(jsonPayload, MediaType.parse("application/json"));

        // Create the request
        Request request = new Request.Builder()
                .url(apiUrl)
                .post(body)
                .build();

        // Execute the request and get the response
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            // Deserialize the response body to an ExplainResponse object
            return gson.fromJson(response.body().string(), ExplainResponse.class);
        }
    }


    /**
     * Custom dialog for displaying UML diagrams.
     */
    private static class UMLDialog extends DialogWrapper {
        private final JLabel contentLabel;
        private final JPanel panel;
        private byte[] imageData;

        protected UMLDialog(String title, byte[] initialImageData) {
            super(true);
            this.imageData = initialImageData;


            panel = new JPanel(new BorderLayout());
            panel.setBackground(Color.white);

            contentLabel = new JLabel();
            contentLabel.setText("Loading.....");

            if (imageData != null) {
                contentLabel.setText("");
                contentLabel.setIcon(new ImageIcon(imageData));
            }

            JScrollPane scrollPane = new JScrollPane(contentLabel);
            scrollPane.setBackground(Color.white);
            scrollPane.getViewport().setBackground(Color.white);

            scrollPane.setPreferredSize(new Dimension(600, 400));

            panel.add(scrollPane, BorderLayout.CENTER);
            init();
            setTitle(title);
        }

        @Override
        protected @Nullable JComponent createCenterPanel() {
            if (imageData != null) {
                contentLabel.setIcon(new ImageIcon(imageData));
                contentLabel.setText("");
            }
            return panel;
        }

        /**
         * Updates the dialog content with UML diagram and title.
         */
        public void updateContent(String title, byte[] newImageData) {
            this.imageData = newImageData;
            contentLabel.setText("");
            contentLabel.setIcon(new ImageIcon(imageData));
            setTitle(title);
            panel.revalidate();
            panel.repaint();
        }
    }
}