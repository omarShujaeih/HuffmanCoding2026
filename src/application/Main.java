package application;

import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;

public class Main extends Application {

    private File selectedFile;
    private File compressedFile;

    private final Label lblFile = new Label("No file selected");
    private final Button btnCompress = new Button("Compress");
    private final Button btnDecompress = new Button("Decompress");
    private final Button btnBrowse = new Button("Browse File");
    private final Button btnClear = new Button("Clear Log");

    private final TextArea txtLog = new TextArea();

    private final TableView<HuffmanData> tbl = new TableView<>();
    private final TableColumn<HuffmanData, String> colByte = new TableColumn<>("Byte (0..255)");
    private final TableColumn<HuffmanData, String> colChar = new TableColumn<>("Char");
    private final TableColumn<HuffmanData, Number> colFreq = new TableColumn<>("Frequency");
    private final TableColumn<HuffmanData, String> colCode = new TableColumn<>("Huffman Code");

    @Override
    public void start(Stage stage) {

    	Label title = new Label("Huffman Coding - Compress / Decompress");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: 800;");

        Label sub = new Label("Choose file -> Compress -> View Header/Codes -> Decompress -> Verify");
        sub.setStyle("-fx-font-size: 13px;");

        VBox header = new VBox(6, title, sub);
        header.setPadding(new Insets(16));
        header.setStyle(
                "-fx-background-color: linear-gradient(to right, #f9fafb, #ecfdf5);" +
                "-fx-border-color: #d1d5db;" +
                "-fx-border-width: 0 0 1 0;"
        );

        title.setStyle(title.getStyle() + "-fx-text-fill:#065f46;");
        sub.setStyle(sub.getStyle() + "-fx-text-fill:#374151;");

        // ---- controls row ----
        btnCompress.setDisable(true);
        btnDecompress.setDisable(true);

        btnBrowse.setOnAction(e -> onBrowse(stage));
        btnCompress.setOnAction(e -> onCompress());
        btnDecompress.setOnAction(e -> onDecompress());
        btnClear.setOnAction(e -> { txtLog.clear(); log("Cleared."); });

        stylePrimary(btnBrowse);
        stylePrimary(btnCompress);
        stylePrimary(btnDecompress);
        styleLink(btnClear);

        lblFile.setStyle("-fx-text-fill:#111827; -fx-font-weight:600;");

        HBox row1 = new HBox(10, btnBrowse, lblFile);
        row1.setAlignment(Pos.CENTER_LEFT);

        HBox row2 = new HBox(10, btnCompress, btnDecompress, btnClear);
        row2.setAlignment(Pos.CENTER_LEFT);

        // ---- table ----
        colByte.setPrefWidth(120);
        colChar.setPrefWidth(90);
        colFreq.setPrefWidth(120);
        colCode.setPrefWidth(360);

        colByte.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getByteUnsigned())));
        colChar.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPrintableChar()));
        colFreq.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getFrequency()));
        colCode.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCode()));

        tbl.getColumns().addAll(colByte, colChar, colFreq, colCode);
        tbl.setPrefHeight(230);

        tbl.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #e5e7eb;" +
                "-fx-border-radius: 10;" +
                "-fx-background-radius: 10;"
        );

        Label codesTitle = new Label("Huffman Codes Table");
        codesTitle.setStyle("-fx-text-fill:#065f46; -fx-font-size:16px; -fx-font-weight:800;");

        Label logTitle = new Label("Log / Header Display");
        logTitle.setStyle("-fx-text-fill:#065f46; -fx-font-size:16px; -fx-font-weight:800;");

        txtLog.setWrapText(true);
        txtLog.setPrefHeight(260);
        txtLog.setStyle(
                "-fx-control-inner-background:#ffffff;" +
                "-fx-text-fill:#111827;" +
                "-fx-border-color:#e5e7eb;" +
                "-fx-border-radius:10;" +
                "-fx-background-radius:10;"
        );

        VBox center = new VBox(12, row1, row2, codesTitle, tbl, logTitle, txtLog);
        center.setPadding(new Insets(18));
        center.setStyle("-fx-background-color:#f3f4f6;"); // light gray

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(center);

        Scene scene = new Scene(root, 800, 700);
        stage.setTitle("Huffman Project 2");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();

        log("Ready.");
    }

    private void onBrowse(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose input file");
        File f = fc.showOpenDialog(stage);
        if (f == null) return;

        selectedFile = f;
        lblFile.setText(f.getAbsolutePath());
        btnCompress.setDisable(false);
        btnDecompress.setDisable(true);
        tbl.getItems().clear();

        log("\nSelected file: " + f.getName() + " (" + f.length() + " bytes)");
    }

    private void onCompress() {
        if (selectedFile == null) return;
        try {
            compressedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".huff");
            HuffmanCodec.CompressResult res = HuffmanCodec.compress(selectedFile, compressedFile);

            tbl.setItems(FXCollections.observableArrayList(res.table()));

            log("\n=== COMPRESS DONE ===");
            log("Output: " + compressedFile.getName());
            log("Original size : " + res.originalSize() + " bytes");
            log("Compressed size: " + res.compressedSize() + " bytes");
            log("\n--- HEADER (originalSize + (byte,freq)) ---");
            log(res.headerText());

            btnDecompress.setDisable(false);
        } catch (Exception ex) {
            log("ERROR: " + ex.getMessage());
        }
    }

    private void onDecompress() {
        if (compressedFile == null || !compressedFile.exists()) {
            log("No compressed file found. Compress first.");
            return;
        }
        try {
            String name = selectedFile.getName();
            int dot = name.lastIndexOf('.');

            String decodedName;
            if (dot != -1) {
                decodedName = name.substring(0, dot) + "_decoded" + name.substring(dot);
            } else {
                decodedName = name + "_decoded";
            }

            File decoded = new File(compressedFile.getParentFile(), decodedName);
            HuffmanCodec.DecompressResult res = HuffmanCodec.decompress(compressedFile, decoded);

            log("\n=== DECOMPRESS DONE ===");
            log("Decoded output: " + decoded.getName());
            log("Decoded bytes written: " + res.decodedSize() + " bytes");

            boolean ok = Files.mismatch(selectedFile.toPath(), decoded.toPath()) == -1;
            log("Verification vs original: " + (ok ? "MATCH ✅" : "NOT MATCH ❌"));

        } catch (Exception ex) {
            log("ERROR: " + ex.getMessage());
        }
    }

    private void log(String s) {
        txtLog.appendText(s + "\n");
    }

    // Green primary buttons
    private void stylePrimary(Button b) {
        b.setStyle(
                "-fx-background-color:#10b981;" +  // emerald
                "-fx-text-fill:white;" +
                "-fx-font-weight:700;" +
                "-fx-background-radius:10;" +
                "-fx-padding:8 14;"
        );
    }

    // Link-style (for Clear)
    private void styleLink(Button b) {
        b.setStyle(
                "-fx-background-color:transparent;" +
                "-fx-text-fill:#065f46;" +
                "-fx-font-weight:800;"
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}
