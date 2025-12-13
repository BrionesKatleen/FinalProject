import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

public class Main extends Application {

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



    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        window.setResizable(false);
        Image bgImage = new Image(Objects.requireNonNull(getClass().getResource("/MemoryCardsMiniMainBG.png")).toExternalForm());
        BackgroundImage bgImg = new BackgroundImage(bgImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER, new BackgroundSize(SCENE_W, SCENE_H, false, false, false, false));
        BackgroundFill overlay = new BackgroundFill(Color.rgb(0, 0, 0, 0.1), CornerRadii.EMPTY, Insets.EMPTY);
        Background background = new Background(Collections.singletonList(overlay), Collections.singletonList(bgImg));

        window.setTitle("Memory Cards");
        Scene mainScene = createMainMenu(background);
        window.setScene(mainScene);
        window.show();

        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), mainScene.getRoot());
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private Scene createMainMenu(Background background) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.TOP_CENTER);
        root.setBackground(background);
        root.setPadding(new Insets(120, 20, 20, 20));

        Text title = new Text("Memory Cards");
        title.setFont(Font.font("Monospace", FontWeight.BOLD, 32));
        title.setFill(Color.web("#ecf0f1"));
        title.setEffect(new DropShadow(5, Color.BLACK));

        // Level & Credits
        level = credits / 100;
        levelLabel = new Label("Level: " + level);
        levelLabel.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 16));
        levelLabel.setTextFill(Color.web("#ecf0f1"));
        levelLabel.setEffect(new DropShadow(3, Color.BLACK));

        creditsLabel = new Label("Credits: " + credits);
        creditsLabel.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 16));
        creditsLabel.setTextFill(Color.web("#ecf0f1"));
        creditsLabel.setEffect(new DropShadow(3, Color.BLACK));

        // Put level and credits side by side
        HBox infoBox = new HBox(20, levelLabel, creditsLabel);
        infoBox.setAlignment(Pos.CENTER);

        // --- Create Start Button ---
        Button startBtn = new Button("Start Game");
        startBtn.setPrefSize(220, 50);
        startBtn.setFont(Font.font("Monospace", FontWeight.BOLD, 20));
        startBtn.setStyle(MAIN_BTN_STYLE);
        startBtn.setTooltip(new Tooltip("Begin a new game!"));
        addButtonEffects(startBtn);
        startBtn.setOnAction(e -> {
            mistakes = 0;
            roundCredits = 0;
            fadeToScene(createDifficultyPage(background));
        });

        // Add everything to root
        root.getChildren().addAll(title, infoBox, startBtn);

        return new Scene(root, SCENE_W, SCENE_H);
    }

    private Scene createDifficultyPage(Background background) {
        BorderPane root = new BorderPane();
        root.setBackground(background);

        StackPane topPane = new StackPane();
        topPane.setPadding(new Insets(10));
        Button backBtn = createBackButton();
        backBtn.setOnAction(e -> fadeToScene(createMainMenu(background)));
        StackPane.setAlignment(backBtn, Pos.TOP_LEFT);
        StackPane.setMargin(backBtn, new Insets(5, 0, 0, 10));
        topPane.getChildren().add(backBtn);
        root.setTop(topPane);

        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);

        Button easy = new Button("Easy");
        easy.setPrefSize(150, 80); // increase height from 48 to 70
        easy.setFont(Font.font("Comic Sans MS", FontWeight.EXTRA_BOLD, 200));
        easy.setStyle(DIFF_BTN_BASE +
                "-fx-background-color: linear-gradient(to bottom, #38b466, #4fcf7f);" +
                "-fx-border-color: #27ae60;" +
                "-fx-text-fill: white;");
        addButtonEffects(easy);
        easy.setOnAction(e -> fadeToScene(createInstructionScene(background, Difficulty.EASY)));

        Button medium = new Button("Medium");
        medium.setPrefSize(150, 80);
        medium.setFont(Font.font("Comic Sans MS", FontWeight.EXTRA_BOLD, 200));
        medium.setStyle(DIFF_BTN_BASE +
                "-fx-background-color: linear-gradient(to bottom, #e0a14a, #f2c25f);" +
                "-fx-border-color: #d68910;" +
                "-fx-text-fill: white;");
        addButtonEffects(medium);
        medium.setOnAction(e -> fadeToScene(createInstructionScene(background, Difficulty.MEDIUM)));

        Button hard = new Button("Hard");
        hard.setPrefSize(150, 80);
        hard.setFont(Font.font("Comic Sans MS", FontWeight.EXTRA_BOLD, 200));
        hard.setStyle(DIFF_BTN_BASE +
                "-fx-background-color: linear-gradient(to bottom, #e06363, #f57c7c);" +
                "-fx-border-color: #c0392b;" +
                "-fx-text-fill: white;");
        addButtonEffects(hard);
        hard.setOnAction(e -> fadeToScene(createInstructionScene(background, Difficulty.HARD)));



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

    private Scene createInstructionScene(Background background, Difficulty difficulty) {
        BorderPane root = new BorderPane();
        root.setBackground(background);

        // Back button
        // Back button
        // Back button (same as difficulty page)
        Button back = createBackButton();
        back.setOnAction(e -> fadeToScene(createDifficultyPage(background)));

        StackPane topPane = new StackPane();
        topPane.setPadding(new Insets(10));
        StackPane.setAlignment(back, Pos.TOP_LEFT);
        StackPane.setMargin(back, new Insets(5, 0, 0, 10));
        topPane.getChildren().add(back);

        root.setTop(topPane);


        VBox center = new VBox(15);
        center.setAlignment(Pos.CENTER);

        // Title
        Text title = new Text("How to Play");
        title.setFont(Font.font("MOnospace", FontWeight.BOLD, 26));
        title.setFill(Color.web("#ecf0f1"));
        title.setEffect(new DropShadow(4, Color.BLACK));

        // Example pair
        HBox sampleBox = new HBox(15);
        sampleBox.setAlignment(Pos.CENTER);
        String example = ALL_DUCKS.get(0);
        for (int i = 0; i < 2; i++) {
            Image img = new Image(Objects.requireNonNull(getClass().getResource(example)).toExternalForm(), 80, 100, true, true);
            sampleBox.getChildren().addAll(new ImageView(img), new ImageView(img));
        }

        // Explanation text with part gold
        Text t1 = new Text("Flip cards to find matching pairs.\n");
        Text t2 = new Text("Each match gives credits depending on difficulty.\n");
        Text t3 = new Text("3 mistakes will end the game.\n");
        Text t4 = new Text("Rare Platinum Duck gives ");
        Text t5 = new Text("+1000 credits!");

        // Set colors
        t1.setFill(Color.web("#ecf0f1"));
        t2.setFill(Color.web("#ecf0f1"));
        t3.setFill(Color.web("#ecf0f1"));
        t4.setFill(Color.web("#ecf0f1"));
        t5.setFill(Color.GOLD);

        // Set font and shadow
        for (Text t : Arrays.asList(t1, t2, t3, t4, t5)) {
            t.setFont(Font.font("Comic Sans MS", FontWeight.NORMAL, 14));
            t.setEffect(new DropShadow(3, Color.BLACK));
        }

        TextFlow explanationFlow = new TextFlow(t1, t2, t3, t4, t5);
        explanationFlow.setTextAlignment(TextAlignment.CENTER);

        center.getChildren().addAll(title, sampleBox, explanationFlow);
        root.setCenter(center);

        // Countdown
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
            if (secsLeft[0] <= 0) startGame(background, difficulty);
        }));
        t.setCycleCount(5);
        t.play();

        return instrScene;
    }

    private void startGame(Background background, Difficulty difficulty) {
        int pairs = switch (difficulty) {
            case EASY -> 4;
            case MEDIUM -> 8;
            case HARD -> 10;
        };

        List<String> deck = generateDeck(pairs);

        BorderPane root = new BorderPane();
        root.setBackground(background);
        root.setPadding(new Insets(10));

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);

        // Top info
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

        // Cards
        List<Card> cards = new ArrayList<>();
        for (String res : deck) {
            Card c = new Card(res, 80, 100);
            c.button.setOnMouseClicked(e -> handleCardClick(c, cards, difficulty, root));
            cards.add(c);
        }

        for (int i = 0; i < cards.size(); i++) {
            int col = i % 4;
            int row = i / 4;
            grid.add(cards.get(i).button, col, row);
        }
        root.setCenter(grid);

        // Back button
        Button back = createBackButton();
        back.setOnAction(e -> fadeToScene(createMainMenu(background)));
        BorderPane.setAlignment(back, Pos.BOTTOM_CENTER);
        BorderPane.setMargin(back, new Insets(10));
        root.setBottom(back);

        Scene gameScene = new Scene(root, SCENE_W, SCENE_H);
        fadeToScene(gameScene);

        // Preview
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
                updateLabels();

                firstSelected = null;

                // Check for next round
                // Check for next round
                boolean allMatched = cards.stream().allMatch(c -> c.isMatched);
                if (allMatched) {
                    mistakes = 0; // <-- reset mistakes here for new round
                    firstSelected = null;
                    PauseTransition nextRound = new PauseTransition(Duration.seconds(1));
                    nextRound.setOnFinished(e -> startGame(root.getBackground(), difficulty));
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
        // -------------------------------
        // Create the semi-transparent overlay
        // -------------------------------
        StackPane overlay = new StackPane();
        overlay.setPrefSize(SCENE_W, SCENE_H);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.65);"); // semi-transparent black

        // -------------------------------
        // Create the box containing Game Over content
        // -------------------------------
        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER); // internal alignment of texts/buttons inside the box
        box.setPadding(new Insets(20));
        box.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #2c3e50, #34495e);" +
                        "-fx-background-radius: 18; -fx-border-radius: 18; -fx-border-color: #f1c40f; -fx-border-width: 2;"
        );

        // Title
        Text title = new Text("GAME OVER");
        title.setFont(Font.font("Monospace", FontWeight.BOLD, 28));
        title.setFill(Color.WHITE);

        // Round credits text
        Text roundCreditText = new Text("Round Credits: " + roundCredits);
        roundCreditText.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 20));
        roundCreditText.setFill(Color.WHITE);

        // Back to menu button
        Button menu = new Button("Back to Menu");
        menu.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 20));
        menu.setPrefWidth(160);
        menu.setStyle(MAIN_BTN_STYLE);
        addButtonEffects(menu);
        menu.setOnAction(e -> {
            roundCredits = 0; // reset round credits
            fadeToScene(createMainMenu(gameRoot.getBackground()));
        });

        // Add all content to the box
        box.getChildren().addAll(title, roundCreditText, menu);

        // -------------------------------
        // MANUAL POSITIONING
        // -------------------------------
        // Adjust these values to move the Game Over box wherever you want
        double xOffset = 200;    // Horizontal: +right, -left
        double yOffset = 250;  // Vertical: +down, -up
        box.setTranslateX(xOffset);
        box.setTranslateY(yOffset);

        // Add box to overlay
        overlay.getChildren().add(box);

        // Add overlay to the game root
        if (gameRoot instanceof Pane) gameRoot.getChildren().add(overlay);

        // -------------------------------
        // Animations: fade + scale
        // -------------------------------
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
        Button back = new Button("←");
        back.setPrefSize(80, 40);
        back.setStyle(BACK_BTN_STYLE);
        addButtonEffects(back);
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

    public static void main(String[] args) {
        launch(args);
    }
}
