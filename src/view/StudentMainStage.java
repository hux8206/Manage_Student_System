package view;

import control.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    private ChatControl chatControl = new ChatControl();
    private SubjectControl subjectControl = new SubjectControl();
    private ClassControl classControl = new ClassControl();
    private ScoreControl scoreControl = new ScoreControl();
    private StudentControl studentControl = new StudentControl();

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
            chatControl.disconnect();
            new Login(stage).show();
        });

        stage.setOnCloseRequest(e -> chatControl.disconnect());

        Scene scene = new Scene(root, 1000, 650);
        stage.setTitle("Student Portal");
        stage.setScene(scene);
        stage.show();
    }

    // ================= CÁC HÀM TẠO GIAO DIỆN TỪNG TAB =================

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
        txtMasv.setEditable(false);
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

    private VBox createScoreView() {
        VBox box = new VBox(20);
        box.setPadding(new Insets(30));

        Label lblTitle = new Label("Kết quả học tập");
        lblTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        lblTitle.setTextFill(Color.web("#1e40af"));

        TableView<java.util.List<String>> table = new TableView<>();
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

    // ================= TAB 4: LỊCH SỬ ĐIỂM DANH (ĐÃ CẬP NHẬT CHỌN LỚP + DANH SÁCH VẮNG) =================
    // ================= TAB 4: LỊCH SỬ ĐIỂM DANH (ĐÃ BỎ NỘI DUNG) =================
    private VBox createAttendanceView() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(30));

        Label lblTitle = new Label("Lịch sử điểm danh");
        lblTitle.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        lblTitle.setTextFill(Color.web("#1e40af"));

        HBox topBox = new HBox(10);
        topBox.setAlignment(Pos.CENTER_LEFT);
        Label lblChonLop = new Label("Chọn lớp học:");
        lblChonLop.setFont(Font.font("Arial", 14));

        ComboBox<String> cbLopHoc = new ComboBox<>();
        cbLopHoc.setPrefWidth(250);
        cbLopHoc.setPromptText("-- Chọn lớp để xem --");

        List<String> enrolledClasses = classControl.getEnrolledClasses(currentUser.getUsername());
        cbLopHoc.getItems().addAll(enrolledClasses);
        topBox.getChildren().addAll(lblChonLop, cbLopHoc);

        Label lblThongKe = new Label("Vui lòng chọn lớp để xem điểm danh.");
        lblThongKe.setStyle("-fx-font-weight: bold; -fx-text-fill: #e11d48; -fx-font-size: 14px;");

        TableView<java.util.List<String>> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<java.util.List<String>, String> colNgay = new TableColumn<>("Thời gian");
        colNgay.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(0)));
        colNgay.setPrefWidth(120);

        TableColumn<java.util.List<String>, String> colTrangThai = new TableColumn<>("Trạng thái của bạn");
        colTrangThai.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(1)));
        colTrangThai.setPrefWidth(150);

        TableColumn<java.util.List<String>, String> colVang = new TableColumn<>("Tình hình vắng nghỉ");
        colVang.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().get(2)));
        colVang.setPrefWidth(400);

        colVang.setCellFactory(tc -> {
            TableCell<java.util.List<String>, String> cell = new TableCell<>();
            javafx.scene.text.Text text = new javafx.scene.text.Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(colVang.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            return cell;
        });

        table.getColumns().addAll(colNgay, colTrangThai, colVang);
        VBox.setVgrow(table, Priority.ALWAYS);

        cbLopHoc.setOnAction(e -> {
            String selected = cbLopHoc.getValue();
            if (selected == null) return;
            String maLop = selected.split(" - ")[0];

            javafx.collections.ObservableList<java.util.List<String>> dataList = javafx.collections.FXCollections.observableArrayList();
            int soBuoiVang = 0;
            int tongSoBuoi = 0;

            try (Connection conn = control.Databaseconnection.getConnection()) {
                // Truy vấn đã xóa 'noidung'
                String sqlMyAtt = "SELECT ngay, trangthai FROM diemdanh WHERE masv = ? AND (malop = ? OR idmonhoc = ?) ORDER BY ngay DESC";
                PreparedStatement pst = conn.prepareStatement(sqlMyAtt);
                pst.setString(1, currentUser.getUsername());
                pst.setString(2, maLop);
                pst.setString(3, maLop);
                ResultSet rs = pst.executeQuery();

                while (rs.next()) {
                    tongSoBuoi++;
                    java.util.List<String> row = new java.util.ArrayList<>();

                    java.sql.Date sqlDate = rs.getDate("ngay");
                    row.add(sqlDate != null ? sqlDate.toString() : "Chưa rõ");

                    String myStatus = rs.getString("trangthai");
                    row.add(myStatus);
                    if (myStatus != null && myStatus.contains("Vắng")) soBuoiVang++;

                    String sqlAbsent = "SELECT sv.ten, d.masv, d.trangthai FROM diemdanh d JOIN sinhvien sv ON d.masv = sv.masv " +
                            "WHERE d.ngay = ? AND (d.malop = ? OR d.idmonhoc = ?) AND d.trangthai LIKE '%Vắng%'";
                    PreparedStatement pstAbs = conn.prepareStatement(sqlAbsent);
                    pstAbs.setDate(1, sqlDate);
                    pstAbs.setString(2, maLop);
                    pstAbs.setString(3, maLop);
                    ResultSet rsAbs = pstAbs.executeQuery();

                    StringBuilder absentList = new StringBuilder();
                    int countAbs = 0;
                    while (rsAbs.next()) {
                        countAbs++;
                        absentList.append("- ").append(rsAbs.getString("ten"))
                                .append(" (").append(rsAbs.getString("masv")).append(") - ")
                                .append(rsAbs.getString("trangthai")).append("\n");
                    }

                    if (countAbs == 0) {
                        row.add("SV vắng: 0\n✅ Lớp đi học đầy đủ");
                    } else {
                        row.add("SV vắng: " + countAbs + "\n" + absentList.toString().trim());
                    }

                    dataList.add(row);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            table.setItems(dataList);
            lblThongKe.setText("Thống kê: Lớp đã học " + tongSoBuoi + " buổi | Bạn vắng " + soBuoiVang + " buổi");
        });

        box.getChildren().addAll(lblTitle, topBox, lblThongKe, table);
        return box;
    }

    // ================== TAB 5: PHÒNG CHAT (BẢN TỰ ĐỘNG BUNG ẢNH/FILE) ==================
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

        // THAY THẾ ListView BẰNG VBox + ScrollPane ĐỂ HỖ TRỢ ẢNH
        VBox chatContent = new VBox(10);
        chatContent.setPadding(new Insets(10));
        chatContent.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 5; -fx-background-radius: 5;");

        ScrollPane chatScroll = new ScrollPane(chatContent);
        chatScroll.setFitToWidth(true);
        chatScroll.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(chatScroll, Priority.ALWAYS);

        // KẾT NỐI SERVER
        chatControl.connect(
                currentUser.getUsername(),
                (room, message) -> {
                    String currentRoom = cbChatClass.getValue();
                    if (currentRoom != null && currentRoom.equals(room)) {
                        addChatMessage(chatContent, chatScroll, "", message);
                    }
                },
                (sysMsg) -> addChatMessage(chatContent, chatScroll, "Hệ thống", sysMsg),

                // LOGIC TỰ BUNG ẢNH VÀ HIỆN FILE
                (room, from, filename, fileId, size, isImage) -> {
                    String currentRoom = cbChatClass.getValue();
                    if (currentRoom != null && currentRoom.equals(room)) {
                        if (isImage) {
                            try {
                                // Tự động tải ngầm ảnh vào thư mục Temp
                                java.io.File tempFile = java.io.File.createTempFile("chat_img_", "_" + filename);
                                tempFile.deleteOnExit(); // Tự xóa khi tắt app

                                Label loadingLbl = new Label("Đang tải ảnh từ " + from + "...");
                                loadingLbl.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
                                chatContent.getChildren().add(loadingLbl);
                                Platform.runLater(() -> chatScroll.setVvalue(1.0));

                                // Tải xong bung ảnh ra ngay trong khung chat
                                chatControl.downloadFile(fileId, tempFile, () -> {
                                    chatContent.getChildren().remove(loadingLbl);
                                    addChatImage(chatContent, chatScroll, from, tempFile);
                                });
                            } catch (Exception ex) { ex.printStackTrace(); }
                        } else {
                            // Hiện nút Tải về cho File thường
                            addChatFile(chatContent, chatScroll, from, filename, fileId);
                        }
                    }
                }
        );

        cbChatClass.setOnAction(e -> {
            chatContent.getChildren().clear();
            if (cbChatClass.getValue() != null) {
                addChatMessage(chatContent, chatScroll, "Hệ thống", "=== Chào mừng bạn đến với phòng chat: " + cbChatClass.getValue() + " ===");
            }
        });

        // THANH CÔNG CỤ
        HBox toolBar = new HBox(15);
        toolBar.setAlignment(Pos.CENTER_LEFT);
        Button btnEmoji = new Button("😊 Emoji");
        Button btnFile = new Button("📁 Gửi File");
        Button btnImage = new Button("🖼 Gửi Ảnh");

        String toolStyle = "-fx-background-color: transparent; -fx-text-fill: #1e40af; -fx-cursor: hand; -fx-font-weight: bold;";
        btnEmoji.setStyle(toolStyle); btnFile.setStyle(toolStyle); btnImage.setStyle(toolStyle);

        TextField txtMessage = new TextField();
        ContextMenu emojiMenu = new ContextMenu();
        String[] emojis = {"😀", "😂", "😍", "😎", "👍", "🙏", "🎉", "❤", "🔥", "✅"};
        HBox emojiBox = new HBox(5);
        for(String em : emojis) {
            Button b = new Button(em);
            b.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 18px;");
            b.setOnAction(ev -> { txtMessage.setText(txtMessage.getText() + em); emojiMenu.hide(); });
            emojiBox.getChildren().add(b);
        }
        emojiMenu.getItems().add(new CustomMenuItem(emojiBox, false));
        btnEmoji.setOnAction(e -> emojiMenu.show(btnEmoji, javafx.geometry.Side.TOP, 0, 0));

        // NÚT GỬI FILE
        btnFile.setOnAction(e -> {
            String selectedRoom = cbChatClass.getValue();
            if (selectedRoom == null) {
                addChatMessage(chatContent, chatScroll, "Hệ thống", "Vui lòng chọn lớp trước khi gửi!"); return;
            }
            javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
            java.io.File file = fc.showOpenDialog(stage);
            if (file != null) {
                // Hiện ngay lập tức cho chính mình thấy
                Label lbl = new Label("Bạn đã gửi tệp: " + file.getName());
                lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #16a34a;");
                chatContent.getChildren().add(lbl);
                Platform.runLater(() -> chatScroll.setVvalue(1.0));

                chatControl.sendFile(selectedRoom, file);
            }
        });

        // NÚT GỬI ẢNH
        btnImage.setOnAction(e -> {
            String selectedRoom = cbChatClass.getValue();
            if (selectedRoom == null) {
                addChatMessage(chatContent, chatScroll, "Hệ thống", "Vui lòng chọn lớp trước khi gửi!"); return;
            }
            javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
            fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.gif"));
            java.io.File file = fc.showOpenDialog(stage);
            if (file != null) {
                // Hiện ảnh ngay lập tức cho chính mình thấy
                addChatImage(chatContent, chatScroll, "Bạn", file);
                chatControl.sendFile(selectedRoom, file);
            }
        });

        toolBar.getChildren().addAll(btnEmoji, btnFile, btnImage);

        // KHUNG NHẬP TIN NHẮN
        HBox inputBox = new HBox(10);
        txtMessage.setPromptText("Nhập tin nhắn...");
        txtMessage.setPrefHeight(40);
        HBox.setHgrow(txtMessage, Priority.ALWAYS);

        Button btnSend = new Button("Gửi");
        btnSend.setPrefHeight(40);
        btnSend.setPrefWidth(80);
        btnSend.setStyle("-fx-background-color: #2cb67d; -fx-text-fill: white; -fx-font-weight: bold;");

        btnSend.setOnAction(e -> {
            String selectedRoom = cbChatClass.getValue();
            String text = txtMessage.getText().trim();
            if (selectedRoom == null) {
                addChatMessage(chatContent, chatScroll, "Hệ thống", "Vui lòng chọn lớp trước khi chat!"); return;
            }
            if (!text.isEmpty()) {
                addChatMessage(chatContent, chatScroll, "Bạn", text);
                chatControl.sendMessage(selectedRoom, text);
                txtMessage.clear();
            }
        });

        txtMessage.setOnAction(e -> btnSend.fire());
        inputBox.getChildren().addAll(txtMessage, btnSend);

        box.getChildren().addAll(lblTitle, topBox, chatScroll, toolBar, inputBox);
        return box;
    }

    // ================= CÁC HÀM TIỆN ÍCH DÀNH RIÊNG CHO CHAT =================

    private void addChatMessage(VBox chatContent, ScrollPane scroll, String sender, String text) {
        String prefix = sender.isEmpty() ? "" : sender + ": ";
        Label lbl = new Label(prefix + text);
        lbl.setWrapText(true);
        lbl.setStyle("-fx-font-size: 14px; -fx-font-family: 'Arial';");

        if (sender.equals("Bạn")) {
            lbl.setStyle("-fx-font-size: 14px; -fx-font-family: 'Arial'; -fx-text-fill: #16a34a; -fx-font-weight: bold;");
        } else if (sender.equals("Hệ thống")) {
            lbl.setStyle("-fx-font-size: 13px; -fx-font-family: 'Arial'; -fx-text-fill: gray; -fx-font-style: italic;");
        }

        chatContent.getChildren().add(lbl);
        Platform.runLater(() -> scroll.setVvalue(1.0)); // Tự động cuộn xuống cuối
    }

    private void addChatImage(VBox chatContent, ScrollPane scroll, String sender, java.io.File imgFile) {
        Label lbl = new Label(sender + " đã gửi một ảnh:");
        lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1e40af;");
        if (sender.equals("Bạn")) lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #16a34a;");

        try {
            Image img = new Image(imgFile.toURI().toString());
            ImageView imgView = new ImageView(img);
            imgView.setFitWidth(250);
            imgView.setPreserveRatio(true);
            imgView.setStyle("-fx-cursor: hand;");

            // Bấm vào ảnh nhỏ để phóng to
            imgView.setOnMouseClicked(e -> showImagePreview(imgFile, imgFile.getName()));

            VBox msgBox = new VBox(5, lbl, imgView);
            msgBox.setStyle("-fx-background-color: #f1f5f9; -fx-padding: 10; -fx-background-radius: 8;");

            chatContent.getChildren().add(msgBox);
            Platform.runLater(() -> scroll.setVvalue(1.0));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addChatFile(VBox chatContent, ScrollPane scroll, String sender, String filename, String fileId) {
        HBox fileBox = new HBox(15);
        fileBox.setAlignment(Pos.CENTER_LEFT);
        fileBox.setStyle("-fx-background-color: #e2e8f0; -fx-padding: 10; -fx-background-radius: 8;");

        Label lbl = new Label("📁 " + sender + " gửi tệp: " + filename);
        lbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Button btnDown = new Button("⬇ Tải về");
        btnDown.setStyle("-fx-background-color: #16a34a; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;");

        btnDown.setOnAction(e -> {
            javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
            fc.setInitialFileName(filename);
            java.io.File save = fc.showSaveDialog(stage);
            if (save != null) {
                btnDown.setText("Đang tải...");
                btnDown.setDisable(true);

                // Tải xong tự động kích hoạt phần mềm máy tính để mở file
                chatControl.downloadFile(fileId, save, () -> {
                    btnDown.setText("✅ Đã tải");
                    try {
                        java.awt.Desktop.getDesktop().open(save);
                    } catch(Exception ex){
                        System.out.println("Lỗi mở file: " + ex.getMessage());
                    }
                });
            }
        });

        fileBox.getChildren().addAll(lbl, btnDown);
        chatContent.getChildren().add(fileBox);
        Platform.runLater(() -> scroll.setVvalue(1.0));
    }

    private void showImagePreview(java.io.File file, String title) {
        Stage imgStage = new Stage();
        imgStage.setTitle("Xem ảnh - " + title);
        try {
            Image image = new Image(file.toURI().toString());
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(800);
            imageView.setFitHeight(600);

            StackPane imgPane = new StackPane(imageView);
            imgPane.setPadding(new Insets(10));
            imgPane.setStyle("-fx-background-color: #2b3a55;");

            Scene imgScene = new Scene(imgPane);
            imgStage.setScene(imgScene);
            imgStage.centerOnScreen();
            imgStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= HÀM TIỆN ÍCH KHUNG =================

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