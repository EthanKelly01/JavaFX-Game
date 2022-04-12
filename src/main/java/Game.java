import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.*;

import java.util.concurrent.TimeUnit;

import static java.lang.Math.abs;

public class Game {
    private final main main;
    private final boolean host;
    Controller srvr = null;
    Client client = null;
    private volatile boolean run = true;

    Stage primaryStage;
    Stage pPaddleStage = new Stage();
    Stage oPaddleStage = new Stage();
    private final EventHandler<WindowEvent> closeEventHandler = event -> {
        oPaddleStage.close();
        primaryStage.close();
        this.end();
    };

    private double gapX = 0, gapY = 0;

    private void calculateGap(MouseEvent event, Stage stage) {
        gapX = event.getScreenX() - stage.getX();
        gapY = event.getScreenY() - stage.getY();
    }

    private void dragStage(MouseEvent event, Stage stage) {
        stage.setX(event.getScreenX() - gapX);
        stage.setY(event.getScreenY() - gapY);
    }

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

    public boolean getConnection() { return srvr != null || client != null; }

    public void run() {
        primaryStage = main.getStage();

        AnchorPane gameScene = new AnchorPane();
        BorderPane pPaddle = new BorderPane();
        AnchorPane oPaddle = new AnchorPane();

        Rectangle2D screenBounds = Screen.getPrimary().getBounds(); //TODO: add multiscreen drifting

        //main stage
        Circle ball = new Circle(25, Color.valueOf("#02219e"));
        {
            gameScene.setStyle("-fx-background-color : #2e3034;");
            gameScene.getChildren().addAll(ball);
            primaryStage.setResizable(false);
        }

        //Player paddle stage
        Circle paddlePong = new Circle(25, Color.valueOf("#02219e"));
        {
            //Stage settings
            {
                pPaddleStage.setTitle("Paddle");
                pPaddleStage.initStyle(StageStyle.UNDECORATED);
                pPaddleStage.setResizable(false);
                pPaddleStage.initModality(Modality.APPLICATION_MODAL);

                pPaddleStage.setX(screenBounds.getMaxX() * 0.75); //init paddle x,y
                pPaddleStage.setY(screenBounds.getMaxY() / 3);

                pPaddleStage.setWidth(100);

                pPaddleStage.setOnCloseRequest(e -> {
                    oPaddleStage.close();
                    primaryStage.close();
                    this.end();
                });
            }

            //Scene settings
            {
                ToolBar winBar = new ToolBar();

                Button closeBtn = new Button("X");
                closeBtn.setOnAction(actionEvent -> pPaddleStage.fireEvent(new WindowEvent(pPaddleStage, WindowEvent.WINDOW_CLOSE_REQUEST)));

                winBar.setMinHeight(25);
                winBar.setMaxHeight(25);
                winBar.getItems().addAll(new Label("Paddle"), closeBtn);
                winBar.setMaxWidth(100);

                Rectangle paddle = new Rectangle(55, 0, 30, 206);
                paddle.setFill(new ImagePattern(new Image("paddle.png")));

                pPaddle.setOnMouseDragged(e -> this.dragStage(e, pPaddleStage));
                pPaddle.setOnMouseMoved(e -> this.calculateGap(e, pPaddleStage));

                pPaddle.setStyle("-fx-background-color : #2e3034;");

                pPaddle.setTop(winBar);
                pPaddle.setBottom(new AnchorPane(paddle, paddlePong));
            }

            pPaddleStage.setScene(new Scene(pPaddle));
        }

        //Opponent paddle stage
        Circle opponentPong = new Circle(25, Color.valueOf("#02219e"));
        {
            oPaddleStage.setTitle("Opponent");
            oPaddleStage.initStyle(StageStyle.UTILITY);
            oPaddleStage.setResizable(false);

            oPaddleStage.setX(screenBounds.getMaxX()*0.25); //init paddle x,y
            oPaddleStage.setY(screenBounds.getMaxY()/3);

            oPaddle.setStyle("-fx-background-color : #2e3034;");

            Rectangle paddle = new Rectangle(0, 0, 30, 206);
            paddle.setFill(new ImagePattern(new Image("paddle2.png")));

            oPaddle.getChildren().addAll(paddle, opponentPong);
            oPaddleStage.setScene(new Scene(oPaddle));
            oPaddleStage.setWidth(100);
        }

        //scene open
        primaryStage.getScene().setRoot(gameScene);

        Thread anim = new Thread(() -> {
            int time = 300; //milliseconds
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

        oPaddleStage.show();
        pPaddleStage.show();

        //game thread
        new Thread(() -> {
            try {anim.join();} catch (InterruptedException ignored) {}

            double xPos = (screenBounds.getMaxX() - screenBounds.getMinX()) / 2, yPos = (screenBounds.getMaxY() - screenBounds.getMinY()) / 2, moveX = 1, moveY = 1;
            final double rad = ball.getRadius();
            long last = System.currentTimeMillis();
            double lastPX = pPaddleStage.getX(), lastOX = oPaddleStage.getX(), lastBallX = xPos;

            while (run) {
                long time = System.currentTimeMillis();

                //update positions
                {
                    xPos += (moveX * (time - last) / 10);
                    yPos += (moveY * (time - last) / 10);

                    ball.setLayoutX(xPos - primaryStage.getX());
                    ball.setLayoutY(yPos - primaryStage.getY());

                    paddlePong.setLayoutX(xPos - pPaddleStage.getX());
                    paddlePong.setLayoutY(yPos - pPaddleStage.getY());

                    opponentPong.setLayoutX(xPos - oPaddleStage.getX());
                    opponentPong.setLayoutY(yPos - oPaddleStage.getY());
                }

                //check collisions
                {
                    if (yPos <= primaryStage.getY() + rad) moveY = abs(moveY); //top and bottom
                    else if (yPos >= primaryStage.getY() + primaryStage.getScene().getHeight() - rad) moveY = -abs(moveY);

                    if (xPos <= 0 || xPos >= screenBounds.getWidth()) { //if ball goes off-screen
                        xPos = (screenBounds.getMaxX() - screenBounds.getMinX()) / 2;
                        yPos = (screenBounds.getMaxY() - screenBounds.getMinY()) / 2;
                    }

                    //player paddle //TODO: find better way to get paddle position
                    if ((xPos - rad < pPaddleStage.getX() + pPaddleStage.getScene().getWidth()) && (xPos + rad > pPaddleStage.getX() + 55) &&
                            (yPos - rad < pPaddleStage.getY() + pPaddleStage.getScene().getHeight()) && (yPos + rad > pPaddleStage.getY())) {
                        if (lastBallX >= lastPX + (pPaddle.getWidth() / 2)) xPos = pPaddleStage.getX() + pPaddle.getWidth() + rad;
                        else xPos = pPaddleStage.getX() - rad;
                        moveX = (xPos - lastBallX) / 5;
                    } else if ((xPos - rad < oPaddleStage.getX() + oPaddleStage.getScene().getWidth() - 55) //opponent paddle
                            && (xPos + rad > oPaddleStage.getX()) && (yPos - rad < oPaddleStage.getY() + oPaddleStage.getScene().getHeight())
                            && (yPos + rad > oPaddleStage.getY())) {
                        if (lastBallX >= lastOX + (pPaddle.getWidth() / 2)) xPos = oPaddleStage.getX() + oPaddle.getWidth() + rad;
                        else xPos = oPaddleStage.getX() - rad;
                        moveX = (xPos - lastBallX) / 5;
                    }
                }

                //update info
                last = time;
                lastPX = pPaddleStage.getX();
                lastOX = oPaddleStage.getX();
                lastBallX = xPos;

                //"AI" lol
                oPaddleStage.setY(yPos - 100);

                try {TimeUnit.MILLISECONDS.sleep(10);} catch (InterruptedException ignored) {}
            }
        }).start();
    }

    public void end() { run = false; }
}