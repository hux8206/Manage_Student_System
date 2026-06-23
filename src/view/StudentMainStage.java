package view;

import control.ClassControl;
import control.ScoreControl;
import control.StudentControl;
import control.SubjectControl;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class StudentMainStage {
    private Stage stage;
    private User currentUser;
    private StackPane contentArea;

    // Các lớp Control
    private SubjectControl subjectControl = new SubjectControl();
    private ClassControl classControl = new ClassControl();
    private ScoreControl scoreControl = new ScoreControl();
    private StudentControl studentControl = new StudentControl(); // Thêm Control cho Sinh viên

    public StudentMainStage(Stage stage, User currentUser) {
        this.stage = stage;
        this.currentUser = currentUser;
    }

    public void show() {
        BorderPane root = new BorderPane();

        // --- SIDEBAR ---
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(20, 10, 20, 10));
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: #2b3a55;");

        Label menuLabel = new Label("MENU");
        menuLabel.setTextFill(Color.web("#a0aec0"));
        menuLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        VBox.setMargin(menuLabel, new Insets(0, 0, 10, 10));

        Button btnInfo = createMenuButton("Thông tin cá nhân");
        Button btnEnroll = createMenuButton("Đăng ký lớp học");
        Button btnScore = createMenuButton("Xem điểm");
        Button btnAttendance = createMenuButton("Điểm danh");
        Button btnChat = createMenuButton("Phòng Chat");
        Button btnLogout = createMenuButton("Đăng xuất");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(menuLabel, btnInfo, btnEnroll, btnScore, btnAttendance, btnChat, spacer, btnLogout);

        // --- HEADER ---
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(15, 20, 15, 20));
        header.setStyle("-fx-background-color: #1e40af;");

        Label title = new Label("Cổng thông tin Sinh Viên");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        Label userLabel = new Label("Xin chào, " + (currentUser != null ? currentUser.getUsername() : "Sinh Viên"));
        userLabel.setTextFill(Color.WHITE);
        userLabel.setFont(Font.font("Arial", 14));

        header.getChildren().addAll(title, headerSpacer, userLabel);

        // --- CONTENT AREA ---
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: #f4f6f9;");

        // Mặc định hiện màn hình Thông tin để ép sinh viên nhập tên trước
        switchContent(createInfoView());

        root.setTop(header);
        root.setLeft(sidebar);
        root.setCenter(contentArea);

        // --- SỰ KIỆN MENU ---
        btnInfo.setOnAction(e -> switchContent(createInfoView()));
        btnEnroll.setOnAction(e -> switchContent(createEnrollmentView()));
        btnScore.setOnAction(e -> switchContent(createScoreView()));
        btnAttendance.setOnAction(e -> switchContent(createAttendanceView()));
        btnChat.setOnAction(e -> switchContent(createChatView()));

        btnLogout.setOnAction(e -> {
            new Login(stage).show();
        });

        Scene scene = new Scene(root, 1000, 650);
        stage.setTitle("Student Portal");
        stage.setScene(scene);
        stage.show();
    }

    // ================= CÁC HÀM TẠO GIAO DIỆN TỪNG TAB =================

    // Tab 1: Đăng ký lớp học
    private VBox createEnrollmentView() {
        VBox box = new VBox(20);
        box.setPadding(new Insets(30));

        Label lblTitle = new Label("Đăng ký học phần / Tham gia lớp");
        lblTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        lblTitle.setTextFill(Color.web("#1e40af"));

        HBox boxMon = new HBox(15);
        boxMon.setAlignment(Pos.CENTER_LEFT);
        Label lblMon = new Label("Chọn môn học:");
        lblMon.setPrefWidth(100);
        lblMon.setFont(Font.font("Arial", 14));

        ComboBox<String> cbMonHoc = new ComboBox<>();
        cbMonHoc.setPromptText("-- Chọn môn học --");
        cbMonHoc.setPrefWidth(300);

        List<String> subjects = subjectControl.getAllSubjectNames();
        cbMonHoc.getItems().addAll(subjects);

        HBox boxLop = new HBox(15);
        boxLop.setAlignment(Pos.CENTER_LEFT);
        Label lblLop = new Label("Chọn lớp:");
        lblLop.setPrefWidth(100);
        lblLop.setFont(Font.font("Arial", 14));

        ComboBox<String> cbLopHoc = new ComboBox<>();
        cbLopHoc.setPromptText("-- Chọn lớp học --");
        cbLopHoc.setPrefWidth(300);

        cbMonHoc.setOnAction(e -> {
            cbLopHoc.getItems().clear();
            String selectedMon = cbMonHoc.getValue();
            if (selectedMon != null) {
                String idMon = selectedMon.split(" - ")[0];
                List<String> classes = classControl.getClassesBySubject(idMon);
                cbLopHoc.getItems().addAll(classes);
            }
        });

        Button btnJoin = new Button("Tham gia ngay");
        btnJoin.setStyle("-fx-background-color: #2cb67d; -fx-text-fill: white; -fx-font-weight: bold;");
        Label lblStatus = new Label("");

        btnJoin.setOnAction(e -> {
            String selectedMon = cbMonHoc.getValue();
            String selectedLop = cbLopHoc.getValue();

            if (selectedMon == null || selectedLop == null) {
                lblStatus.setTextFill(Color.RED);
                lblStatus.setText("Vui lòng chọn đầy đủ môn học và lớp học!");
                return;
            }

            String idMonHoc = selectedMon.split(" - ")[0];
            String maLop = selectedLop.split(" - ")[0];
            String maSV = currentUser.getUsername();

            // Kiểm tra xem sinh viên đã cập nhật tên chưa
            String tenSV = studentControl.getStudentName(maSV);
            if (tenSV == null || tenSV.trim().isEmpty()) {
                lblStatus.setTextFill(Color.RED);
                lblStatus.setText("Vui lòng qua tab 'Thông tin cá nhân' để nhập Họ Tên trước khi đăng ký môn!");
                return;
            }

            boolean success = scoreControl.enrollStudent(maSV, idMonHoc, maLop);
            if (success) {
                lblStatus.setTextFill(Color.web("#2cb67d"));
                lblStatus.setText("Thành công! Đã thêm môn học vào bảng điểm của bạn.");
            } else {
                lblStatus.setTextFill(Color.RED);
                lblStatus.setText("Bạn đã đăng ký môn này rồi hoặc có lỗi xảy ra!");
            }
        });

        boxMon.getChildren().addAll(lblMon, cbMonHoc);
        boxLop.getChildren().addAll(lblLop, cbLopHoc);
        box.getChildren().addAll(lblTitle, boxMon, boxLop, btnJoin, lblStatus);
        return box;
    }

    // Tab 2: Thông tin cá nhân (Đã chuyển thành Form điền thông tin)
    private VBox createInfoView() {
        VBox box = new VBox(20);
        box.setPadding(new Insets(30));

        Label lblTitle = new Label("Hồ sơ Sinh viên");
        lblTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        lblTitle.setTextFill(Color.web("#1e40af"));

        String masv = currentUser != null ? currentUser.getUsername() : "";
        String currentName = studentControl.getStudentName(masv);

        GridPane grid = new GridPane();
        grid.setVgap(15);
        grid.setHgap(10);

        Label lblMasv = new Label("Tài khoản (Mã SV):");
        lblMasv.setFont(Font.font("Arial", 14));
        TextField txtMasv = new TextField(masv);
        txtMasv.setEditable(false); // Khóa không cho sửa MSSV
        txtMasv.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #64748b;");
        txtMasv.setPrefWidth(250);

        Label lblName = new Label("Họ và Tên:");
        lblName.setFont(Font.font("Arial", 14));
        TextField txtName = new TextField(currentName);
        txtName.setPromptText("Nhập họ và tên đầy đủ...");
        txtName.setPrefWidth(250);

        grid.add(lblMasv, 0, 0);
        grid.add(txtMasv, 1, 0);
        grid.add(lblName, 0, 1);
        grid.add(txtName, 1, 1);

        Button btnSave = new Button("Cập nhật thông tin");
        btnSave.setStyle("-fx-background-color: #2cb67d; -fx-text-fill: white; -fx-font-weight: bold;");
        Label lblStatus = new Label("");

        btnSave.setOnAction(e -> {
            String newName = txtName.getText().trim();
            if (newName.isEmpty()) {
                lblStatus.setTextFill(Color.RED);
                lblStatus.setText("Họ và tên không được để trống!");
                return;
            }

            boolean isSuccess = studentControl.updateStudentInfo(masv, newName);
            if (isSuccess) {
                lblStatus.setTextFill(Color.web("#2cb67d"));
                lblStatus.setText("Cập nhật hồ sơ thành công!");
            } else {
                lblStatus.setTextFill(Color.RED);
                lblStatus.setText("Có lỗi xảy ra trong quá trình lưu!");
            }
        });

        box.getChildren().addAll(lblTitle, grid, btnSave, lblStatus);
        return box;
    }

    // Tab 3: Xem điểm (Đã căn chỉnh bảng giãn đầy)
    private VBox createScoreView() {
        VBox box = new VBox(20);
        box.setPadding(new Insets(30));

        Label lblTitle = new Label("Kết quả học tập");
        lblTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        lblTitle.setTextFill(Color.web("#1e40af"));

        TableView<java.util.List<String>> table = new TableView<>();
        // THUỘC TÍNH NÀY GIÚP BẢNG TRẢI ĐỀU RA HẾT CHIỀU RỘNG
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<java.util.List<String>, String> colMon = new TableColumn<>("Mã môn học");
        colMon.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(0)));

        TableColumn<java.util.List<String>, String> colCC = new TableColumn<>("Chuyên cần");
        colCC.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(1)));

        TableColumn<java.util.List<String>, String> colBT = new TableColumn<>("Bài tập");
        colBT.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(2)));

        TableColumn<java.util.List<String>, String> colGK = new TableColumn<>("Giữa kỳ");
        colGK.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(3)));

        TableColumn<java.util.List<String>, String> colCK = new TableColumn<>("Cuối kỳ");
        colCK.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(4)));

        TableColumn<java.util.List<String>, String> colTong = new TableColumn<>("Điểm tổng");
        colTong.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(5)));

        table.getColumns().addAll(colMon, colCC, colBT, colGK, colCK, colTong);
        VBox.setVgrow(table, Priority.ALWAYS);

        javafx.collections.ObservableList<java.util.List<String>> dataList = javafx.collections.FXCollections.observableArrayList();
        try (Connection conn = control.Databaseconnection.getConnection();
             PreparedStatement pst = conn.prepareStatement("SELECT idmonhoc, chuyencan, baitap, giuaki, cuoiki, diemtong FROM diem WHERE masv = ?")) {
            pst.setString(1, currentUser.getUsername());
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                java.util.List<String> row = new java.util.ArrayList<>();
                row.add(rs.getString("idmonhoc"));
                row.add(rs.getString("chuyencan"));
                row.add(rs.getString("baitap"));
                row.add(rs.getString("giuaki"));
                row.add(rs.getString("cuoiki"));
                row.add(rs.getString("diemtong"));
                dataList.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        table.setItems(dataList);

        box.getChildren().addAll(lblTitle, table);
        return box;
    }

    // Tab 4: Lịch sử điểm danh (Đã căn chỉnh bảng giãn đầy)
    @SuppressWarnings({"rawtypes", "unchecked"})
    private VBox createAttendanceView() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));

        // Hiển thị điểm chuyên cần hiện tại
        Label lblDiemCC = new Label("Điểm chuyên cần: 8.5/10");
        lblDiemCC.setStyle("-fx-font-size: 18px; -fx-text-fill: #e11d48; -fx-font-weight: bold;");

        TableView<model.Attendance> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<model.Attendance, String> colNgay = new TableColumn<>("Ngày");
        colNgay.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNgay()));

        TableColumn<model.Attendance, String> colNoiDung = new TableColumn<>("Nội dung bài giảng");
        colNoiDung.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNoiDung()));

        TableColumn<model.Attendance, String> colTrangThai = new TableColumn<>("Tình trạng");
        colTrangThai.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTrangthai()));

        table.getColumns().addAll(colNgay, colNoiDung, colTrangThai);

        // Load dữ liệu từ AttendanceControl...

        box.getChildren().addAll(lblDiemCC, table);
        return box;
    }

    // Tab 5: Phòng Chat
    private VBox createChatView() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(30));

        Label lblTitle = new Label("Phòng thảo luận");
        lblTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        lblTitle.setTextFill(Color.web("#1e40af"));

        HBox topBox = new HBox(10);
        topBox.setAlignment(Pos.CENTER_LEFT);
        Label lblChonLop = new Label("Chuyển phòng chat:");
        ComboBox<String> cbChatClass = new ComboBox<>();
        cbChatClass.setPrefWidth(250);

        List<String> enrolledClasses = classControl.getEnrolledClasses(currentUser.getUsername());
        cbChatClass.getItems().addAll(enrolledClasses);

        topBox.getChildren().addAll(lblChonLop, cbChatClass);

        ListView<String> chatBox = new ListView<>();
        VBox.setVgrow(chatBox, Priority.ALWAYS);
        chatBox.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px;");

        cbChatClass.setOnAction(e -> {
            chatBox.getItems().clear();
            if (cbChatClass.getValue() != null) {
                chatBox.getItems().add("=== Chào mừng bạn đến với phòng chat: " + cbChatClass.getValue() + " ===");
            }
        });

        HBox inputBox = new HBox(10);
        TextField txtMessage = new TextField();
        txtMessage.setPromptText("Nhập tin nhắn...");
        txtMessage.setPrefHeight(40);
        HBox.setHgrow(txtMessage, Priority.ALWAYS);

        Button btnSend = new Button("Gửi");
        btnSend.setPrefHeight(40);
        btnSend.setPrefWidth(80);
        btnSend.setStyle("-fx-background-color: #2cb67d; -fx-text-fill: white; -fx-font-weight: bold;");

        btnSend.setOnAction(e -> {
            if (cbChatClass.getValue() == null) {
                chatBox.getItems().add("Hệ thống: Vui lòng chọn lớp trước khi chat!");
                return;
            }
            if (!txtMessage.getText().trim().isEmpty()) {
                String sender = currentUser != null ? currentUser.getUsername() : "Khách";
                chatBox.getItems().add(sender + ": " + txtMessage.getText());
                txtMessage.clear();
            }
        });

        inputBox.getChildren().addAll(txtMessage, btnSend);
        box.getChildren().addAll(lblTitle, topBox, chatBox, inputBox);
        return box;
    }

    // ================= HÀM TIỆN ÍCH DÙNG CHUNG =================

    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPadding(new Insets(12, 15, 12, 15));
        btn.setFont(Font.font("Arial", 14));
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: BASELINE_LEFT; -fx-cursor: hand;");

        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #3b4a6b; -fx-text-fill: white; -fx-alignment: BASELINE_LEFT; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-alignment: BASELINE_LEFT; -fx-cursor: hand;"));
        return btn;
    }

    private void switchContent(javafx.scene.Node node) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(node);
    }
}