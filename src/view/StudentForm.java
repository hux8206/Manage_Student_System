package view;

import control.StudentControl;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Student;

import java.io.File;
import java.text.Normalizer;
import java.util.List;
import java.util.stream.Collectors;

public class StudentForm {
    private Stage stage;
    private String idmonhoc, tenmon, malop, tenlop;
    private StudentControl svControl = new StudentControl();

    private TableView<Student> table = new TableView<>();
    private TextField txtMasv   = new TextField();
    private TextField txtTen    = new TextField();
    private TextField txtSearch = new TextField();
    private Label lblMessage    = new Label();

    public StudentForm(Stage stage, String idmonhoc, String tenmon, String malop, String tenlop) {
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
        Label lblTitle = new Label("Quản lý Sinh Viên");
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
        HBox center = new HBox(15);
        center.setPadding(new Insets(15));

        // Bảng
        VBox tableBox = new VBox(10);
        HBox.setHgrow(tableBox, Priority.ALWAYS);

        HBox searchBox = new HBox(10);
        txtSearch.setPromptText("Tìm theo mã SV hoặc tên...");
        txtSearch.setPrefHeight(36);
        HBox.setHgrow(txtSearch, Priority.ALWAYS);

        Button btnSearch = new Button("Tìm kiếm");
        btnSearch.setPrefHeight(36);
        btnSearch.setStyle("-fx-background-color: #2563eb;-fx-text-fill: white;-fx-background-radius: 6;-fx-cursor: hand;");
        btnSearch.setOnAction(e -> search());

        Button btnRefresh = new Button("Làm mới");
        btnRefresh.setPrefHeight(36);
        btnRefresh.setStyle("-fx-background-color: #6b7280;-fx-text-fill: white;-fx-background-radius: 6;-fx-cursor: hand;");
        btnRefresh.setOnAction(e -> { txtSearch.clear(); loadTable(); });

        Button btnImportCSV = new Button("Tải file");
        btnImportCSV.setPrefHeight(36);
        btnImportCSV.setStyle("-fx-background-color: #16a34a;-fx-text-fill: white;-fx-background-radius: 6;-fx-cursor: hand;-fx-font-weight: bold;");
        btnImportCSV.setOnAction(e -> importExcel());

        searchBox.getChildren().addAll(txtSearch, btnSearch, btnRefresh, btnImportCSV);

        TableColumn<Student, String> colMasv = new TableColumn<>("Mã SV");
        colMasv.setCellValueFactory(new PropertyValueFactory<>("masv"));
        colMasv.setPrefWidth(120);

        TableColumn<Student, String> colTen = new TableColumn<>("Họ tên");
        colTen.setCellValueFactory(new PropertyValueFactory<>("ten"));
        colTen.setPrefWidth(200);

        TableColumn<Student, String> colLop = new TableColumn<>("Lớp");
        colLop.setCellValueFactory(new PropertyValueFactory<>("malop"));
        colLop.setPrefWidth(100);

        table.getColumns().addAll(colMasv, colTen, colLop);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        table.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                txtMasv.setText(selected.getMasv());
                txtTen.setText(selected.getTen());
                txtMasv.setDisable(true);
            }
        });

        tableBox.getChildren().addAll(searchBox, table);

        // Form nhập liệu
        VBox formBox = new VBox(12);
        formBox.setPrefWidth(260);
        formBox.setPadding(new Insets(20));
        formBox.setStyle("-fx-background-color: white;-fx-border-color: #ddd;-fx-border-radius: 8;-fx-background-radius: 8;");

        Label lblForm = new Label("Thông tin sinh viên");
        lblForm.setFont(Font.font("Arial", FontWeight.BOLD, 15));

        Label lblLopHienTai = new Label("Lớp: " + tenlop);
        lblLopHienTai.setFont(Font.font("Arial", 13));
        lblLopHienTai.setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");

        Label lblMasv = new Label("Mã sinh viên");
        lblMasv.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        txtMasv.setPromptText("Nhập mã SV...");
        txtMasv.setPrefHeight(36);

        Label lblTen = new Label("Họ và tên");
        lblTen.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        txtTen.setPromptText("Nhập họ tên...");
        txtTen.setPrefHeight(36);

        lblMessage.setWrapText(true);
        lblMessage.setFont(Font.font("Arial", 13));

        Button btnAdd    = new Button("Thêm");
        Button btnUpdate = new Button("Cập nhật");
        Button btnDelete = new Button("Xóa");
        Button btnClear  = new Button("Xóa form");

        styleButton(btnAdd,    "#16a34a");
        styleButton(btnUpdate, "#2563eb");
        styleButton(btnDelete, "#dc2626");
        styleButton(btnClear,  "#6b7280");

        btnAdd.setOnAction(e -> {
            String masv = txtMasv.getText().trim();
            String ten  = txtTen.getText().trim();
            if (masv.isEmpty() || ten.isEmpty()) {
                showMessage("Vui lòng điền đầy đủ!", "#dc2626"); return;
            }
            // Sinh viên thêm vào lớp đang chọn
            if (svControl.add(new Student(masv, ten, malop))) {
                showMessage("Thêm sinh viên thành công!", "#16a34a");
                loadTable(); clearForm();
            } else showMessage("Mã SV đã tồn tại!", "#dc2626");
        });

        btnUpdate.setOnAction(e -> {
            String masv = txtMasv.getText().trim();
            String ten  = txtTen.getText().trim();
            if (masv.isEmpty() || ten.isEmpty()) {
                showMessage("Vui lòng chọn sinh viên cần sửa!", "#dc2626"); return;
            }
            if (svControl.update(new Student(masv, ten, malop))) {
                showMessage("Cập nhật thành công!", "#16a34a");
                loadTable(); clearForm();
            } else showMessage("Cập nhật thất bại!", "#dc2626");
        });

        btnDelete.setOnAction(e -> {
            String masv = txtMasv.getText().trim();
            if (masv.isEmpty()) { showMessage("Vui lòng chọn sinh viên cần xóa!", "#dc2626"); return; }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận xóa");
            confirm.setContentText("Xóa sinh viên " + masv + "?");
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    if (svControl.delete(masv)) {
                        showMessage("Xóa thành công!", "#16a34a");
                        loadTable(); clearForm();
                    } else showMessage("Xóa thất bại!", "#dc2626");
                }
            });
        });

        btnClear.setOnAction(e -> clearForm());

        HBox btnRow1 = new HBox(8, btnAdd, btnUpdate);
        HBox btnRow2 = new HBox(8, btnDelete, btnClear);
        btnRow1.setAlignment(Pos.CENTER);
        btnRow2.setAlignment(Pos.CENTER);

        formBox.getChildren().addAll(
                lblForm, new Separator(),
                lblLopHienTai,
                lblMasv, txtMasv,
                lblTen, txtTen,
                lblMessage, btnRow1, btnRow2
        );

        center.getChildren().addAll(tableBox, formBox);
        root.setCenter(center);
        loadTable();

        Scene scene = new Scene(root, 900, 580);
        stage.setTitle("Sinh Viên - " + tenlop);
        stage.setScene(scene);
        stage.show();
    }

    private void importExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn file CSV danh sách sinh viên");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        File file = fileChooser.showOpenDialog(stage);
        if (file == null) return;

        int themMoi = 0, trungLap = 0;

        try (java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.InputStreamReader(
                        new java.io.FileInputStream(file), "UTF-8"))) {

            String line;
            boolean isFirst = true;

            while ((line = br.readLine()) != null) {
                if (isFirst) { isFirst = false; continue; } // Bỏ qua header
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length < 2) continue;

                String masv = parts[0].trim();
                String ten  = parts[1].trim();
                if (masv.isEmpty() || ten.isEmpty()) continue;

                if (svControl.add(new Student(masv, ten, malop))) {
                    themMoi++;
                } else {
                    trungLap++;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi đọc file!\nHãy kiểm tra format CSV.", Alert.AlertType.ERROR);
            return;
        }

        loadTable();
        showAlert(
                "Nhập file thành công!\n" +
                        "Thêm mới: " + themMoi + " sinh viên\n" +
                        "Trùng lặp (bỏ qua): " + trungLap + " sinh viên",
                Alert.AlertType.INFORMATION
        );
    }

    private void loadTable() {
        // Chỉ hiển thị sinh viên của lớp đang chọn
        List<Student> list = svControl.getByLop(malop);
        list.sort((a, b) -> {
            String tenA = a.getTen().trim().split("\\s+")[a.getTen().trim().split("\\s+").length - 1];
            String tenB = b.getTen().trim().split("\\s+")[b.getTen().trim().split("\\s+").length - 1];
            return tenA.compareToIgnoreCase(tenB);
        });
        table.setItems(FXCollections.observableArrayList(list));
    }

    private void search() {
        String kw = removeAccent(txtSearch.getText().trim().toLowerCase());

        if (kw.isEmpty()) {
            loadTable();
            return;
        }

        List<Student> result = svControl.getByLop(malop).stream()
                .filter(sv ->
                        removeAccent(sv.getMasv().toLowerCase()).contains(kw) ||
                                removeAccent(sv.getTen().toLowerCase()).contains(kw)
                )
                .collect(Collectors.toList());

        table.setItems(FXCollections.observableArrayList(result));
    }

    public static String removeAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        return temp.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private void clearForm() {
        txtMasv.clear(); txtTen.clear();
        lblMessage.setText("");
        txtMasv.setDisable(false);
        table.getSelectionModel().clearSelection();
    }

    private void showMessage(String msg, String color) {
        lblMessage.setText(msg);
        lblMessage.setStyle("-fx-text-fill: " + color + ";");
    }

    private void showAlert(String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void styleButton(Button btn, String color) {
        btn.setPrefHeight(36); btn.setPrefWidth(100);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btn.setStyle("-fx-background-color:" + color + ";-fx-text-fill:white;-fx-background-radius:6;-fx-cursor:hand;");
    }
}