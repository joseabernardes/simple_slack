package views.main;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.jfoenix.controls.JFXListView;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

/**
 * FXML Controller class
 *
 * @author José Bernardes
 */
public class MainController implements Initializable {

    @FXML
    private JFXListView<Label> groups;

    @FXML
    private JFXListView<Label> direct;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        for (int i = 0; i < 10; i++) {
            Label lbl = new Label("# Grupo " + i);
            lbl.getStyleClass().add("lbl-cell");
            groups.getItems().add(lbl);
        }

        for (int i = 0; i < 4; i++) {
            Label lbl = new Label("@ User " + i);
            lbl.getStyleClass().add("lbl-cell");
            direct.getItems().add(lbl);
        }
//        groups.setExpanded(true);

    }

}
// name.charAt(0).toUppercase());