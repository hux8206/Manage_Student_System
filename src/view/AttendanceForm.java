package view;

import control.AttendanceControl;
import control.StudentControl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.Attendance;
import model.Student;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AttendanceForm {
    private Stage stage;
    private String idmonhoc, tenmon, malop, tenlop;
    private AttendanceControl ddControl = new AttendanceControl();
    private StudentControl svControl = new StudentControl();

    // Bảng hiển thị danh sách điểm danh
    private TableView<DiemDanhRow> table = new TableView<>();
    private DatePicker datePicker = new DatePicker(LocalDate.now());

    public AttendanceForm(Stage stage, String idmonhoc, String tenmon, String malop, String tenlop) {
        this.stage    = stage;
        this.idmonhoc = idmonhoc;
        this.tenmon   = tenmon;
        this.malop    = malop;
        this.tenlop   = tenlop;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f4f8;");

        // Header
        HBox header = new HBox();
        header.setPadding(new Insets(15, 25, 15, 25));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #1e40af;");

        VBox titleBox = new VBox(2);
        Label lblTitle = new Label("Điểm Danh");
        lblTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        lblTitle.setStyle("-fx-text-fill: white;");
        Label lblSub = new Label("Môn: " + tenmon + "  |  Lớp: " + tenlop);
        lblSub.setFont(Font.font("Arial", 12));
        lblSub.setStyle("-fx-text-fill: #bfdbfe;");
        titleBox.getChildren().addAll(lblTitle, lblSub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnBack = new Button("← Quay lại");
        btnBack.setStyle("-fx-background-color: white;-fx-text-fill: #1e40af;-fx-background-radius: 6;-fx-cursor: hand;-fx-font-weight: bold;");
        btnBack.setOnAction(e -> new MainStage(stage, idmonhoc, tenmon, malop, tenlop).show());

        header.getChildren().addAll(titleBox, spacer, btnBack);
        root.setTop(header);

        // Center
        VBox center = new VBox(12);
        center.setPadding(new Insets(15));

        // Thanh chọn ngày
        HBox dateBox = new HBox(12);
        dateBox.setAlignment(Pos.CENTER_LEFT);
        dateBox.setPadding(new Insets(10, 15, 10, 15));
        dateBox.setStyle("-fx-background-color: white;-fx-border-color: #e2e8f0;-fx-border-radius: 8;-fx-background-radius: 8;");

        Label lblNgay = new Label("Ngày điểm danh:");
        lblNgay.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        datePicker.setPrefHeight(36);

        // Thanh chọn ngày
        HBox dBox = new HBox(12);
        dBox.setAlignment(Pos.CENTER_LEFT);
        dBox.setPadding(new Insets(10, 15, 10, 15));
        dBox.setStyle("-fx-background-color: white;-fx-border-color: #e2e8f0;-fx-border-radius: 8;-fx-background-radius: 8;");

        Label lNgay = new Label("Ngày điểm danh:");
        lNgay.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        datePicker.setPrefHeight(36);
        // Tự động tải danh sách khi người dùng chọn ngày mới
        datePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (newDate != null) loadDiemDanh();
        });

        // NÚT MỚI: Xem lịch sử vắng
        Button btnLichSu = new Button("Lịch sử vắng");
        btnLichSu.setPrefHeight(36);
        btnLichSu.setStyle("-fx-background-color: #f59e0b;-fx-text-fill: white;-fx-background-radius: 6;-fx-cursor: hand;-fx-font-weight: bold;");
        btnLichSu.setOnAction(e -> showLichSuVang());

        Button btnSave = new Button("💾 Lưu điểm danh");
        btnSave.setPrefHeight(36);
        btnSave.setStyle("-fx-background-color: #16a34a;-fx-text-fill: white;-fx-background-radius: 6;-fx-cursor: hand;-fx-font-weight: bold;");
        btnSave.setOnAction(e -> saveDiemDanh());

        // Nút chọn nhanh tất cả
        Button btnAllCoMat = new Button("Tất cả có mặt");
        btnAllCoMat.setPrefHeight(36);
        btnAllCoMat.setStyle("-fx-background-color: #0891b2;-fx-text-fill: white;-fx-background-radius: 6;-fx-cursor: hand;");
        btnAllCoMat.setOnAction(e -> setAllTrangthai("Có mặt"));

        Button btnAllVang = new Button("Tất cả vắng");
        btnAllVang.setPrefHeight(36);
        btnAllVang.setStyle("-fx-background-color: #dc2626;-fx-text-fill: white;-fx-background-radius: 6;-fx-cursor: hand;");
        btnAllVang.setOnAction(e -> setAllTrangthai("Vắng"));

        Label lblMessage = new Label("");
        lblMessage.setFont(Font.font("Arial", 13));
        Region sp2 = new Region();
        HBox.setHgrow(sp2, Priority.ALWAYS);

        // Đưa nút btnLichSu vào HBox thay cho btnLoad
        dateBox.getChildren().addAll(lblNgay, datePicker, btnLichSu, sp2, btnAllCoMat, btnAllVang, btnSave);

        // Bảng điểm danh
        TableColumn<DiemDanhRow, String> colMasv = new TableColumn<>("Mã SV");
        colMasv.setCellValueFactory(new PropertyValueFactory<>("masv"));
        colMasv.setPrefWidth(120);

        TableColumn<DiemDanhRow, String> colTen = new TableColumn<>("Họ tên");
        colTen.setCellValueFactory(new PropertyValueFactory<>("ten"));
        colTen.setPrefWidth(200);

        // Cột trạng thái với ComboBox
        TableColumn<DiemDanhRow, String> colTrangthai = new TableColumn<>("Trạng thái");
        colTrangthai.setPrefWidth(180);
        colTrangthai.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<String> combo = new ComboBox<>(
                    FXCollections.observableArrayList("Có mặt", "Vắng", "Vắng có phép")
            );

            {
                combo.setOnAction(e -> {
                    DiemDanhRow row = getTableView().getItems().get(getIndex());
                    if (row != null) row.setTrangthai(combo.getValue());
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    DiemDanhRow row = getTableView().getItems().get(getIndex());
                    combo.setValue(row.getTrangthai());
                    setGraphic(combo);
                }
            }
        });

        TableColumn<DiemDanhRow, Integer> colVang = new TableColumn<>("Tổng buổi vắng");
        colVang.setCellValueFactory(new PropertyValueFactory<>("soVang"));
        colVang.setPrefWidth(130);

        table.getColumns().addAll(colMasv, colTen, colTrangthai, colVang);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        // Label tổng kết
        Label lblTongKet = new Label("");
        lblTongKet.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lblTongKet.setStyle("-fx-text-fill: #1e40af;");

        center.getChildren().addAll(dateBox, table, lblMessage, lblTongKet);
        root.setCenter(center);

        // Load danh sách ngay khi mở
        loadDiemDanh();

        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("Điểm danh - " + tenmon + " - " + tenlop);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    private void loadDiemDanh() {
        LocalDate ngay = datePicker.getValue();
        List<Student> svList = svControl.getByLop(malop);
        List<Attendance> ddList = ddControl.getByNgay(idmonhoc, malop, ngay);

        ObservableList<DiemDanhRow> rows = FXCollections.observableArrayList();

        for (Student sv : svList) {
            // Tìm trạng thái đã lưu
            String trangthai = "Có mặt"; // mặc định
            for (Attendance dd : ddList) {
                if (dd.getMasv().equals(sv.getMasv())) {
                    trangthai = dd.getTrangthai();
                    break;
                }
            }
            int soVang = ddControl.getSoVang(sv.getMasv(), idmonhoc, malop);
            rows.add(new DiemDanhRow(sv.getMasv(), sv.getTen(), trangthai, soVang));
        }

        table.setItems(rows);
    }

    private void saveDiemDanh() {
        LocalDate ngay = datePicker.getValue();
        List<Attendance> list = new ArrayList<>();

        for (DiemDanhRow row : table.getItems()) {
            list.add(new Attendance(0,row.getMasv(), idmonhoc, malop, ngay, row.getTrangthai()));
        }

        if (ddControl.saveAll(list)) {
            // Reload để cập nhật số buổi vắng
            loadDiemDanh();
            showAlert("Lưu điểm danh thành công!", Alert.AlertType.INFORMATION);
        } else {
            showAlert("Lưu thất bại!", Alert.AlertType.ERROR);
        }
    }

    private void setAllTrangthai(String trangthai) {
        for (DiemDanhRow row : table.getItems()) {
            row.setTrangthai(trangthai);
        }
        table.refresh();
    }

    private void showAlert(String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // Inner class đại diện 1 hàng trong bảng
    public static class DiemDanhRow {
        private String masv;
        private String ten;
        private String trangthai;
        private int soVang;

        public DiemDanhRow(String masv, String ten, String trangthai, int soVang) {
            this.masv      = masv;
            this.ten       = ten;
            this.trangthai = trangthai;
            this.soVang    = soVang;
        }

        public String getMasv()      { return masv; }
        public String getTen()       { return ten; }
        public String getTrangthai() { return trangthai; }
        public int getSoVang()       { return soVang; }

        public void setMasv(String masv)         { this.masv = masv; }
        public void setTen(String ten)           { this.ten = ten; }
        public void setTrangthai(String tt)      { this.trangthai = tt; }
        public void setSoVang(int soVang)        { this.soVang = soVang; }
    }

    private void showLichSuVang() {
        Stage historyStage = new Stage();
        historyStage.setTitle("Lịch sử vắng mặt - " + tenmon);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f0f4f8;");

        Label lblTitle = new Label("Tra cứu danh sách vắng mặt");
        lblTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        lblTitle.setStyle("-fx-text-fill: #1e40af;");

        HBox topBox = new HBox(10);
        topBox.setAlignment(Pos.CENTER_LEFT);
        Label lblChonNgay = new Label("Chọn ngày đã học:");
        lblChonNgay.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        ComboBox<LocalDate> cboNgay = new ComboBox<>();
        // Gọi xuống DB lấy những ngày thực sự có dữ liệu điểm danh
        List<LocalDate> listNgay = ddControl.getNgayDaDiemDanh(idmonhoc, malop);
        cboNgay.setItems(FXCollections.observableArrayList(listNgay));
        cboNgay.setPromptText("-- Chọn ngày --");

        ListView<String> listViewVang = new ListView<>();
        listViewVang.setPlaceholder(new Label("Vui lòng chọn ngày để xem..."));
        VBox.setVgrow(listViewVang, Priority.ALWAYS);

        // Bắt sự kiện khi chọn ngày trên ComboBox
        cboNgay.setOnAction(e -> {
            LocalDate selectedDate = cboNgay.getValue();
            if (selectedDate != null) {
                // Lấy danh sách điểm danh của ngày đó
                List<Attendance> ddList = ddControl.getByNgay(idmonhoc, malop, selectedDate);
                // Lấy danh sách sinh viên để map tên
                List<Student> svList = svControl.getByLop(malop);

                ObservableList<String> vangList = FXCollections.observableArrayList();

                for (Attendance dd : ddList) {
                    // Lọc ra những ông nào có chữ "Vắng" (bao gồm cả Vắng và Vắng có phép)
                    if (dd.getTrangthai().contains("Vắng")) {
                        String tenSv = "Không rõ";
                        for (Student sv : svList) {
                            if (sv.getMasv().equals(dd.getMasv())) {
                                tenSv = sv.getTen();
                                break;
                            }
                        }
                        vangList.add("Mã SV: " + dd.getMasv() + " | Tên: " + tenSv + " - (" + dd.getTrangthai() + ")");
                    }
                }

                if (vangList.isEmpty()) {
                    listViewVang.setItems(FXCollections.observableArrayList("✅ Lớp đi học đầy đủ vào ngày này!"));
                } else {
                    listViewVang.setItems(vangList);
                }
            }
        });

        topBox.getChildren().addAll(lblChonNgay, cboNgay);
        root.getChildren().addAll(lblTitle, topBox, listViewVang);

        Scene scene = new Scene(root, 450, 400);
        historyStage.setScene(scene);
        historyStage.show();
    }
}