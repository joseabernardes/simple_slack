/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package views;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXScrollPane;
import com.jfoenix.svg.SVGGlyph;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 *
 * @author PC
 */
public class MainAplication extends Application {

    @Override
//    public void start(Stage primaryStage) throws Exception {
//
//        JFXListView<Label> list = new JFXListView<>();
//        for (int i = 1; i < 5; i++) {
//            list.getItems().add(new Label("Item " + i));
//        }
//
//        JFXButton button = new JFXButton("Popup!");
//        StackPane main = new StackPane();
//        main.getChildren().add(button);
//
//        JFXPopup popup = new JFXPopup(list);
////        popup.setPopupContent(list);
//        button.setOnAction(e -> popup.show(button, PopupVPosition.TOP, PopupHPosition.LEFT));
//
//        final Scene scene = new Scene(main, 800, 800);
//
//        primaryStage.setTitle("JFX Popup Demo");
//        primaryStage.setScene(scene);
//        primaryStage.setResizable(false);
//        primaryStage.show();
//    }

    public void start(Stage stage) {
        JFXListView<Label> list = new JFXListView<>();
        for (int i = 0; i < 100; i++) {
            list.getItems().add(new Label("Item " + i));
        }
        list.getStyleClass().add("mylistview");
        list.setMaxHeight(3400);

        StackPane container = new StackPane(list);
        container.setPadding(new Insets(24));

        JFXScrollPane pane = new JFXScrollPane();
        pane.setContent(container);

        JFXButton button = new JFXButton("");
        SVGGlyph arrow = new SVGGlyph(0,
                "FULLSCREEN",
                "M402.746 877.254l-320-320c-24.994-24.992-24.994-65.516 0-90.51l320-320c24.994-24.992 65.516-24.992 90.51 0 24.994 24.994 "
                + "24.994 65.516 0 90.51l-210.746 210.746h613.49c35.346 0 64 28.654 64 64s-28.654 64-64 64h-613.49l210.746 210.746c12.496 "
                + "12.496 18.744 28.876 18.744 45.254s-6.248 32.758-18.744 45.254c-24.994 24.994-65.516 24.994-90.51 0z",
                Color.WHITE);
        arrow.setSize(20, 16);
        button.setGraphic(arrow);
        button.setRipplerFill(Color.WHITE);
//        pane.getTopBar().getChildren().add(button);

        Label title = new Label("Title");
//        pane.getBottomBar().getChildren().add(title);
        title.setStyle("-fx-text-fill:WHITE; -fx-font-size: 40;");
//        JFXScrollPane.smoothScrolling((ScrollPane) pane.getChildren().get(0));
        pane.getTopBar().getChildren().clear();
        pane.getBottomBar().getChildren().clear();
        StackPane.setMargin(title, new Insets(0, 0, 0, 80));
        StackPane.setAlignment(title, Pos.CENTER_LEFT);
        StackPane.setAlignment(button, Pos.CENTER_LEFT);
        StackPane.setMargin(button, new Insets(0, 0, 0, 20));

        final Scene scene = new Scene(new StackPane(pane), 600, 600, Color.WHITE);
        stage.setTitle("JFX ListView Demo ");
        stage.setScene(scene);
        stage.show();
    }

//      public void start(Stage primaryStage) throws Exception {
// 
//         
//        JFXHamburger show = new JFXHamburger();
//        show.setPadding(new Insets(10,5,10,5));
//        JFXRippler r = new JFXRippler(show,RipplerMask.CIRCLE,RipplerPos.BACK);
// 
//        JFXListView<Label> list = new JFXListView<Label>();
//        for(int i = 1 ; i < 5 ; i++) list.getItems().add(new Label("Item " + i));
//         
//        AnchorPane container = new AnchorPane();
//        container.getChildren().add(r);
//        AnchorPane.setLeftAnchor(r, 200.0);
//        AnchorPane.setTopAnchor(r, 210.0);
//         
//        StackPane main = new StackPane();
//        main.getChildren().add(container);
////         
////        JFXPopup popup = new JFXPopup();
////        popup.setContent(list);
////        popup.setPopupContainer(main);
////        popup.setSource(r);
////        r.setOnMouseClicked((e)-> popup.show(PopupVPosition.TOP, PopupHPosition.LEFT));
////         
//        final Scene scene = new Scene(main, 800, 800);
////        scene.getStylesheets().add(MainAplication.class.getResource("/resources/css/jfoenix-components.css").toExternalForm());
// 
//        primaryStage.setTitle("JFX Popup Demo");
//        primaryStage.setScene(scene);
//        primaryStage.setResizable(false);
//        primaryStage.show();        
//    }
    public static void main(String[] args) {
        launch(args);
    }
}

/*

public class MainAplication extends Application implements EventHandler<ActionEvent> {

    private Button sign, registar;
    private TabPane tp;
    private Tab login, regist;
    private TextField username, usernameR;
    private PasswordField pwd, pwdR, confirmpwd;
    private Label user, pass, userR, passR, confirm;
    private GridPane gpLogin, gpRegistar;
    private Scene scene;
  

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Simple Slack");
        stage.getIcons().add(new Image("/images/Slack_Square.png"));

        Label label = new Label();
        label.setText("Welcome to Simple Slack");
        ImageView iv = new ImageView("/images/icon.png");
        iv.setFitHeight(30);
        iv.setFitWidth(30);
        label.setGraphic(iv);

        //String str = "Welcome To " + iv + "imple" + iv + "lack";
        //
        
        
        label.setFont(new Font("Arial", 30));
        label.setTextFill(Color.web("#0076a3"));
        label.setTextAlignment(TextAlignment.JUSTIFY);

        BorderPane bp = new BorderPane();

        tp = new TabPane();
        //tp.setLayoutY(100);

        regist = new Tab("Registar");
        login = new Tab("Log In");
        regist.setClosable(false);
        login.setClosable(false);

        
        tp.getTabs().add(login);
        tp.getTabs().add(regist);
        
        
        if (tp.getSelectionModel().getSelectedItem().equals(login)) {
            gpLogin = new GridPane();
            user = new Label("Username: ");
            gpLogin.add(user, 0, 0);
            username = new TextField();
            username.setPromptText("username");
            gpLogin.add(username, 1, 0);

            pass = new Label("Password: ");
            gpLogin.add(pass, 0, 1);

            pwd = new PasswordField();
            pwd.setPromptText("Password");
            gpLogin.add(pwd, 1, 1);

            sign = new Button("Log in");
            gpLogin.add(sign, 1, 2);

            login.setContent(gpLogin);
        } else if (tp.getSelectionModel().getSelectedItem().equals(regist)) {
            gpRegistar = new GridPane();

            userR = new Label("Username: ");
            gpRegistar.add(userR, 0, 0);
            usernameR = new TextField();
            usernameR.setPromptText("username");
            gpRegistar.add(usernameR, 1, 0);
            passR = new Label("Password: ");
            gpRegistar.add(passR, 0, 1);
            pwdR = new PasswordField();
            pwdR.setPromptText("Password");
            gpRegistar.add(pwdR, 1, 1);
            confirm = new Label("Password");
            gpRegistar.add(confirm, 0, 2);
            confirmpwd = new PasswordField();
            confirmpwd.setPromptText("Confirm password");
            gpRegistar.add(confirmpwd, 1, 2);
            registar = new Button("Regist");
            gpRegistar.add(registar, 1, 3);
            regist.setContent(gpRegistar);
        }

        bp.setTop(label);
        bp.setCenter(tp);
        scene = new Scene(bp, 700, 500);
        scene.getStylesheets().add(MainAplication.class.getResource("style.css").toExternalForm());

        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void handle(ActionEvent t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
////        BorderPane p = new BorderPane();
//        sign = new Button();
//        sign.setText("Sign In");
////        p.getChildren().add(button);
 */
