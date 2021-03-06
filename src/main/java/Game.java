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
import java.util.Random;

//represents the game itself (main is just the menus)
public class Game {
    private final main main;
    private final boolean host;
    Controller srvr = null;
    Client client = null;
    private volatile boolean run = true;
    private volatile double xPos, yPos, oppX, oppY, pX, pY;

    //used to update game loop in multiplayer
    public void updateClient(double x1, double y1, double x2, double y2) {
        xPos = x1;
        yPos = y1;
        pX = x2;
        pY = y2;
    }
    public void updateHost(double x, double y) {
        oppX = x;
        oppY = y;
    }

    //controls window drag
    private double gapX = 0, gapY = 0;
    private void calculateGap(MouseEvent event, Stage stage) {
        gapX = event.getScreenX() - stage.getX();
        gapY = event.getScreenY() - stage.getY();
    }
    private void dragStage(MouseEvent event, Stage stage) {
        stage.setX(event.getScreenX() - gapX);
        stage.setY(event.getScreenY() - gapY);
    }

    //client constructor
    public Game(main main, int port, String address) {
        this.main = main;
        host = false;
        client = new Client(this, address, port);
    }
    //host constructor
    public Game(main main, int port) {
        this.main = main;
        host = true;
        new Thread(srvr = new Controller(port, this)).start();
    }

    public boolean getConnection() { return srvr != null || client != null; }

    public void run() {
        Stage primaryStage = main.getStage();
        Stage pPaddleStage = new Stage();
        Stage oPaddleStage = new Stage();

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
                    if (srvr != null) srvr.end();
                    oPaddleStage.close();
                    primaryStage.close();
                    this.end();
                });
            }

            //Scene settings
            {
                //children
                ToolBar winBar = new ToolBar();

                Button closeBtn = new Button("X");
                closeBtn.setOnAction(actionEvent -> pPaddleStage.fireEvent(new WindowEvent(pPaddleStage, WindowEvent.WINDOW_CLOSE_REQUEST)));

                winBar.setMinHeight(25);
                winBar.setMaxHeight(25);
                winBar.getItems().addAll(new Label("Paddle"), closeBtn);
                winBar.setMaxWidth(100);

                Rectangle paddle = new Rectangle(55, 0, 30, 206);
                paddle.setFill(new ImagePattern(new Image("paddle.png")));

                //scene
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

            xPos = (screenBounds.getMaxX() - screenBounds.getMinX()) / 2;
            yPos = (screenBounds.getMaxY() - screenBounds.getMinY()) / 2;
            double moveX = 1, moveY = 1;
            final double rad = ball.getRadius();
            long last = System.currentTimeMillis();
            double lastPX = pPaddleStage.getX(), lastOX = oPaddleStage.getX(), lastBallX = xPos;

            long aiTime = 0;
            boolean direction = true;
            Random rand = new Random();

            while (run) {
                long time = System.currentTimeMillis();

                if (host) {
                    //update positions
                    {
                        xPos += (moveX * (time - last) / 10);
                        yPos += (moveY * (time - last) / 10);
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
                    {
                        last = time;
                        lastPX = pPaddleStage.getX();
                        lastOX = oPaddleStage.getX();
                        lastBallX = xPos;
                    }

                    //"AI" for when no opponent is connected
                    if (!srvr.getOpponent()) {
                        oPaddleStage.setY(yPos - 100);
                        if (aiTime <= time) {
                            direction = !direction;
                            aiTime = rand.nextLong(1000) + time + 500;
                        }
                        if (direction) oPaddleStage.setX(oPaddleStage.getX() + (((screenBounds.getWidth() * 0.3) - oPaddleStage.getX()) / (aiTime - time)));
                        else oPaddleStage.setX(oPaddleStage.getX() - ((oPaddleStage.getX() - screenBounds.getMinX()) / (aiTime - time)));
                    } else {
                        oPaddleStage.setX(oppX);
                        oPaddleStage.setY(oppY);
                    }
                } else {
                    client.send(oPaddleStage.getX(), oPaddleStage.getY());
                    pPaddleStage.setX(pX);
                    pPaddleStage.setY(pY);
                }

                ball.setLayoutX(xPos - primaryStage.getX());
                ball.setLayoutY(yPos - primaryStage.getY());

                paddlePong.setLayoutX(xPos - pPaddleStage.getX());
                paddlePong.setLayoutY(yPos - pPaddleStage.getY());

                opponentPong.setLayoutX(xPos - oPaddleStage.getX());
                opponentPong.setLayoutY(yPos - oPaddleStage.getY());

                //sleeping for a few milliseconds keeps the game running more smoothly
                try {TimeUnit.MILLISECONDS.sleep(10);} catch (InterruptedException ignored) {}
            }
        }).start();
    }

    public void end() { run = false; }
}