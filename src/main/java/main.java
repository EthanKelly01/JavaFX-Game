//Author: E. Kelly
/* Proj Description:
- an evolving project using JavaFX, networking, and multithreading
- pong, but using multiple windows. You drag a window around instead of controlling a paddle
- pong, with windows, hence PongWin(dows)
 */
//All art credit to Alex Sawatzky

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import java.lang.Math;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class main extends Application {
    private Stage primStage = null;
    public Stage getStage() { return primStage; }
    Game game;

    @Override
    public void start(Stage primaryStage) throws ParserConfigurationException, IOException, SAXException {
        primStage = primaryStage;

        //create scenes
        VBox home = new VBox(); //main menu
        VBox clientMenu = new VBox(5);

        Rectangle2D screenBounds = Screen.getPrimary().getBounds(); //TODO: better positioning system

        //String address = "localhost"; //default vars
        String address = "192.168.2.30";
        int port = 6666;

        //main menu
        {
            home.setStyle("-fx-background-color : #2e3034;");

            Button hostBtn = new Button("Host Game");
            Button joinBtn = new Button("Join Game");
            hostBtn.setPrefSize(200, 10);
            joinBtn.setPrefSize(200, 10);

            hostBtn.setOnAction(e -> {
                //do something
                //primaryStage.getScene().setRoot(gamePage);
                //pPaddleStage.show();
            });

            joinBtn.setOnAction(e -> {
                //do something
                primaryStage.getScene().setRoot(clientMenu);
            });

            home.setAlignment(Pos.CENTER);
            home.setSpacing(10);
            home.setPadding(new Insets(20));
            home.getChildren().addAll(new ImageView(new Image("logo.png")), hostBtn, joinBtn);
        }

        //Client menu
        {
            clientMenu.setStyle("-fx-background-color : #f9e7bf;");

            clientMenu.setAlignment(Pos.CENTER);
            clientMenu.setPadding(new Insets(20));

            TextField portField = new TextField(Integer.toString(port));
            portField.setMaxWidth(50);
            UnaryOperator<TextFormatter.Change> modifyChange = c -> {
                if (c.isContentChange() && (c.getControlNewText().length() > 5 || !Pattern.matches("^[0-9]*$", c.getControlNewText()))) {
                    c.setText(c.getControlText());
                    c.setRange(0, c.getControlText().length());
                }
                return c;
            };
            portField.setTextFormatter(new TextFormatter<>(modifyChange));

            GridPane temp = new GridPane();
            temp.setAlignment(Pos.CENTER);
            temp.setHgap(5);
            temp.add(new Label("Port:"), 0, 1);
            temp.add(portField, 1, 1);

            TextField ipField = new TextField((address));

            GridPane temp2 = new GridPane();
            temp2.setAlignment(Pos.CENTER);
            temp2.setHgap(5);
            temp2.add(new Label("IP Address:"), 0, 1);
            temp2.add(ipField, 1, 1);

            TextField usernameField = new TextField("anon");

            GridPane temp3 = new GridPane();
            temp3.setAlignment(Pos.CENTER);
            temp3.setHgap(5);
            temp3.add(new Label("Username:"), 0, 1);
            temp3.add(usernameField, 1, 1);

            Button connectBtn = new Button("Connect");
            portField.setOnKeyPressed(keyEvent -> {if (keyEvent.getCode() == KeyCode.ENTER) connectBtn.fire();});

            connectBtn.setOnAction(e -> {
                game = new Game(this, port, address);
                game.run();
            });

            clientMenu.getChildren().addAll(temp2, temp, temp3, connectBtn);
        }

        //set stage
        {
            primaryStage.setTitle("PongWin");
            primaryStage.getIcons().add(new Image("icon.png"));
            //primaryStage.getIcons().add(new Image("icon_bg.png"));

            primaryStage.setResizable(false);

            primaryStage.setScene(new Scene(home, 450, 300)); //v Center the window v
            primaryStage.setX(((screenBounds.getMaxX() - screenBounds.getMinX()) / 2) - (primaryStage.getScene().getWidth() / 2));
            primaryStage.setY((screenBounds.getMaxY() - screenBounds.getMinY()) / 2 - (primaryStage.getScene().getHeight() / 2));

            //timeline
            primaryStage.show();
            //TODO: intro anim

            primaryStage.setOnCloseRequest(event -> {
                if (game != null) game.end();
            });
        }
    }

    public static void main(String[] args){
        launch();
    }
}