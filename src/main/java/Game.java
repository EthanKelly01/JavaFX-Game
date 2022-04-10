import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Game implements Runnable {
    private main main;
    private volatile boolean host, run = true;
    Controller srvr = null;
    Client client = null;

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

        AnchorPane pPaddle = new AnchorPane();
        Stage pPaddleStage = new Stage();

        AnchorPane oPaddle = new AnchorPane();
        Stage oPaddleStage = new Stage();

        Rectangle2D screenBounds = Screen.getPrimary().getBounds(); //TODO: add multiscreen drifting
        Bounds paddleBounds = ((AnchorPane)pPaddle.getScene().getRoot()).getChildren().get(0).getBoundsInLocal();

        //Player paddle stage
        Circle paddlePong = new Circle(25, Color.valueOf("#02219e"));
        {
            pPaddleStage.initModality(Modality.APPLICATION_MODAL);
            pPaddleStage.setTitle("Paddle");
            pPaddleStage.initStyle(StageStyle.UTILITY);
            pPaddleStage.setResizable(false);

            pPaddleStage.setX(screenBounds.getMaxX()*0.75); //init paddle x,y
            pPaddleStage.setY(screenBounds.getMaxY()/3);

            pPaddle.getChildren().addAll(new Rectangle(70, 10, 10, 180), paddlePong);
            pPaddleStage.setScene(new Scene(pPaddle, 100, 200));
        }

        //Opponent paddle stage
        {

        }

        Circle circle = new Circle(25, Color.valueOf("#02219e"));
        circle.setLayoutX(100); //TODO: set to mid of window
        circle.setLayoutY(100);
        ((AnchorPane)primaryStage.getScene().getRoot()).getChildren().addAll(circle);
        //TODO: add paddles

        double moveX = 1, moveY = 1;
        double windowX = pPaddleStage.getX(), windowY = pPaddleStage.getY();


        //scene open
        //show new stages, animate main stage

        //game loop
        while (run) {
            Bounds bounds = primaryStage.getScene().getRoot().getBoundsInLocal();
            if (moveX > 5) moveX = 5;
            if (moveY > 5) moveY = 5;
            circle.setLayoutX(circle.getLayoutX() + moveX);
            circle.setLayoutY(circle.getLayoutY() + moveY);

            paddlePong.setLayoutX(primaryStage.getX() + circle.getLayoutX() - pPaddleStage.getX());
            paddlePong.setLayoutY(primaryStage.getY() + circle.getLayoutY() - pPaddleStage.getY());

            //TODO: detect kicks
            double winSpeedX = pPaddleStage.getX() - windowX, winSpeedY = pPaddleStage.getY() - windowY;

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

            windowX = pPaddleStage.getX();
            windowY = pPaddleStage.getY();
        }
    }

    public void end() {

    }
}