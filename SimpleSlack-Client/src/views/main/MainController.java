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
import java.io.PrintWriter;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import model.Group;
import model.Message;
import model.PrivateChat;
import model.User;
import utils.Protocol;
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
    @FXML
    private Label logged_user;

    private ListMessageController controller;

    private User clientUser;

    private PrintWriter out;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    public void addJoinedGroups(List<Group> list) {
        for (Group group : list) {
            Label lbl = new Label("# " + group.getName());
            lbl.getStyleClass().add("lbl-cell");
            groupList.getItems().add(lbl);
            lbl.setUserData(group);
        }
        groupList.setOnMouseClicked((MouseEvent event) -> {
            onGroupListClickEvent(event);
        });
    }

    public void addPrivateChat(List<PrivateChat> list) {
        clientUser.setPrivateChat(list);
        
        for (PrivateChat chat : list) {
            Label lbl = new Label("@ " + chat.getUser().getUsername());
            lbl.getStyleClass().add("lbl-cell");
            privateList.getItems().add(lbl);
            lbl.setUserData(chat);
        }
        privateList.setOnMouseClicked((MouseEvent event) -> {
            onPrivateListClickEvent(event);
        });
    }

    public void addMessageToPrivateChat(Message message){
        for (PrivateChat object : clientUser.getPrivateChat()) {
            if(object.getUser().getId() == message.getId()){
                object.addMessage(message);
                break;
            }
        }
        
    }
    
    
    
    private void onGroupListClickEvent(MouseEvent event) {
        Group group = (Group) groupList.getSelectionModel().getSelectedItem().getUserData();
        setChatPane(group.getMessages(), group.getName(),group.getId(), ListMessageController.GROUP);
    }

    private void onPrivateListClickEvent(MouseEvent event) {
        PrivateChat chat = (PrivateChat) privateList.getSelectionModel().getSelectedItem().getUserData();
        setChatPane(chat.getMessages(), chat.getUser().getUsername(),chat.getUser().getId(), ListMessageController.PRIVATE);
    }

    private void setChatPane(ObservableList<Message> messagesList, String name, int user_id, int typeOfChat) {
        try {
            if (controller == null) {//first time
                FXMLLoader loader = new FXMLLoader(getClass().getResource("list_message/ListMessage.fxml"));
                Parent node = loader.load();
                controller = loader.getController();
                controller.setController(main, name,user_id, typeOfChat, messagesList, out, clientUser);
                pane.getChildren().clear();
                pane.getChildren().add(node);
            } else {
                controller.setController(main, name,user_id, typeOfChat, messagesList, out,clientUser);
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
        dialog.show();

    }

    public void setController(int id ,String username, PrintWriter out) {
        logged_user.setText(username);
        clientUser = new User(id,username);
        this.out = out;
        
        
        
        out.println(Protocol.makeJSONResponse(Protocol.Client.Group.LIST_JOINED_GROUPS,""));
        out.println(Protocol.makeJSONResponse(Protocol.Client.Private.LIST_PRIVATE_CHAT,""));
    }

}