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
        Circle ball = new Circle(25, Color.valueOf("#02219e"));
        {
            gameScene.setStyle("-fx-background-color : #2e3034;");
            gameScene.getChildren().addAll(ball);
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
            double xPos = (screenBounds.getMaxX() - screenBounds.getMinX()) / 2, yPos = (screenBounds.getMaxY() - screenBounds.getMinY()) / 2,
                    moveX = 1, moveY = 1, lastBallX = xPos, lastBallY = yPos,
                    rad = ball.getRadius();

            double paddlePosX = pPaddleStage.getX(), paddlePosY = pPaddleStage.getY(),
                    lastXPos = paddlePosX, lastYPos = paddlePosY;
            boolean alignedX = false, alignedY = false;


            //game loop
            while (run) {
                long time = System.currentTimeMillis();

                //update positions
                {
                    if (moveX > 10) moveX = 10;
                    else if (moveX < -10) moveX = -10;


                    xPos += (moveX * (time - last) / 10);
                    yPos += (moveY * (time - last) / 10);

                    ball.setLayoutX(xPos - primaryStage.getX());
                    ball.setLayoutY(yPos - primaryStage.getY());

                    paddlePong.setLayoutX(xPos - pPaddleStage.getX());
                    paddlePong.setLayoutY(yPos - pPaddleStage.getY());
                }

                //get info
                {
                    paddlePosX = pPaddleStage.getX();
                    paddlePosY = pPaddleStage.getY();

                    alignedX = (xPos - rad < paddlePosX + pPaddleStage.getScene().getWidth())
                            && (xPos + rad > paddlePosX + 55); //TODO: find a way to get paddle location
                    alignedY = (yPos - rad < paddlePosY + pPaddleStage.getScene().getHeight())
                            && (yPos + rad > paddlePosY);
                }

                //check collisions
                {
                    if (xPos <= primaryStage.getX() + rad) moveX = abs(moveX);
                    else if (xPos >= primaryStage.getX() + primaryStage.getScene().getWidth() - rad) moveX = -abs(moveX);
                    if (yPos <= primaryStage.getY() + rad) moveY = abs(moveY);
                    else if (yPos >= primaryStage.getY() + primaryStage.getScene().getHeight() - rad) moveY = -abs(moveY);

                    if (alignedX && alignedY) { //TODO: fix the speed pass-through thing
                        moveX += abs((paddlePosX - lastXPos) / 5);
                        if (lastBallX >= lastXPos + (pPaddle.getWidth() / 2)) moveX = abs(moveX);
                        else moveX = -abs(moveX);
                    }
                }

                //update info
                {
                    last = time;
                    lastXPos = paddlePosX;
                    lastYPos = paddlePosY;
                    lastBallX = xPos;
                    lastBallY = yPos;
                }

                try {TimeUnit.MILLISECONDS.sleep(10);} catch (InterruptedException ignored) {}
            }
        });
        temp.start();
    }

    public void end() { run = false; }
}