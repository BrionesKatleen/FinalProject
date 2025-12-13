import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class Main extends Application {

    private boolean escPressedOnce = false;

    @Override
    public void start(Stage stage) {

        // =====================
        // DUCK SPRITES
        // =====================
        Image frontDuck = new Image(getClass().getResource("/FrontSideOriginalDuck.png").toExternalForm());
        Image rightStayDuck = new Image(getClass().getResource("/RightSideStayOriginalDuck.png").toExternalForm());
        Image rightStompDuck = new Image(getClass().getResource("/RightSideStompOriginalDuck.png").toExternalForm());
        Image leftStayDuck = new Image(getClass().getResource("/LeftSideStayOriginalDuck.png").toExternalForm());
        Image leftStompDuck = new Image(getClass().getResource("/LeftSideStompOriginalDuck.png").toExternalForm());

        // =====================
        // GRASS
        // =====================
        Image grassImg = new Image(getClass().getResource("/OriginalDesktopGrass.png").toExternalForm());
        ImageView grassView = new ImageView(grassImg);
        grassView.setPreserveRatio(false);
        grassView.setFitHeight(64);

        // =====================
        // DUCK VIEW
        // =====================
        ImageView duckView = new ImageView(rightStayDuck);
        duckView.setFitWidth(128);
        duckView.setFitHeight(128);
        duckView.setPreserveRatio(true);

        StackPane duckContainer = new StackPane(duckView);
        duckContainer.setAlignment(Pos.BOTTOM_LEFT);
        duckContainer.setPrefSize(200, 200);

        // =====================
        // ROOT
        // =====================
        Pane root = new Pane();

        double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
        double sceneHeight = 200;

        root.setPrefSize(screenWidth, sceneHeight);

        // Grass positioning
        grassView.setFitWidth(screenWidth);
        grassView.setLayoutY(sceneHeight - grassView.getFitHeight());

        // Adjustable duck offset (change this to move duck up/down)
        double duckOffset = -8; // negative = slightly lower, positive = higher

        // Duck positioning
        duckContainer.setLayoutY(
                sceneHeight - duckView.getFitHeight() - grassView.getFitHeight() + duckOffset
        );

        root.getChildren().addAll(duckContainer, grassView);

        Scene scene = new Scene(root, screenWidth, sceneHeight, Color.TRANSPARENT);

        // =====================
        // FOCUS FIX
        // =====================
        root.setFocusTraversable(true);
        Platform.runLater(() -> root.requestFocus()); // auto-focus on startup
        scene.setOnMouseClicked(e -> root.requestFocus()); // click to focus

        // =====================
        // STAGE
        // =====================
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);
        stage.setScene(scene);
        stage.setX(0);
        stage.setY(Screen.getPrimary().getVisualBounds().getMaxY() - sceneHeight);
        stage.show();

        // =====================
        // ANIMATIONS
        // =====================
        double duckWidth = duckView.getFitWidth();
        double leftX = 0;
        double rightX = screenWidth - duckWidth;

        Timeline walkRightAnim = new Timeline(
                new KeyFrame(Duration.millis(250), e -> duckView.setImage(rightStayDuck)),
                new KeyFrame(Duration.millis(500), e -> duckView.setImage(rightStompDuck))
        );
        walkRightAnim.setCycleCount(Animation.INDEFINITE);

        Timeline walkLeftAnim = new Timeline(
                new KeyFrame(Duration.millis(250), e -> duckView.setImage(leftStayDuck)),
                new KeyFrame(Duration.millis(500), e -> duckView.setImage(leftStompDuck))
        );
        walkLeftAnim.setCycleCount(Animation.INDEFINITE);

        TranslateTransition moveRight = new TranslateTransition(Duration.seconds(30), duckContainer);
        moveRight.setFromX(leftX);
        moveRight.setToX(rightX);

        TranslateTransition moveLeft = new TranslateTransition(Duration.seconds(30), duckContainer);
        moveLeft.setFromX(rightX);
        moveLeft.setToX(leftX);

        moveRight.setOnFinished(e -> {
            walkRightAnim.stop();
            duckView.setImage(frontDuck);
            PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
            pause.setOnFinished(ev -> {
                walkLeftAnim.play();
                moveLeft.play();
            });
            pause.play();
        });

        moveLeft.setOnFinished(e -> {
            walkLeftAnim.stop();
            duckView.setImage(frontDuck);
            PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
            pause.setOnFinished(ev -> {
                walkRightAnim.play();
                moveRight.play();
            });
            pause.play();
        });

        walkRightAnim.play();
        moveRight.play();
// AAAAAAAAAAAHHHHHHHHHHHHH
        // =====================
        // ESC TO EXIT
        // =====================
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                if (escPressedOnce) {
                    root.getChildren().clear();
                    stage.close();
                } else {
                    escPressedOnce = true;
                    PauseTransition reset = new PauseTransition(Duration.seconds(1));
                    reset.setOnFinished(ev -> escPressedOnce = false);
                    reset.play();
                }
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
