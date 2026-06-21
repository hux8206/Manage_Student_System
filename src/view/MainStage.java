package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.Attendance;

public class MainStage {
    private Stage stage;
    private String idmonhoc;
    private String tenmon;
    private String malop;
    private String tenlop;

    public MainStage(Stage stage, String idmonhoc, String tenmon, String malop, String tenlop) {
        this.stage    = stage;
        this.idmonhoc = idmonhoc;
        this.tenmon   = tenmon;
        this.malop    = malop;
        this.tenlop   = tenlop;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f4f8;");

        // TOP - Header
        HBox header = new HBox();
        header.setPadding(new Insets(15, 25, 15, 25));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #1e40af;");

        VBox titleBox = new VBox(2);
        Label lblTitle = new Label("Hệ thống Quản lý Sinh Viên");
        lblTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        lblTitle.setStyle("-fx-text-fill: white;");

        Label lblSub = new Label("Môn: " + tenmon + "  |  Lớp: " + tenlop);
        lblSub.setFont(Font.font("Arial", 12));
        lblSub.setStyle("-fx-text-fill: #bfdbfe;");

        titleBox.getChildren().addAll(lblTitle, lblSub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnDoiMon = new Button("⇄ Đổi môn/lớp");
        btnDoiMon.setStyle(
                "-fx-background-color: #3b82f6;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;"
        );
        btnDoiMon.setOnAction(e -> new SelectionForm(stage).show());

        Button btnLogout = new Button("Đăng xuất");
        btnLogout.setStyle(
                "-fx-background-color: #ef4444;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-weight: bold;"
        );
        btnLogout.setOnAction(e -> new Login(stage).show());

        HBox btnBox = new HBox(10, btnDoiMon, btnLogout);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        header.getChildren().addAll(titleBox, spacer, btnBox);
        root.setTop(header);

        // LEFT - Menu (chỉ 3 mục)
        VBox menu = new VBox(5);
        menu.setPrefWidth(200);
        menu.setPadding(new Insets(15, 10, 15, 10));
        menu.setStyle("-fx-background-color: #1e3a5f;");

        Label lblMenu = new Label("MENU");
        lblMenu.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lblMenu.setStyle("-fx-text-fill: #94a3b8;");
        lblMenu.setPadding(new Insets(0, 0, 10, 8));

        Button btnSinhVien = createMenuButton("Sinh Viên");
        Button btnDiem     = createMenuButton("Điểm");
        Button btnDiemDanh = createMenuButton("Điểm danh");
        Button btnThongKe  = createMenuButton("Thống kê");

        menu.getChildren().addAll(lblMenu, btnSinhVien, btnDiem, btnDiemDanh, btnThongKe);
        root.setLeft(menu);

        // CENTER
        StackPane centerPane = new StackPane();
        centerPane.setStyle("-fx-background-color: #f0f4f8;");
        showWelcome(centerPane);
        root.setCenter(centerPane);

        // Sự kiện menu
        btnSinhVien.setOnAction(e -> {
            setActiveButton(btnSinhVien, btnDiem, btnDiemDanh, btnThongKe);
            new StudentForm(stage, idmonhoc, tenmon, malop, tenlop).show();
        });

        btnDiem.setOnAction(e -> {
            setActiveButton(btnDiem, btnSinhVien, btnDiemDanh, btnThongKe);
            new ScoreForm(stage, idmonhoc, tenmon, malop, tenlop).show();
        });

        btnDiemDanh.setOnAction(e -> {
            setActiveButton(btnDiemDanh, btnSinhVien, btnDiem, btnThongKe);
            new AttendanceForm(stage, idmonhoc, tenmon, malop, tenlop).show();
        });

        btnThongKe.setOnAction(e -> {
            setActiveButton(btnThongKe, btnSinhVien, btnDiemDanh, btnDiem);
            new StatisticForm(stage, idmonhoc, tenmon, malop, tenlop).show();
        });

        Scene scene = new Scene(root, 950, 620);
        stage.setTitle("Quản lý - " + tenmon + " - " + tenlop);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    private void showWelcome(StackPane pane) {
        VBox welcome = new VBox(15);
        welcome.setAlignment(Pos.CENTER);

        Label icon = new Label("");
        icon.setFont(Font.font(60));

        Label lblWelcome = new Label("Chào mừng!");
        lblWelcome.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        lblWelcome.setStyle("-fx-text-fill: #1e40af;");

        Label lblMon = new Label("Môn học: " + tenmon);
        lblMon.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        lblMon.setStyle("-fx-text-fill: #374151;");

        Label lblLop = new Label("Lớp: " + tenlop);
        lblLop.setFont(Font.font("Arial", 15));
        lblLop.setStyle("-fx-text-fill: #6b7280;");

        Label lblSub = new Label("Chọn chức năng từ menu bên trái");
        lblSub.setFont(Font.font("Arial", 13));
        lblSub.setStyle("-fx-text-fill: #9ca3af;");

        // Thống kê nhanh
        HBox stats = new HBox(20);
        stats.setAlignment(Pos.CENTER);
        stats.setPadding(new Insets(20, 0, 0, 0));

        int soSinhVien = new control.StudentControl().getByLop(malop).size();
        control.SubjectControl mhControl = new control.SubjectControl();
        model.Subject currentMon = mhControl.getById(idmonhoc);
        int soTinChi = (currentMon != null) ? currentMon.getSotinchi() : 3;
        int soDiem = new control.ScoreControl().getByMonAndLop(idmonhoc, malop, soTinChi ).size();

        stats.getChildren().addAll(
                createStatCard("Sinh viên", soSinhVien + " người"),
                createStatCard("Đã nhập điểm", soDiem + " bản ghi")
        );

        welcome.getChildren().addAll(icon, lblWelcome, lblMon, lblLop, lblSub, stats);
        pane.getChildren().add(welcome);
    }

    private VBox createStatCard(String title, String value) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20, 30, 20, 30));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;"
        );
        Label lblTitle = new Label(title);
        lblTitle.setFont(Font.font("Arial", 13));
        lblTitle.setStyle("-fx-text-fill: #6b7280;");

        Label lblValue = new Label(value);
        lblValue.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        lblValue.setStyle("-fx-text-fill: #1e40af;");

        card.getChildren().addAll(lblTitle, lblValue);
        return card;
    }

    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(42);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setFont(Font.font("Arial", 14));
        btn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #cbd5e1;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 0 0 0 12;"
        );
        btn.setOnMouseEntered(e -> {
            if (!btn.getStyle().contains("#3b82f6")) {
                btn.setStyle(
                        "-fx-background-color: #2d4f7c;" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 8;" +
                                "-fx-cursor: hand;" +
                                "-fx-padding: 0 0 0 12;"
                );
            }
        });
        btn.setOnMouseExited(e -> {
            if (!btn.getStyle().contains("#3b82f6")) {
                btn.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-text-fill: #cbd5e1;" +
                                "-fx-background-radius: 8;" +
                                "-fx-cursor: hand;" +
                                "-fx-padding: 0 0 0 12;"
                );
            }
        });
        return btn;
    }

    private void setActiveButton(Button active, Button... others) {
        active.setStyle(
                "-fx-background-color: #3b82f6;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 0 0 0 12;" +
                        "-fx-font-weight: bold;"
        );
        for (Button btn : others) {
            btn.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-text-fill: #cbd5e1;" +
                            "-fx-background-radius: 8;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 0 0 0 12;"
            );
        }
    }
}