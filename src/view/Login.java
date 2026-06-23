package view;
import control.LoginControl;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.animation.*;
import javafx.util.Duration;

public class Login {
    private Stage stage;

    public Login(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        // Root layout
        StackPane root = new StackPane();

        // Gradient background
        BackgroundFill bgFill = new BackgroundFill(
                new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#0f0c29")),
                        new Stop(0.5, Color.web("#302b63")),
                        new Stop(1, Color.web("#24243e"))
                ),
                CornerRadii.EMPTY, Insets.EMPTY
        );
        root.setBackground(new Background(bgFill));

        // Decorative circles
        Circle circle1 = new Circle(120);
        circle1.setFill(Color.web("#7f5af0", 0.15));
        circle1.setTranslateX(-280);
        circle1.setTranslateY(-180);

        Circle circle2 = new Circle(80);
        circle2.setFill(Color.web("#2cb67d", 0.1));
        circle2.setTranslateX(280);
        circle2.setTranslateY(200);

        Circle circle3 = new Circle(50);
        circle3.setFill(Color.web("#7f5af0", 0.2));
        circle3.setTranslateX(200);
        circle3.setTranslateY(-220);

        // Card
        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(50, 60, 50, 60));
        card.setMaxWidth(420);
        card.setMaxHeight(520);
        card.setBackground(new Background(new BackgroundFill(
                Color.web("#ffffff", 0.05),
                new CornerRadii(24), Insets.EMPTY
        )));
        card.setBorder(new Border(new BorderStroke(
                Color.web("#ffffff", 0.1),
                BorderStrokeStyle.SOLID,
                new CornerRadii(24),
                new BorderWidths(1)
        )));

        DropShadow cardShadow = new DropShadow();
        cardShadow.setColor(Color.web("#7f5af0", 0.3));
        cardShadow.setRadius(40);
        cardShadow.setSpread(0.1);
        card.setEffect(cardShadow);

        // Title
        Text title = new Text("Chào mừng trở lại");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setFill(Color.WHITE);

        Text subtitle = new Text("Đăng nhập để tiếp tục");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setFill(Color.web("#94a1b2"));

        // Username field
        VBox usernameBox = new VBox(8);
        Label usernameLabel = new Label("Tên đăng nhập (Mã số SV)");
        usernameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        usernameLabel.setTextFill(Color.web("#94a1b2"));

        TextField usernameField = createStyledTextField("Nhập tên đăng nhập (Mã số SV)...");

        usernameBox.getChildren().addAll(usernameLabel, usernameField);

        // Password field
        VBox passwordBox = new VBox(8);
        Label passwordLabel = new Label("Mật khẩu");
        passwordLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        passwordLabel.setTextFill(Color.web("#94a1b2"));

        PasswordField passwordField = createStyledPasswordField("Nhập mật khẩu...");

        passwordBox.getChildren().addAll(passwordLabel, passwordField);

        // Error label
        Label errorLabel = new Label("");
        errorLabel.setTextFill(Color.web("#ff6b6b"));
        errorLabel.setFont(Font.font("Arial", 13));

        // Login button
        Button loginBtn = createPrimaryButton("Đăng nhập");

        loginBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Vui lòng điền đầy đủ thông tin!");
                return;
            }

            String hashedPassword = control.HashPassword.hashPass(password);

            LoginControl lc = new LoginControl();
            // Lấy đối tượng user sau khi kiểm tra
            model.User loggedInUser = lc.authenticate(username, hashedPassword);

            if (loggedInUser != null) {
                // Kiểm tra phân quyền
                String role = loggedInUser.getRole();

                if (role.equalsIgnoreCase("teacher")) {
                    // Nếu là teacher -> Chuyển vào form quản lý cũ của bạn
                    new SelectionForm(stage).show();
                } else {
                    // Nếu là student -> Chuyển vào form dành cho sinh viên
                    // Bạn cần tạo class StudentMainStage trong package view để dòng này không bị lỗi
                    new StudentMainStage(stage, loggedInUser).show();
                }
            } else {
                errorLabel.setTextFill(Color.web("#ff6b6b"));
                errorLabel.setText("Sai tên đăng nhập hoặc mật khẩu!");
            }
        });

        // Register link
        HBox registerBox = new HBox(6);
        registerBox.setAlignment(Pos.CENTER);
        Label noAccountLabel = new Label("Chưa có tài khoản?");
        noAccountLabel.setTextFill(Color.web("#94a1b2"));
        noAccountLabel.setFont(Font.font("Arial", 13));

        Hyperlink registerLink = new Hyperlink("Đăng ký ngay");
        registerLink.setTextFill(Color.web("#7f5af0"));
        registerLink.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        registerLink.setBorder(Border.EMPTY);
        registerLink.setPadding(Insets.EMPTY);
        registerLink.setOnAction(e -> {
            Register registerScreen = new Register(stage);
            registerScreen.show();
        });

        registerBox.getChildren().addAll(noAccountLabel, registerLink);

        // Divider
        Rectangle divider = new Rectangle(300, 1);
        divider.setFill(Color.web("#ffffff", 0.1));

        card.getChildren().addAll(
                title, subtitle,
                new Region() {{ setMinHeight(10); }},
                usernameBox, passwordBox,
                errorLabel,
                loginBtn,
                divider,
                registerBox
        );

        root.getChildren().addAll(circle1, circle2, circle3, card);

        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), card);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideUp = new TranslateTransition(Duration.millis(600), card);
        slideUp.setFromY(30);
        slideUp.setToY(0);

        fadeIn.play();
        slideUp.play();

        Scene scene = new Scene(root, 900, 650);
        stage.setTitle("Đăng nhập");
        stage.setScene(scene);
        stage.show();
    }

    public static TextField createStyledTextField(String placeholder) {
        TextField field = new TextField();
        field.setPromptText(placeholder);
        field.setStyle(
                "-fx-background-color: rgba(255,255,255,0.07);" +
                        "-fx-text-fill: white;" +
                        "-fx-prompt-text-fill: #555f7a;" +
                        "-fx-border-color: rgba(255,255,255,0.1);" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 12 16 12 16;" +
                        "-fx-font-size: 14px;"
        );
        field.setPrefHeight(46);
        field.setMaxWidth(Double.MAX_VALUE);

        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle(
                        "-fx-background-color: rgba(127,90,240,0.1);" +
                                "-fx-text-fill: white;" +
                                "-fx-prompt-text-fill: #555f7a;" +
                                "-fx-border-color: #7f5af0;" +
                                "-fx-border-radius: 10;" +
                                "-fx-background-radius: 10;" +
                                "-fx-padding: 12 16 12 16;" +
                                "-fx-font-size: 14px;"
                );
            } else {
                field.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.07);" +
                                "-fx-text-fill: white;" +
                                "-fx-prompt-text-fill: #555f7a;" +
                                "-fx-border-color: rgba(255,255,255,0.1);" +
                                "-fx-border-radius: 10;" +
                                "-fx-background-radius: 10;" +
                                "-fx-padding: 12 16 12 16;" +
                                "-fx-font-size: 14px;"
                );
            }
        });
        return field;
    }

    public static PasswordField createStyledPasswordField(String placeholder) {
        PasswordField field = new PasswordField();
        field.setPromptText(placeholder);
        field.setStyle(
                "-fx-background-color: rgba(255,255,255,0.07);" +
                        "-fx-text-fill: white;" +
                        "-fx-prompt-text-fill: #555f7a;" +
                        "-fx-border-color: rgba(255,255,255,0.1);" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 12 16 12 16;" +
                        "-fx-font-size: 14px;"
        );
        field.setPrefHeight(46);
        field.setMaxWidth(Double.MAX_VALUE);

        field.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                field.setStyle(
                        "-fx-background-color: rgba(127,90,240,0.1);" +
                                "-fx-text-fill: white;" +
                                "-fx-prompt-text-fill: #555f7a;" +
                                "-fx-border-color: #7f5af0;" +
                                "-fx-border-radius: 10;" +
                                "-fx-background-radius: 10;" +
                                "-fx-padding: 12 16 12 16;" +
                                "-fx-font-size: 14px;"
                );
            } else {
                field.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.07);" +
                                "-fx-text-fill: white;" +
                                "-fx-prompt-text-fill: #555f7a;" +
                                "-fx-border-color: rgba(255,255,255,0.1);" +
                                "-fx-border-radius: 10;" +
                                "-fx-background-radius: 10;" +
                                "-fx-padding: 12 16 12 16;" +
                                "-fx-font-size: 14px;"
                );
            }
        });
        return field;
    }

    public static Button createPrimaryButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(48);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        btn.setTextFill(Color.WHITE);
        btn.setStyle(
                "-fx-background-color: linear-gradient(to right, #7f5af0, #2cb67d);" +
                        "-fx-background-radius: 12;" +
                        "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: linear-gradient(to right, #6b46e0, #25a06b);" +
                        "-fx-background-radius: 12;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(127,90,240,0.5), 15, 0, 0, 4);"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: linear-gradient(to right, #7f5af0, #2cb67d);" +
                        "-fx-background-radius: 12;" +
                        "-fx-cursor: hand;"
        ));
        return btn;
    }
}