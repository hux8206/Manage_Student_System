package view;

import control.ScoreControl;
import control.StudentControl;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import model.Score;
import model.Student;

import java.util.List;

public class StatisticForm {
    private Stage stage;
    private String idmonhoc, tenmon, malop, tenlop;
    private ScoreControl diemControl = new ScoreControl();
    private StudentControl svControl = new StudentControl();

    public StatisticForm(Stage stage, String idmonhoc, String tenmon, String malop, String tenlop) {
        this.stage = stage;
        this.idmonhoc = idmonhoc;
        this.tenmon = tenmon;
        this.malop = malop;
        this.tenlop = tenlop;
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f4f8;");

        // ── Header ──────────────────────────────────────────────────
        HBox header = new HBox();
        header.setPadding(new Insets(15, 25, 15, 25));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #1e40af;");

        VBox titleBox = new VBox(2);
        Label lblTitle = new Label("Thống kê - Báo cáo");
        lblTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        lblTitle.setStyle("-fx-text-fill: white;");
        Label lblSub = new Label("Môn: " + tenmon + "  |  Lớp: " + tenlop);
        lblSub.setFont(Font.font("Arial", 12));
        lblSub.setStyle("-fx-text-fill: #bfdbfe;");
        titleBox.getChildren().addAll(lblTitle, lblSub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnBack = new Button("← Quay lại");
        btnBack.setStyle("-fx-background-color: white;-fx-text-fill: #1e40af;" +
                "-fx-background-radius: 6;-fx-cursor: hand;-fx-font-weight: bold;");
        btnBack.setOnAction(e -> new MainStage(stage, idmonhoc, tenmon, malop, tenlop).show());

        header.getChildren().addAll(titleBox, spacer, btnBack);
        root.setTop(header);

        // ── Dữ liệu ─────────────────────────────────────────────────
        control.SubjectControl mhControl = new control.SubjectControl();
        model.Subject currentMon = mhControl.getById(idmonhoc);
        int soTinChi = (currentMon != null) ? currentMon.getSotinchi() : 3;
        List<Score> diemList = diemControl.getByMonAndLop(idmonhoc, malop, soTinChi);
        List<Student> svList = svControl.getByLop(malop);

        int tongSV     = svList.size();
        int daNhapDiem = diemList.size();
        int chuaNhap   = tongSV - daNhapDiem;

        double diemTB  = diemList.stream().mapToDouble(Score::getDiemTong).average().orElse(0);
        double diemMax = diemList.stream().mapToDouble(Score::getDiemTong).max().orElse(0);
        double diemMin = diemList.stream().mapToDouble(Score::getDiemTong).min().orElse(0.0);

        long soGioi = diemList.stream().filter(d -> d.getDiemTong() >= 8).count();
        long soKha  = diemList.stream().filter(d -> d.getDiemTong() >= 6 && d.getDiemTong() < 8).count();
        long soTB   = diemList.stream().filter(d -> d.getDiemTong() >= 4 && d.getDiemTong() < 6).count();
        long soYeu  = diemList.stream().filter(d -> d.getDiemTong() < 4).count();

        // ── Center layout ────────────────────────────────────────────
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;-fx-background: #f0f4f8;");

        VBox center = new VBox(15);
        center.setPadding(new Insets(15));

        // Stat cards hàng 1
        HBox statsBox = new HBox(15);
        statsBox.getChildren().addAll(
                createStatCard("👥 Tổng sinh viên", tongSV + " người",          "#1e40af"),
                createStatCard("✅ Đã nhập điểm",   daNhapDiem + " SV",         "#16a34a"),
                createStatCard("⏳ Chưa nhập",       chuaNhap + " SV",           "#d97706"),
                createStatCard("📊 Điểm TB",         String.format("%.1f", diemTB), "#7c3aed")
        );

        // ── Khu vực biểu đồ ──────────────────────────────────────────
        HBox chartsBox = new HBox(20);
        chartsBox.setAlignment(Pos.TOP_CENTER);

        // 1) Biểu đồ cột - Phân loại học lực
        VBox barChartBox = createSectionCard("📊 Phân loại học lực");
        Canvas barCanvas = new Canvas(380, 220);
        drawBarChart(barCanvas.getGraphicsContext2D(), soGioi, soKha, soTB, soYeu, daNhapDiem);
        barChartBox.getChildren().add(barCanvas);
        HBox.setHgrow(barChartBox, Priority.ALWAYS);

        // 2) Biểu đồ tròn - Tỉ lệ nhập điểm
        VBox pieChartBox = createSectionCard("🥧 Tỉ lệ nhập điểm");
        Canvas pieCanvas = new Canvas(300, 220);
        drawPieChart(pieCanvas.getGraphicsContext2D(), daNhapDiem, chuaNhap, tongSV);
        pieChartBox.getChildren().add(pieCanvas);

        chartsBox.getChildren().addAll(barChartBox, pieChartBox);

        // ── Bảng chi tiết ────────────────────────────────────────────
        Label lblBang = new Label("📋 Chi tiết điểm - " + tenmon + " - Lớp " + tenlop);
        lblBang.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        lblBang.setStyle("-fx-text-fill: #1e40af;");

        TableView<Score> table = new TableView<>();
        table.setPrefHeight(220);

        TableColumn<Score, String> colMasv = new TableColumn<>("Mã SV");
        colMasv.setCellValueFactory(new PropertyValueFactory<>("masv"));

        TableColumn<Score, Double> colDiem = new TableColumn<>("Điểm tổng");
        colDiem.setCellValueFactory(new PropertyValueFactory<>("diemTong"));

        TableColumn<Score, String> colXepLoai = new TableColumn<>("Xếp loại");
        colXepLoai.setCellValueFactory(cellData -> {
            double d = cellData.getValue().getDiemTong();
            String xl = d >= 8 ? "Giỏi" : d >= 6 ? "Khá" : d >= 4 ? "Trung bình" : "Yếu";
            return new javafx.beans.property.SimpleStringProperty(xl);
        });
        // Tô màu xếp loại
        colXepLoai.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                String color = switch (item) {
                    case "Giỏi"       -> "#16a34a";
                    case "Khá"        -> "#2563eb";
                    case "Trung bình" -> "#d97706";
                    default           -> "#dc2626";
                };
                setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
            }
        });

        table.getColumns().addAll(colMasv, colDiem, colXepLoai);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setItems(FXCollections.observableArrayList(diemList));

        Label lblMinMax = new Label(
                String.format("🏆 Điểm cao nhất: %.1f   |   ⬇ Điểm thấp nhất: %.1f", diemMax, diemMin));
        lblMinMax.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lblMinMax.setStyle("-fx-text-fill: #374151;");

        center.getChildren().addAll(statsBox, chartsBox, lblBang, table, lblMinMax);
        scroll.setContent(center);
        root.setCenter(scroll);

        Scene scene = new Scene(root, 960, 680);
        stage.setTitle("Thống kê - " + tenmon + " - " + tenlop);
        stage.setScene(scene);
        stage.show();
    }

    // ── Biểu đồ cột ──────────────────────────────────────────────────
    private void drawBarChart(GraphicsContext gc, long gioi, long kha, long tb, long yeu, int total) {
        double w = 380, h = 220;
        double padL = 40, padB = 50, padT = 15, padR = 15;
        double chartW = w - padL - padR;
        double chartH = h - padT - padB;

        long max = Math.max(1, Math.max(Math.max(gioi, kha), Math.max(tb, yeu)));

        String[] labels = {"Giỏi", "Khá", "TB", "Yếu"};
        long[]   vals   = {gioi, kha, tb, yeu};
        Color[]  colors = {
                Color.web("#16a34a"), Color.web("#2563eb"),
                Color.web("#f59e0b"), Color.web("#dc2626")
        };

        double barW   = chartW / (labels.length * 2.0);
        double gap    = barW;
        double startX = padL + gap / 2;

        // Trục Y – đường kẻ ngang
        gc.setStroke(Color.web("#e5e7eb"));
        gc.setLineWidth(1);
        int gridLines = 4;
        for (int i = 0; i <= gridLines; i++) {
            double y = padT + chartH - (chartH * i / gridLines);
            gc.strokeLine(padL, y, w - padR, y);
            gc.setFill(Color.web("#9ca3af"));
            gc.setFont(Font.font("Arial", 10));
            gc.setTextAlign(TextAlignment.RIGHT);
            gc.fillText(String.valueOf(max * i / gridLines), padL - 4, y + 4);
        }

        // Vẽ cột
        for (int i = 0; i < labels.length; i++) {
            double x       = startX + i * (barW + gap);
            double barH    = vals[i] == 0 ? 2 : (double) vals[i] / max * chartH;
            double y       = padT + chartH - barH;

            // Bóng đổ nhẹ
            gc.setFill(Color.rgb(0, 0, 0, 0.08));
            gc.fillRoundRect(x + 3, y + 3, barW, barH, 6, 6);

            // Cột màu
            gc.setFill(colors[i]);
            gc.fillRoundRect(x, y, barW, barH, 6, 6);

            // Số trên cột
            gc.setFill(Color.web("#1f2937"));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(String.valueOf(vals[i]), x + barW / 2, y - 5);

            // Nhãn dưới
            gc.setFill(Color.web("#374151"));
            gc.setFont(Font.font("Arial", 11));
            gc.fillText(labels[i], x + barW / 2, padT + chartH + 18);

            // % dưới nhãn
            double pct = total == 0 ? 0 : (double) vals[i] / total * 100;
            gc.setFill(Color.web("#6b7280"));
            gc.setFont(Font.font("Arial", 10));
            gc.fillText(String.format("%.0f%%", pct), x + barW / 2, padT + chartH + 33);
        }

        // Đường trục X
        gc.setStroke(Color.web("#d1d5db"));
        gc.setLineWidth(1.5);
        gc.strokeLine(padL, padT + chartH, w - padR, padT + chartH);
    }

    // ── Biểu đồ tròn ─────────────────────────────────────────────────
    private void drawPieChart(GraphicsContext gc, int daNhap, int chuaNhap, int total) {
        double w = 300, h = 220;
        double cx = 110, cy = 105, r = 80;

        if (total == 0) {
            gc.setFill(Color.web("#9ca3af"));
            gc.setFont(Font.font("Arial", 13));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("Không có dữ liệu", w / 2, h / 2);
            return;
        }

        double angleDa   = (double) daNhap   / total * 360;
        double angleChua = (double) chuaNhap / total * 360;

        // Slice 1: Đã nhập
        gc.setFill(Color.web("#16a34a"));
        gc.fillArc(cx - r, cy - r, r * 2, r * 2, 90, -angleDa, javafx.scene.shape.ArcType.ROUND);

        // Slice 2: Chưa nhập
        if (angleChua > 0) {
            gc.setFill(Color.web("#f59e0b"));
            gc.fillArc(cx - r, cy - r, r * 2, r * 2, 90 - angleDa, -angleChua, javafx.scene.shape.ArcType.ROUND);
        }

        // Vòng viền trắng
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeOval(cx - r, cy - r, r * 2, r * 2);

        // Chú thích (legend)
        double lx = cx + r + 15;

        drawLegendItem(gc, lx, cy - 25, Color.web("#16a34a"),
                "Đã nhập", String.format("%d SV (%.0f%%)", daNhap, (double) daNhap / total * 100));
        drawLegendItem(gc, lx, cy + 10, Color.web("#f59e0b"),
                "Chưa nhập", String.format("%d SV (%.0f%%)", chuaNhap, (double) chuaNhap / total * 100));

        // Tổng ở giữa (donut effect)
        gc.setFill(Color.WHITE);
        gc.fillOval(cx - r * 0.45, cy - r * 0.45, r * 0.9, r * 0.9);
        gc.setFill(Color.web("#1f2937"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(String.valueOf(total), cx, cy + 3);
        gc.setFont(Font.font("Arial", 9));
        gc.setFill(Color.web("#6b7280"));
        gc.fillText("Tổng SV", cx, cy + 15);
    }

    private void drawLegendItem(GraphicsContext gc, double x, double y,
                                Color color, String label, String value) {
        gc.setFill(color);
        gc.fillRoundRect(x, y, 12, 12, 3, 3);
        gc.setFill(Color.web("#374151"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(label, x + 16, y + 10);
        gc.setFill(Color.web("#6b7280"));
        gc.setFont(Font.font("Arial", 10));
        gc.fillText(value, x + 16, y + 23);
    }

    // ── Helpers ───────────────────────────────────────────────────────
    private VBox createSectionCard(String title) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: white;" +
                "-fx-border-color: #e2e8f0;-fx-border-radius: 10;-fx-background-radius: 10;");
        Label lbl = new Label(title);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lbl.setStyle("-fx-text-fill: #1e40af;");
        card.getChildren().add(lbl);
        return card;
    }

    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15, 20, 15, 20));
        card.setStyle("-fx-background-color: white;-fx-border-color: #e2e8f0;" +
                "-fx-border-radius: 10;-fx-background-radius: 10;");
        Label lblTitle = new Label(title);
        lblTitle.setFont(Font.font("Arial", 12));
        lblTitle.setStyle("-fx-text-fill: #6b7280;");
        Label lblValue = new Label(value);
        lblValue.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        lblValue.setStyle("-fx-text-fill: " + color + ";");
        card.getChildren().addAll(lblTitle, lblValue);
        return card;
    }
}