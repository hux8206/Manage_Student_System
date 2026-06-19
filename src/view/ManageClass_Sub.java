package view;

import control.ClassControl;
import control.SubjectControl;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.CLass;
import model.Subject;

public class ManageClass_Sub {
    private Stage stage;
    private SubjectControl mhControl  = new SubjectControl();
    private ClassControl lopControl    = new ClassControl();

    private TableView<Subject> tableMonHoc = new TableView<>();
    private TableView<CLass> tableLop       = new TableView<>();

    private TextField txtIdmonhoc = new TextField();
    private TextField txtTenmon   = new TextField();
    private TextField txtMalop    = new TextField();
    private TextField txtTenlop   = new TextField();
    private Label lblMsgMon       = new Label();
    private Label lblMsgLop       = new Label();

    public ManageClass_Sub(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f4f8;");

        // Header
        HBox header = new HBox();
        header.setPadding(new Insets(15, 25, 15, 25));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #1e40af;");

        Label lblTitle = new Label("Quản lý Môn học & Lớp học");
        lblTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        lblTitle.setStyle("-fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnBack = new Button("← Quay lại");
        btnBack.setStyle("-fx-background-color: white;-fx-text-fill: #1e40af;-fx-background-radius: 6;-fx-cursor: hand;-fx-font-weight: bold;");
        btnBack.setOnAction(e -> new SelectionForm(stage).show());

        header.getChildren().addAll(lblTitle, spacer, btnBack);
        root.setTop(header);

        // Center - 2 phần: Môn học trái, Lớp học phải
        HBox center = new HBox(15);
        center.setPadding(new Insets(15));

        // ===== PHẦN MÔN HỌC =====
        VBox monHocPane = new VBox(10);
        HBox.setHgrow(monHocPane, Priority.ALWAYS);
        monHocPane.setPadding(new Insets(15));
        monHocPane.setStyle("-fx-background-color: white;-fx-border-color: #e2e8f0;-fx-border-radius: 10;-fx-background-radius: 10;");

        Label lblMonHoc = new Label("📚 Môn học");
        lblMonHoc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        lblMonHoc.setStyle("-fx-text-fill: #1e40af;");

        // Bảng môn học
        TableColumn<Subject, String> colIdmon = new TableColumn<>("Mã môn");
        colIdmon.setCellValueFactory(new PropertyValueFactory<>("idmonhoc"));
        TableColumn<Subject, String> colTenmon = new TableColumn<>("Tên môn");
        colTenmon.setCellValueFactory(new PropertyValueFactory<>("tenmon"));
        tableMonHoc.getColumns().addAll(colIdmon, colTenmon);
        tableMonHoc.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableMonHoc.setPrefHeight(200);

        tableMonHoc.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                txtIdmonhoc.setText(sel.getIdmonhoc());
                txtTenmon.setText(sel.getTenmon());
                txtIdmonhoc.setDisable(true);
            }
        });

        // Form môn học
        Label lblIdmon = new Label("Mã môn");
        lblIdmon.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        txtIdmonhoc.setPromptText("Nhập mã môn...");
        txtIdmonhoc.setPrefHeight(34);

        Label lblTenmon = new Label("Tên môn");
        lblTenmon.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        txtTenmon.setPromptText("Nhập tên môn...");
        txtTenmon.setPrefHeight(34);

        lblMsgMon.setFont(Font.font("Arial", 12));

        Button btnAddMon    = new Button("Thêm");
        Button btnUpdateMon = new Button("Sửa");
        Button btnDeleteMon = new Button("Xóa");
        Button btnClearMon  = new Button("Xóa form");

        styleButton(btnAddMon,    "#16a34a", 80);
        styleButton(btnUpdateMon, "#2563eb", 80);
        styleButton(btnDeleteMon, "#dc2626", 80);
        styleButton(btnClearMon,  "#6b7280", 80);

        btnAddMon.setOnAction(e -> {
            String id  = txtIdmonhoc.getText().trim();
            String ten = txtTenmon.getText().trim();
            if (id.isEmpty() || ten.isEmpty()) { showMsg(lblMsgMon, "Điền đầy đủ!", "#dc2626"); return; }
            if (mhControl.add(new Subject(id, ten))) {
                showMsg(lblMsgMon, "Thêm thành công!", "#16a34a");
                loadMonHoc(); clearFormMon();
            } else showMsg(lblMsgMon, "Mã môn đã tồn tại!", "#dc2626");
        });

        btnUpdateMon.setOnAction(e -> {
            String id  = txtIdmonhoc.getText().trim();
            String ten = txtTenmon.getText().trim();
            if (id.isEmpty() || ten.isEmpty()) { showMsg(lblMsgMon, "Chọn môn cần sửa!", "#dc2626"); return; }
            if (mhControl.update(new Subject(id, ten))) {
                showMsg(lblMsgMon, "Cập nhật thành công!", "#16a34a");
                loadMonHoc(); clearFormMon();
            } else showMsg(lblMsgMon, "Cập nhật thất bại!", "#dc2626");
        });

        btnDeleteMon.setOnAction(e -> {
            String id = txtIdmonhoc.getText().trim();
            if (id.isEmpty()) { showMsg(lblMsgMon, "Chọn môn cần xóa!", "#dc2626"); return; }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setContentText("Xóa môn " + id + "?");
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    if (mhControl.delete(id)) {
                        showMsg(lblMsgMon, "Xóa thành công!", "#16a34a");
                        loadMonHoc(); clearFormMon();
                    } else showMsg(lblMsgMon, "Xóa thất bại!", "#dc2626");
                }
            });
        });

        btnClearMon.setOnAction(e -> clearFormMon());

        HBox btnRowMon = new HBox(6, btnAddMon, btnUpdateMon, btnDeleteMon, btnClearMon);

        monHocPane.getChildren().addAll(
                lblMonHoc, tableMonHoc,
                new Separator(),
                lblIdmon, txtIdmonhoc,
                lblTenmon, txtTenmon,
                lblMsgMon, btnRowMon
        );

        // ===== PHẦN LỚP HỌC =====
        VBox lopPane = new VBox(10);
        HBox.setHgrow(lopPane, Priority.ALWAYS);
        lopPane.setPadding(new Insets(15));
        lopPane.setStyle("-fx-background-color: white;-fx-border-color: #e2e8f0;-fx-border-radius: 10;-fx-background-radius: 10;");

        Label lblLopHoc = new Label("🏫 Lớp học");
        lblLopHoc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        lblLopHoc.setStyle("-fx-text-fill: #1e40af;");

        // Bảng lớp học
        TableColumn<CLass, String> colMalop  = new TableColumn<>("Mã lớp");
        colMalop.setCellValueFactory(new PropertyValueFactory<>("malop"));
        TableColumn<CLass, String> colTenlop = new TableColumn<>("Tên lớp");
        colTenlop.setCellValueFactory(new PropertyValueFactory<>("tenlop"));
        tableLop.getColumns().addAll(colMalop, colTenlop);
        tableLop.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableLop.setPrefHeight(200);

        tableLop.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                txtMalop.setText(sel.getMalop());
                txtTenlop.setText(sel.getTenlop());
                txtMalop.setDisable(true);
            }
        });

        // Form lớp học
        Label lblMalopF = new Label("Mã lớp");
        lblMalopF.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        txtMalop.setPromptText("Nhập mã lớp...");
        txtMalop.setPrefHeight(34);

        Label lblTenlopF = new Label("Tên lớp");
        lblTenlopF.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        txtTenlop.setPromptText("Nhập tên lớp...");
        txtTenlop.setPrefHeight(34);

        lblMsgLop.setFont(Font.font("Arial", 12));

        Button btnAddLop    = new Button("Thêm");
        Button btnUpdateLop = new Button("Sửa");
        Button btnDeleteLop = new Button("Xóa");
        Button btnClearLop  = new Button("Xóa form");

        styleButton(btnAddLop,    "#16a34a", 80);
        styleButton(btnUpdateLop, "#2563eb", 80);
        styleButton(btnDeleteLop, "#dc2626", 80);
        styleButton(btnClearLop,  "#6b7280", 80);

        btnAddLop.setOnAction(e -> {
            String ma  = txtMalop.getText().trim();
            String ten = txtTenlop.getText().trim();

            // Lấy mã môn trực tiếp từ ô Text bên trái cho linh hoạt
            String idmon = txtIdmonhoc.getText().trim();

            if (ma.isEmpty() || ten.isEmpty()) {
                showMsg(lblMsgLop, "Điền đầy đủ mã và tên lớp!", "#dc2626");
                return;
            }
            if (idmon.isEmpty()) {
                // Sửa lại câu thông báo cho rõ ràng cách dùng
                showMsg(lblMsgLop, "Vui lòng CLICK CHỌN 1 MÔN trên bảng bên trái!", "#dc2626");
                return;
            }

            // Dùng idmon thay vì selectedMon.getIdmonhoc()
            if (lopControl.add(new CLass(ma, ten, idmon))) {
                showMsg(lblMsgLop, "Thêm thành công!", "#16a34a");
                loadLop();
                clearFormLop();
            } else {
                showMsg(lblMsgLop, "Mã lớp đã tồn tại!", "#dc2626");
            }
        });

        btnUpdateLop.setOnAction(e -> {
            String ma  = txtMalop.getText().trim();
            String ten = txtTenlop.getText().trim();
            String idmon = txtIdmonhoc.getText().trim();

            if (ma.isEmpty() || ten.isEmpty()) {
                showMsg(lblMsgLop, "Chọn lớp cần sửa!", "#dc2626");
                return;
            }
            if (idmon.isEmpty()) {
                showMsg(lblMsgLop, "Vui lòng CLICK CHỌN 1 MÔN trên bảng bên trái!", "#dc2626");
                return;
            }

            if (lopControl.update(new CLass(ma, ten, idmon))) {
                showMsg(lblMsgLop, "Cập nhật thành công!", "#16a34a");
                loadLop();
                clearFormLop();
            } else {
                showMsg(lblMsgLop, "Cập nhật thất bại!", "#dc2626");
            }
        });

        btnDeleteLop.setOnAction(e -> {
            String ma = txtMalop.getText().trim();
            if (ma.isEmpty()) { showMsg(lblMsgLop, "Chọn lớp cần xóa!", "#dc2626"); return; }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setContentText("Xóa lớp " + ma + "?");
            confirm.showAndWait().ifPresent(r -> {
                if (r == ButtonType.OK) {
                    if (lopControl.delete(ma)) {
                        showMsg(lblMsgLop, "Xóa thành công!", "#16a34a");
                        loadLop(); clearFormLop();
                    } else showMsg(lblMsgLop, "Xóa thất bại!", "#dc2626");
                }
            });
        });

        btnClearLop.setOnAction(e -> clearFormLop());

        HBox btnRowLop = new HBox(6, btnAddLop, btnUpdateLop, btnDeleteLop, btnClearLop);

        lopPane.getChildren().addAll(
                lblLopHoc, tableLop,
                new Separator(),
                lblMalopF, txtMalop,
                lblTenlopF, txtTenlop,
                lblMsgLop, btnRowLop
        );

        center.getChildren().addAll(monHocPane, lopPane);
        root.setCenter(center);

        loadMonHoc();
        loadLop();

        Scene scene = new Scene(root, 900, 650);
        stage.setTitle("Quản lý Môn học & Lớp học");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    private void loadMonHoc() {
        tableMonHoc.setItems(FXCollections.observableArrayList(mhControl.getAll()));
    }

    private void loadLop() {
        tableLop.setItems(FXCollections.observableArrayList(lopControl.getAll()));
    }

    private void clearFormMon() {
        txtIdmonhoc.clear(); txtTenmon.clear();
        lblMsgMon.setText("");
        txtIdmonhoc.setDisable(false);
        tableMonHoc.getSelectionModel().clearSelection();
    }

    private void clearFormLop() {
        txtMalop.clear(); txtTenlop.clear();
        lblMsgLop.setText("");
        txtMalop.setDisable(false);
        tableLop.getSelectionModel().clearSelection();
    }

    private void showMsg(Label lbl, String msg, String color) {
        lbl.setText(msg);
        lbl.setStyle("-fx-text-fill: " + color + ";");
    }

    private void styleButton(Button btn, String color, int width) {
        btn.setPrefHeight(34); btn.setPrefWidth(width);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        btn.setStyle("-fx-background-color:" + color + ";-fx-text-fill:white;-fx-background-radius:6;-fx-cursor:hand;");
    }
}