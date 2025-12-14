import backend.models.Food;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import backend.services.GameBridge;

import java.net.URL;
import java.util.*;
import java.util.function.BiFunction;



public class Main extends Application {

    private static MediaPlayer mediaPlayer;

    // UPDATE: Replace hardcoded stats with GameBridge integration
    private GameBridge gameBridge = GameBridge.getInstance();

    // UPDATE: These will be populated from GameBridge
    private double happiness = gameBridge.getStat("HAPPINESS");
    private double hunger = gameBridge.getStat("HUNGER");
    private double energy = gameBridge.getStat("ENERGY");
    private double cleanliness = gameBridge.getStat("CLEANLINESS");

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
    private final String CARD_BACK = "/OrigBackPart.png";
    private final List<String> ALL_DUCKS = Arrays.asList(
            "/BlueDuckCard.png", "/BrownDuckCard.png", "/GrayDuckCard.png",
            "/GreenDuckCard.png", "/OrangeDuckCard.png", "/PinkDuckCard.png",
            "/PlatinumDuckCard.png", "/PurpleDuckCard.png", "/RedDuckCard.png",
            "/WhiteDuckCard.png", "/YellowDuckCard.png"
    );

    private int credits = 0;
    private int roundCredits = 0;
    private int mistakes = 0;
    private int level = 0;

    private Label creditsLabel, levelLabel, mistakesLabel;
    private Stage window;
    private final Random rng = new Random();
    private final int SCENE_W = 400;
    private final int SCENE_H = 500;

    private Card firstSelected = null;

    private final String MAIN_BTN_STYLE = "-fx-background-color: linear-gradient(to bottom, #ffffff, #e0e0e0); " +
            "-fx-text-fill: #2c3e50; -fx-font-family: 'Arial'; -fx-font-size: 16px; -fx-font-weight: bold; " +
            "-fx-background-radius: 15; -fx-border-radius: 15; -fx-border-color: #34495e; -fx-border-width: 2; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2); -fx-cursor: hand;";

    private final String DIFF_BTN_BASE = "-fx-font-family: 'Monospace'; -fx-font-size: 14px; -fx-font-weight: bold; " +
            "-fx-background-radius: 15; -fx-border-radius: 15; -fx-border-width: 2; -fx-text-fill: white; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 4, 0, 0, 1); -fx-cursor: hand;";

    private final String BACK_BTN_STYLE = "-fx-background-color: linear-gradient(to bottom, #fff9c4, #fff59d); " + // light yellow gradient
            "-fx-text-fill: #2c3e50; " +
            "-fx-font-size: 18px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 50; " +   // very rounded (circular)
            "-fx-border-radius: 50; " +
            "-fx-border-color: #f0e68c; " +   // subtle border
            "-fx-border-width: 1; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 3, 0, 0, 1); " +
            "-fx-cursor: hand;";

    // UPDATE: Food quantities will be managed by GameBridge
    private int[] foodQuantities = {4, 3, 5, 2};
    private Label quantityLabel;
    private Button buyButton;
    private Label priceLabel; // class-level for price
    private Image currentDuckImage; // tracks the current base duck image

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

        // UPDATE: Replace hardcoded login with GameBridge login
        loginButton.setOnAction(e -> {
            String user = usernameField.getText();
            String pass = passwordField.getText();

            // UPDATE: Use GameBridge for authentication
            if (gameBridge.login(user, pass)) {
                // UPDATE: Sync stats from GameBridge after successful login
                updateStatsFromGameBridge();
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
    /**
     * HELPERS
     * */
    // ADD LINE: Helper method to update stats from GameBridge
    private void updateStatsFromGameBridge() {
        happiness = gameBridge.getStat("HAPPINESS");
        hunger = gameBridge.getStat("HUNGER");
        energy = gameBridge.getStat("ENERGY");
        cleanliness = gameBridge.getStat("CLEANLINESS");

        // UPDATE: Sync food quantities from GameBridge
        updateFoodQuantitiesFromGameBridge();
    }

    // ADD LINE: Helper method to update food quantities from GameBridge
    private void updateFoodQuantitiesFromGameBridge() {
        for (int i = 0; i < foods.length; i++) {
            foodQuantities[i] = gameBridge.getFoodQuantity(i);
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

        // UPDATE: Replace with GameBridge registration
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

            // UPDATE: Use GameBridge for registration
            if (gameBridge.register(user, pass)) {
                // UPDATE: Sync stats from GameBridge after successful registration
                updateStatsFromGameBridge();
                DuckHouse(stage, user);
            } else {
                error.setText("Registration failed. Username may already exist.");
                error.setVisible(true);
            }
        });

        centerBox.getChildren().addAll(title, userBox, passBox, confirmBox, error, signupButton);

        root.getChildren().addAll(bgView, centerBox, overlay);

        Scene scene = new Scene(root, 400, 500);
        scene.getStylesheets().add(getClass().getResource("/DuckStyle.css").toExternalForm());
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    // --- Universal duck helper (clean version, no dirty duck) ---
    private Image getDuckForScene(String defaultFile) {
        return selectedCharacter != null ? selectedCharacter : new Image(getClass().getResource(defaultFile).toExternalForm());
    }

    private Image dirtImage = new Image(
            getClass().getResource("dirty.png")
                    .toExternalForm()
    );

    private boolean isNightMode = false; // global flag

    public class GameState {
        public static boolean isNightMode = false; // default: day
    }

    // --- DuckHouse method (unchanged, uses getDuckForScene) ---
    private void DuckHouse(Stage stage, String username) {
        BorderPane layout = sceneTemplate(stage, "house.png", username);
        StackPane root = (StackPane) stage.getScene().getRoot();

        double DEFAULT_SIZE = 80;
        double SELECTED_SIZE = 170;

        Image characterToUse = getDuckForScene("/dockie.png");

        ImageView charac = new ImageView(characterToUse);
        charac.setFitWidth((selectedCharacter != null) ? SELECTED_SIZE : DEFAULT_SIZE);
        charac.setPreserveRatio(true);
        StackPane.setAlignment(charac, Pos.BOTTOM_CENTER);
        StackPane.setMargin(charac, new Insets(0, 0, 187, 0));

        // Always add duck
        if (!root.getChildren().contains(charac)) root.getChildren().add(charac);

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

    private void kitchen(Stage stage, String username) {
        BorderPane layout = sceneTemplate(stage, "kitchen.png", username);
        StackPane root = (StackPane) stage.getScene().getRoot();

        double DEFAULT_SIZE = 150;

        // --- Duck Images ---
        Image defaultDuck = getDuckForScene("/dockieKitchen.png");
        ImageView duck = new ImageView(defaultDuck);

        // --- Selected character settings ---
        boolean isSelectedDuck = selectedCharacter != null;
        currentDuckImage = isSelectedDuck ? selectedCharacter : defaultDuck;
        duck.setImage(currentDuckImage);

        // Define per-hat eating animations, widths, and margins
        Image[] hatEatingAnimations = new Image[] {
                new Image(getClass().getResource("/eating with cat hat (1).PNG").toExternalForm()),
                new Image(getClass().getResource("/eating with frog hat (1).PNG").toExternalForm()),
                new Image(getClass().getResource("/eating with stitch hat (1).PNG").toExternalForm())
        };

        double[] hatWidths = {295, 295, 295}; // Adjust per hat
        Insets[] hatMargins = {
                new Insets(0, -200, 115, 0), // Cat hat
                new Insets(0, -200, 120, 0), // Frog hat
                new Insets(0, -200, 110, 0)  // Stitch hat
        };

        // Set size and margin
        if (isSelectedDuck) {
            duck.setFitWidth(hatWidths[outfitIndex]);
            StackPane.setMargin(duck, hatMargins[outfitIndex]);
        } else {
            duck.setFitWidth(DEFAULT_SIZE);
            StackPane.setMargin(duck, new Insets(0, -200, 135, 0)); // original duck
        }

        duck.setPreserveRatio(true);
        StackPane.setAlignment(duck, Pos.BOTTOM_CENTER);

        // Always add duck to root (even in night mode)
        if (!root.getChildren().contains(duck)) root.getChildren().add(duck);
        duck.setMouseTransparent(false); // enable click for animation

        // --- Eating Animation Handler ---
        duck.setOnMouseClicked(e -> {
            // Update GameBridge when duck is fed by clicking
            gameBridge.performAction("PLAY");
            updateStatsFromGameBridge();

            Image eatingAnim;
            double animWidth;
            double translateX = 0, translateY = 0;

            if (isSelectedDuck) {
                eatingAnim = hatEatingAnimations[outfitIndex];
                animWidth = hatWidths[outfitIndex];
            } else {
                eatingAnim = new Image(getClass().getResource("/eating.png").toExternalForm());
                animWidth = 305;
                translateX = 1;
                translateY = 60;
            }

            playEatingAnimation(duck, eatingAnim, animWidth, translateX, translateY);

            // ADDED: Show crunch animation when clicking the duck
            showCrunchAnimationSimple(duck);
        });

        // --- Food pane setup ---
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

        createFood(foodPane, duck); // add food first
        foodPane.toFront();          // food appears on top

        // --- Button handlers ---
        btnLeft.setOnAction(e -> {
            foodIndex = (foodIndex - 1 + foods.length) % foods.length;
            updateFood(foodPane, duck);
            duck.setImage(currentDuckImage);
        });

        btnRight.setOnAction(e -> {
            foodIndex = (foodIndex + 1) % foods.length;
            updateFood(foodPane, duck);
            duck.setImage(currentDuckImage);
        });

        stage.setScene(new Scene(layout, 400, 500));
        stage.show();
    }

    // --- Eating Animation Handler ---
    private void playEatingAnimation(ImageView duck, Image eatingImage, double width, double translateX, double translateY) {
        double originalWidth = duck.getFitWidth();
        double originalTranslateX = duck.getTranslateX();
        double originalTranslateY = duck.getTranslateY();

        duck.setImage(eatingImage);
        duck.setFitWidth(width);
        duck.setTranslateX(originalTranslateX + translateX);
        duck.setTranslateY(originalTranslateY + translateY);

        PauseTransition pt = new PauseTransition(Duration.millis(500));
        pt.setOnFinished(e -> {
            duck.setImage(currentDuckImage); // revert to correct duck
            duck.setFitWidth(originalWidth);
            duck.setTranslateX(originalTranslateX);
            duck.setTranslateY(originalTranslateY);
        });
        pt.play();
    }

    // --- Character-aware mouth open animation ---
    private void showMouthOpen(ImageView duck) {
        double originalWidth = duck.getFitWidth();
        double originalTranslateX = duck.getTranslateX();
        double originalTranslateY = duck.getTranslateY();

        Image mouthOpenImage;
        double width = originalWidth;
        double translateX = 0;
        double translateY = 0;

        if (selectedCharacter != null) {
            // Use selected character's eating/open-mouth variant
            String[] mouthOpenVariants = {
                    "/eating with cat hat (1).PNG",
                    "/eating with frog hat (1).PNG",
                    "/eating with stitch hat (1).PNG"
            };

            // Width per outfit
            double[] widths = {285, 290, 290};
            width = widths[outfitIndex];

            // X offset per outfit (negative = left, positive = right)
            double[] translateXs = {0, -5, -10}; // adjust per outfit
            translateX = translateXs[outfitIndex];

            // Y offset per outfit (negative = up, positive = down)
            double[] translateYs = {0, 5, 0}; // adjust per outfit
            translateY = translateYs[outfitIndex];

            mouthOpenImage = new Image(getClass().getResource(mouthOpenVariants[outfitIndex]).toExternalForm());
        } else {
            // Default duck eating image
            mouthOpenImage = new Image(getClass().getResource("/eating.png").toExternalForm());
            width = 305;
            translateX = 1;
            translateY = 60;
        }

        duck.setImage(mouthOpenImage);
        duck.setFitWidth(width);
        duck.setTranslateX(originalTranslateX + translateX);
        duck.setTranslateY(originalTranslateY + translateY);

        PauseTransition pt = new PauseTransition(Duration.millis(200));
        pt.setOnFinished(e -> {
            duck.setImage(currentDuckImage); // revert to correct duck
            duck.setFitWidth(originalWidth);
            duck.setTranslateX(originalTranslateX);
            duck.setTranslateY(originalTranslateY);
        });
        pt.play();
    }


    private void createFood(Pane foodPane, ImageView duck) {
        // Remove old UI elements
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
        foodDisplay.setLayoutY(110);
        foodDisplay.toFront();

        // Get quantity from GameBridge
        int quantity = gameBridge.getFoodQuantity(foodIndex);

        // Quantity label
        quantityLabel = new Label("x" + quantity);
        quantityLabel.setStyle("-fx-font-family: Comic Sans MS; -fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        quantityLabel.setLayoutX(130);
        quantityLabel.setLayoutY(100);

        // Buy button (ALWAYS VISIBLE for buying more)
        buyButton = new Button("Buy");
        buyButton.getStyleClass().add("purchase-button");
        buyButton.setPrefWidth(50);
        buyButton.setLayoutX(85);
        buyButton.setLayoutY(230);
        buyButton.setVisible(true);  // Always show buy button

        // Price label (ALWAYS SHOW PRICE)
        int foodId = mapFoodIndexToId(foodIndex);
        Food food = (Food) gameBridge.getItemById("FOOD", foodId);
        int price = (food != null) ? (int) food.getPrice() : 10;

        priceLabel = new Label(String.valueOf(price));
        priceLabel.setStyle("-fx-font-family: Comic Sans MS; -fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: yellow;");
        priceLabel.setLayoutX(100);
        priceLabel.setLayoutY(205);

        // Buy button action
        buyButton.setOnAction(e -> {
            int userCoins = gameBridge.getUserValue("COINS");

            if (userCoins >= price) {
                // Buy 5 pieces of food
                if (gameBridge.purchaseItem("FOOD", foodId , 1)) {
                    // Update UI
                    updateFoodQuantitiesFromGameBridge();
                    updateFood(foodPane, duck);
                    System.out.println("Bought 1x " + food.getName() + " for " + price + " coins!");
                } else {
                    System.out.println("Failed to buy food!");
                }
            } else {
                System.out.println("Not enough coins! Need " + price + ", have " + userCoins);
            }
        });

        makeDraggable(foodDisplay, duck, foodPane);

        foodPane.getChildren().addAll(foodDisplay, quantityLabel, buyButton, priceLabel);
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

                int currentQuantity = gameBridge.getFoodQuantity(foodIndex);

                if (currentQuantity > 0) {
                    int foodId = mapFoodIndexToId(foodIndex);

                    // Feed the duck using GameBridge
                    gameBridge.performAction("FEED", foodId);

                    // Update stats from GameBridge
                    updateStatsFromGameBridge();

                    // Decrease quantity in GameBridge
                    gameBridge.setFoodQuantity(foodIndex, currentQuantity - 1);

                    // Update UI
                    updateFoodQuantitiesFromGameBridge();

                    String[] foodNames = {"Peas", "Bird Seeds", "Corn", "Oats"};
                    System.out.println("Fed " + foodNames[foodIndex] + " to the duck!");

                    // ADDED: Show crunch animation after feeding
                    showCrunchAnimationSimple(duck);

                } else {
                    String[] foodNames = {"Peas", "Bird Seeds", "Corn", "Oats"};
                    System.out.println("No " + foodNames[foodIndex] + " left in inventory!");
                }

                updateFood(pane, duck);
            }
            duck.setMouseTransparent(true);
        });

        food.setOnMouseReleased(e -> duck.setMouseTransparent(true));
    }

    // Alternative simpler method
// Alternative simpler method
    private void showCrunchAnimationSimple(ImageView duck) {
        System.out.println("showCrunchAnimationSimple called! Duck parent: " +
                (duck.getParent() != null ? duck.getParent().getClass().getSimpleName() : "null"));

        try {
            // Get crunch image
            Image crunchImage = new Image(getClass().getResource("/crunch.png").toExternalForm());
            ImageView crunchView = new ImageView(crunchImage);
            crunchView.setFitWidth(80);
            crunchView.setPreserveRatio(true);

            // IMPORTANT: Get the root StackPane instead of the duck's immediate parent
            StackPane root = (StackPane) duck.getScene().getRoot();

            if (root != null) {
                // Convert duck's position from its parent's coordinates to root coordinates
                Bounds duckBoundsInParent = duck.getBoundsInParent();
                Point2D duckPositionInRoot = duck.localToScene(duckBoundsInParent.getMinX(), duckBoundsInParent.getMinY());

                // Position crunch above the duck (in root coordinates)
                double crunchX = duckPositionInRoot.getX() + duckBoundsInParent.getWidth() / 2 - 40;
                double crunchY = duckPositionInRoot.getY() - 40;

                // Convert to root's coordinate system
                Point2D rootCoords = root.sceneToLocal(crunchX, crunchY);

                crunchView.setTranslateX(rootCoords.getX());
                crunchView.setTranslateY(rootCoords.getY());

                // Add to the root StackPane (this ensures it's on top of everything)
                root.getChildren().add(crunchView);
                crunchView.toFront();

                System.out.println("Crunch added to root at: " + rootCoords.getX() + ", " + rootCoords.getY());

                // Remove after 300ms
                PauseTransition remove = new PauseTransition(Duration.millis(300));
                remove.setOnFinished(e -> root.getChildren().remove(crunchView));
                remove.play();
            } else {
                System.out.println("Could not find root StackPane!");
            }
        } catch (Exception e) {
            System.out.println("Error in simple crunch: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ADD LINE: Helper method to map frontend food index to backend food ID
    private int mapFoodIndexToId(int foodIndex) {
        // Map your frontend food array indices to backend food IDs
        switch (foodIndex) {
            case 0: return 1; // peas
            case 1: return 2; // birdseed
            case 2: return 3; // corn
            case 3: return 4; // oats
            default: return 1;
        }
    }

    // Drag helper
    private static class Delta { double x, y; }
    private ImageView brushBubble;
    private FadeTransition brushBubbleFade;

    private void bathRoom(Stage stage, String username) {
        BorderPane layout = sceneTemplate(stage, "cr.png", username);
        StackPane root = (StackPane) stage.getScene().getRoot();

        double DEFAULT_SIZE = 150;
        double SELECTED_SIZE = 290;

        // --- Duck Image ---
        Image characterToUse = getDuckForScene("/dockieBath.png");
        ImageView dockieView = new ImageView(characterToUse);

        dockieView.setFitWidth((selectedCharacter != null) ? SELECTED_SIZE : DEFAULT_SIZE);
        dockieView.setPreserveRatio(true);
        StackPane.setAlignment(dockieView, Pos.CENTER);
        dockieView.setTranslateY(-20);

        // Always add duck to root first (behind interactables)
        if (!root.getChildren().contains(dockieView)) {
            root.getChildren().add(dockieView);
        }

        // --- Bucket and Brush images (day/night) ---
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

        // Make draggable with duck behind
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

                    // ADD LINE: Update GameBridge when duck is cleaned
                    gameBridge.performAction("CLEAN");
                    updateStatsFromGameBridge();

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
                } else {
                    // ADD LINE: Update GameBridge when duck is brushed
                    gameBridge.performAction("CLEAN");
                    updateStatsFromGameBridge();
                }
            }

            btn.setTranslateX(origPos[0]);
            btn.setTranslateY(origPos[1]);
            duck.setOpacity(1.0);
        });
    }

    private Image getSleepingDuck() {
        if (selectedCharacter == null) {
            return new Image(getClass().getResource("/DuckSleeping.png").toExternalForm());
        }

        switch (outfitIndex) {
            case 0:
                return new Image(getClass().getResource("/sleeping with cat hat.png").toExternalForm());
            case 1:
                return new Image(getClass().getResource("/sleeping with frog hat.png").toExternalForm());
            case 2:
                return new Image(getClass().getResource("/sleeping with stitch hat.png").toExternalForm());
            default:
                return new Image(getClass().getResource("/DuckSleeping.png").toExternalForm());
        }
    }
    boolean bedroomLampOn = true;      // Lamp ON
    boolean bedroomDuckSleeping = false; // Duck awake

    private void bedRoom(Stage stage, String username) {
        // Force lamp ON when entering bedroom
        bedroomLampOn = true;
        bedroomDuckSleeping = false;

        BorderPane layout = sceneTemplate(stage, "room.png", username);
        StackPane root = (StackPane) stage.getScene().getRoot();

        // --- Duck Images ---
        Image awakeDuck = (selectedCharacter != null)
                ? selectedCharacter
                : new Image(getClass().getResource("/dockieBed.png").toExternalForm());
        Image sleepingDuck = getSleepingDuck();

        // ... rest of your code remains unchanged

        // --- Lamp Images ---
        Image lampOff = new Image(getClass().getResource("/lambing.png").toExternalForm());
        Image lampOn = new Image(getClass().getResource("/lamning.png").toExternalForm());

        // --- Background Images ---
        Image bgDay = new Image(getClass().getResource("/room.png").toExternalForm());
        Image bgNight = new Image(getClass().getResource("/nightver.png").toExternalForm());

        // --- Duck Sizes ---
        double DEFAULT_AWAKE = 150;
        double DEFAULT_SLEEPING = 220;
        double SELECTED_AWAKE = 290;
        double SELECTED_SLEEPING = 320;

        // --- Original margins for sleeping ducks ---
        Insets defaultSleepingMargin = new Insets(0, 0, 160, 0);
        Insets selectedSleepingMargin = new Insets(0, 0, 178, 0);

        // --- Determine if this is a selected character sleeping duck ---
        boolean isCustomSleepingDuck = selectedCharacter != null;

        // --- Duck ImageView (based on persistent bedroom state) ---
        ImageView duckView = new ImageView(bedroomDuckSleeping ? sleepingDuck : awakeDuck);
        double duckWidth = bedroomDuckSleeping
                ? (isCustomSleepingDuck ? SELECTED_SLEEPING : DEFAULT_SLEEPING)
                : ((selectedCharacter != null) ? SELECTED_AWAKE : DEFAULT_AWAKE);
        duckView.setFitWidth(duckWidth);
        duckView.setPreserveRatio(true);
        StackPane.setAlignment(duckView, Pos.BOTTOM_CENTER);
        StackPane.setMargin(duckView, bedroomDuckSleeping
                ? (isCustomSleepingDuck ? selectedSleepingMargin : defaultSleepingMargin)
                : new Insets(0, 0, 180, 0));

        if (!root.getChildren().contains(duckView)) root.getChildren().add(duckView);

        // --- Lamp Button ---
        ImageView lampView = new ImageView(bedroomLampOn ? lampOn : lampOff);
        lampView.setFitWidth(80);
        lampView.setPreserveRatio(true);

        ToggleButton lampButton = new ToggleButton();
        lampButton.setGraphic(lampView);
        lampButton.getStyleClass().add("lamp-button");
        lampButton.setSelected(bedroomLampOn);

        // Set bedroom background based on persisted state
        ((ImageView) root.getChildren().get(0)).setImage(bedroomLampOn ? bgDay : bgNight);

        lampButton.setOnAction(e -> {
            bedroomLampOn = lampButton.isSelected();
            bedroomDuckSleeping = !bedroomLampOn; // update sleeping state

            if (bedroomLampOn) {
                // DAY MODE
                lampView.setImage(lampOn);
                ((ImageView) root.getChildren().get(0)).setImage(bgDay);
                duckView.setImage(awakeDuck);
                duckView.setFitWidth((selectedCharacter != null) ? SELECTED_AWAKE : DEFAULT_AWAKE);
                StackPane.setMargin(duckView, new Insets(0, 0, 180, 0));
            } else {
                // NIGHT MODE
                lampView.setImage(lampOff);
                ((ImageView) root.getChildren().get(0)).setImage(bgNight);
                duckView.setImage(sleepingDuck);
                duckView.setFitWidth((selectedCharacter != null) ? SELECTED_SLEEPING : DEFAULT_SLEEPING);
                StackPane.setMargin(duckView, isCustomSleepingDuck ? selectedSleepingMargin : defaultSleepingMargin);
            }
        });

        StackPane.setAlignment(lampButton, Pos.TOP_RIGHT);
        StackPane.setMargin(lampButton, new Insets(60, 30, 0, 0));
        if (!root.getChildren().contains(lampButton)) root.getChildren().add(lampButton);
    }

    private int outfitIndex = 0;
    // UPDATE: Player level will be managed by GameBridge
    private int playerLevel = 1;

    private Image desktopFrontDuck;
    private Image desktopRightStayDuck;
    private Image desktopRightStompDuck;
    private Image desktopLeftStayDuck;
    private Image desktopLeftStompDuck;
    // track selected outfit

    private void closet(Stage stage, String username) {
        BorderPane layout = sceneTemplate(stage, "closet.png", username);
        StackPane root = (StackPane) stage.getScene().getRoot();

        // --- Character Images & Level requirements ---
        Image[] characters = new Image[] {
                new Image(getClass().getResource("F with hat.png").toExternalForm()),   // Level 10
                new Image(getClass().getResource("F with frog hat.png").toExternalForm()),  // Level 20
                new Image(getClass().getResource("F with hat stitch.png").toExternalForm()) // Level 30
        };

        int[] levelRequired = {10, 20, 30}; // Example levels
        playerLevel = gameBridge.getUserValue("LEVEL");

        boolean[] owned = new boolean[characters.length];
        for (int i = 0; i < characters.length; i++) {
            owned[i] = playerLevel >= levelRequired[i];
        }

        // --- Main preview ---
        ImageView charac = new ImageView(characters[outfitIndex]);
        charac.setFitWidth(250);
        charac.setPreserveRatio(true);
        StackPane.setAlignment(charac, Pos.BOTTOM_CENTER);
        StackPane.setMargin(charac, new Insets(0, 0, 180, 18));
        if (!root.getChildren().contains(charac)) root.getChildren().add(charac);

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

        // --- Use & Remove buttons ---
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

                switch (outfitIndex) {
                    case 0: // Hat 1
                        desktopFrontDuck = new Image(getClass().getResource("/FrontSideCatDuck.PNG").toExternalForm());
                        desktopRightStayDuck = new Image(getClass().getResource("/RightSideStayCatDuck.PNG").toExternalForm());
                        desktopRightStompDuck = new Image(getClass().getResource("/RightSideStompCatDuck.PNG").toExternalForm());
                        desktopLeftStayDuck = new Image(getClass().getResource("/LeftSideStayCatDuck.PNG").toExternalForm());
                        desktopLeftStompDuck = new Image(getClass().getResource("/LeftSideStompCatDuck.PNG").toExternalForm());
                        break;
                    case 1: // Hat 2
                        desktopFrontDuck = new Image(getClass().getResource("/FrontSideFrogDuck.PNG").toExternalForm());
                        desktopRightStayDuck = new Image(getClass().getResource("/RightSideStayFrogDuck.PNG").toExternalForm());
                        desktopRightStompDuck = new Image(getClass().getResource("/RightSideStompFrogDuck.PNG").toExternalForm());
                        desktopLeftStayDuck = new Image(getClass().getResource("/LeftSideStayFrogDuck.PNG").toExternalForm());
                        desktopLeftStompDuck = new Image(getClass().getResource("/LeftSideStompFrogDuck.PNG").toExternalForm());
                        break;
                    case 2: // Hat 3
                        desktopFrontDuck = new Image(getClass().getResource("/FrontSideStitchesDuck.png").toExternalForm());
                        desktopRightStayDuck = new Image(getClass().getResource("/RightSideStayStitchesDuck.PNG").toExternalForm());
                        desktopRightStompDuck = new Image(getClass().getResource("/RightSideStompStitchesDuck.PNG").toExternalForm());
                        desktopLeftStayDuck = new Image(getClass().getResource("/LeftSideStayStitchesDuck.PNG").toExternalForm());
                        desktopLeftStompDuck = new Image(getClass().getResource("/LeftSideStompStitchesDuck.PNG").toExternalForm());
                        break;
                }

                DeskTop(stage, username);
            }
        });

        removeBtn.setOnAction(e -> {
            if (owned[outfitIndex]) {
                selectedCharacter = null;

                desktopFrontDuck = null;
                desktopRightStayDuck = null;
                desktopRightStompDuck = null;
                desktopLeftStayDuck = null;
                desktopLeftStompDuck = null;

                DeskTop(stage, username);
            }
        });

        updateButtons.run(); // initialize buttons
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
        switchBtn.setOnAction(e -> {
            // Close the current frame
            stage.close();

            // Open the floating duck desktop with username
            DeskTop(new Stage(), username);
        });

        HBox topControls = new HBox(10, settingsBtn, switchBtn);
        topControls.setAlignment(Pos.CENTER_LEFT);

        VBox leftStack = new VBox(5, topControls, duckMenuBtn, topPlayBtn);
        leftStack.setAlignment(Pos.TOP_LEFT);

        // ---------- BOTTOM NAV PIC BUTTONS ----------
        Button btn1 = makePicButton("houseIcon", 45);
        Button btn2 = makePicButton("bedIcon", 45);
        Button btn3 = makePicButton("foodIcon", 45);
        Button btn4 = makePicButton("closetIcon", 45);
        Button btn5 = makePicButton("bathIcon", 45);

        btn1.setOnAction(e -> DuckHouse(stage, username));
        btn2.setOnAction(e -> bedRoom(stage, username));
        btn3.setOnAction(e -> kitchen(stage, username));
        btn4.setOnAction(e -> closet(stage, username));
        btn5.setOnAction(e -> bathRoom(stage, username));

        HBox bottomRow = new HBox(15, btn1, btn2, btn3, btn4, btn5);
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

        // UPDATE: Get level from GameBridge
        int userLevel = gameBridge.getUserValue("LEVEL");
        Label levelLabel = new Label("Level " + userLevel);
        levelLabel.getStyleClass().add("level-badge");

        VBox statsBox = new VBox(25);
        statsBox.setAlignment(Pos.CENTER);

        // UPDATE: Use stats from GameBridge
        statsBox.getChildren().addAll(
                makeStat("Happiness", happiness),
                makeStat("Hunger", hunger),
                makeStat("Energy", energy),
                makeStat("Cleanliness", cleanliness)
        );

        centerBox.getChildren().addAll(levelLabel, statsBox);
        card.getChildren().add(centerBox);

        // --- Coins Counter ---
        // UPDATE: Get coins from GameBridge
        int playerCoins = gameBridge.getUserValue("COINS");
        Label coinsLabel = new Label("Coins: " + playerCoins);
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

        root.getChildren().addAll(card, overlay, coinsLabel);

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

        Image playIcon = new Image(getClass().getResource("MemoryCard.png").toExternalForm());
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

        VBox buttonBox = new VBox(20, picBtn);
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
    private String currentUsername;

    public void cardFlip(Stage stage, String username) {
        window = stage;
        window.setResizable(false);
        this.currentUsername = username; // store username for later use
        window = stage;
        window.setResizable(false);
        Image bgImage = new Image(Objects.requireNonNull(getClass().getResource("/MemoryCardsMiniMainBG.png")).toExternalForm());
        BackgroundImage bgImg = new BackgroundImage(bgImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, new BackgroundSize(SCENE_W, SCENE_H, false, false, false, false));
        BackgroundFill overlay = new BackgroundFill(Color.rgb(0, 0, 0, 0.1), CornerRadii.EMPTY, Insets.EMPTY);
        Background background = new Background(Collections.singletonList(overlay), Collections.singletonList(bgImg));

        window.setTitle("Memory Cards");
        Scene mainScene = createMainMenu(background, stage, username); // pass stage & username
        window.setScene(mainScene);
        window.show();

        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), mainScene.getRoot());
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

// -------------------------------
// All helper methods
// -------------------------------

    private Scene createMainMenu(Background background, Stage stage, String username) {
        StackPane rootStack = new StackPane(); // StackPane to layer background + back button

        VBox root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setBackground(background);
        root.setPadding(new Insets(120, 20, 20, 20));

        Text title = new Text("Memory Cards");
        title.setFont(Font.font("Monospace", FontWeight.BOLD, 32));
        title.setFill(Color.web("#ecf0f1"));
        title.setEffect(new DropShadow(5, Color.BLACK));

        level = credits / 100;
        levelLabel = new Label("Level: " + level);
        levelLabel.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 16));
        levelLabel.setTextFill(Color.web("#ecf0f1"));
        levelLabel.setEffect(new DropShadow(3, Color.BLACK));

        creditsLabel = new Label("Credits: " + credits);
        creditsLabel.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 16));
        creditsLabel.setTextFill(Color.web("#ecf0f1"));
        creditsLabel.setEffect(new DropShadow(3, Color.BLACK));

        HBox infoBox = new HBox(20, levelLabel, creditsLabel);
        infoBox.setAlignment(Pos.CENTER);

        Button startBtn = new Button("Start Game");
        startBtn.setPrefSize(220, 50);
        startBtn.setFont(Font.font("Monospace", FontWeight.BOLD, 20));
        startBtn.setStyle(MAIN_BTN_STYLE);
        startBtn.setTooltip(new Tooltip("Begin a new game!"));
        addButtonEffects(startBtn);
        startBtn.setOnAction(e -> {
            mistakes = 0;
            roundCredits = 0;
            fadeToScene(createDifficultyPage(background, stage, username));
        });

        root.getChildren().addAll(title, infoBox, startBtn);

        // --- Back button ---
        Button backBtn = createBackButton();
        backBtn.setOnAction(e -> DuckHouse(stage, username));
        StackPane.setAlignment(backBtn, Pos.TOP_LEFT);
        StackPane.setMargin(backBtn, new Insets(10));

        rootStack.getChildren().addAll(root, backBtn);

        return new Scene(rootStack, SCENE_W, SCENE_H);
    }

    private Scene createDifficultyPage(Background background, Stage stage, String username) {
        BorderPane root = new BorderPane();
        root.setBackground(background);

        StackPane topPane = new StackPane();
        topPane.setPadding(new Insets(10));
        Button backBtn = createBackButton();
        backBtn.setOnAction(e -> fadeToScene(createMainMenu(background, stage, username)));
        StackPane.setAlignment(backBtn, Pos.TOP_LEFT);
        StackPane.setMargin(backBtn, new Insets(5, 0, 0, 10));
        topPane.getChildren().add(backBtn);
        root.setTop(topPane);

        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);

        Button easy = new Button("Easy");
        easy.setPrefSize(150, 80);
        easy.setFont(Font.font("Comic Sans MS", FontWeight.EXTRA_BOLD, 20));
        easy.setStyle(DIFF_BTN_BASE + "-fx-background-color: linear-gradient(to bottom, #38b466, #4fcf7f);-fx-border-color: #27ae60;-fx-text-fill: white;");
        addButtonEffects(easy);
        easy.setOnAction(e -> fadeToScene(createInstructionScene(background, Difficulty.EASY, stage, username)));

        Button medium = new Button("Medium");
        medium.setPrefSize(150, 80);
        medium.setFont(Font.font("Comic Sans MS", FontWeight.EXTRA_BOLD, 20));
        medium.setStyle(DIFF_BTN_BASE + "-fx-background-color: linear-gradient(to bottom, #e0a14a, #f2c25f);-fx-border-color: #d68910;-fx-text-fill: white;");
        addButtonEffects(medium);
        medium.setOnAction(e -> fadeToScene(createInstructionScene(background, Difficulty.MEDIUM, stage, username)));

        Button hard = new Button("Hard");
        hard.setPrefSize(150, 80);
        hard.setFont(Font.font("Comic Sans MS", FontWeight.EXTRA_BOLD, 20));
        hard.setStyle(DIFF_BTN_BASE + "-fx-background-color: linear-gradient(to bottom, #e06363, #f57c7c);-fx-border-color: #c0392b;-fx-text-fill: white;");
        addButtonEffects(hard);
        hard.setOnAction(e -> fadeToScene(createInstructionScene(background, Difficulty.HARD, stage, username)));

        box.getChildren().addAll(easy, medium, hard);
        root.setCenter(box);

        Label footer = new Label("Credits: " + credits + " Level: " + (credits / 100));
        footer.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 14));
        footer.setTextFill(Color.web("#ecf0f1"));
        footer.setEffect(new DropShadow(2, Color.BLACK));
        BorderPane.setAlignment(footer, Pos.BOTTOM_CENTER);
        BorderPane.setMargin(footer, new Insets(0, 0, 10, 0));
        root.setBottom(footer);

        return new Scene(root, SCENE_W, SCENE_H);
    }



    private Scene createInstructionScene(Background background, Difficulty difficulty, Stage stage, String username) {
        BorderPane root = new BorderPane();
        root.setBackground(background);

        Button back = createBackButton();
        back.setOnAction(e -> fadeToScene(createDifficultyPage(background, stage, username)));

        StackPane topPane = new StackPane();
        topPane.setPadding(new Insets(10));
        StackPane.setAlignment(back, Pos.TOP_LEFT);
        StackPane.setMargin(back, new Insets(5, 0, 0, 10));
        topPane.getChildren().add(back);
        root.setTop(topPane);

        VBox center = new VBox(15);
        center.setAlignment(Pos.CENTER);

        Text title = new Text("How to Play");
        title.setFont(Font.font("Monospace", FontWeight.BOLD, 26));
        title.setFill(Color.web("#ecf0f1"));
        title.setEffect(new DropShadow(4, Color.BLACK));

        HBox sampleBox = new HBox(15);
        sampleBox.setAlignment(Pos.CENTER);
        String example = ALL_DUCKS.get(0);
        for (int i = 0; i < 2; i++) {
            Image img = new Image(Objects.requireNonNull(getClass().getResource(example)).toExternalForm(), 80, 100, true, true);
            sampleBox.getChildren().addAll(new ImageView(img), new ImageView(img));
        }

        Text t1 = new Text("Flip cards to find matching pairs.\n");
        Text t2 = new Text("Each match gives credits depending on difficulty.\n");
        Text t3 = new Text("3 mistakes will end the game.\n");
        Text t4 = new Text("Rare Platinum Duck gives ");
        Text t5 = new Text("+1000 credits!");

        t1.setFill(Color.web("#ecf0f1"));
        t2.setFill(Color.web("#ecf0f1"));
        t3.setFill(Color.web("#ecf0f1"));
        t4.setFill(Color.web("#ecf0f1"));
        t5.setFill(Color.GOLD);

        for (Text t : Arrays.asList(t1, t2, t3, t4, t5)) {
            t.setFont(Font.font("Comic Sans MS", FontWeight.NORMAL, 14));
            t.setEffect(new DropShadow(3, Color.BLACK));
        }

        TextFlow explanationFlow = new TextFlow(t1, t2, t3, t4, t5);
        explanationFlow.setTextAlignment(TextAlignment.CENTER);

        center.getChildren().addAll(title, sampleBox, explanationFlow);
        root.setCenter(center);

        Label countdown = new Label("Starting in 5...");
        countdown.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        countdown.setTextFill(Color.web("#ecf0f1"));
        countdown.setEffect(new DropShadow(2, Color.BLACK));
        BorderPane.setAlignment(countdown, Pos.BOTTOM_CENTER);
        BorderPane.setMargin(countdown, new Insets(0, 0, 10, 0));
        root.setBottom(countdown);

        Scene instrScene = new Scene(root, SCENE_W, SCENE_H);
        final int[] secsLeft = {5};
        Timeline t = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
            secsLeft[0]--;
            countdown.setText("Starting in " + secsLeft[0] + "...");
            if (secsLeft[0] <= 0) startGame(background, difficulty, stage, username);
        }));
        t.setCycleCount(5);
        t.play();

        return instrScene;
    }

    private void startGame(Background background, Difficulty difficulty, Stage stage, String username) {
        int pairs = switch (difficulty) {
            case EASY -> 4;
            case MEDIUM -> 8;
            case HARD -> 10;
        };

        List<String> deck = generateDeck(pairs);

        BorderPane root = new BorderPane();
        root.setBackground(background);
        root.setPadding(new Insets(10));

        HBox infoBox = new HBox(20);
        infoBox.setAlignment(Pos.CENTER);
        levelLabel = new Label("Level: " + level);
        creditsLabel = new Label("Credits: " + credits);
        mistakesLabel = new Label("Mistakes: " + mistakes + "/3");
        for (Label l : Arrays.asList(levelLabel, creditsLabel, mistakesLabel)) {
            l.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 16));
            l.setTextFill(Color.web("#ecf0f1"));
            l.setEffect(new DropShadow(2, Color.BLACK));
        }
        infoBox.getChildren().addAll(levelLabel, creditsLabel, mistakesLabel);
        root.setTop(infoBox);

        List<Card> cards = new ArrayList<>();
        for (String res : deck) {
            Card c = new Card(res, 80, 100);
            c.button.setOnMouseClicked(e -> handleCardClick(c, cards, difficulty, root));
            cards.add(c);
        }

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);

        for (int i = 0; i < cards.size(); i++) {
            int col = i % 4;
            int row = i / 4;
            grid.add(cards.get(i).button, col, row);
        }
        root.setCenter(grid);

        // --- Back button ---
        Button back = createBackButton();
        back.setOnAction(e -> DuckHouse(stage, username));
        BorderPane.setAlignment(back, Pos.BOTTOM_CENTER);
        BorderPane.setMargin(back, new Insets(10));
        root.setBottom(back);

        Scene gameScene = new Scene(root, SCENE_W, SCENE_H);
        fadeToScene(gameScene);

        for (Card c : cards) c.revealTemp();
        int previewTime = switch (difficulty) {
            case EASY -> 10;
            case MEDIUM -> 15;
            case HARD -> 20;
        };
        PauseTransition preview = new PauseTransition(Duration.seconds(previewTime));
        preview.setOnFinished(e -> cards.forEach(Card::hide));
        preview.play();

        mistakes = 0;
        firstSelected = null;
    }

    private void handleCardClick(Card clicked, List<Card> cards, Difficulty difficulty, Pane root) {
        if (clicked.isRevealed || clicked.isMatched) return;

        clicked.reveal();
        clicked.button.setEffect(new Glow(0.3));

        if (firstSelected == null) {
            firstSelected = clicked;
        } else {
            if (firstSelected.resource.equals(clicked.resource)) {
                firstSelected.setMatched();
                clicked.setMatched();

                roundCredits += switch (difficulty) {
                    case EASY -> 2;
                    case MEDIUM -> 3;
                    case HARD -> 5;
                };

                if (clicked.isPlatinumPair()) roundCredits += 1000;

                credits += roundCredits;
                level = credits / 100;

                // ADD LINE: Update GameBridge with earned coins
                gameBridge.modifyCoins(roundCredits);

                updateLabels();

                firstSelected = null;

                boolean allMatched = cards.stream().allMatch(c -> c.isMatched);
                if (allMatched) {
                    mistakes = 0;
                    firstSelected = null;
                    PauseTransition nextRound = new PauseTransition(Duration.seconds(1));
                    nextRound.setOnFinished(e -> startGame(root.getBackground(), difficulty, window, currentUsername));

                    nextRound.play();
                }

            } else {
                mistakes++;
                updateLabels();

                PauseTransition pause = new PauseTransition(Duration.seconds(0.7));
                pause.setOnFinished(e -> {
                    firstSelected.hide();
                    clicked.hide();
                    firstSelected = null;
                    if (mistakes >= 3) showGameOver(root);
                });
                pause.play();
            }
        }
    }

    private void updateLabels() {
        levelLabel.setText("Level: " + level);
        creditsLabel.setText("Credits: " + credits);
        mistakesLabel.setText("Mistakes: " + mistakes + "/3");
    }

    private List<String> generateDeck(int pairs) {
        List<String> nonPlatinum = new ArrayList<>();
        for (String s : ALL_DUCKS) if (!s.equals("/PlatinumDuckCard.png")) nonPlatinum.add(s);
        Collections.shuffle(nonPlatinum, rng);

        List<String> chosen = new ArrayList<>();
        for (int i = 0; i < pairs; i++) chosen.add(nonPlatinum.get(i % nonPlatinum.size()));

        if (rng.nextDouble() < 0.005) {
            int idx = rng.nextInt(chosen.size());
            chosen.set(idx, "/PlatinumDuckCard.png");
        }

        List<String> deck = new ArrayList<>();
        for (String s : chosen) {
            deck.add(s);
            deck.add(s);
        }

        Collections.shuffle(deck, rng);
        return deck;
    }

    private void showGameOver(Pane gameRoot) {
        StackPane overlay = new StackPane();
        overlay.setPrefSize(SCENE_W, SCENE_H);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.65);");

        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50, #34495e);" +
                "-fx-background-radius: 18; -fx-border-radius: 18; -fx-border-color: #f1c40f; -fx-border-width: 2;");

        Text title = new Text("GAME OVER");
        title.setFont(Font.font("Monospace", FontWeight.BOLD, 28));
        title.setFill(Color.WHITE);

        Text roundCreditText = new Text("Round Credits: " + roundCredits);
        roundCreditText.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 20));
        roundCreditText.setFill(Color.WHITE);

        Button menu = new Button("Back to Menu");
        menu.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 20));
        menu.setPrefWidth(160);
        menu.setStyle(MAIN_BTN_STYLE);
        addButtonEffects(menu);
        menu.setOnAction(e -> {
            roundCredits = 0;
            fadeToScene(createMainMenu(gameRoot.getBackground(), window, currentUsername));

        });

        box.getChildren().addAll(title, roundCreditText, menu);

        double xOffset = 200;
        double yOffset = 250;
        box.setTranslateX(xOffset);
        box.setTranslateY(yOffset);

        overlay.getChildren().add(box);

        if (gameRoot instanceof Pane) gameRoot.getChildren().add(overlay);

        FadeTransition fade = new FadeTransition(Duration.millis(350), overlay);
        fade.setFromValue(0);
        fade.setToValue(1);

        ScaleTransition scale = new ScaleTransition(Duration.millis(350), box);
        scale.setFromX(0.7);
        scale.setFromY(0.7);
        scale.setToX(1.0);
        scale.setToY(1.0);

        fade.play();
        scale.play();
    }

    private void fadeToScene(Scene newScene) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), window.getScene().getRoot());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            window.setScene(newScene);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), newScene.getRoot());
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    private Button createBackButton() {
        Button back = new Button("<"); // Use < character
        back.setPrefSize(40, 40);      // Smaller square button
        back.setFont(Font.font("Monospace", FontWeight.BOLD, 24)); // Bold and big enough
        back.setTextFill(Color.WHITE); // White text
        back.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-cursor: hand;"); // Transparent background
        addButtonEffects(back); // Keep the hover scale effect
        return back;
    }


    private void addButtonEffects(Button button) {
        button.setOnMouseEntered(e -> {
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(150), button);
            scaleUp.setToX(1.05);
            scaleUp.setToY(1.05);
            scaleUp.play();
        });
        button.setOnMouseExited(e -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(150), button);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            scaleDown.play();
        });
    }

    // --- Inner classes ---
    private class Card {
        String resource;
        Button button;
        boolean isRevealed = false;
        boolean isMatched = false;
        ImageView frontView;
        ImageView backView;

        Card(String resource, double w, double h) {
            this.resource = resource;
            Image frontImg = new Image(Objects.requireNonNull(getClass().getResource(resource)).toExternalForm(), w, h, true, true);
            frontView = new ImageView(frontImg);
            Image backImg = new Image(Objects.requireNonNull(getClass().getResource(CARD_BACK)).toExternalForm(), w, h, true, true);
            backView = new ImageView(backImg);
            button = new Button();
            button.setGraphic(backView);
            button.setPrefSize(w, h);
            button.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-cursor: hand;");
        }

        void reveal() {
            if (isMatched) return;
            button.setGraphic(frontView);
            isRevealed = true;
        }

        void revealTemp() {
            button.setGraphic(frontView);
            isRevealed = true;
        }

        void hide() {
            if (isMatched) return;
            button.setGraphic(backView);
            isRevealed = false;
        }

        void setMatched() {
            isMatched = true;
            button.setGraphic(frontView);
            button.setDisable(true);
        }

        boolean isPlatinumPair() {
            return "/PlatinumDuckCard.png".equals(resource);
        }
    }

    private enum Difficulty { EASY, MEDIUM, HARD }

    private boolean escPressedOnce = false;

    public void DeskTop(Stage stage, String username) {
        Image frontDuck = (desktopFrontDuck != null) ? desktopFrontDuck : new Image(getClass().getResource("/FrontSideOriginalDuck.png").toExternalForm());
        Image rightStayDuck = (desktopRightStayDuck != null) ? desktopRightStayDuck : new Image(getClass().getResource("/RightSideStayOriginalDuck.png").toExternalForm());
        Image rightStompDuck = (desktopRightStompDuck != null) ? desktopRightStompDuck : new Image(getClass().getResource("/RightSideStompOriginalDuck.png").toExternalForm());
        Image leftStayDuck = (desktopLeftStayDuck != null) ? desktopLeftStayDuck : new Image(getClass().getResource("/LeftSideStayOriginalDuck.png").toExternalForm());
        Image leftStompDuck = (desktopLeftStompDuck != null) ? desktopLeftStompDuck : new Image(getClass().getResource("/LeftSideStompOriginalDuck.png").toExternalForm());

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
        root.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null))); // transparent root
        double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
        double sceneHeight = 200;
        root.setPrefSize(screenWidth, sceneHeight);

        // Grass positioning
        grassView.setFitWidth(screenWidth);
        grassView.setLayoutY(sceneHeight - grassView.getFitHeight());

        // Duck positioning
        double duckOffset = -8; // adjust duck slightly lower
        duckContainer.setLayoutY(sceneHeight - duckView.getFitHeight() - grassView.getFitHeight() + duckOffset);

        // Add nodes: duck behind, grass in front
        root.getChildren().addAll(duckContainer, grassView);

        Scene scene = new Scene(root, screenWidth, sceneHeight, Color.TRANSPARENT);

        // =====================
        // FOCUS FIX
        // =====================
        root.setFocusTraversable(true);
        Platform.runLater(() -> root.requestFocus());
        scene.setOnMouseClicked(e -> root.requestFocus());

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

        // =====================
        // ESC TO GO BACK TO DUCKHOUSE (new stage)
        // =====================
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                // stop animations
                walkRightAnim.stop();
                walkLeftAnim.stop();
                moveRight.stop();
                moveLeft.stop();

                // close floating desktop
                stage.close();

                // open DuckHouse in a new stage
                DuckHouse(new Stage(), username);
            }
        });
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

        // UPDATE: Logout using GameBridge
        btn2.setOnAction(e -> {
            gameBridge.logout();
            start(stage);
        });

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

    // ADD LINE: Cleanup method for GameBridge
    @Override
    public void stop() {
        // Cleanup GameBridge when application closes
        gameBridge.shutdown();

        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}