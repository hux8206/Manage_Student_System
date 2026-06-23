package server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * ChatServer - Multi-feature Chat Server
 * Features: Text, Emoji, File Transfer, Voice/Video Call Signaling, Steganography
 * Protocol: JSON-based messaging over TCP port 5005
 *           File chunks over TCP port 5006
 */
public class ChatServer {

    // ── Ports ──────────────────────────────────────────────
    static final int MSG_PORT  = 5005;   // Text + signaling
    static final int FILE_PORT = 5006;   // File / image transfer
    static final int VOICE_PORT = 5007;   // UDP voice relay
    static final int VIDEO_PORT = 5008;   // UDP video relay

    // ── Client registry ────────────────────────────────────
    static final Map<String, ClientSession> sessions = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        System.out.println("╔══════════════════════════════════╗");
        System.out.println("║     AdvChat Server  v2.0         ║");
        System.out.println("╠══════════════════════════════════╣");
        System.out.printf ("║  Message port : %-5d            ║%n", MSG_PORT);
        System.out.printf ("║  File    port : %-5d            ║%n", FILE_PORT);
        System.out.println("╚══════════════════════════════════╝");

        // File-transfer listener in separate thread
        new Thread(ChatServer::runFileServer, "file-server").start();

        // UDP relay listeners for voice/video calls
        new Thread(() -> runUdpRelay(VOICE_PORT), "voice-relay").start();
        new Thread(() -> runUdpRelay(VIDEO_PORT), "video-relay").start();

        // Message server (main thread)
        try (ServerSocket ss = new ServerSocket(MSG_PORT)) {
            while (true) {
                Socket client = ss.accept();
                new Thread(new MsgHandler(client), "msg-" + client.getPort()).start();
            }
        }
    }

    // ═══════════════════════════════════════════════════════
    //  MESSAGE HANDLER
    // ═══════════════════════════════════════════════════════
    static class MsgHandler implements Runnable {
        private final Socket socket;
        private ClientSession session;

        MsgHandler(Socket socket) { this.socket = socket; }

        @Override public void run() {
            try (BufferedReader in  = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), "UTF-8"));
                 PrintWriter   out  = new PrintWriter(
                         new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true)) {

                String line;
                while ((line = in.readLine()) != null) {
                    JsonMsg msg = JsonMsg.parse(line);
                    if (msg == null) continue;

                    switch (msg.type) {
                        case "JOIN":   handleJoin(msg, out);   break;
                        case "CHAT":   handleChat(msg);         break;
                        case "CALL":       handleSignal(msg);  break;  // voice call signal
                        case "VIDEO_CALL": handleSignal(msg);  break;  // video call signal
                        case "STEGO":      handleChat(msg);    break;  // steganography (khong echo lai)
                        case "TYPING": broadcast(line, session == null ? null : session.name); break;
                        default:       broadcast(line, null);   break;
                    }
                }
            } catch (IOException ignored) {
            } finally {
                if (session != null) {
                    sessions.remove(session.name);
                    broadcast(JsonMsg.system(session.name + " đã rời phòng chat."), null);
                    System.out.println("[LEAVE] " + session.name);
                }
            }
        }

        private void handleJoin(JsonMsg msg, PrintWriter out) {
            String name = msg.get("name");
            if (name == null || name.isBlank()) name = "User-" + socket.getPort();
            session = new ClientSession(name, out);
            sessions.put(name, session);
            System.out.println("[JOIN] " + name);

            // Send user-list to newcomer
            out.println(JsonMsg.userList(new ArrayList<>(sessions.keySet())));
            // Notify others
            broadcast(JsonMsg.system(name + " đã vào phòng chat. 👋"), name);
            // Send updated user-list to all
            broadcastAll(JsonMsg.userList(new ArrayList<>(sessions.keySet())));
        }

        private void handleChat(JsonMsg msg) {
            String raw = msg.toJson();
            System.out.println("[MSG] " + msg.get("from") + ": " +
                    (msg.type.equals("STEGO") ? "[STEGO IMAGE]" : msg.get("text")));
            // Gửi đến tất cả NGOẠI TRỪ người gửi (client tự hiển thị ngay)
            broadcast(raw, msg.get("from"));
        }

        private void handleSignal(JsonMsg msg) {
            // Route call signals to specific target or broadcast
            String to = msg.get("to");
            if (to != null && sessions.containsKey(to)) {
                sessions.get(to).out.println(msg.toJson());
            } else {
                broadcast(msg.toJson(), msg.get("from"));
            }
        }
    }

    // ═══════════════════════════════════════════════════════
    //  FILE SERVER  (port 5006)
    // ═══════════════════════════════════════════════════════
    static void runFileServer() {
        try (ServerSocket ss = new ServerSocket(FILE_PORT)) {
            System.out.println("[FILE-SERVER] Listening on " + FILE_PORT);
            while (true) {
                Socket s = ss.accept();
                new Thread(() -> handleFileTransfer(s), "file-" + s.getPort()).start();
            }
        } catch (IOException e) {
            System.err.println("[FILE-SERVER] Error: " + e.getMessage());
        }
    }

    /**
     * Protocol for file send:
     *   Client → Server:
     *     LINE 1: SENDER_NAME
     *     LINE 2: FILENAME
     *     LINE 3: FILESIZE (bytes)
     *     Then raw bytes of file
     *   Server → all clients (on MSG port):
     *     JSON { type:"FILE_READY", from, filename, fileId, size }
     *   Any client wanting to download connects to port 5006:
     *     Sends: DOWNLOAD <fileId>
     *     Server sends: raw bytes
     */
    static final Map<String, byte[]> fileStore = new ConcurrentHashMap<>();
    static long fileIdCounter = 0;

    static void handleFileTransfer(Socket s) {
        try {
            BufferedInputStream in = new BufferedInputStream(s.getInputStream());
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());

            String firstLine = readLineUTF8(in);
            if (firstLine == null) return;

            if (firstLine.startsWith("DOWNLOAD ")) {
                String fileId = firstLine.substring(9).trim();
                byte[] data = fileStore.get(fileId);
                if (data != null) {
                    dout.writeInt(data.length);
                    dout.write(data);
                    dout.flush();
                } else {
                    dout.writeInt(-1);
                }
            } else {
                // UPLOAD
                String senderName = firstLine;
                String roomName   = readLineUTF8(in); // <-- Bổ sung đọc mã Lớp học
                String fileName   = readLineUTF8(in);
                int    fileSize   = Integer.parseInt(readLineUTF8(in).trim());

                byte[] data = new byte[fileSize];
                int read = 0;
                while (read < fileSize) {
                    int r = in.read(data, read, fileSize - read);
                    if (r < 0) break;
                    read += r;
                }

                String fileId = "F" + (++fileIdCounter) + "_" + System.currentTimeMillis();
                fileStore.put(fileId, data);

                boolean isImage = isImageFile(fileName);
                String msgType  = isImage ? "IMAGE_READY" : "FILE_READY";

                // Bổ sung "room" vào JSON để phát sóng đúng lớp
                String notify = "{\"type\":\"" + msgType + "\","
                        + "\"from\":\"" + esc(senderName) + "\","
                        + "\"room\":\"" + esc(roomName) + "\","
                        + "\"filename\":\"" + esc(fileName) + "\","
                        + "\"fileId\":\"" + fileId + "\","
                        + "\"size\":" + fileSize + "}";
                broadcast(notify, null);
                System.out.println("[FILE] Stored: " + fileName + " in room: " + roomName);
            }
        } catch (Exception e) {
            System.err.println("[FILE-HANDLER] " + e.getMessage());
        } finally {
            try { s.close(); } catch (IOException ignored) {}
        }
    }
    static boolean isImageFile(String name) {
        String low = name.toLowerCase();
        return low.endsWith(".png") || low.endsWith(".jpg") || low.endsWith(".jpeg")
                || low.endsWith(".gif") || low.endsWith(".bmp") || low.endsWith(".webp");
    }

    static String readLine(DataInputStream din) throws IOException {
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = din.read()) != -1 && c != '\n') {
            if (c != '\r') sb.append((char) c);
        }
        return sb.toString();
    }


    // ═══════════════════════════════════════════════════════
    //  UDP RELAY SERVER  (voice/video)
    // ═══════════════════════════════════════════════════════
    static final Map<Integer, Map<String, Set<SocketAddress>>> udpRooms = new ConcurrentHashMap<>();

    static void runUdpRelay(int port) {
        udpRooms.putIfAbsent(port, new ConcurrentHashMap<>());
        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("[UDP-RELAY] Listening on " + port);
            byte[] buf = new byte[65535];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                String callId = readCallId(packet.getData(), packet.getLength());
                if (callId == null || callId.isBlank()) continue;

                SocketAddress sender = packet.getSocketAddress();
                Map<String, Set<SocketAddress>> rooms = udpRooms.get(port);
                Set<SocketAddress> peers = rooms.computeIfAbsent(callId, k -> ConcurrentHashMap.newKeySet());
                peers.add(sender);

                for (SocketAddress peer : peers) {
                    if (peer.equals(sender)) continue;
                    DatagramPacket forward = new DatagramPacket(
                            packet.getData(), packet.getLength(), peer
                    );
                    socket.send(forward);
                }
            }
        } catch (IOException e) {
            System.err.println("[UDP-RELAY " + port + "] Error: " + e.getMessage());
        }
    }

    static String readCallId(byte[] data, int len) {
        if (len < 2) return null;
        int idLen = ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
        if (idLen <= 0 || idLen > 300 || len < 2 + idLen) return null;
        try {
            return new String(data, 2, idLen, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    // ═══════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════
    static void broadcast(String json, String excludeName) {
        for (ClientSession s : sessions.values()) {
            if (excludeName == null || !s.name.equals(excludeName)) {
                s.out.println(json);
            }
        }
    }

    static void broadcastAll(String json) {
        for (ClientSession s : sessions.values()) s.out.println(json);
    }

    static String esc(String s) {
        return s.replace("\\","\\\\").replace("\"","\\\"");
    }

    // ═══════════════════════════════════════════════════════
    //  DATA CLASSES
    // ═══════════════════════════════════════════════════════
    static class ClientSession {
        String name; PrintWriter out;
        ClientSession(String name, PrintWriter out) { this.name=name; this.out=out; }
    }

    static class JsonMsg {
        String type;
        private final Map<String,String> fields = new LinkedHashMap<>();

        String get(String key) { return fields.get(key); }

        String toJson() {
            StringBuilder sb = new StringBuilder("{\"type\":\"").append(type).append("\"");
            for (Map.Entry<String,String> e : fields.entrySet()) {
                sb.append(",\"").append(e.getKey()).append("\":\"").append(esc(e.getValue())).append("\"");
            }
            sb.append("}");
            return sb.toString();
        }

        static JsonMsg parse(String json) {
            try {
                JsonMsg m = new JsonMsg();
                json = json.trim();
                if (!json.startsWith("{")) return null;
                json = json.substring(1, json.length()-1);
                // simple key-value extraction (no nested)
                String[] pairs = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                for (String pair : pairs) {
                    String[] kv = pair.split(":", 2);
                    if (kv.length < 2) continue;
                    String k = kv[0].trim().replaceAll("\"","");
                    String v = kv[1].trim();
                    if (v.startsWith("\"")) v = v.substring(1, v.length()-1);
                    if (k.equals("type")) m.type = v;
                    else m.fields.put(k, v.replace("\\\"","\"").replace("\\\\","\\"));
                }
                return m.type != null ? m : null;
            } catch (Exception e) { return null; }
        }

        static String system(String text) {
            return "{\"type\":\"SYSTEM\",\"text\":\"" + esc(text) + "\"}";
        }

        static String userList(List<String> names) {
            StringBuilder sb = new StringBuilder("{\"type\":\"USERS\",\"list\":\"");
            sb.append(String.join(",", names)).append("\"}");
            return sb.toString();
        }

        static String esc(String s) {
            return s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n");
        }
    }
    static String readLineUTF8(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int c;
        while ((c = in.read()) != -1 && c != '\n') {
            if (c != '\r') buffer.write(c);
        }
        if (buffer.size() == 0 && c == -1) return null; // EOF
        return new String(buffer.toByteArray(), "UTF-8");
    }
}