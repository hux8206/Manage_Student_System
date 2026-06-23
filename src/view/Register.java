package view;
import control.RegisterControl;
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

public class Register {
    private Stage stage;

    public Register(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        StackPane root = new StackPane();

        // Gradient background (khác màu với login)
        BackgroundFill bgFill = new BackgroundFill(
                new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#0a1628")),
                        new Stop(0.5, Color.web("#0d2137")),
                        new Stop(1, Color.web("#071220"))
                ),
                CornerRadii.EMPTY, Insets.EMPTY
        );
        root.setBackground(new Background(bgFill));

        // Decorative circles
        Circle circle1 = new Circle(100);
        circle1.setFill(Color.web("#2cb67d", 0.12));
        circle1.setTranslateX(280);
        circle1.setTranslateY(-180);

        Circle circle2 = new Circle(70);
        circle2.setFill(Color.web("#7f5af0", 0.1));
        circle2.setTranslateX(-260);
        circle2.setTranslateY(200);

        // Card
        VBox card = new VBox(16);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(45, 60, 45, 60));
        card.setMaxWidth(440);
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
        cardShadow.setColor(Color.web("#2cb67d", 0.3));
        cardShadow.setRadius(40);
        cardShadow.setSpread(0.1);
        card.setEffect(cardShadow);

        // Title
        Text title = new Text("Tạo tài khoản");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setFill(Color.WHITE);

        Text subtitle = new Text("Đăng ký để bắt đầu");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setFill(Color.web("#94a1b2"));

        // Username field
        VBox usernameBox = new VBox(8);
        Label usernameLabel = new Label("Tên đăng nhập (Mã SV)");
        usernameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        usernameLabel.setTextFill(Color.web("#94a1b2"));
        TextField usernameField = Login.createStyledTextField("Ví dụ: 26DA042...");
        usernameBox.getChildren().addAll(usernameLabel, usernameField);

        // Email field
        VBox emailBox = new VBox(8);
        Label emailLabel = new Label("Email");
        emailLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        emailLabel.setTextFill(Color.web("#94a1b2"));
        TextField emailField = Login.createStyledTextField("Nhập email...");
        emailBox.getChildren().addAll(emailLabel, emailField);

        // Password field
        VBox passwordBox = new VBox(8);
        Label passwordLabel = new Label("Mật khẩu");
        passwordLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        passwordLabel.setTextFill(Color.web("#94a1b2"));
        PasswordField passwordField = Login.createStyledPasswordField("Nhập mật khẩu...");
        passwordBox.getChildren().addAll(passwordLabel, passwordField);

        // Confirm password field
        VBox confirmBox = new VBox(8);
        Label confirmLabel = new Label("Xác nhận mật khẩu");
        confirmLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        confirmLabel.setTextFill(Color.web("#94a1b2"));
        PasswordField confirmField = Login.createStyledPasswordField("Nhập lại mật khẩu...");
        confirmBox.getChildren().addAll(confirmLabel, confirmField);

        // Message label
        Label messageLabel = new Label("");
        messageLabel.setFont(Font.font("Arial", 13));
        messageLabel.setWrapText(true);

        // Register button
        Button registerBtn = Login.createPrimaryButton("Tạo tài khoản");

        registerBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = passwordField.getText().trim();
            String confirm = confirmField.getText().trim();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                messageLabel.setTextFill(Color.web("#ff6b6b"));
                messageLabel.setText("⚠ Vui lòng điền đầy đủ thông tin!");
                return;
            }

            // --- BẮT ĐẦU KIỂM TRA REGEX MÃ SINH VIÊN ---
            String mssvRegex = "^\\d{2}[a-zA-Z]{2,3}\\d{3,4}$";
            if (!username.matches(mssvRegex)) {
                messageLabel.setTextFill(Color.web("#ff6b6b"));
                messageLabel.setText("✗ Mã SV không hợp lệ! (VD: 26DA042)");
                return;
            }
            // Tự động in hoa chữ cái trong Mã SV
            username = username.toUpperCase();
            // -------------------------------------------

            if (!password.equals(confirm)) {
                messageLabel.setTextFill(Color.web("#ff6b6b"));
                messageLabel.setText("✗ Mật khẩu xác nhận không khớp!");
                return;
            }

            if (!email.contains("@")) {
                messageLabel.setTextFill(Color.web("#ff6b6b"));
                messageLabel.setText("✗ Email không hợp lệ!");
                return;
            }

            // --- BẮT ĐẦU MÃ HÓA MẬT KHẨU ---
            String hashedPassword = control.HashPassword.hashPass(password);

            RegisterControl rc = new RegisterControl();
            // Truyền chuỗi đã mã hóa (hashedPassword) vào hàm thay vì password gốc
            if (rc.registerUser(username, email, hashedPassword)) {
                messageLabel.setTextFill(Color.web("#2cb67d"));
                messageLabel.setText("✓ Đăng ký thành công! Chuyển sang đăng nhập...");

                PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
                pause.setOnFinished(ev -> {
                    Login login = new Login(stage);
                    login.show();
                });
                pause.play();
            } else {
                messageLabel.setTextFill(Color.web("#ff6b6b"));
                messageLabel.setText("✗ Tên đăng nhập đã tồn tại hoặc lỗi kết nối!");
            }
        });

        // Back to login
        HBox loginBox = new HBox(6);
        loginBox.setAlignment(Pos.CENTER);
        Label hasAccountLabel = new Label("Đã có tài khoản?");
        hasAccountLabel.setTextFill(Color.web("#94a1b2"));
        hasAccountLabel.setFont(Font.font("Arial", 13));

        Hyperlink loginLink = new Hyperlink("Đăng nhập");
        loginLink.setTextFill(Color.web("#2cb67d"));
        loginLink.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        loginLink.setBorder(Border.EMPTY);
        loginLink.setPadding(Insets.EMPTY);
        loginLink.setOnAction(e -> {
            Login login = new Login(stage);
            login.show();
        });

        loginBox.getChildren().addAll(hasAccountLabel, loginLink);

        Rectangle divider = new Rectangle(300, 1);
        divider.setFill(Color.web("#ffffff", 0.1));

        card.getChildren().addAll(
                title, subtitle,
                new Region() {{ setMinHeight(5); }},
                usernameBox, emailBox, passwordBox, confirmBox,
                messageLabel,
                registerBtn,
                divider,
                loginBox
        );

        root.getChildren().addAll(circle1, circle2, card);

        // Animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), card);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideUp = new TranslateTransition(Duration.millis(600), card);
        slideUp.setFromY(30);
        slideUp.setToY(0);

        fadeIn.play();
        slideUp.play();

        Scene scene = new Scene(root, 900, 700);
        stage.setTitle("Đăng ký");
        stage.setScene(scene);
        stage.show();
    }
}