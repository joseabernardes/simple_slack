/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simpleslack;

import java.io.IOException;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author José Bernardes
 *
 */
public class SimpleSlack extends Application {

    private Button calcular, start, config;
    private RadioButton distance, time, cost;
    private ListView<String> listView;
    private GridPane pane;
    private Label total, lblDistance, lblTime, lblVelocity, lblCost;
    private Stage mainStage;
    private BorderPane bp;

    @Override
    public void start(final Stage stage) throws Exception {
        mainStage = stage;
        start = new Button("Iniciar Nova Rota");
//        start.setOnAction(this);
        calcular = new Button("Calcular Rota");
//        calcular.setOnAction(this);
//        config = new Button("", new ImageView(new Image("/config.png")));
//        config.setOnAction(this);
        distance = new RadioButton("Distancia");
        distance.setSelected(true);
        time = new RadioButton("Tempo");
        cost = new RadioButton("Custo");
        ToggleGroup radio = new ToggleGroup();
        distance.setToggleGroup(radio);
        time.setToggleGroup(radio);
        cost.setToggleGroup(radio);
        listView = new ListView<String>();
        listView.setFocusTraversable(false);
        total = new Label("Totais");
        total.setFont(Font.font(null, FontWeight.BOLD, 16));
        total.setPadding(new Insets(0, 0, 0, 80));
        lblDistance = new Label("Distancia: ");
        lblTime = new Label("Tempo: ");
        lblVelocity = new Label("Velocidade: ");
        lblCost = new Label("Custo");
        bp = new BorderPane();

        pane = new GridPane();
        pane.setDisable(true);

        pane.setVgap(10);
        pane.setHgap(10);
        pane.setPrefWidth(250);
        pane.setAlignment(Pos.BASELINE_CENTER);
        pane.add(calcular, 0, 1);
        pane.add(distance, 0, 2);
        pane.add(time, 0, 3);
        pane.add(cost, 0, 4);
        pane.add(listView, 0, 5);
        pane.add(total, 0, 6);
        pane.add(lblDistance, 0, 7);
        pane.add(lblTime, 0, 8);
        pane.add(lblVelocity, 0, 9);
        pane.add(lblCost, 0, 10);

        BorderPane buttons = new BorderPane();
        buttons.setPadding(new Insets(10, 10, 10, 10));
        buttons.setLeft(start);
        buttons.setRight(config);
        buttons.setBottom(pane);

        bp.setRight(buttons);
        Scene scene = new Scene(bp);
        stage.setScene(scene);
        stage.setTitle("Self-Service Tour");
//        stage.getIcons().add(new Image("/maps-icon.png"));
        stage.show();
//        stage.setOnCloseRequest(we -> {
//            System.out.println("Aplicação a fechar...");
//            try {
//                System.out.println("A escrever nos ficheiros...");
//                GraphCSVReader.writeGraph("GraphVertex.csv", "GraphEdges.csv", network);
//                System.out.println("Ficheiros escritos com sucesso");
//            } catch (IOException ex) {
//                System.err.println("Falha ao escrever nos ficheiros...");
//            }
//
//        });

    }

//    @Override
//    public void start(Stage primaryStage) {
//        Button btn = new Button();
//        Text ss = new Text();
//        ss.setText("sdfghjm");
//        btn.setText("Say 'Hello World'");
//        btn.setOnAction(new EventHandler<ActionEvent>() {
//
//            @Override
//            public void handle(ActionEvent event) {
//                System.out.println("Hello World!");
//            }
//        });
//
//        StackPane root = new StackPane();
//        root.getChildren().add(btn);
//        root.getChildren().add(ss);
//
//        Scene scene = new Scene(root, 300, 250);
//
//        primaryStage.setTitle("Hello World!");
//        primaryStage.setScene(scene);
//        primaryStage.show();
//    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
