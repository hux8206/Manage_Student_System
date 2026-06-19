package view;

import control.ClassControl;
import control.SubjectControl;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import model.CLass;
import model.Subject;

public class SelectionForm {
    private Stage stage;
    private SubjectControl mhControl = new SubjectControl();
    private ClassControl lopControl   = new ClassControl();

    public SelectionForm(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        root.setStyle("-fx-background-color: #f0f4f8;");

        Label icon = new Label("🎓");
        icon.setFont(Font.font(50));

        Label lblTitle = new Label("Chọn môn & lớp");
        lblTitle.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        lblTitle.setStyle("-fx-text-fill: #1e40af;");

        Label lblSub = new Label("Chọn môn học và lớp bạn muốn quản lý");
        lblSub.setFont(Font.font("Arial", 14));
        lblSub.setStyle("-fx-text-fill: #6b7280;");

        // Card chọn môn lớp
        VBox card = new VBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(30, 40, 30, 40));
        card.setMaxWidth(420);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #e2e8f0;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;"
        );

        // ComboBox môn học
        Label lblMon = new Label("Môn học");
        lblMon.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lblMon.setStyle("-fx-text-fill: #374151;");

        ComboBox<String> cboMon = new ComboBox<>();
        cboMon.setMaxWidth(Double.MAX_VALUE);
        cboMon.setPrefHeight(40);
        cboMon.setPromptText("-- Chọn môn học --");
        loadComboMon(cboMon);

        // ComboBox lớp
        Label lblLop = new Label("Lớp học");
        lblLop.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lblLop.setStyle("-fx-text-fill: #374151;");

        ComboBox<String> cboLop = new ComboBox<>();
        cboLop.setMaxWidth(Double.MAX_VALUE);
        cboLop.setPrefHeight(40);
        cboLop.setPromptText("-- Chọn lớp --");
        // Đã xóa loadComboLop(cboLop) mặc định ở đây để tránh load toàn bộ lớp

        // --- BẮT SỰ KIỆN CASCADING: Khi chọn Môn sẽ tự động load Lớp ---
        cboMon.setOnAction(e -> {
            String selMon = cboMon.getValue();
            if (selMon != null) {
                // Lấy ID môn học (phần text trước dấu "-")
                String idmonhoc = selMon.split(" - ")[0].trim();

                // Nạp danh sách lớp tương ứng vào Combobox Lớp
                loadComboLopTheoMon(cboLop, idmonhoc);

                // Reset lại hiển thị của Combobox Lớp
                cboLop.getSelectionModel().clearSelection();
                cboLop.setPromptText("-- Chọn lớp --");
            }
        });
        // --------------------------------------------------------------

        Label lblMessage = new Label("");
        lblMessage.setFont(Font.font("Arial", 13));
        lblMessage.setStyle("-fx-text-fill: #dc2626;");

        // Nút vào quản lý
        Button btnVao = new Button("Vào quản lý →");
        btnVao.setMaxWidth(Double.MAX_VALUE);
        btnVao.setPrefHeight(42);
        btnVao.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        btnVao.setStyle(
                "-fx-background-color: #2563eb;" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        );

        btnVao.setOnAction(e -> {
            String selMon = cboMon.getValue();
            String selLop = cboLop.getValue();

            if (selMon == null || selLop == null) {
                lblMessage.setText("Vui lòng chọn đầy đủ môn và lớp!");
                return;
            }

            String idmonhoc = selMon.split(" - ")[0].trim();
            String tenmon   = selMon.split(" - ")[1].trim();
            String malop    = selLop.split(" - ")[0].trim();
            String tenlop   = selLop.split(" - ")[1].trim();

            lblMessage.setText(""); // Xóa thông báo lỗi cũ nếu có
            new MainStage(stage, idmonhoc, tenmon, malop, tenlop).show();
        });

        // Nút quản lý môn & lớp
        Button btnQuanLy = new Button("⚙ Quản lý môn học & lớp");
        btnQuanLy.setMaxWidth(Double.MAX_VALUE);
        btnQuanLy.setPrefHeight(40);
        btnQuanLy.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btnQuanLy.setStyle(
                "-fx-background-color: #f0f9ff;" +
                        "-fx-text-fill: #0369a1;" +
                        "-fx-border-color: #bae6fd;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        );
        btnQuanLy.setOnAction(e -> {
            new ManageClass_Sub(stage).show();
        });

        // Nút đăng xuất
        Button btnLogout = new Button("Đăng xuất");
        btnLogout.setMaxWidth(Double.MAX_VALUE);
        btnLogout.setPrefHeight(38);
        btnLogout.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #6b7280;" +
                        "-fx-border-color: #d1d5db;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        );
        btnLogout.setOnAction(e -> new Login(stage).show());

        card.getChildren().addAll(
                lblMon, cboMon,
                lblLop, cboLop,
                lblMessage,
                btnVao,
                new Separator(),
                btnQuanLy,
                btnLogout
        );

        root.getChildren().addAll(icon, lblTitle, lblSub, card);

        Scene scene = new Scene(root, 500, 600);
        stage.setTitle("Chọn môn & lớp");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    private void loadComboMon(ComboBox<String> cbo) {
        for (Subject mh : mhControl.getAll()) {
            cbo.getItems().add(mh.getIdmonhoc() + " - " + mh.getTenmon());
        }
    }

    // --- HÀM MỚI: Chỉ load các lớp thuộc về mã môn học được truyền vào ---
    private void loadComboLopTheoMon(ComboBox<String> cbo, String idmonhoc) {
        cbo.getItems().clear(); // Xóa sạch danh sách cũ
        for (CLass lop : lopControl.getByMonHoc(idmonhoc)) {
            cbo.getItems().add(lop.getMalop() + " - " + lop.getTenlop());
        }
    }
}