package com.flower.fxutils;

import com.google.common.base.Preconditions;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;

public class ProgressForm extends VBox {
    final static Logger LOGGER = LoggerFactory.getLogger(ProgressForm.class);

    final Stage stage;
    final Refreshable parent;

    @Nullable @FXML Label messageLabel;
    @Nullable @FXML ProgressBar progressBar;
    @Nullable @FXML Button okButton;
    @Nullable @FXML ProgressOperation progressOperation;

    public ProgressForm(Stage stage, Refreshable parent, ProgressOperation progressOperation) {
        this.stage = stage;
        this.parent = parent;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ProgressForm.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        Task<Integer> task = new Task<Integer>() {
            @Override protected Integer call() throws Exception {
                try {
                    progressOperation.action(this::updateProgress);
                    return 1;
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Error: " + e, ButtonType.OK);
                    LOGGER.error("Error:", e);
                    alert.showAndWait();
                    return 1;
                }
            }

            public void updateProgress(String message, double progress, boolean isDone) {
                updateMessage(message);
                updateProgress(progress, 1.0);
            }
        };

        Preconditions.checkNotNull(messageLabel).textProperty().bind(task.messageProperty());
        Preconditions.checkNotNull(progressBar).progressProperty().bind(task.progressProperty());
        Preconditions.checkNotNull(okButton).disableProperty().bind(Bindings.when(task.progressProperty().greaterThanOrEqualTo(1)).then(false).otherwise(true));

        new Thread(task).start();
    }

    public void updateProgress(String message, double progress, boolean isDone) {
        Preconditions.checkNotNull(messageLabel).setText(message);
        Preconditions.checkNotNull(progressBar).setProgress(progress);
        if (isDone) {
            Preconditions.checkNotNull(okButton).setDisable(false);
        }
    }

    public void close() {
        try {
            stage.close();
            parent.refreshContent();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error: " + e, ButtonType.OK);
            LOGGER.error("Error:", e);
            alert.showAndWait();
        }
    }
}
