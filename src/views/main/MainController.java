package views.main;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import model.Group;
import model.Message;
import model.User;
import views.main.list_message.ListMessageController;

/**
 * FXML Controller class
 *
 * @author José Bernardes
 */
public class MainController implements Initializable {

    @FXML
    private JFXListView<Label> groupList;
    @FXML
    private JFXListView<Label> privateList;
    @FXML
    private Pane pane;
    @FXML
    private StackPane main;

    private ListMessageController controller;
    private ObservableList<Message> privateMessages;

    private User clientUser;

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
            groupList.getItems().add(lbl);
            lbl.setUserData("grupo_" + i);
        }
        groupList.setOnMouseClicked((MouseEvent event) -> {
            onGroupListClickEvent(event);
        });
        for (int i = 0; i < 4; i++) {
            Label lbl = new Label("@ User " + i);
            lbl.getStyleClass().add("lbl-cell");
            privateList.getItems().add(lbl);
            lbl.setUserData("private_" + i);
        }
        privateList.setOnMouseClicked((MouseEvent event) -> {
            onDirectListClickEvent(event);
        });
        getMessages();

    }

    public void onGroupListClickEvent(MouseEvent event) {
        String groupID = groupList.getSelectionModel().getSelectedItem().getUserData().toString();
        setChatPane(groupID, ListMessageController.GROUP);
    }

    public void onDirectListClickEvent(MouseEvent event) {
        String privateID = privateList.getSelectionModel().getSelectedItem().getUserData().toString();
        setChatPane(privateID, ListMessageController.PRIVATE);
    }

    public void getMessages() {
        List<Message> mensagens = new ArrayList<Message>();
        mensagens.add(new Message("Paulo", LocalDateTime.now(), "Ola"));
        mensagens.add(new Message("Paulo", LocalDateTime.now(), "Ola wpokr pogkpj go idjbshfvkbdsgkvjnv"));
        mensagens.add(new Message("Joel", LocalDateTime.now(), "O sdhgosdhgbhsgliajsgbkmglkmglk la"));
        mensagens.add(new Message("Joel", LocalDateTime.now(), "sdigj psoOla"));
        mensagens.add(new Message("Paulo", LocalDateTime.now(), "Ola wpokr pogkpj go idjbshfvkbdsgkvjnv"));
        mensagens.add(new Message("Joel", LocalDateTime.now(), "O sdhgosdhgbhsgliajsgbkmglkmglk la"));
        mensagens.add(new Message("Alfredo", LocalDateTime.now(), "Odspgj pg ijspg jfpb ojsgojgp jd pgsjgposjdgp sojgpsodjg psdojg psdog jspogj spdo gjpsog jspdogjspogjspogjspogjspogjpsogjspdodsadkas+fokas+foas+fkas+gokas+fpksa+fpaskf+gjpdfogjspgojspgojspgojsdpgojspgojsdpgosjdgposdjgpsodfjgpsodgjpjogla"));
        mensagens.add(new Message("Paulo", LocalDateTime.now(), "Ola"));
        privateMessages = FXCollections.observableArrayList(mensagens);

    }

    public void setChatPane(String groupname, int typeOfChat) {
        try {

            if (controller == null) {//first time
                FXMLLoader loader = new FXMLLoader(getClass().getResource("list_message/ListMessage.fxml"));
                Parent node = loader.load();
                controller = loader.getController();
                controller.setController(main, groupname, typeOfChat, privateMessages);
                pane.getChildren().clear();
                pane.getChildren().add(node);
            } else {
                controller.setController(main, groupname, typeOfChat, privateMessages);
            }
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void onClickJoinGroup(MouseEvent event) {

        VBox box = new VBox();
        ScrollPane scroll = new ScrollPane();
        scroll.getStyleClass().add("addLists");
//        scroll.getViewportBounds().

        final ToggleGroup group = new ToggleGroup();
        for (int i = 0; i < 10; i++) { //getUsers
            JFXRadioButton user = new JFXRadioButton("Grupo " + i);
            user.setToggleGroup(group);
            user.setSelected(false);
            user.setPadding(new Insets(5, 0, 5, 0));

            box.getChildren().add(user);
        }
        scroll.setMaxHeight(200.0);
        scroll.setContent(box);
        JFXDialogLayout content = new JFXDialogLayout();
        JFXDialog dialog = new JFXDialog(main, content, JFXDialog.DialogTransition.CENTER);
        content.setHeading(new Text("Iniciar conversa de grupo"));
        content.setBody(scroll);
        JFXButton ok = new JFXButton("Iniciar");
        JFXButton cancel = new JFXButton("Voltar");
        cancel.setOnAction((ActionEvent event1) -> {
            dialog.close();
        });
        content.setActions(cancel, ok);
        dialog.show();

    }

    @FXML
    private void onClickAddPrivate(MouseEvent event) {
        VBox box = new VBox();
        ScrollPane scroll = new ScrollPane();
        scroll.getStyleClass().add("addLists");
//        scroll.getViewportBounds().

        final ToggleGroup group = new ToggleGroup();
        for (int i = 0; i < 10; i++) { //getUsers
            JFXRadioButton user = new JFXRadioButton("User " + i);
            user.setToggleGroup(group);
            user.setSelected(false);
            user.setPadding(new Insets(5, 0, 5, 0));

            box.getChildren().add(user);
        }
        scroll.setMaxHeight(200.0);
        scroll.setContent(box);
        JFXDialogLayout content = new JFXDialogLayout();
        JFXDialog dialog = new JFXDialog(main, content, JFXDialog.DialogTransition.CENTER);
        content.setHeading(new Text("Iniciar conversa privada"));
        content.setBody(scroll);
        JFXButton ok = new JFXButton("Iniciar");
        JFXButton cancel = new JFXButton("Voltar");
        cancel.setOnAction((ActionEvent event1) -> {
            dialog.close();
        });
        content.setActions(cancel, ok);
        dialog.show();

    }

    @FXML
    private void onClickAddGroup(MouseEvent event) {
        JFXDialogLayout content = new JFXDialogLayout();
        JFXDialog dialog = new JFXDialog(main, content, JFXDialog.DialogTransition.CENTER);
        content.setHeading(new Text("Adicionar novo Grupo"));
        JFXTextField text = new JFXTextField();
        text.setPromptText("Nome do grupo");
        content.setBody(text);
        JFXButton ok = new JFXButton("Adicionar");
        JFXButton cancel = new JFXButton("Voltar");
        cancel.setOnAction((ActionEvent event1) -> {
            dialog.close();
        });
        content.setActions(cancel, ok);
        privateMessages.add(new Message("JOAQUIM", LocalDateTime.now(), "BEM VINDOS AMIGOS"));

        dialog.show();

    }

}
// name.charAt(0).toUppercase());
