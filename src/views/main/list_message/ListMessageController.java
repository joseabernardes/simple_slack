/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package views.main.list_message;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import model.Message;

/**
 * FXML Controller class
 *
 * @author José Bernardes
 */
public class ListMessageController implements Initializable {

    public static final int PRIVATE = 1;
    public static final int GROUP = 2;

    @FXML
    private JFXListView<Message> messages;
    @FXML
    private JFXTextField textGroup;
    @FXML
    private Label groupName;
    @FXML
    private JFXButton leave_group;
    @FXML
    private JFXButton edit_group;
    @FXML
    private Label title;

    private StackPane main;

    public ListMessageController() {
    }

    public void setController(StackPane main, String chatName, int typeOfChat, ObservableList<Message> messagesList) {
        this.main = main;
        if (typeOfChat == ListMessageController.GROUP) {
            title.setText("Group Chat");
            leave_group.setVisible(true);
            edit_group.setVisible(true);
        } else {
            title.setText("Private Chat");
            leave_group.setVisible(false);
            edit_group.setVisible(false);
        }
        groupName.setText(chatName);
        messages.setItems(messagesList);
        messages.setCellFactory(param -> new Cell());

    }

    static class Cell extends ListCell<Message> {

        private final AnchorPane pane;
        private ImageView avatar;
        private final Label username;
        private final Label time;
        private final Text message;

        public Cell() {
            super();
            this.getStyleClass().add("list_msg");

            try {
                avatar = new ImageView(new Image(new FileInputStream("src/views/images/avatar.png")));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ListMessageController.class.getName()).log(Level.SEVERE, null, ex);
            }
            avatar.setFitHeight(46.0);
            avatar.setFitWidth(56.0);
            avatar.setLayoutX(5.0);
            avatar.setLayoutY(10.0);
            avatar.setPreserveRatio(true);
            username = new Label("Username");
            username.setLayoutX(71.0);
            username.setLayoutY(10.0);
            username.setStyle("-fx-text-fill: black");
            username.setFont(new Font("System Bold", 15.0));
            time = new Label("12/12/2017 14:31");
            time.setLayoutX(554.0);
            time.setLayoutY(13.0);
            time.setStyle("-fx-text-fill: #9a9a9a");
            time.setFont(new Font("System ", 11.0));
            message = new Text("ola amigos");
            message.setLayoutX(71.0);
            message.setLayoutY(49.0);
            message.setWrappingWidth(550.0);
            pane = new AnchorPane(avatar, username, time, message);
        }

        @Override
        protected void updateItem(Message item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);
            setGraphic(null);
            if (item != null && !empty) {
                message.setText(item.getMessage());
                time.setText(item.getDate().toString());
                username.setText(item.getUsername());
                setGraphic(pane);
            }
//            Platform.runLater(() -> messages.scrollTo(messages.getItems().size() - 1));
        }

    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        messages.getItems().addListener(new ListChangeListener<Message>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Message> c) {
                System.out.println("Changed");
                messages.scrollTo(c.getList().size() - 1);

            }
        });

    }

    @FXML
    private void onClickSend(MouseEvent event) {
        sendMessage();
    }

    @FXML
    private void onAction(ActionEvent event) {
        sendMessage();
    }

    private void sendMessage() {
        String text = textGroup.getText();
        System.out.println("SEND: " + text + "TO: " + groupName.getText());
        textGroup.setText("");
    }

    @FXML
    private void onClickLeave(MouseEvent event) {
        JFXDialogLayout content = new JFXDialogLayout();
        JFXDialog dialog = new JFXDialog(main, content, JFXDialog.DialogTransition.CENTER);
        content.setHeading(new Text("Sair do grupo"));
        content.setBody(new Text("Pretende sair do grupo " + groupName.getText()));
        JFXButton ok = new JFXButton("Sim");
        ok.setOnAction((ActionEvent event1) -> {
            System.out.println("Leave group " + groupName.getText());
        });
        JFXButton cancel = new JFXButton("Não");
        cancel.setOnAction((ActionEvent event1) -> {
            dialog.close();
        });
        content.setActions(cancel, ok);
        dialog.show();
    }

    @FXML
    private void onClickEdit(MouseEvent event) {
        System.out.println("Edit group " + groupName.getText());
    }

    @FXML
    private void onClickRemove(MouseEvent event) {

        JFXDialogLayout content = new JFXDialogLayout();
        JFXDialog dialog = new JFXDialog(main, content, JFXDialog.DialogTransition.CENTER);
        content.setHeading(new Text("Remover grupo"));
        content.setBody(new Text("Pretende sair e tentar remover o grupo " + groupName.getText()));
        JFXButton ok = new JFXButton("Sim");
        ok.setOnAction((ActionEvent event1) -> {
            System.out.println("Remove group " + groupName.getText());
        });
        JFXButton cancel = new JFXButton("Não");
        cancel.setOnAction((ActionEvent event1) -> {
            dialog.close();
        });
        content.setActions(cancel, ok);
        dialog.show();
    }
}
