import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class Main extends Application {

    private static MediaPlayer mediaPlayer;
    private double happiness = 0.60;
    private double hunger = 0.70;
    private double energy = 0.50;
    private double cleanliness = 0.50;
    private ImageView foodDisplay;
    private int foodIndex = 0;
    private final String[] foods = {"peas.png", "birdseed.png", "corn.png", "oats.png"};
    private static Image selectedCharacter = null;
    private String[] foodsNight = {
            "peas night.png",
            "bird seeds night.png",
            "corn night.png",
            "oats night.png"
    };
    // Adjustable sizes per scene
    @Override
    public void start(Stage stage) {

        Label userLabel = new Label("Username");
        userLabel.getStyleClass().add("form-label");
        userLabel.setFont(Font.font(10));
        userLabel.setMaxWidth(Double.MAX_VALUE);
        userLabel.setAlignment(Pos.CENTER);

        Label passLabel = new Label("Password");
        passLabel.getStyleClass().add("form-label");
        passLabel.setFont(Font.font(10));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        usernameField.setMaxWidth(150);
        usernameField.setPrefWidth(150);
        usernameField.setStyle("-fx-font-size: 11px; -fx-pref-height: 22px;");
        usernameField.getStyleClass().add("login-field");

        VBox userBox = new VBox(4, userLabel, usernameField);
        userBox.setAlignment(Pos.CENTER);
        userBox.setLayoutX(120);
        userBox.setLayoutY(240);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setMaxWidth(150);
        passwordField.setPrefWidth(150);
        passwordField.setStyle("-fx-font-size: 11px; -fx-pref-height: 22px;");
        passwordField.getStyleClass().add("login-field");

        VBox passBox = new VBox(4, passLabel, passwordField);
        passBox.setAlignment(Pos.CENTER);
        passBox.setLayoutX(120);
        passBox.setLayoutY(300);

        Label error = new Label();
        error.getStyleClass().add("error-label");
        error.setVisible(false);
        error.setLayoutX(80);
        error.setLayoutY(425);

        Button loginButton = new Button("🦆 Sign In");
        loginButton.getStyleClass().add("login-button");
        loginButton.setPrefWidth(110);
        loginButton.setPrefHeight(26);
        loginButton.setStyle("-fx-font-size: 11px;");
        loginButton.setLayoutX(140);
        loginButton.setLayoutY(380);
        loginButton.setOnAction(e -> {
            String user = usernameField.getText();
            String pass = passwordField.getText();
            if (user.equals("Zashkie") && pass.equals("1234")) {
                DuckHouse(stage, user);
            } else {
                error.setText("Invalid Username or Password");
                error.setVisible(true);
            }
        });

        Pane layoutPane = new Pane(userBox, passBox, loginButton, error);

        Button signUpBtn = new Button("Sign Up");
        signUpBtn.getStyleClass().add("login-button");
        signUpBtn.setFont(Font.font(9));
        signUpBtn.setPrefWidth(110);
        signUpBtn.setStyle("-fx-font-size: 11px;");
        signUpBtn.setPrefHeight(26);
        signUpBtn.setPadding(new Insets(1, 4, 1, 4));
        signUpBtn.setLayoutX(275);
        signUpBtn.setLayoutY(445);
        signUpBtn.setOnAction(e -> signUpFrame(stage));

        layoutPane.getChildren().add(signUpBtn);

        Image bgImage = new Image(getClass().getResource("/MainLoginGrame.jpg").toExternalForm());
        ImageView bgView = new ImageView(bgImage);
        bgView.setPreserveRatio(false);

        Image logoImage = new Image(getClass().getResource("/ducki.png").toExternalForm());
        ImageView logoView = new ImageView(logoImage);
        logoView.setFitWidth(250);
        logoView.setPreserveRatio(true);
        logoView.setLayoutX(70);
        logoView.setLayoutY(1);

        Pane logoPane = new Pane(logoView);
        logoPane.setMouseTransparent(true);

        StackPane root = new StackPane(bgView, layoutPane, logoPane);
        Scene scene = new Scene(root, 400, 500);
        bgView.fitWidthProperty().bind(scene.widthProperty());
        bgView.fitHeightProperty().bind(scene.heightProperty());
        scene.getStylesheets().add(getClass().getResource("/DuckStyle.css").toExternalForm());

        stage.setTitle("QuackMate - Login");
        Image icon = new Image(getClass().getResource("/QuackMate.png").toExternalForm());
        stage.getIcons().add(icon);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();

        if (mediaPlayer == null) {
            String musicFile = getClass().getResource("/bgmusic.mp3").toExternalForm();
            Media sound = new Media(musicFile);
            mediaPlayer = new MediaPlayer(sound);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.play();
        }
    }

    private void aboutUs(Stage stage) {
        aboutUs(stage, ""); // pass empty username
    }

    public void signUpFrame(Stage stage) {

        StackPane root = new StackPane();

        // Background image
        Image bgImage = new Image(getClass().getResource("/MainLoginGrame.jpg").toExternalForm());
        ImageView bgView = new ImageView(bgImage);
        bgView.setPreserveRatio(false);
        bgView.setFitWidth(400); // match scene width
        bgView.setFitHeight(500); // match scene height

        // --- CENTER CONTAINER ---
        VBox centerBox = new VBox(15);
        centerBox.setAlignment(Pos.CENTER);

        // Title
        Label title = new Label("Create Account");
        title.getStyleClass().add("title-label");

        // Username
        Label userLabel = new Label("Username");
        userLabel.getStyleClass().add("form-label");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");
        usernameField.getStyleClass().add("login-field");
        usernameField.setMaxWidth(160);
        VBox userBox = new VBox(3, userLabel, usernameField);
        userBox.setAlignment(Pos.CENTER);

        // Password
        Label passLabel = new Label("Password");
        passLabel.getStyleClass().add("form-label");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.getStyleClass().add("login-field");
        passwordField.setMaxWidth(160);
        VBox passBox = new VBox(3, passLabel, passwordField);
        passBox.setAlignment(Pos.CENTER);

        // Confirm Password
        Label confirmLabel = new Label("Confirm Password");
        confirmLabel.getStyleClass().add("form-label");
        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Re-enter password");
        confirmField.getStyleClass().add("login-field");
        confirmField.setMaxWidth(160);
        VBox confirmBox = new VBox(3, confirmLabel, confirmField);
        confirmBox.setAlignment(Pos.CENTER);

        // Error message
        Label error = new Label();
        error.getStyleClass().add("error-label");
        error.setVisible(false);

        BorderPane overlay = new BorderPane();
        overlay.setPickOnBounds(false);
        Pane backPane = new Pane();
        Button backBtn = new Button("⬅");
        backBtn.getStyleClass().add("login-button");
        backBtn.setPrefHeight(30);
        backBtn.setPrefWidth(70);
        backBtn.setStyle("-fx-font-size: 13px;");
        backBtn.setOnAction(e -> start(stage));
        backBtn.setLayoutX(10);
        backBtn.setLayoutY(10);

        backPane.getChildren().add(backBtn);
        overlay.setTop(backPane);

        // Create button
        Button signupButton = new Button("Create");
        signupButton.getStyleClass().add("login-button");
        signupButton.setPrefWidth(130);
        signupButton.setOnAction(e -> {
            String user = usernameField.getText();
            String pass = passwordField.getText();
            String confirm = confirmField.getText();

            if (user.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                error.setText("Please fill out all fields");
                error.setVisible(true);
                return;
            }

            if (!pass.equals(confirm)) {
                error.setText("Passwords do not match");
                error.setVisible(true);
                return;
            }

            DuckHouse(stage, user);
        });

        centerBox.getChildren().addAll(title, userBox, passBox, confirmBox, error, signupButton);

        root.getChildren().addAll(bgView, centerBox, overlay);

        Scene scene = new Scene(root, 400, 500);
        scene.getStylesheets().add(getClass().getResource("/DuckStyle.css").toExternalForm());
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    private void DuckHouse(Stage stage, String username) {
        BorderPane layout = sceneTemplate(stage, "house.png", username);
        StackPane root = (StackPane) stage.getScene().getRoot();

        double DEFAULT_SIZE = 80;
        double SELECTED_SIZE = 130;

        // Use unified duck logic
        Image characterToUse = getDuckForScene("/dockie.png");

        ImageView charac = new ImageView(characterToUse);
        charac.setFitWidth((selectedCharacter != null) ? SELECTED_SIZE : DEFAULT_SIZE);
        charac.setPreserveRatio(true);
        StackPane.setAlignment(charac, Pos.BOTTOM_CENTER);
        StackPane.setMargin(charac, new Insets(0, 0, 210, 0));

        if (!isNightMode && !root.getChildren().contains(charac)) root.getChildren().add(charac);

        // SIGN
        Image sign = new Image(getClass().getResource(isNightMode ? "/sign night.png" : "/sign.png").toExternalForm());
        ImageView signView = new ImageView(sign);
        signView.setPreserveRatio(true);
        signView.setFitWidth(isNightMode ? 200 : 180);
        StackPane.setAlignment(signView, Pos.TOP_CENTER);
        StackPane.setMargin(signView, new Insets(isNightMode ? 60 : 70, 0, 0, 0));
        if (!root.getChildren().contains(signView)) root.getChildren().add(signView);

        // USER LABEL
        Label userLabel = new Label(username + "'s House");
        userLabel.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        userLabel.setTextFill(Color.WHITE);
        userLabel.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        userLabel.setPrefWidth(180);
        userLabel.setPrefHeight(40);
        StackPane.setAlignment(userLabel, Pos.TOP_CENTER);
        StackPane.setMargin(userLabel, new Insets(130, 0, 0, 57));
        if (!root.getChildren().contains(userLabel)) root.getChildren().add(userLabel);
    }
    // Dirty duck constant
    private final Image dirtyDuck = new Image(getClass().getResource("/dirtyDuck.png").toExternalForm());

    // Universal duck helper
    private Image getDuckForScene(String defaultFile) {
        if (cleanliness <= 0.3) {
            return dirtyDuck;
        }
        return selectedCharacter != null ? selectedCharacter : new Image(getClass().getResource(defaultFile).toExternalForm());
    }

    // Food quantities matching the number of foods
    private int[] foodQuantities = {4, 3, 5, 2};
    private Label quantityLabel;
    private Button buyButton;
    private Label priceLabel; // class-level for price

    private void kitchen(Stage stage, String username) {
        BorderPane layout = sceneTemplate(stage, "kitchen.png", username);
        StackPane root = (StackPane) stage.getScene().getRoot();

        double DEFAULT_SIZE = 150;
        double SELECTED_SIZE = 255;

        // Use unified duck logic
        Image characterToUse = getDuckForScene("/dockieKitchen.png");

        ImageView duck = new ImageView(characterToUse);
        duck.setFitWidth((selectedCharacter != null) ? SELECTED_SIZE : DEFAULT_SIZE);
        duck.setPreserveRatio(true);
        StackPane.setAlignment(duck, Pos.BOTTOM_CENTER);
        StackPane.setMargin(duck, new Insets(0, -200, 135, 0));

        if (!isNightMode && !root.getChildren().contains(duck)) root.getChildren().add(duck);
        duck.setMouseTransparent(true);

        Pane foodPane = new Pane();
        foodPane.setPrefSize(200, 300);
        layout.setCenter(foodPane);

        Button btnLeft = new Button();
        btnLeft.getStyleClass().add("arrow-button-left");
        btnLeft.setPrefSize(30, 30);
        btnLeft.setLayoutX(20);
        btnLeft.setLayoutY(210);

        Button btnRight = new Button();
        btnRight.getStyleClass().add("arrow-button-right");
        btnRight.setPrefSize(30, 30);
        btnRight.setLayoutX(155);
        btnRight.setLayoutY(210);

        foodPane.getChildren().addAll(btnLeft, btnRight);

        createFood(foodPane, duck);

        btnLeft.setOnAction(e -> {
            foodIndex = (foodIndex - 1 + foods.length) % foods.length;
            updateFood(foodPane, duck);
        });

        btnRight.setOnAction(e -> {
            foodIndex = (foodIndex + 1) % foods.length;
            updateFood(foodPane, duck);
        });

        stage.setScene(new Scene(layout, 400, 500));
        stage.show();
    }



    private void showMouthOpen(ImageView duck) {
        double originalWidth = duck.getFitWidth();
        double originalTranslateX = duck.getTranslateX();
        double originalTranslateY = duck.getTranslateY();

        duck.setImage(new Image(getClass().getResource("/eating.png").toExternalForm()));
        duck.setFitWidth(305);
        duck.setTranslateX(originalTranslateX + 1);
        duck.setTranslateY(originalTranslateY + 60);

        PauseTransition pt = new PauseTransition(Duration.millis(200));
        pt.setOnFinished(e -> {
            duck.setImage(new Image(getClass().getResource("/dockieKitchen.png").toExternalForm()));
            duck.setFitWidth(originalWidth);
            duck.setTranslateX(originalTranslateX);
            duck.setTranslateY(originalTranslateY);
        });
        pt.play();
    }

    private void createFood(Pane foodPane, ImageView duck) {
        // Remove old UI elements before adding new ones
        if (foodDisplay != null) foodPane.getChildren().remove(foodDisplay);
        if (quantityLabel != null) foodPane.getChildren().remove(quantityLabel);
        if (buyButton != null) foodPane.getChildren().remove(buyButton);
        if (priceLabel != null) foodPane.getChildren().remove(priceLabel);

        String foodFile = isNightMode ? foodsNight[foodIndex] : foods[foodIndex];

        // Food image
        foodDisplay = new ImageView(new Image(getClass().getResourceAsStream("/" + foodFile)));
        foodDisplay.setFitWidth(100);
        foodDisplay.setPreserveRatio(true);
        foodDisplay.setLayoutX(53);
        foodDisplay.setLayoutY(125);

        // Quantity label
        quantityLabel = new Label("x" + foodQuantities[foodIndex]);
        quantityLabel.setStyle("-fx-font-family: Comic Sans MS; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        quantityLabel.setLayoutX(130);
        quantityLabel.setLayoutY(100);

        // Buy button
        buyButton = new Button("Buy");
        buyButton.getStyleClass().add("purchase-button");
        buyButton.setPrefWidth(50);
        buyButton.setLayoutX(85);
        buyButton.setLayoutY(230);

        // Price label (only show if quantity == 0)
        if (foodQuantities[foodIndex] == 0) {
            priceLabel = new Label("10"); // fixed price
            priceLabel.setStyle("-fx-font-family: Comic Sans MS; -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: yellow;");
            priceLabel.setLayoutX(100);
            priceLabel.setLayoutY(205); // above buy button
            buyButton.setVisible(true);
            foodPane.getChildren().add(priceLabel);
        } else {
            priceLabel = null;
            buyButton.setVisible(false);
        }

        buyButton.setOnAction(e -> {
            foodQuantities[foodIndex] += 5;
            updateFood(foodPane, duck);
        });

        makeDraggable(foodDisplay, duck, foodPane);

        foodPane.getChildren().addAll(foodDisplay, quantityLabel, buyButton);
    }


    private void updateFood(Pane foodPane, ImageView duck) {
        if (foodDisplay != null) foodPane.getChildren().remove(foodDisplay);
        if (quantityLabel != null) foodPane.getChildren().remove(quantityLabel);
        if (buyButton != null) foodPane.getChildren().remove(buyButton);
        if (priceLabel != null) foodPane.getChildren().remove(priceLabel);

        createFood(foodPane, duck);
    }

    // Distance check
    private boolean isFoodNearDuck(ImageView food, ImageView duck, double threshold) {
        double duckCenterX = duck.getBoundsInParent().getMinX() + duck.getBoundsInParent().getWidth() / 2;
        double duckCenterY = duck.getBoundsInParent().getMinY() + duck.getBoundsInParent().getHeight() / 2;

        double foodCenterX = food.getBoundsInParent().getMinX() + food.getBoundsInParent().getWidth() / 2;
        double foodCenterY = food.getBoundsInParent().getMinY() + food.getBoundsInParent().getHeight() / 2;

        double distance = Math.hypot(duckCenterX - foodCenterX, duckCenterY - foodCenterY);
        return distance <= threshold;
    }

    // Make food draggable + feeding logic
    private void makeDraggable(ImageView food, ImageView duck, Pane pane) {
        final Delta dragDelta = new Delta();

        food.setOnMousePressed(e -> {
            dragDelta.x = e.getSceneX() - food.getLayoutX();
            dragDelta.y = e.getSceneY() - food.getLayoutY();
            food.toFront();
        });

        food.setOnMouseDragged(e -> {
            food.setLayoutX(e.getSceneX() - dragDelta.x);
            food.setLayoutY(e.getSceneY() - dragDelta.y);
            food.toFront();

            if (isFoodNearDuck(food, duck, 80)) {
                showMouthOpen(duck);
            }
        });

        food.setOnDragDetected(e -> {
            food.startFullDrag();
            duck.setMouseTransparent(false);
            food.toFront();
            e.consume();
        });

        duck.setOnMouseDragReleased(e -> {
            Object src = e.getGestureSource();
            if (src instanceof ImageView draggedFood) {
                draggedFood.toFront();
                showMouthOpen(duck);

                if (foodQuantities[foodIndex] > 0) {
                    foodQuantities[foodIndex]--;
                }
                updateFood(pane, duck);
            }
            duck.setMouseTransparent(true);
        });

        food.setOnMouseReleased(e -> duck.setMouseTransparent(true));
    }

    // Drag helper
    private static class Delta { double x, y; }

    private ImageView brushBubble;
    private FadeTransition brushBubbleFade;


    private void bathRoom(Stage stage, String username) {
        BorderPane layout = sceneTemplate(stage, "cr.png", username);
        StackPane root = (StackPane) stage.getScene().getRoot();

        double DEFAULT_SIZE = 150;
        double SELECTED_SIZE = 250;

        // Use unified duck logic
        Image characterToUse = getDuckForScene("/dockieBath.png");

        ImageView dockieView = null;
        if (!isNightMode) {
            dockieView = new ImageView(characterToUse);
            dockieView.setFitWidth((selectedCharacter != null) ? SELECTED_SIZE : DEFAULT_SIZE);
            dockieView.setPreserveRatio(true);
            StackPane.setAlignment(dockieView, Pos.CENTER);
            dockieView.setTranslateY(20);
            root.getChildren().add(dockieView);
        }

        // --- Bucket and Brush images, day/night versions ---
        String bucketFile = isNightMode ? "/water (2).png" : "/water (1).png";
        String brushFile  = isNightMode ? "/scrub night.png" : "/scrub.png";

        Image bucket = new Image(getClass().getResource(bucketFile).toExternalForm());
        Image brush  = new Image(getClass().getResource(brushFile).toExternalForm());

        ImageView brushView = new ImageView(brush);
        ImageView bucketView = new ImageView(bucket);

        if (isNightMode) {
            brushView.setFitWidth(80);
            brushView.setFitHeight(125);
        } else {
            brushView.setFitWidth(60);
            brushView.setFitHeight(60);
        }

        bucketView.setFitWidth(60);
        bucketView.setFitHeight(60);

        // --- Buttons ---
        Button bucketBtn = new Button();
        bucketBtn.setGraphic(bucketView);
        bucketBtn.setStyle("-fx-background-color: transparent;");

        Button brushBtn = new Button();
        brushBtn.setGraphic(brushView);
        brushBtn.setStyle("-fx-background-color: transparent;");

        StackPane.setAlignment(brushBtn, Pos.CENTER_RIGHT);
        brushBtn.setTranslateX(-20);
        brushBtn.setTranslateY(40);

        StackPane.setAlignment(bucketBtn, Pos.CENTER_LEFT);
        bucketBtn.setTranslateX(10);
        bucketBtn.setTranslateY(40);

        root.getChildren().addAll(bucketBtn, brushBtn);

        double[] bucketOrig = { bucketBtn.getTranslateX(), bucketBtn.getTranslateY() };
        double[] brushOrig = { brushBtn.getTranslateX(), brushBtn.getTranslateY() };

        // Make draggable
        makeDraggable(brushBtn, brushOrig, dockieView, root, true);
        makeDraggable(bucketBtn, bucketOrig, dockieView, root, false);
    }


    private void makeDraggable(Button btn, double[] origPos, ImageView duck, StackPane parentForBubbles, boolean isBrush) {
        final double[] mouseOffset = new double[2];

        // --- Sound Effect for bucket ---
        AudioClip splashSound = new AudioClip(getClass().getResource("/WaterSplashSFX.mp3").toExternalForm());

        // Splash for bucket
        Image splashImage = new Image(getClass().getResource("/splash.png").toExternalForm());
        ImageView splash = new ImageView(splashImage);
        splash.setFitWidth(200);
        splash.setPreserveRatio(true);
        splash.setVisible(false);

        // Brush bubble (only if brush)
        if (isBrush) {
            brushBubble = new ImageView(new Image(getClass().getResource("/bubbles.png").toExternalForm()));
            brushBubble.setFitWidth(250);
            brushBubble.setPreserveRatio(true);
            brushBubble.setVisible(false);
            parentForBubbles.getChildren().add(brushBubble);
            brushBubble.toFront();

            brushBubbleFade = new FadeTransition(Duration.millis(4000), brushBubble);
            brushBubbleFade.setFromValue(0);
            brushBubbleFade.setToValue(1);
        }

        StackPane parent = (StackPane) duck.getParent();
        parent.getChildren().add(splash);
        splash.toFront();

        btn.setOnMousePressed(e -> {
            mouseOffset[0] = e.getSceneX() - btn.getTranslateX();
            mouseOffset[1] = e.getSceneY() - btn.getTranslateY();
            btn.toFront();
        });

        btn.setOnMouseDragged(e -> {
            btn.setTranslateX(e.getSceneX() - mouseOffset[0]);
            btn.setTranslateY(e.getSceneY() - mouseOffset[1]);

            if (isBrush && brushBubble != null && btn.getBoundsInParent().intersects(duck.getBoundsInParent())) {
                // --- Bubble follows brush with adjustable position ---
                double offsetX = -40;
                double offsetY = 150;

                double brushX = btn.getTranslateX() + btn.getWidth() / 2 + offsetX;
                double brushY = btn.getTranslateY() + btn.getHeight() / 2 + offsetY;

                brushBubble.setLayoutX(brushX - brushBubble.getFitWidth() / 2);
                brushBubble.setLayoutY(brushY - brushBubble.getFitHeight() / 2);

                if (!brushBubble.isVisible()) {
                    brushBubble.setVisible(true);
                    brushBubbleFade.playFromStart();
                }
            }

            btn.toFront();
        });

        btn.setOnMouseReleased(e -> {
            Bounds btnBounds = btn.localToScene(btn.getBoundsInLocal());
            Bounds duckBounds = duck.localToScene(duck.getBoundsInLocal());

            if (btnBounds.intersects(duckBounds)) {
                if (!isBrush) { // bucket splash
                    splashSound.play();

                    double duckCenterX = (duckBounds.getMinX() + duckBounds.getMaxX()) / 2;
                    double duckCenterY = (duckBounds.getMinY() + duckBounds.getMaxY()) / 2;

                    Point2D splashPos = parent.sceneToLocal(duckCenterX, duckCenterY);
                    splash.setLayoutX(splashPos.getX() - splash.getFitWidth() / 2);
                    splash.setLayoutY(splashPos.getY() - splash.getFitHeight() / 2);
                    splash.setVisible(true);

                    PauseTransition pt = new PauseTransition(Duration.millis(500));
                    pt.setOnFinished(ev -> splash.setVisible(false));
                    pt.play();

                    if (brushBubble != null) {
                        brushBubble.setVisible(false);
                        brushBubbleFade.stop();
                    }
                }
            }

            btn.setTranslateX(origPos[0]);
            btn.setTranslateY(origPos[1]);
            duck.setOpacity(1.0);
        });
    }


    private boolean isNightMode = false; // global flag
    private void bedRoom(Stage stage, String username) {
        BorderPane layout = sceneTemplate(stage, "room.png", username);
        StackPane root = (StackPane) stage.getScene().getRoot();

        // --- Duck Images ---
        Image awakeDuck = (selectedCharacter != null) ? selectedCharacter : new Image(getClass().getResource("/dockieBed.png").toExternalForm());
        Image sleepingDuck = new Image(getClass().getResource("/DuckSleeping.png").toExternalForm());

        // --- Lamp Images ---
        Image lampOff = new Image(getClass().getResource("/lambing.png").toExternalForm());
        Image lampOn = new Image(getClass().getResource("/lamning.png").toExternalForm());

        // --- Background Images ---
        Image bgDay = new Image(getClass().getResource("/room.png").toExternalForm());
        Image bgNight = new Image(getClass().getResource("/nightver.png").toExternalForm());

        // --- Duck ImageView ---
        double DEFAULT_SIZE = 150;
        double SELECTED_SIZE = 250;
        ImageView duckView = new ImageView(isNightMode ? sleepingDuck : awakeDuck);
        double duckWidth = isNightMode ? 210 : ((selectedCharacter != null) ? SELECTED_SIZE : DEFAULT_SIZE);
        duckView.setFitWidth(duckWidth);
        duckView.setPreserveRatio(true);
        StackPane.setAlignment(duckView, Pos.BOTTOM_CENTER);
        StackPane.setMargin(duckView, isNightMode ? new Insets(0, 0, 160, 0) : new Insets(0, 0, 180, 0));
        if (!root.getChildren().contains(duckView)) {
            root.getChildren().add(duckView);
        }

        // --- Lamp Button ---
        ImageView lampView = new ImageView(isNightMode ? lampOff : lampOn);
        lampView.setFitWidth(80);
        lampView.setPreserveRatio(true);

        ToggleButton lampButton = new ToggleButton();
        lampButton.setGraphic(lampView);
        lampButton.getStyleClass().add("lamp-button");
        lampButton.setSelected(!isNightMode); // lamp ON if day

        lampButton.setOnAction(e -> {
            if (lampButton.isSelected()) {
                // Lamp ON → day mode
                isNightMode = false;
                lampView.setImage(lampOn);
                ((ImageView) root.getChildren().get(0)).setImage(bgDay);
                duckView.setImage(awakeDuck);
            } else {
                // Lamp OFF → night mode
                isNightMode = true;
                lampView.setImage(lampOff);
                ((ImageView) root.getChildren().get(0)).setImage(bgNight);
                duckView.setImage(sleepingDuck);
            }

            // Adjust duck size & margin according to mode
            double updatedWidth = isNightMode ? 210 : ((selectedCharacter != null) ? SELECTED_SIZE : DEFAULT_SIZE);
            duckView.setFitWidth(updatedWidth);
            StackPane.setMargin(duckView, isNightMode ? new Insets(0, 0, 160, 0) : new Insets(0, 0, 180, 0));
        });

        StackPane.setAlignment(lampButton, Pos.TOP_RIGHT);
        StackPane.setMargin(lampButton, new Insets(60, 30, 0, 0));
        if (!root.getChildren().contains(lampButton)) {
            root.getChildren().add(lampButton);
        }
    }

    private int outfitIndex = 0;
    private int playerLevel = 1; // Class-level player level, can update from stats

    private void closet(Stage stage, String username) {
        BorderPane layout = sceneTemplate(stage, "closet.png", username);
        StackPane root = (StackPane) stage.getScene().getRoot();

        // --- Character Images & Level requirements ---
        Image[] characters = new Image[] {
                new Image(getClass().getResource("DuckCatHat.png").toExternalForm()),   // Level 10
                new Image(getClass().getResource("DuckFrogHat.png").toExternalForm()),  // Level 20
                new Image(getClass().getResource("DuckStitchHat.png").toExternalForm()) // Level 30
        };
        int[] levelRequired = {10, 20, 30};

        // Ownership is now automatic based on playerLevel
        boolean[] owned = new boolean[characters.length];
        for (int i = 0; i < characters.length; i++) {
            owned[i] = playerLevel >= levelRequired[i];
        }

        // Main preview
        ImageView charac = new ImageView(characters[0]);
        charac.setFitWidth(250);
        charac.setPreserveRatio(true);
        StackPane.setAlignment(charac, Pos.BOTTOM_CENTER);
        StackPane.setMargin(charac, new Insets(0, 0, 180, 18));
        if (!isNightMode && !root.getChildren().contains(charac))
            root.getChildren().add(charac);

        // --- Arrow Buttons ---
        Button leftArrow = new Button();
        leftArrow.getStyleClass().add("arrow-button-left");
        Button rightArrow = new Button();
        rightArrow.getStyleClass().add("arrow-button-right");
        StackPane.setAlignment(leftArrow, Pos.CENTER_LEFT);
        StackPane.setAlignment(rightArrow, Pos.CENTER_RIGHT);
        StackPane.setMargin(leftArrow, new Insets(0, 0, 0, 10));
        StackPane.setMargin(rightArrow, new Insets(0, 10, 0, 0));
        root.getChildren().addAll(leftArrow, rightArrow);

        // --- Buttons ---
        Button useBtn = new Button("Use");
        useBtn.getStyleClass().add("buy-button");
        useBtn.setPrefSize(120, 25);

        Button removeBtn = new Button("Remove");
        removeBtn.getStyleClass().add("buy-button");
        removeBtn.setPrefSize(120, 25);

        HBox buttonBox = new HBox(20, useBtn, removeBtn);
        buttonBox.setAlignment(Pos.CENTER);
        StackPane.setAlignment(buttonBox, Pos.BOTTOM_CENTER);
        StackPane.setMargin(buttonBox, new Insets(300, 0, 80, 0));
        root.getChildren().add(buttonBox);

        // --- Update button state ---
        Runnable updateButtons = () -> {
            if (owned[outfitIndex]) {
                useBtn.setText("Use");
                useBtn.setDisable(false);
                removeBtn.setDisable(false);
            } else {
                useBtn.setText("Unlock at Lv " + levelRequired[outfitIndex]);
                useBtn.setDisable(true);
                removeBtn.setDisable(true);
            }
        };

        // --- Arrow button actions ---
        leftArrow.setOnAction(e -> {
            outfitIndex = (outfitIndex - 1 + characters.length) % characters.length;
            charac.setImage(characters[outfitIndex]);
            updateButtons.run();
        });

        rightArrow.setOnAction(e -> {
            outfitIndex = (outfitIndex + 1) % characters.length;
            charac.setImage(characters[outfitIndex]);
            updateButtons.run();
        });

        // --- Use & Remove actions ---
        useBtn.setOnAction(e -> {
            if (owned[outfitIndex]) {
                selectedCharacter = characters[outfitIndex];
            }
        });

        removeBtn.setOnAction(e -> {
            if (owned[outfitIndex]) {
                selectedCharacter = null;
            }
        });

        updateButtons.run(); // initialize buttons for first character
    }

    // --- Helper method for creating picture buttons ---
    private Button makePicButton(String fileName, int size) {
        Image img = new Image(getClass().getResource("/" + fileName + ".png").toExternalForm());
        ImageView view = new ImageView(img);
        view.setFitWidth(size);
        view.setFitHeight(size);
        view.setPreserveRatio(true);

        Button b = new Button();
        b.setGraphic(view);
        b.getStyleClass().add("duck-button");
        b.setPrefSize(size + 15, size + 15);
        b.setMinSize(size + 15, size + 15);
        b.setMaxSize(size + 15, size + 15);
        return b;
    }

// --- Rest of your code (sceneTemplate, statsFrame, makeStat, etc.) remains unchanged ---


    // --- Main scene template ---
    private BorderPane sceneTemplate(Stage stage, String bgFile, String username) {

        // Night mode map
        Map<String, String> nightModeMap = new HashMap<>();
        nightModeMap.put("house.png", "home night.png");
        nightModeMap.put("kitchen.png", "kitchen night.png");
        nightModeMap.put("cr.png", "bath night.png");
        nightModeMap.put("room.png", "nightver.png");
        nightModeMap.put("closet.png", "closet.png");

        if (isNightMode && nightModeMap.containsKey(bgFile)) {
            bgFile = nightModeMap.get(bgFile);
        }

        // Background
        Image backGround = new Image(getClass().getResource("/" + bgFile).toExternalForm());
        ImageView bg = new ImageView(backGround);
        bg.setPreserveRatio(false);

        // ---------- TOP LEFT BUTTONS (image buttons) ----------
        Button duckMenuBtn = makePicButton("duckIcon", 40);
        duckMenuBtn.setOnAction(e -> statsFrame(stage, username));

        Button topPlayBtn = makePicButton("gameIcon", 40);
        topPlayBtn.setOnAction(e -> GamesFrame(stage, username));

        // Settings & switch remain normal
        Button settingsBtn = new Button("⚙");
        settingsBtn.getStyleClass().add("mute-button");
        settingsBtn.setOnAction(e -> SettingsFrame(stage, username));

        ToggleButton switchBtn = new ToggleButton();
        switchBtn.getStyleClass().add("switch-button");
        switchBtn.setOnAction(e -> System.out.println(switchBtn.isSelected()));

        HBox topControls = new HBox(10, settingsBtn, switchBtn);
        topControls.setAlignment(Pos.CENTER_LEFT);

        VBox leftStack = new VBox(5, topControls, duckMenuBtn, topPlayBtn);
        leftStack.setAlignment(Pos.TOP_LEFT);

        // ---------- BOTTOM NAV PIC BUTTONS ----------
        Button btn1 = makePicButton("houseIcon", 35);
        Button btn2 = makePicButton("bedIcon", 35);
        Button btn3 = makePicButton("foodIcon", 35);
        Button btn4 = makePicButton("closetIcon", 35);
        Button btn5 = makePicButton("bathIcon", 35);

        btn1.setOnAction(e -> DuckHouse(stage, username));
        btn2.setOnAction(e -> bedRoom(stage, username));
        btn3.setOnAction(e -> kitchen(stage, username));
        btn4.setOnAction(e -> closet(stage, username));
        btn5.setOnAction(e -> bathRoom(stage, username));

        HBox bottomRow = new HBox(20, btn1, btn2, btn3, btn4, btn5);
        bottomRow.setAlignment(Pos.CENTER);
        bottomRow.setPadding(new Insets(10));

        // Build layout
        BorderPane layout = new BorderPane();
        layout.setTop(leftStack);
        layout.setBottom(bottomRow);

        StackPane root = new StackPane(bg, layout);

        Scene scene = new Scene(root, 400, 500);
        scene.getStylesheets().add(getClass().getResource("/DuckStyle.css").toExternalForm());

        bg.fitWidthProperty().bind(scene.widthProperty());
        bg.fitHeightProperty().bind(scene.heightProperty());

        stage.setScene(scene);
        stage.setResizable(false);
        stage.setMaximized(false);
        stage.show();

        return layout;
    }

    public void statsFrame(Stage stage, String username) {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #fff9d6;");

        Pane card = new Pane();
        card.setPrefSize(340, 420);

        VBox centerBox = new VBox(35);
        centerBox.setAlignment(Pos.CENTER);

        centerBox.layoutXProperty().bind(
                card.widthProperty().subtract(centerBox.widthProperty()).divide(2)
        );
        centerBox.layoutYProperty().bind(
                card.heightProperty().subtract(centerBox.heightProperty()).divide(2)
        );

        Label levelLabel = new Label("Level 1");
        levelLabel.getStyleClass().add("level-badge");

        VBox statsBox = new VBox(25);
        statsBox.setAlignment(Pos.CENTER);

        statsBox.getChildren().addAll(
                makeStat("Happiness", happiness),
                makeStat("Hunger", hunger),
                makeStat("Energy", energy),
                makeStat("Cleanliness", cleanliness)
        );

        centerBox.getChildren().addAll(levelLabel, statsBox);
        card.getChildren().add(centerBox);

        // --- Coins Counter ---
        int playerCoins = 0;
        Label coinsLabel = new Label("Coins: " + playerCoins); // assume playerCoins is an int
        coinsLabel.getStyleClass().add("coins-counter");
        StackPane.setAlignment(coinsLabel, Pos.BOTTOM_CENTER);
        StackPane.setMargin(coinsLabel, new Insets(0, 0, 20, 0));

        BorderPane overlay = new BorderPane();
        overlay.setPickOnBounds(false);

        Pane homePane = new Pane();
        Button homeBtn = new Button("🏠");
        homeBtn.getStyleClass().add("duck-button");
        homeBtn.setOnAction(e -> DuckHouse(stage, username));
        homeBtn.setLayoutX(10);
        homeBtn.setLayoutY(10);

        homePane.getChildren().add(homeBtn);
        overlay.setTop(homePane);

        root.getChildren().addAll(card, overlay, coinsLabel); // add coinsLabel here

        Scene scene = new Scene(root, 400, 500);
        scene.getStylesheets().add(getClass().getResource("/DuckStyle.css").toExternalForm());

        stage.setScene(scene);
        stage.show();
    }


    private VBox makeStat(String name, double value){
        Label label = new Label(name);
        label.getStyleClass().add("stat-label");

        ProgressBar bar = new ProgressBar(value);
        bar.getStyleClass().add("stat-bar");
        bar.setPrefWidth(230);

        VBox v = new VBox(5, label, bar);
        v.setAlignment(Pos.CENTER_LEFT);
        return v;
    }
    private void GamesFrame(Stage stage, String username) {
        Image bg = new Image(getClass().getResource("/game.png").toExternalForm());
        ImageView backGround = new ImageView(bg);
        backGround.setPreserveRatio(false);

        Image playIcon = new Image(getClass().getResource("/game2.png").toExternalForm());
        ImageView playView = new ImageView(playIcon);
        playView.setFitWidth(200);
        playView.setFitHeight(200);
        playView.setPreserveRatio(true);

        Button picBtn = new Button();
        picBtn.setGraphic(playView);
        picBtn.getStyleClass().add("duck-button");
        picBtn.setStyle("-fx-background-radius: 0; -fx-padding: 0;");
        picBtn.setOnAction(e -> cardFlip(stage, username));
        picBtn.setOnMousePressed(e -> { picBtn.setScaleX(0.9); picBtn.setScaleY(0.9); });
        picBtn.setOnMouseReleased(e -> { picBtn.setScaleX(1.0); picBtn.setScaleY(1.0); });

        Image playIcon2 = new Image(getClass().getResource("/duckierun.png").toExternalForm());
        ImageView playView2 = new ImageView(playIcon2);
        playView2.setFitWidth(200);
        playView2.setFitHeight(200);
        playView2.setPreserveRatio(true);

        Button picBtn2 = new Button();
        picBtn2.setGraphic(playView2);
        picBtn2.getStyleClass().add("duck-button");
        picBtn2.setStyle("-fx-background-radius: 0; -fx-padding: 0;");
        picBtn2.setOnAction(e -> flappyDuck(stage, username));
        picBtn2.setOnMousePressed(e -> { picBtn2.setScaleX(0.9); picBtn2.setScaleY(0.9); });
        picBtn2.setOnMouseReleased(e -> { picBtn2.setScaleX(1.0); picBtn2.setScaleY(1.0); });

        VBox buttonBox = new VBox(20, picBtn, picBtn2);
        buttonBox.setAlignment(Pos.CENTER);

        Button btn1 = new Button("⬅");
        btn1.getStyleClass().addAll("duck-button", "house-button");
        btn1.setPrefSize(50, 50);
        btn1.setOnAction(e -> DuckHouse(stage, username));

        BorderPane layout = new BorderPane();
        layout.setTop(btn1);
        BorderPane.setAlignment(btn1, Pos.TOP_LEFT);
        layout.setCenter(buttonBox);

        StackPane root = new StackPane(backGround, layout);
        Scene scene = new Scene(root, 400, 500, Color.BLACK);
        scene.getStylesheets().add(getClass().getResource("/DuckStyle.css").toExternalForm());

        backGround.fitWidthProperty().bind(scene.widthProperty());
        backGround.fitHeightProperty().bind(scene.heightProperty());

        stage.setScene(scene);
        stage.setResizable(false);
        stage.setMaximized(false);
        stage.show();
    }

    public void flappyDuck (Stage stage, String username){
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #fff9d6;");

        Scene scene = new Scene(root, 400, 500);
        scene.getStylesheets().add(getClass().getResource("/DuckStyle.css").toExternalForm());

        Button backBtn = new Button("❮");
        backBtn.getStyleClass().add("back-button");
        backBtn.setOnAction(e -> GamesFrame(stage, username));

        StackPane.setAlignment(backBtn, Pos.TOP_LEFT);
        StackPane.setMargin(backBtn, new Insets(10, 0, 0, 10));

        root.getChildren().add(backBtn);

        stage.setScene(scene);
        stage.show();
    }

    public void cardFlip (Stage stage, String username){
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #fff9d6;");

        Scene scene = new Scene(root, 400, 500);
        scene.getStylesheets().add(getClass().getResource("/DuckStyle.css").toExternalForm());

        Button backBtn = new Button("❮");
        backBtn.getStyleClass().add("back-button");
        backBtn.setOnAction(e -> GamesFrame(stage, username));

        StackPane.setAlignment(backBtn, Pos.TOP_LEFT);
        StackPane.setMargin(backBtn, new Insets(10, 0, 0, 10));

        root.getChildren().add(backBtn);

        stage.setScene(scene);
        stage.show();
    }
    public void SettingsFrame(Stage stage, String username) {

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #fff9d6;"); // Set background color

        // Duck image
        Image duckImg = new Image(getClass().getResource("/dockie.png").toExternalForm());
        ImageView duckView = new ImageView(duckImg);
        duckView.setFitWidth(150);
        duckView.setPreserveRatio(true);
        duckView.setLayoutX(130);
        duckView.setLayoutY(30);

        // Powered by image
        Image poweredImg = new Image(getClass().getResource("/powered.png").toExternalForm());
        ImageView poweredBy = new ImageView(poweredImg);
        poweredBy.setFitWidth(200);
        poweredBy.setPreserveRatio(true);

        // Buttons
        Button btn1 = new Button("⬅");
        btn1.getStyleClass().addAll("logout-button", "house-button");
        btn1.setPrefSize(50, 50);
        btn1.setOnAction(e -> DuckHouse(stage, username));
        btn1.setLayoutX(10);
        btn1.setLayoutY(10);

        Button btn2 = new Button("Log Out");
        btn2.getStyleClass().addAll("logout-button");
        btn2.setPrefSize(125, 35);
        btn2.setLayoutX(140);
        btn2.setLayoutY(230);
        btn2.setAlignment(Pos.CENTER);
        btn2.setOnAction(e -> start(stage));

        // Mute Button
        Button muteBtn = new Button("🔇");
        muteBtn.getStyleClass().add("mute-button");
        muteBtn.setPrefSize(50, 50);
        muteBtn.setLayoutX(330);
        muteBtn.setLayoutY(10);
        muteBtn.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.setMute(!mediaPlayer.isMute());
                muteBtn.setText(mediaPlayer.isMute() ? "🔇" : "🔊");
            }
        });

        Image i = new Image(getClass().getResource("/i.png").toExternalForm());
        ImageView iView = new ImageView(i);
        iView.setFitWidth(40);
        iView.setPreserveRatio(true);

        Button infoBtn = new Button();
        infoBtn.setGraphic(iView);
        infoBtn.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        infoBtn.setShape(new Circle(25)); // shape
        infoBtn.setLayoutX(355); // adjust pa left or right
        infoBtn.setLayoutY(460); // adjust pa up or down
        infoBtn.setOnAction(e -> aboutUs(stage, username));

        Pane buttonPane = new Pane(btn1, btn2, duckView, muteBtn, infoBtn);

        root.getChildren().addAll(buttonPane, poweredBy);

        StackPane.setAlignment(poweredBy, Pos.BOTTOM_CENTER);
        StackPane.setMargin(poweredBy, new Insets(0, 0, 10, 0));

        Scene scene = new Scene(root, 400, 500);
        scene.getStylesheets().add(getClass().getResource("/DuckStyle.css").toExternalForm());

        stage.setResizable(false);
        stage.setMaximized(false);
        stage.setScene(scene);
        stage.show();
    }


    private void aboutUs(Stage stage, String username) {
        StackPane root = new StackPane();

        Image bg = new Image(getClass().getResource("/MainLoginGrame.jpg").toExternalForm());
        ImageView backGround = new ImageView(bg);
        backGround.setPreserveRatio(false);
        root.getChildren().add(backGround);

        Image sign = new Image(getClass().getResource("/QuackNet.png").toExternalForm());
        ImageView signView = new ImageView(sign);
        signView.setFitWidth(250);
        signView.setPreserveRatio(true);
        StackPane.setAlignment(signView, Pos.TOP_CENTER);
        StackPane.setMargin(signView, new Insets(-60, 0, 0, 0));
        root.getChildren().add(signView);

        Button backBtn = new Button("❮");
        backBtn.getStyleClass().add("back-button2");
        backBtn.setOnAction(e -> SettingsFrame(stage, username));
        StackPane.setAlignment(backBtn, Pos.TOP_LEFT);
        StackPane.setMargin(backBtn, new Insets(10, 0, 0, 10));
        root.getChildren().add(backBtn);

        int buttonSize = 80;

        BiFunction<String, String, VBox> createProfile = (img, name) -> {
            Button picBtn = createCircularImageButton(img, buttonSize);

            Label label = new Label(name);
            label.getStyleClass().add("form-label");
            label.setFont(Font.font(10));
            label.setAlignment(Pos.CENTER);
            label.setMaxWidth(buttonSize);

            VBox box = new VBox(5, picBtn, label);
            box.setAlignment(Pos.CENTER);

            return box;
        };

        VBox p1 = createProfile.apply("/joshua.png", "Joshua Largado");
        VBox p2 = createProfile.apply("/katleen.png", "Katleen Kriones");
        VBox p3 = createProfile.apply("/zash.png", "Zashkie Bontia");

        HBox hBoxTop = new HBox(20, p1, p2, p3);
        hBoxTop.setLayoutX(35);
        hBoxTop.setLayoutY(110);

        VBox p4 = createProfile.apply("/grace.png", "Grace Galua");
        VBox p5 = createProfile.apply("/hazel.png", "Hazel Brigoli");
        VBox p6 = createProfile.apply("/flor.png", "Flor Gamali");

        HBox hBoxBottom = new HBox(20, p4, p5, p6);
        hBoxBottom.setLayoutX(35);
        hBoxBottom.setLayoutY(270);

        Pane floatingPane = new Pane(hBoxTop, hBoxBottom);
        floatingPane.setPickOnBounds(false);
        root.getChildren().add(floatingPane);

        Scene scene = new Scene(root, 400, 500);
        scene.getStylesheets().add(getClass().getResource("/DuckStyle.css").toExternalForm());

        backGround.fitWidthProperty().bind(scene.widthProperty());
        backGround.fitHeightProperty().bind(scene.heightProperty());

        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    private Button createCircularImageButton(String imagePath, int size) {
        Image img = new Image(getClass().getResource(imagePath).toExternalForm());
        ImageView imgView = new ImageView(img);
        imgView.setFitWidth(size);
        imgView.setFitHeight(size);
        imgView.setPreserveRatio(true);

        Circle clip = new Circle(size / 2, size / 2, size / 2);
        imgView.setClip(clip);

        Button btn = new Button();
        btn.setStyle("-fx-background-color: transparent;");
        btn.setGraphic(imgView);
        btn.setPrefSize(size, size);
        return btn;
    }

    public static void main(String[] args) {
        launch(args);
    }
}