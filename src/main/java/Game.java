import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Math.abs;

public class Game {
    private main main;
    private boolean host;
    Controller srvr = null;
    Client client = null;
    private volatile boolean run = true;

    public Game(main main, int port, String address) {
        this.main = main;
        host = false;
        //connect to server
    }

    public Game(main main, int port) {
        this.main = main;
        host = true;
        //start server
    }

    public void run() {
        Stage primaryStage = main.getStage();

        AnchorPane gameScene = new AnchorPane();
        AnchorPane pPaddle = new AnchorPane();
        Stage pPaddleStage = new Stage();

        AnchorPane oPaddle = new AnchorPane();
        Stage oPaddleStage = new Stage();

        Rectangle2D screenBounds = Screen.getPrimary().getBounds(); //TODO: add multiscreen drifting

        //main stage
        Circle circle = new Circle(25, Color.valueOf("#02219e"));
        {
            gameScene.setStyle("-fx-background-color : #2e3034;");
            gameScene.getChildren().addAll(circle);
        }

        //Player paddle stage
        Circle paddlePong = new Circle(25, Color.valueOf("#02219e"));
        {
            pPaddleStage.initModality(Modality.APPLICATION_MODAL);
            pPaddleStage.setTitle("Paddle");
            pPaddleStage.initStyle(StageStyle.UTILITY);
            pPaddleStage.setResizable(false);

            pPaddleStage.setX(screenBounds.getMaxX()*0.75); //init paddle x,y
            pPaddleStage.setY(screenBounds.getMaxY()/3);

            pPaddle.setStyle("-fx-background-color : #2e3034;");

            Rectangle paddle = new Rectangle(55, 0, 30, 206);
            paddle.setFill(new ImagePattern(new Image("paddle.png")));

            pPaddle.getChildren().addAll(paddle, paddlePong);
            pPaddleStage.setScene(new Scene(pPaddle));
            pPaddleStage.setWidth(100);
        }

        //Opponent paddle stage
        {
            //do something...
        }

        //scene open
        primaryStage.getScene().setRoot(gameScene);

        Thread anim = new Thread(() -> {
            int time = 200; //milliseconds
            long init = System.currentTimeMillis(), last = init, temp;
            double xGrowth = (500 - primaryStage.getScene().getWidth()) / time, yGrowth = (1000 - primaryStage.getScene().getHeight()) / time;

            while (((temp = System.currentTimeMillis()) - init) < time) {
                long timeStep = temp - last;
                primaryStage.setWidth(primaryStage.getWidth() + (xGrowth * timeStep));
                primaryStage.setHeight(primaryStage.getHeight() + (yGrowth * timeStep));
                primaryStage.setX(((screenBounds.getMaxX() - screenBounds.getMinX()) / 2) - (primaryStage.getScene().getWidth() / 2));
                primaryStage.setY(((screenBounds.getMaxY() - screenBounds.getMinY()) / 2) - (primaryStage.getScene().getHeight() / 2));
                last = temp;
            }
        });

        anim.start();

        pPaddleStage.show();

        Thread temp = new Thread(() -> {
            try {anim.join();} catch (InterruptedException ignored) {}

            //loop prep
            long last = System.currentTimeMillis();
            double xPos = (screenBounds.getMaxX() - screenBounds.getMinX()) / 2, yPos = (screenBounds.getMaxY() - screenBounds.getMinY()) / 2;
            double moveX = 1, moveY = 1;

            //game loop
            while (run) {
                long time = System.currentTimeMillis();

                //get info
                {

                }

                //check collisions
                {
                    double rad = circle.getRadius();
                    if (xPos <= primaryStage.getX() + rad) moveX = abs(moveX);
                    else if (xPos >= primaryStage.getX() + primaryStage.getScene().getWidth() - rad) moveX = -abs(moveX);
                    if (yPos <= primaryStage.getY() + rad) moveY = abs(moveY);
                    else if (yPos >= primaryStage.getY() + primaryStage.getScene().getHeight() - rad) moveY = -abs(moveY);
                }

                //update positions
                {
                    xPos += (moveX * (time - last) / 10);
                    yPos += (moveY * (time - last) / 10);

                    circle.setLayoutX(xPos - primaryStage.getX());
                    circle.setLayoutY(yPos - primaryStage.getY());

                    paddlePong.setLayoutX(xPos - pPaddleStage.getX());
                    paddlePong.setLayoutY(yPos - pPaddleStage.getY());
                }

                /*Bounds bounds = primaryStage.getScene().getRoot().getBoundsInLocal();
                double winSpeedX = pPaddleStage.getX() - windowX, winSpeedY = pPaddleStage.getY() - windowY;
                boolean alignY = paddlePong.getLayoutY() >= paddleBounds.getMinY() && paddlePong.getLayoutY() <= paddleBounds.getMaxY();

                if (paddlePong.getLayoutX() >= paddleBounds.getMinX() - circle.getRadius() && alignY) { //hit from left
                    moveX = -(Math.abs(moveX - winSpeedX / 2));
                } else if (paddlePong.getLayoutX() <= paddleBounds.getMaxX() + circle.getRadius() && alignY) { //hit from right
                    moveX = Math.abs(moveX + winSpeedX / 2);
                }*/

                last = time;
                try {TimeUnit.MILLISECONDS.sleep(10);} catch (InterruptedException ignored) {}
            }
        });
        temp.start();
    }

    public void end() { run = false; }
}