package view;

import control.Databaseconnection;
import javafx.application.Application;
import javafx.stage.Stage;

public class RunApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            java.sql.Connection conn = Databaseconnection.getConnection();
            System.out.println("Kết nối MySQL thành công!");
            conn.close();
        } catch (Exception e) {
            System.out.println("Kết nối thất bại: " + e.getMessage());
        }
        Login login = new Login(primaryStage);
        login.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}