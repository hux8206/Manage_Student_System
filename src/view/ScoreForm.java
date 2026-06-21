package view;

import control.ScoreControl;
import control.StudentControl;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
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
import model.Score;
import model.Student;

import java.util.List;

public class ScoreForm {
    private Stage stage;
    private String idmonhoc, tenmon, malop, tenlop;
    private ScoreControl diemControl     = new ScoreControl();
    private StudentControl svControl    = new StudentControl();

    private TableView<Score> table       = new TableView<>();
    private ComboBox<String> cboSv      = new ComboBox<>();
    private TextField txtChuyenCan      = new TextField();
    private TextField txtBaiTap         = new TextField();
    private TextField txtGiuaKi         = new TextField();
    private TextField txtCuoiKi         = new TextField();
    private Label lblDiemTong           = new Label("Điểm tổng: --");
    private Label lblMessage            = new Label();
    private int currentSoTinChi = 3;

    public ScoreForm(Stage stage, String idmonhoc, String tenmon, String malop, String tenlop) {
        this.stage    = stage;
        this.idmonhoc = idmonhoc;
        this.tenmon   = tenmon;
        this.malop    = malop;
        this.tenlop   = tenlop;
    }

    public void show() {
        control.SubjectControl mhControl = new control.SubjectControl();
        model.Subject currentMon = mhControl.getById(idmonhoc);
        if (currentMon != null) {
            currentSoTinChi = currentMon.getSotinchi();
        }
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f4f8;");

        // Header
        HBox header = new HBox();
        header.setPadding(new Insets(15, 25, 15, 25));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #1e40af;");

        VBox titleBox = new VBox(2);
        Label lblTitle = new Label("Quản lý Điểm");
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

        // Bảng điểm
        VBox tableBox = new VBox(10);
        HBox.setHgrow(tableBox, Priority.ALWAYS);

        Label lblBang = new Label("Bảng điểm - " + tenmon + " - Lớp " + tenlop);
        lblBang.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lblBang.setStyle("-fx-text-fill: #1e40af;");

        // Các cột bảng
        TableColumn<Score, String> colMasv = new TableColumn<>("Mã SV");
        colMasv.setCellValueFactory(new PropertyValueFactory<>("masv"));
        colMasv.setPrefWidth(100);

        TableColumn<Score, Double> colCC = new TableColumn<>("Chuyên cần\n(10%)");
        colCC.setCellValueFactory(new PropertyValueFactory<>("chuyenCan"));
        colCC.setPrefWidth(100);

        TableColumn<Score, Double> colBT = new TableColumn<>("Bài tập\n(20%)");
        colBT.setCellValueFactory(new PropertyValueFactory<>("baiTap"));
        colBT.setPrefWidth(100);

        TableColumn<Score, Double> colGK = new TableColumn<>("Giữa kì\n(20%)");
        colGK.setCellValueFactory(new PropertyValueFactory<>("giuaKi"));
        colGK.setPrefWidth(100);

        TableColumn<Score, Double> colCK = new TableColumn<>("Cuối kì\n(50%)");
        colCK.setCellValueFactory(new PropertyValueFactory<>("cuoiKi"));
        colCK.setPrefWidth(100);

        TableColumn<Score, Double> colTong = new TableColumn<>("Điểm tổng");
        colTong.setCellValueFactory(new PropertyValueFactory<>("diemTong"));
        colTong.setPrefWidth(100);
        // Tô màu điểm tổng
        colTong.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.1f", item));
                    if (item >= 8)      setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                    else if (item >= 6) setStyle("-fx-text-fill: #2563eb; -fx-font-weight: bold;");
                    else if (item >= 4) setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold;");
                    else                setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                }
            }
        });

        table.getColumns().addAll(colMasv, colCC, colBT, colGK, colCK, colTong);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        // Click vào hàng thì điền vào form
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                cboSv.setValue(selected.getMasv());
                txtChuyenCan.setText(String.valueOf(selected.getChuyenCan()));
                txtBaiTap.setText(String.valueOf(selected.getBaiTap()));
                txtGiuaKi.setText(String.valueOf(selected.getGiuaKi()));
                txtCuoiKi.setText(String.valueOf(selected.getCuoiKi()));
                updateDiemTong();
            }
        });

        tableBox.getChildren().addAll(lblBang, table);

        // Form nhập điểm
        VBox formBox = new VBox(10);
        formBox.setPrefWidth(280);
        formBox.setPadding(new Insets(20));
        formBox.setStyle("-fx-background-color: white;-fx-border-color: #ddd;-fx-border-radius: 8;-fx-background-radius: 8;");

        Label lblForm = new Label("Nhập điểm");
        lblForm.setFont(Font.font("Arial", FontWeight.BOLD, 15));

        Label lblMonHienTai = new Label("Môn: " + tenmon);
        lblMonHienTai.setFont(Font.font("Arial", 13));
        lblMonHienTai.setStyle("-fx-text-fill: #2563eb;-fx-font-weight: bold;");

        Label lblSvForm = new Label("Sinh viên");
        lblSvForm.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        cboSv.setMaxWidth(Double.MAX_VALUE);
        cboSv.setPrefHeight(36);
        cboSv.setPromptText("Chọn sinh viên...");
        loadComboSv();

        // Các ô nhập điểm
        txtChuyenCan.setPromptText("0 - 10");
        txtBaiTap.setPromptText("0 - 10");
        txtGiuaKi.setPromptText("0 - 10");
        txtCuoiKi.setPromptText("0 - 10");

        for (TextField tf : new TextField[]{txtChuyenCan, txtBaiTap, txtGiuaKi, txtCuoiKi}) {
            tf.setPrefHeight(36);
            tf.textProperty().addListener((obs, o, n) -> updateDiemTong());
        }

        // --- ẨN/HIỆN CỘT VÀ ĐỔI TÊN THEO SỐ TÍN CHỈ ---
        if (currentSoTinChi == 3) {
            colCC.setText("Chuyên cần\n(10%)");
            colBT.setText("Bài tập\n(20%)");
            colGK.setText("Giữa kì\n(20%)");
            colCK.setText("Cuối kì\n(50%)");
        } else if (currentSoTinChi == 2) {
            colCC.setText("Chuyên cần\n(10%)");
            colGK.setText("Giữa kì\n(30%)");
            colCK.setText("Cuối kì\n(60%)");
            colBT.setVisible(false);
            txtBaiTap.setDisable(true);
            txtBaiTap.setText("0");
        } else if (currentSoTinChi == 1) {
            colCC.setText("Chuyên cần\n(40%)");
            colCK.setText("Cuối kì\n(60%)");
            colGK.setVisible(false);
            colBT.setVisible(false);
            txtChuyenCan.setDisable(true);
            txtBaiTap.setDisable(true);
            txtChuyenCan.setText("0");
            txtBaiTap.setText("0");
        }
        // ----------------------------------------------

        lblDiemTong.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        lblDiemTong.setStyle(
                "-fx-text-fill: #1e40af;" +
                        "-fx-background-color: #eff6ff;" +
                        "-fx-padding: 10;" +
                        "-fx-border-color: #bfdbfe;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;"
        );
        lblDiemTong.setMaxWidth(Double.MAX_VALUE);
        lblDiemTong.setAlignment(Pos.CENTER);

        lblMessage.setWrapText(true);
        lblMessage.setFont(Font.font("Arial", 13));

        Button btnSave   = new Button("💾 Lưu điểm");
        Button btnDelete = new Button("Xóa");
        Button btnClear  = new Button("Xóa form");

        btnSave.setMaxWidth(Double.MAX_VALUE);
        btnSave.setPrefHeight(40);
        btnSave.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        btnSave.setStyle("-fx-background-color: #16a34a;-fx-text-fill: white;-fx-background-radius: 8;-fx-cursor: hand;");

        styleButton(btnDelete, "#dc2626");
        styleButton(btnClear,  "#6b7280");

        btnSave.setOnAction(e -> saveDiem());
        btnDelete.setOnAction(e -> deleteDiem());
        btnClear.setOnAction(e -> clearForm());

        HBox btnRow = new HBox(8, btnDelete, btnClear);
        btnRow.setAlignment(Pos.CENTER);

        formBox.getChildren().addAll(
                lblForm, new Separator(),
                lblMonHienTai,
                lblSvForm, cboSv,
                createFieldRow("Chuyên cần (10%)", txtChuyenCan),
                createFieldRow("Bài tập (20%)",    txtBaiTap),
                createFieldRow("Giữa kì (20%)",    txtGiuaKi),
                createFieldRow("Cuối kì (50%)",    txtCuoiKi),
                lblDiemTong,
                lblMessage,
                btnSave, btnRow
        );

        center.getChildren().addAll(tableBox, formBox);
        root.setCenter(center);
        loadTable();

        Scene scene = new Scene(root, 980, 580);
        stage.setTitle("Điểm - " + tenmon + " - " + tenlop);
        stage.setScene(scene);
        stage.show();
    }

    private VBox createFieldRow(String label, TextField field) {
        VBox box = new VBox(4);
        Label lbl = new Label(label);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        lbl.setStyle("-fx-text-fill: #374151;");
        box.getChildren().addAll(lbl, field);
        return box;
    }

    private void updateDiemTong() {
        try {
            double cc = txtChuyenCan.getText().isEmpty() ? 0 : Double.parseDouble(txtChuyenCan.getText());
            double bt = txtBaiTap.getText().isEmpty()    ? 0 : Double.parseDouble(txtBaiTap.getText());
            double gk = txtGiuaKi.getText().isEmpty()    ? 0 : Double.parseDouble(txtGiuaKi.getText());
            double ck = txtCuoiKi.getText().isEmpty()    ? 0 : Double.parseDouble(txtCuoiKi.getText());

            // Tính toán với trọng số tự động theo số tín chỉ
            double tong = Score.tinhTong(cc, bt, gk, ck, currentSoTinChi);
            lblDiemTong.setText(String.format("Điểm tổng: %.1f", tong));

            if (tong >= 8)      lblDiemTong.setStyle("-fx-text-fill: #16a34a;-fx-font-weight: bold;-fx-background-color: #f0fdf4;-fx-padding: 10;-fx-border-color: #bbf7d0;-fx-border-radius: 6;-fx-background-radius: 6;");
            else if (tong >= 6) lblDiemTong.setStyle("-fx-text-fill: #2563eb;-fx-font-weight: bold;-fx-background-color: #eff6ff;-fx-padding: 10;-fx-border-color: #bfdbfe;-fx-border-radius: 6;-fx-background-radius: 6;");
            else if (tong >= 4) lblDiemTong.setStyle("-fx-text-fill: #d97706;-fx-font-weight: bold;-fx-background-color: #fffbeb;-fx-padding: 10;-fx-border-color: #fde68a;-fx-border-radius: 6;-fx-background-radius: 6;");
            else                lblDiemTong.setStyle("-fx-text-fill: #dc2626;-fx-font-weight: bold;-fx-background-color: #fef2f2;-fx-padding: 10;-fx-border-color: #fecaca;-fx-border-radius: 6;-fx-background-radius: 6;");
        } catch (NumberFormatException e) {
            lblDiemTong.setText("Điểm tổng: --");
        }
    }

    private void saveDiem() {
        String masv = cboSv.getValue();
        if (masv != null && masv.contains(" - ")) masv = masv.split(" - ")[0].trim();

        if (masv == null || masv.isEmpty()) {
            showMessage("Vui lòng chọn sinh viên trên bảng!", "#dc2626"); return;
        }

        try {
            // Nếu ô text bị bỏ trống -> Tự động gán bằng 0
            double cc = txtChuyenCan.getText().trim().isEmpty() ? 0 : Double.parseDouble(txtChuyenCan.getText().trim());
            double bt = txtBaiTap.getText().trim().isEmpty()    ? 0 : Double.parseDouble(txtBaiTap.getText().trim());
            double gk = txtGiuaKi.getText().trim().isEmpty()    ? 0 : Double.parseDouble(txtGiuaKi.getText().trim());
            double ck = txtCuoiKi.getText().trim().isEmpty()    ? 0 : Double.parseDouble(txtCuoiKi.getText().trim());

            if (cc < 0 || cc > 10 || bt < 0 || bt > 10 || gk < 0 || gk > 10 || ck < 0 || ck > 10) {
                showMessage("Điểm phải từ 0 đến 10!", "#dc2626"); return;
            }

            Score d = new Score(masv, idmonhoc, cc, bt, gk, ck, currentSoTinChi);

            // Dùng hàm save (tự động update nếu đã có, hoặc insert nếu chưa có)
            if (diemControl.save(d)) {
                showMessage("Lưu điểm thành công!", "#16a34a");
                loadTable(); // Load lại bảng để thấy thay đổi
                // Cố tình bỏ clearForm() ở đây để user nhập xong vẫn thấy data vừa nhập, UX sẽ tốt hơn
            } else {
                showMessage("Lưu thất bại!", "#dc2626");
            }
        } catch (NumberFormatException e) {
            showMessage("Điểm phải là số hợp lệ!", "#dc2626");
        }
    }

    private void deleteDiem() {
        String masv = cboSv.getValue();
        if (masv != null && masv.contains(" - ")) masv = masv.split(" - ")[0].trim();
        if (masv == null || masv.isEmpty()) {
            showMessage("Vui lòng chọn sinh viên!", "#dc2626"); return;
        }
        final String finalMasv = masv;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setContentText("Xóa điểm của " + masv + "?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                if (diemControl.delete(finalMasv, idmonhoc)) {
                    showMessage("Xóa thành công!", "#16a34a");
                    loadTable(); clearForm();
                } else showMessage("Xóa thất bại!", "#dc2626");
            }
        });
    }

    private void loadTable() {
        List<Student> danhSachSV = svControl.getByLop(malop);
        List<Score> danhSachDiem = diemControl.getByMonAndLop(idmonhoc, malop,currentSoTinChi);

        ObservableList<Score> listHienThi = FXCollections.observableArrayList();

        for (Student sv : danhSachSV) {
            Score diemCuaSV = null;
            for (Score d : danhSachDiem) {
                if (d.getMasv().equals(sv.getMasv())) {
                    diemCuaSV = d;
                    break;
                }
            }
            if (diemCuaSV == null) {
                diemCuaSV = new Score(sv.getMasv(), idmonhoc, 0, 0, 0, 0,currentSoTinChi);
            }
            listHienThi.add(diemCuaSV);
        }

        table.setItems(listHienThi);
    }

    private void loadComboSv() {
        ObservableList<String> list = FXCollections.observableArrayList();
        for (Student sv : svControl.getByLop(malop)) {
            list.add(sv.getMasv() + " - " + sv.getTen());
        }
        cboSv.setItems(list);
    }

    private void clearForm() {
        cboSv.setValue(null);
        txtChuyenCan.clear(); txtBaiTap.clear();
        txtGiuaKi.clear();    txtCuoiKi.clear();
        lblDiemTong.setText("Điểm tổng: --");
        lblMessage.setText("");
        table.getSelectionModel().clearSelection();
    }

    private void showMessage(String msg, String color) {
        lblMessage.setText(msg);
        lblMessage.setStyle("-fx-text-fill: " + color + ";");
    }

    private void styleButton(Button btn, String color) {
        btn.setPrefHeight(36); btn.setPrefWidth(100);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btn.setStyle("-fx-background-color:" + color + ";-fx-text-fill:white;-fx-background-radius:6;-fx-cursor:hand;");
    }
}