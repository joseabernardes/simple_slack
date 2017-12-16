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
import com.jfoenix.controls.JFXSnackbar;
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
import model.GroupClient;
import model.MessageClient;
import model.PrivateChatClient;
import model.UserClient;
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

    private UserClient clientUser;

    private PrintWriter out;

    private JFXSnackbar snackBar;

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        snackBar = new JFXSnackbar(main);
    }

    public void addJoinedGroups(List<GroupClient> list) {
        for (GroupClient group : list) {
            addGroupChat(group);
        }
        groupList.setOnMouseClicked((MouseEvent event) -> {
            onGroupListClickEvent(event);
        });
    }

    private void addGroupChat(GroupClient group) {
        Label lbl = new Label("# " + group.getName());
        lbl.getStyleClass().add("lbl-cell");
        groupList.getItems().add(lbl);
        lbl.setUserData(group);
    }

    public void addPrivateChats(List<PrivateChatClient> list) {
        clientUser.setPrivateChat(list);

        for (PrivateChatClient chat : list) {
            addPrivateChat(chat);
        }
        privateList.setOnMouseClicked((MouseEvent event) -> {
            onPrivateListClickEvent(event);
        });
    }

    private void addPrivateChat(PrivateChatClient chat) {
        Label lbl = new Label("@ " + chat.getUser().getUsername());
        lbl.getStyleClass().add("lbl-cell");
        privateList.getItems().add(lbl);
        lbl.setUserData(chat);

    }
    
    
    public void addGroupToClientUser(GroupClient group){
        clientUser.addGroup(group);
        addGroupChat(group);  
    }
    
    

    public void addMessageToPrivateChat(MessageClient message) {
        int id;
        int idLabel;
        if (message.getId_destiny() == clientUser.getId()) { //se eu for o destino da mensagem
            id = message.getId();
            idLabel = id;
        } else { //se nao, significa que é uma mensagem enviada por mim para outra pessoa, e tenho de a colocar no chat dessa pessoa
            id = message.getId_destiny();
            idLabel = -1;
        }
        Object obj = clientUser.addMessage(new UserClient(id, message.getUsername()), message);
        if (obj instanceof PrivateChatClient) {//Se é a primeira mensagem recebida
            addPrivateChat((PrivateChatClient) obj);
        }

        Label selected = privateList.getSelectionModel().getSelectedItem();

        if (selected == null) { //caso nao exista nenhuma selecionada, define uma nova, assim ele considera sempre que nao está selecionada
            selected = new Label();
        }
        /*
        se eu for o destino da mensagem, ver quem enviou e meter o dele a vermelho
        
        se eu nao for o destino, nao meter!
        
         */
        for (Label label : privateList.getItems()) {
            if (!label.equals(selected) && ((PrivateChatClient) label.getUserData()).getUser().getId() == idLabel) {//se a label for a do destino da mensagem
                label.setStyle("-fx-text-fill: #F44336;");
            }
        }
    }

    private void onGroupListClickEvent(MouseEvent event) {
        if (groupList.getSelectionModel().getSelectedItem() != null) {
            if (privateList.getSelectionModel().getSelectedItem() != null) {
                privateList.getSelectionModel().clearSelection();
            }
            groupList.getSelectionModel().getSelectedItem().setStyle("-fx-text-fill: #b8b0b7;");
            GroupClient group = (GroupClient) groupList.getSelectionModel().getSelectedItem().getUserData();
            setChatPane(group.getMessages(), group.getName(), group.getId(), ListMessageController.GROUP);
        }

    }

    private void onPrivateListClickEvent(MouseEvent event) {
        if (privateList.getSelectionModel().getSelectedItem() != null) {
            if (groupList.getSelectionModel().getSelectedItem() != null) {
                groupList.getSelectionModel().clearSelection();
            }
            PrivateChatClient chat = (PrivateChatClient) privateList.getSelectionModel().getSelectedItem().getUserData();
            privateList.getSelectionModel().getSelectedItem().setStyle("-fx-text-fill: #b8b0b7;");
            setChatPane(chat.getMessages(), chat.getUser().getUsername(), chat.getUser().getId(), ListMessageController.PRIVATE);
        }

    }

    private void setChatPane(ObservableList<MessageClient> messagesList, String name, int user_id, int typeOfChat) {
        try {
            if (controller == null) {//first time
                FXMLLoader loader = new FXMLLoader(getClass().getResource("list_message/ListMessage.fxml"));
                Parent node = loader.load();
                controller = loader.getController();
                controller.setController(main, name, user_id, typeOfChat, messagesList, out, clientUser);
                pane.getChildren().clear();
                pane.getChildren().add(node);
            } else {
                controller.setController(main, name, user_id, typeOfChat, messagesList, out, clientUser);
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
            user.setUserData("Grupo N" + i);
            box.getChildren().add(user);
        }
        scroll.setMaxHeight(200.0);
        scroll.setContent(box);
        JFXDialogLayout content = new JFXDialogLayout();
        JFXDialog dialog = new JFXDialog(main, content, JFXDialog.DialogTransition.CENTER);
        content.setHeading(new Text("Iniciar conversa de grupo"));
        content.setBody(scroll);
        JFXButton ok = new JFXButton("Iniciar");
        ok.setOnAction((ActionEvent event1) -> {
            String user = group.getSelectedToggle().getUserData().toString();
//            clientUser.addMessage(clientUser, message)
            GroupClient grp = new GroupClient(341, user, user);
//            PrivateChatClient chat = new PrivateChatClient(new UserClient(41, user));
            clientUser.addGroup(grp);
            addGroupChat(grp);
            System.out.println(user);
            dialog.close();
        });

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
            user.setUserData("User N" + i);
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
        ok.setOnAction((ActionEvent event1) -> {
            String user = group.getSelectedToggle().getUserData().toString();
//            clientUser.addMessage(clientUser, message)
            PrivateChatClient chat = new PrivateChatClient(new UserClient(41, user));
            clientUser.getPrivateChat().add(chat);
            addPrivateChat(chat);
            System.out.println(user);
            dialog.close();

        });
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

    public void displayError(String error) {
        snackBar.show(error, 3000);
    }

    public void setController(int id, String username, PrintWriter out) {
        logged_user.setText(username);
        clientUser = new UserClient(id, username);
        this.out = out;
        out.println(Protocol.makeJSONResponse(Protocol.Client.Group.LIST_JOINED_GROUPS, ""));
        out.println(Protocol.makeJSONResponse(Protocol.Client.Private.LIST_PRIVATE_CHAT, ""));
    }
}
