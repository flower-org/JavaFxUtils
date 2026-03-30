package com.flower.fxutils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class JavaFxUtils {
  public final static KeyCodeCombination KEY_CODE_COPY1 = new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY);
  public final static KeyCodeCombination KEY_CODE_COPY2 = new KeyCodeCombination(KeyCode.INSERT, KeyCombination.CONTROL_ANY);
  public final static KeyCodeCombination KEY_CODE_COPY3 = new KeyCodeCombination(KeyCode.C, KeyCombination.META_DOWN);

  public final static ButtonType BUTTON_TYPE_YES = new ButtonType("Yes");
  public final static ButtonType BUTTON_TYPE_NO = new ButtonType("No");

  public enum YesNo {
    YES,
    NO
  }

  public static final UnaryOperator<TextFormatter.Change> DECIMAL_TEXT_FILTER = change -> {
    String newText = change.getControlNewText();
    if (newText.matches("\\d*([.,]\\d*)?")) {
      return change;
    }
    return null;
  };

  public static void autoResizeTableColumns(TableView<?> table) {
    //Set the right policy
    table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
    table.getColumns().stream().forEach((column) -> {
      //Minimal width = columnheader
      Text t = new Text(column.getText());
      double max = t.getLayoutBounds().getWidth();
      for (int i = 0; i < table.getItems().size(); i++) {
        //cell must not be empty
        if (column.getCellData(i) != null) {
          t = new Text(column.getCellData(i).toString());
          double calcwidth = t.getLayoutBounds().getWidth();
          //remember new max-width
          if (calcwidth > max) {
            max = calcwidth;
          }
        }
      }
      //set the new max-widht with some extra space
      column.setPrefWidth(max + 10.0d);
    });
  }

  public static TextFormatter<TextFormatter.Change> createDecimalTextFormatter() {
    return new TextFormatter<>(DECIMAL_TEXT_FILTER);
  }

  public static YesNo showYesNoDialog(String titleHeaderContext) {
    return showYesNoDialog(titleHeaderContext, titleHeaderContext);
  }

  public static YesNo showYesNoDialog(String titleHeader, String context) {
    return showYesNoDialog(titleHeader, titleHeader, context);
  }

  public static YesNo showYesNoDialog(String title, String header, String context) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle(title);
    alert.setHeaderText(header);
    alert.setContentText(context);

    // Customize the buttons (Yes/No)
    alert.getButtonTypes().setAll(BUTTON_TYPE_YES, BUTTON_TYPE_NO);
    Optional<ButtonType> dialogResult = alert.showAndWait();
    if (dialogResult.isPresent()) {
      if (dialogResult.get() == BUTTON_TYPE_YES) {
        return YesNo.YES;
      }
    }
    return YesNo.NO;
  }

  public static void showMessage(String titleHeaderContext) {
    showMessage(titleHeaderContext, titleHeaderContext);
  }

  public static void showErrorMessage(String titleHeaderContext) {
    showMessage(titleHeaderContext, Alert.AlertType.ERROR);
  }

  public static void showMessage(String titleHeaderContext, Alert.AlertType alertType) {
    showMessage(titleHeaderContext, titleHeaderContext, titleHeaderContext, alertType);
  }

  public static void showMessage(String titleHeader, String context) {
    showMessage(titleHeader, titleHeader, context);
  }

  public static void showMessage(String title, String header, String context) {
    showMessage(title, header, context, Alert.AlertType.INFORMATION);
  }

  public static void showMessage(String title, String header, String context, Alert.AlertType alertType) {
    Alert alert = new Alert(alertType);
    alert.setTitle(title);
    alert.setHeaderText(header);
    alert.setContentText(context);

    alert.showAndWait();
  }

  public static @Nullable String showCustomDialog(String title, String header, String context, String... options) {
    List<ButtonType> buttonTypeList = new ArrayList<>();
    for (String option : options) {
      ButtonType buttonType = new ButtonType(option);
      buttonTypeList.add(buttonType);
    }

    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle(title);
    alert.setHeaderText(header);
    alert.setContentText(context);

    // Init custom buttons
    alert.getButtonTypes().setAll(buttonTypeList);
    Optional<ButtonType> dialogResult = alert.showAndWait();
    return dialogResult
            .map(ButtonType::getText)
            .orElse(null);
  }

  public static boolean isOnKeyPressedCtrlC(KeyEvent event) {
    return KEY_CODE_COPY1.match(event) || KEY_CODE_COPY2.match(event) || KEY_CODE_COPY3.match(event);
  }

  public static <T> void onKeyPressedCtrlC(KeyEvent event, TableView<T> tableView) {
    if (isOnKeyPressedCtrlC(event)) {
      StringBuilder itemsBuilder = new StringBuilder();
      for (Object selectedItem : tableView.getSelectionModel().getSelectedItems()) {
        StringBuilder itemBuilder = new StringBuilder();
        for (TableColumn column : tableView.getColumns()) {
          Object cellData = column.getCellData(selectedItem);
          String cellValue = cellData == null ? "" : cellData.toString();
          if (!itemBuilder.isEmpty()) { itemBuilder.append(" | "); }
          itemBuilder.append(cellValue);
        }

        if (!itemsBuilder.isEmpty()) { itemsBuilder.append("\n"); }
        itemsBuilder.append(itemBuilder);
      }

      copyToClipboard(itemsBuilder.toString());
    }
  }

  public static <T> void onKeyPressedCtrlC(KeyEvent event, TreeTableView<T> tableView) {
    if (isOnKeyPressedCtrlC(event)) {
      StringBuilder itemsBuilder = new StringBuilder();
      for (Object selectedItem : tableView.getSelectionModel().getSelectedItems()) {
        StringBuilder itemBuilder = new StringBuilder();
        for (TreeTableColumn column : tableView.getColumns()) {
          Object cellData = column.getCellData(selectedItem);
          String cellValue = cellData == null ? "" : cellData.toString();
          if (!itemBuilder.isEmpty()) { itemBuilder.append(" | "); }
          itemBuilder.append(cellValue);
        }

        if (!itemsBuilder.isEmpty()) { itemsBuilder.append("\n"); }
        itemsBuilder.append(itemBuilder);
      }

      copyToClipboard(itemsBuilder.toString());
    }
  }

  public static <T> void onKeyPressedCtrlC(KeyEvent event, TreeView<T> treeView) {
    if (isOnKeyPressedCtrlC(event)) {
      StringBuilder itemsBuilder = new StringBuilder();
      for (TreeItem selectedItem : treeView.getSelectionModel().getSelectedItems()) {
        if (!itemsBuilder.isEmpty()) { itemsBuilder.append("\n"); }
        itemsBuilder.append(selectedItem == null || selectedItem.valueProperty().get() == null ? "" : selectedItem.valueProperty().get().toString());
      }

      copyToClipboard(itemsBuilder.toString());
    }
  }

  public static void copyToClipboard(String text) {
    final ClipboardContent clipboardContent = new ClipboardContent();
    clipboardContent.putString(text);
    Clipboard.getSystemClipboard().setContent(clipboardContent);
  }

  //TODO: unit test coverage
  @Nullable
  public static <T> Pair<TreeItem<T>, Boolean> filterTree(Function<T, Boolean> shouldKeepRecord,
                                                          @Nullable BiFunction<T, Boolean, T> markDirectMatchRecords,
                                                          TreeItem<T> root,
                                                          boolean parentMatched) {
    boolean isMatch = shouldKeepRecord.apply(root.getValue());
    TreeItem<T> filteredRoot = markDirectMatchRecords == null
      ? new TreeItem<>(root.getValue())
      : new TreeItem<>(markDirectMatchRecords.apply(root.getValue(), isMatch));

    boolean expand = false;
    List<TreeItem<T>> childList = new ArrayList<>();
    for (TreeItem<T> child : root.getChildren()) {
      Pair<TreeItem<T>, Boolean> childRecord = filterTree(shouldKeepRecord, markDirectMatchRecords, child, isMatch || parentMatched);
      if (childRecord != null) {
        if (parentMatched || isMatch || childRecord.getValue()) {
          childList.add(childRecord.getKey());
        }
        if (childRecord.getValue()) {
          expand = true;
        }
      }
    }

    if (!isMatch && !parentMatched && childList.isEmpty()) {
      return null;
    } else {
      filteredRoot.getChildren().addAll(childList);
      filteredRoot.setExpanded(expand);

      return Pair.of(filteredRoot, expand || isMatch);
    }
  }
}
