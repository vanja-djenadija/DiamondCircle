package org.unibl.etf.pj2.diamondcircle.gui_controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import org.unibl.etf.pj2.diamondcircle.GameService;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.unibl.etf.pj2.diamondcircle.GameService.RESULTS_PATH;

public class ResultsController {

    @FXML
    private ListView<String> filesListView;

    @FXML
    private TextArea contentTextArea;

    @FXML
    public void initialize() {
        showFileNames();
    }

    private void showFileNames() {
        File[] files = new File(RESULTS_PATH).listFiles();
        assert files != null;
        filesListView.getItems().addAll(Arrays.stream(files).map(File::getName).collect(Collectors.toList()));
    }

    public void showFileContent() {
        StringBuilder resultStringBuilder = new StringBuilder();
        File file = new File(RESULTS_PATH + File.separator + filesListView.getSelectionModel().getSelectedItem());
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
            contentTextArea.setText(resultStringBuilder.toString());
        } catch (IOException e) {
            Logger.getLogger(GameService.class.getName()).log(Level.SEVERE, e.fillInStackTrace().toString());
        }
    }
}
