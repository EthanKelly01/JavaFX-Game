import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
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

public class main extends Application {
    @Override
    public void start(Stage primaryStage) throws ParserConfigurationException, IOException, SAXException {
        primaryStage.setTitle("JavaFX Game");

        //create scenes
        VBox home = new VBox(); //main menu
        AnchorPane animPage = new AnchorPane(); //game page
        VBox aboutPage = new VBox(); //about page

        AnchorPane paddlePage = new AnchorPane();
        Circle paddlePong = new Circle(25, Color.valueOf("#02219e"));
        paddlePage.getChildren().addAll(new Rectangle(70, 10,10, 180), paddlePong);
        Stage paddleStage = new Stage();
        paddleStage.setScene(new Scene(paddlePage, 100, 200));
        paddleStage.initModality(Modality.APPLICATION_MODAL);
        paddleStage.setTitle("Paddle");
        paddleStage.initStyle(StageStyle.UTILITY);
        paddleStage.setResizable(false);
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        paddleStage.setX(screenBounds.getWidth()*0.75);
        paddleStage.setY(primaryStage.getY());

        //main menu
        {
            Button animBtn = new Button("Animation");
            animBtn.setPrefSize(200, 10);
            animBtn.setOnAction(e -> {
                primaryStage.getScene().setRoot(animPage);
                paddleStage.show();
            });
            Button aboutBtn = new Button("About");
            aboutBtn.setPrefSize(200, 10);
            aboutBtn.setOnAction(e -> primaryStage.getScene().setRoot(aboutPage));

            home.setAlignment(Pos.CENTER);
            home.setSpacing(10);
            home.getChildren().addAll(animBtn, aboutBtn);
            home.setPadding(new Insets(25, 25, 25, 25));
        }

        //Animation scene
        {
            Circle circle = new Circle(25, Color.valueOf("#02219e"));
            circle.setLayoutX(100); //TODO: set to mid of window
            circle.setLayoutY(100);
            animPage.getChildren().add(circle);
            //TODO: add paddles

            //I made the old timey screensaver thing. Still moves across the screen every 2sec and adapts to changing screensize
            Timeline timeline = new Timeline(new KeyFrame(Duration.millis(10), new EventHandler<ActionEvent>() {
                double moveX = 1, moveY = 1;
                double windowX = paddleStage.getX(), windowY = paddleStage.getY();
                Rectangle2D screenBounds = Screen.getPrimary().getBounds(); //TODO: add multiscreen drifting
                Bounds paddleBounds = paddlePage.getChildren().get(0).getBoundsInLocal();
                @Override public void handle(ActionEvent event) {
                    Bounds bounds = primaryStage.getScene().getRoot().getBoundsInLocal();
                    if (moveX > 5) moveX = 5;
                    if (moveY > 5) moveY = 5;
                    circle.setLayoutX(circle.getLayoutX() + moveX);
                    circle.setLayoutY(circle.getLayoutY() + moveY);

                    paddlePong.setLayoutX(primaryStage.getX() + circle.getLayoutX() - paddleStage.getX());
                    paddlePong.setLayoutY(primaryStage.getY() + circle.getLayoutY() - paddleStage.getY());

                    //TODO: detect kicks
                    double winSpeedX = paddleStage.getX() - windowX, winSpeedY = paddleStage.getY() - windowY;

                    boolean alignY = paddlePong.getLayoutY() >= paddleBounds.getMinY() && paddlePong.getLayoutY() <= paddleBounds.getMaxY();
                    if (paddlePong.getLayoutX() >= paddleBounds.getMinX() - circle.getRadius() && alignY){ //hit from left
                        moveX = - (Math.abs(moveX - winSpeedX/2));
                    } else if (paddlePong.getLayoutX() <= paddleBounds.getMaxX() + circle.getRadius() && alignY){ //hit from right
                        moveX = Math.abs(moveX + winSpeedX/2);
                    }

                    if (circle.getLayoutX() <= (bounds.getMinX() + circle.getRadius())) moveX *= -1;

                    //if (circle.getLayoutY() >= (screenBounds.getMaxY() - circle.getRadius()) || circle.getLayoutY() <= (screenBounds.getMinY() + circle.getRadius())) moveY *= -1;
                    if (circle.getLayoutY() >= (bounds.getMaxY() - circle.getRadius()) || circle.getLayoutY() <= (bounds.getMinY() + circle.getRadius())) moveY *= -1;
                    if (paddlePong.getLayoutX() > screenBounds.getMaxX()) {
                        circle.setLayoutX(100); //loss, restart
                        circle.setLayoutY(100);
                        moveX = moveY = 1;
                    }

                    windowX = paddleStage.getX();
                    windowY = paddleStage.getY();
                }
            }));
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();
        }

        //About scene
        {
            Button homeBtn = new Button("Home");
            homeBtn.setCancelButton(true);
            homeBtn.setPrefSize(50, 10);
            homeBtn.setOnAction(e -> primaryStage.getScene().setRoot(home));
            aboutPage.getChildren().add(new ToolBar(homeBtn, new Text("You can also hit escape to go back!")));

            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File("src/main/resources/data.xml"));
            document.getDocumentElement().normalize(); //these open the xml for reading
            Label label = new Label(document.getElementsByTagName("name").item(0).getTextContent()), label1 = new Label(document.getElementsByTagName("email").item(0).getTextContent()),
                    label2 = new Label(((Element) document.getElementsByTagName("student").item(0)).getAttribute("id")),
                    label3 = new Label(document.getElementsByTagName("software-description").item(0).getTextContent());
            aboutPage.getChildren().addAll(label, label1, label2, label3);
        }

        //start stage
        primaryStage.setScene(new Scene(home, 300, 250));
        primaryStage.show();
    }

    public static void main(String[] args){
        launch();
    }
}