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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import model.MessageClient;
import model.UserClient;
import org.json.simple.JSONObject;
import utils.Protocol;

/**
 * FXML Controller class
 *
 * @author José Bernardes
 */
public class ListMessageController implements Initializable {

    public static final int PRIVATE = 1;
    public static final int GROUP = 2;

    @FXML
    private JFXListView<MessageClient> messages;
    @FXML
    private JFXTextField textField;
    @FXML
    private Label chatNameLbl;
    @FXML
    private JFXButton leave_group;
    @FXML
    private JFXButton edit_group;
    @FXML
    private Label title;

    private StackPane main;

    private File file;
    @FXML
    private ImageView file_icon;

    private PrintWriter out;

    /**
     * User id ou Group id
     */
    private int idChat;
    private ObservableList<MessageClient> messagesList;
    private UserClient client_user;
    private int typeOfChat;

    public ListMessageController() {
    }

    public void setController(StackPane main, String chatName, int id, int typeOfChat, ObservableList<MessageClient> messagesList, PrintWriter out, UserClient client_user) {
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
        this.typeOfChat = typeOfChat;
        this.client_user = client_user;
        this.out = out;
        idChat = id;
        this.messagesList = messagesList;
//        SortedList<Message> sr = messagesList.sorted();
        chatNameLbl.setText(chatName);
        messages.setItems(messagesList);
//        messages.setItems(sr);
        messages.setCellFactory(param -> new Cell(out, client_user));

    }

    static class Cell extends ListCell<MessageClient> {

        private final AnchorPane pane;
        private ImageView avatar;
        private final Label username;
        private final Label time;
        private final Hyperlink link;
        private final HBox messagePane;
        private final Text message;
        private final PrintWriter out;
        private final UserClient client_user;

        public Cell(PrintWriter out, UserClient client_user) {
            super();
            this.out = out;
            this.client_user = client_user;
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

//         
            link = new Hyperlink("files_sender.xml");
            link.getStyleClass().add("hyperlink");
//            link.setPadding(Insets.EMPTY);

            message = new Text("ola amigos");
//            message.setLayoutX(71.0);
//            message.setLayoutY(49.0);
            message.setWrappingWidth(550.0);

            messagePane = new HBox();
            messagePane.setLayoutX(71.0);

//              messagePane.setLayoutY(49.0);
            messagePane.setLayoutY(39.0);
            pane = new AnchorPane(avatar, username, time, messagePane);

        }

        @Override
        protected void updateItem(MessageClient item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);
            setGraphic(null);
            if (item != null && !empty) {
                message.setText(item.getMessage());
                time.setText(item.getDate().toString());
                username.setText(item.getUsername());
                setGraphic(pane);

                messagePane.getChildren().clear();
                if (item.isFile()) {
                    if (item.getId() == client_user.getId()) {//se fui eu que enviei o ficheiro
                        message.setFont(Font.font("System", FontPosture.ITALIC, 15.0));
                        message.setText("Send '" + item.getMessage() + "'" + " " + convertBytes(item.getFileSize()));
                        messagePane.getChildren().add(message);

                    } else {
                        messagePane.getChildren().add(link);
                        HBox.setMargin(link, new Insets(-4.0, 0, -4.0, 0));

                        message.setText(convertBytes(item.getFileSize()));
                        message.setWrappingWidth(80.0);
//                    message.setFont(Font.font(10.0));
                        link.setText(item.getMessage());
                        link.setOnAction((ActionEvent event) -> {
                            downloadFile(item.getMessage());

                        });
                        messagePane.getChildren().add(message);
//                    HBox.setMargin(message, new Insets(3.0, 0, 0, 0));

                    }

                } else {
                    messagePane.getChildren().add(message);
                }
            }
        }

        private void downloadFile(String fileName) {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setInitialDirectory(new File(System.getProperty("user.home")));
            File selectedDirectory = chooser.showDialog(null);
            if (selectedDirectory != null) {
                System.out.println(selectedDirectory.getAbsolutePath());
                System.out.println(fileName);
                JSONObject obj = new JSONObject();
                obj.put("file_name", fileName);
                obj.put("path", selectedDirectory.getAbsolutePath());
                out.println(Protocol.makeJSONResponse(Protocol.Client.Private.RECEIVE_FILE, obj));
            }
        }
    }

    private void downloadFile(String fileName) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File selectedDirectory = chooser.showDialog(null);
        if (selectedDirectory != null) {
            System.out.println(selectedDirectory.getAbsolutePath());
            System.out.println(fileName);
            JSONObject obj = new JSONObject();
            obj.put("id", String.valueOf(idChat));
            obj.put("file_name", file.getName());
            out.println(Protocol.makeJSONResponse(Protocol.Client.Private.RECEIVE_FILE, obj));

        }
    }
//    private void downloadFile(String fileName) {
//        FileChooser fc = new FileChooser();
//        fc.setInitialDirectory(new File(System.getProperty("user.home")));
//        File selectedFile = fc.showOpenDialog(null);
//    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        /*     messages.getItems().addListener(new ListChangeListener<Message>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Message> c) {
                System.out.println("Changed");
                messages.scrollTo(c.getList().size() - 1);
                
            }
        });*/

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
        if (file == null) {
            String text = textField.getText();
            JSONObject obj = new JSONObject();
            obj.put("id", idChat);
            obj.put("msg", text);
            if (this.typeOfChat == ListMessageController.PRIVATE) {
                out.println(Protocol.makeJSONResponse(Protocol.Client.Private.SEND_MSG, obj));
            } else {
                out.println(Protocol.makeJSONResponse(Protocol.Client.Group.SEND_MSG, obj));
            }
        } else {
            JSONObject obj = new JSONObject();
            obj.put("id", String.valueOf(idChat));
            obj.put("file_name", file.getName());
            obj.put("size", String.valueOf(file.length()));
            obj.put("path", file.getAbsolutePath());
            if (this.typeOfChat == ListMessageController.PRIVATE) {
                out.println(Protocol.makeJSONResponse(Protocol.Client.Private.SEND_FILE, obj));
            } else {
                out.println(Protocol.makeJSONResponse(Protocol.Client.Group.SEND_MSG, obj));
            }
            System.out.println(obj);
            file = null;
            file_icon.getStyleClass().clear();
            file_icon.getStyleClass().add("file_empty");
        }
        textField.setText("");
        textField.setDisable(false);
    }

    @FXML
    private void onClickLeave(MouseEvent event) {
        JFXDialogLayout content = new JFXDialogLayout();
        JFXDialog dialog = new JFXDialog(main, content, JFXDialog.DialogTransition.CENTER);
        content.setHeading(new Text("Sair do grupo"));
        content.setBody(new Text("Pretende sair do grupo " + chatNameLbl.getText()));
        JFXButton ok = new JFXButton("Sim");
        ok.setOnAction((ActionEvent event1) -> {
            out.println(Protocol.makeJSONResponse(Protocol.Client.Group.LEAVE, String.valueOf(idChat)));
            System.out.println("Try to leave group " + chatNameLbl.getText());
            dialog.close();
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

        JFXDialogLayout content = new JFXDialogLayout();
        JFXDialog dialog = new JFXDialog(main, content, JFXDialog.DialogTransition.CENTER);
        content.setHeading(new Text("Editar nome Grupo"));
        JFXTextField text = new JFXTextField();
        text.setPromptText("Novo nome");
        content.setBody(text);
        JFXButton ok = new JFXButton("Editar");
        JFXButton cancel = new JFXButton("Voltar");
        cancel.setOnAction((ActionEvent event1) -> {
            dialog.close();
        });
        content.setActions(cancel, ok);
        dialog.show();
        System.out.println("Edit group " + chatNameLbl.getText());
    }

    @FXML
    private void onClickRemove(MouseEvent event) {
        String text;
        String titleDialog;
        if (typeOfChat == ListMessageController.GROUP) {
            text = "Pretende sair e tentar remover o grupo ";
            titleDialog = "Remover grupo";
        } else {
            text = "Pretende remover a conversa com o user ";
            titleDialog = "Remover chat";
        }

        JFXDialogLayout content = new JFXDialogLayout();
        JFXDialog dialog = new JFXDialog(main, content, JFXDialog.DialogTransition.CENTER);
        content.setHeading(new Text(titleDialog));
        content.setBody(new Text(text + chatNameLbl.getText()));
        JFXButton ok = new JFXButton("Sim");
        ok.setOnAction((ActionEvent event1) -> {
            if (typeOfChat == ListMessageController.GROUP) {
                out.println(Protocol.makeJSONResponse(Protocol.Client.Group.REMOVE, String.valueOf(idChat)));
            } else {
                out.println(Protocol.makeJSONResponse(Protocol.Client.Private.REMOVE_PRIVATE_CHAT, String.valueOf(idChat)));
            }
            System.out.println("Try to remove chat / group " + chatNameLbl.getText());
            dialog.close();
        });

        JFXButton cancel = new JFXButton("Não");
        cancel.setOnAction((ActionEvent event1) -> {
            dialog.close();
        });
        content.setActions(cancel, ok);
        dialog.show();
    }

    @FXML
    private void onClickFile(MouseEvent event) {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File(System.getProperty("user.home")));
        File selectedFile = fc.showOpenDialog(null);
        file_icon.getStyleClass().clear();
//        JFXSnackbar snack = new JFXSnackbar(main);
        if (selectedFile != null) {
            file = selectedFile;
            file_icon.getStyleClass().add("file_not_empty");

//            snack.show("Ficheiro \"" + selectedFile.getName() + "\" anexado á mensagem", 3000);
            textField.setText("Send file \"" + selectedFile.getName() + "\"");
            textField.setDisable(true);
        } else {
            file = null;
            file_icon.getStyleClass().add("file_empty");

            textField.setText("");
            textField.setDisable(false);
            //            snack.show("Nenhum anexo selecionado", 2000);
        }
    }

    public static String convertBytes(long bytes) {
        boolean si = true;
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

}
