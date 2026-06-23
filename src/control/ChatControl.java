package control;

import javafx.application.Platform;
import java.io.*;
import java.net.Socket;
import java.util.function.BiConsumer;

public class ChatControl {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    private BiConsumer<String, String> onMessageReceived;
    private java.util.function.Consumer<String> onSystemMessage;
    private FileReceiveListener onFileReceived;

    // Interface đã phân biệt được Ảnh (isImage = true) hay File thường
    public interface FileReceiveListener {
        void onFileReceived(String room, String from, String filename, String fileId, String size, boolean isImage);
    }

    public void connect(String username, BiConsumer<String, String> onMessageReceived,
                        java.util.function.Consumer<String> onSystemMessage, FileReceiveListener onFileReceived) {
        this.username = username;
        this.onMessageReceived = onMessageReceived;
        this.onSystemMessage = onSystemMessage;
        this.onFileReceived = onFileReceived;

        new Thread(() -> {
            try {
                socket = new Socket("127.0.0.1", 5005);
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

                out.println("{\"type\":\"JOIN\",\"name\":\"" + username + "\"}");

                String line;
                while ((line = in.readLine()) != null) {
                    processIncomingMessage(line);
                }
            } catch (Exception e) {
                if (onSystemMessage != null) {
                    Platform.runLater(() -> onSystemMessage.accept("❌ Không thể kết nối tới Server Chat."));
                }
            }
        }).start();
    }

    private void processIncomingMessage(String json) {
        String type = extractJsonValue(json, "type");

        if ("CHAT".equals(type)) {
            String from = extractJsonValue(json, "from");
            String room = extractJsonValue(json, "room");
            String text = extractJsonValue(json, "text");
            if (onMessageReceived != null) {
                Platform.runLater(() -> onMessageReceived.accept(room, from + ": " + text));
            }
        } else if ("SYSTEM".equals(type)) {
            String text = extractJsonValue(json, "text");
            if (onSystemMessage != null) Platform.runLater(() -> onSystemMessage.accept("Hệ thống: " + text));
        } else if ("FILE_READY".equals(type) || "IMAGE_READY".equals(type)) {
            String from = extractJsonValue(json, "from");
            String room = extractJsonValue(json, "room");
            String filename = extractJsonValue(json, "filename");
            String fileId = extractJsonValue(json, "fileId");
            String size = extractJsonValue(json, "size");
            boolean isImage = "IMAGE_READY".equals(type); // Kiểm tra xem có phải ảnh không

            if (onFileReceived != null) {
                Platform.runLater(() -> onFileReceived.onFileReceived(room, from, filename, fileId, size, isImage));
            }
        }
    }

    public void sendMessage(String room, String text) {
        if (out != null && !text.isEmpty()) {
            String json = "{\"type\":\"CHAT\",\"from\":\"" + username + "\",\"room\":\"" + room + "\",\"text\":\"" + text + "\"}";
            out.println(json);
        }
    }

    public void sendFile(String room, java.io.File file) {
        new Thread(() -> {
            try {
                byte[] data = java.nio.file.Files.readAllBytes(file.toPath());
                try (Socket fs = new Socket("127.0.0.1", 5006)) {
                    java.io.OutputStream os = fs.getOutputStream();
                    os.write((username + "\n").getBytes("UTF-8"));
                    os.write((room + "\n").getBytes("UTF-8"));
                    os.write((file.getName() + "\n").getBytes("UTF-8"));
                    os.write((data.length + "\n").getBytes("UTF-8"));
                    os.write(data);
                    os.flush();
                }
                if (onSystemMessage != null) Platform.runLater(() -> onSystemMessage.accept("📤 Đã gửi: " + file.getName()));
            } catch (Exception e) {
                if (onSystemMessage != null) Platform.runLater(() -> onSystemMessage.accept("❌ Lỗi gửi tệp: " + e.getMessage()));
            }
        }).start();
    }

    // ĐÃ BỔ SUNG: Truyền thêm hàm Runnable onSuccess để bung file ra sau khi tải xong
    public void downloadFile(String fileId, java.io.File saveTo, Runnable onSuccess) {
        new Thread(() -> {
            try (Socket fs = new Socket("127.0.0.1", 5006)) {
                DataOutputStream dos = new DataOutputStream(fs.getOutputStream());
                DataInputStream dis = new DataInputStream(fs.getInputStream());

                dos.writeBytes("DOWNLOAD " + fileId + "\n");
                dos.flush();

                int size = dis.readInt();
                if (size < 0) {
                    if (onSystemMessage != null) Platform.runLater(() -> onSystemMessage.accept("❌ File không tồn tại trên server."));
                    return;
                }

                byte[] data = new byte[size];
                int read = 0;
                while (read < size) {
                    int r = dis.read(data, read, size - read);
                    if (r < 0) break;
                    read += r;
                }
                java.nio.file.Files.write(saveTo.toPath(), data);

                if (onSystemMessage != null) Platform.runLater(() -> onSystemMessage.accept("✅ Tải thành công: " + saveTo.getName()));
                if (onSuccess != null) Platform.runLater(onSuccess); // Gọi hàm mở file/xem ảnh tại đây!
            } catch (Exception e) {
                if (onSystemMessage != null) Platform.runLater(() -> onSystemMessage.accept("❌ Lỗi tải tệp: " + e.getMessage()));
            }
        }).start();
    }

    public void disconnect() {
        try { if (socket != null && !socket.isClosed()) socket.close(); } catch (Exception ignored) {}
    }

    private String extractJsonValue(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) return "";
        start += search.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return "";
        return json.substring(start, end).replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
    }
}