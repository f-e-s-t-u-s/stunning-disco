import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Server {
    private static final int PORT = 1234;
    private static final ConcurrentMap<PrintWriter, String> clientWriters = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT + "...");

            while (true) {
                // Accept a new client connection
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                // Create a new thread for the client
                ClientHandler clientHandler = new ClientHandler(socket);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private PrintWriter writer;
        private String nickname;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (
                InputStream inputStream = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                OutputStream outputStream = socket.getOutputStream()
            ) {
                writer = new PrintWriter(outputStream, true);

                // Get nickname from client
                writer.println("Enter your nickname:");
                nickname = reader.readLine();
                if (nickname == null || nickname.trim().isEmpty()) {
                    nickname = "Anonymous";
                }

                // Register client
                clientWriters.put(writer, nickname);

                // Notify others that this client has joined
                broadcast(nickname + " has joined the chat.");

                String message;
                while ((message = reader.readLine()) != null) {
                    System.out.println(nickname + ": " + message);

                    // Exit if the client sends "bye"
                    if (message.equals("bye")) {
                        System.out.println(nickname + " sent bye... EXITING");
                        break;
                    }

                    // Broadcast message to all clients
                    broadcast(nickname + ": " + message);
                }

                // Notify others that this client has left
                broadcast(nickname + " has left the chat.");
            } catch (IOException e) {
                System.err.println("I/O error: " + e.getMessage());
            } finally {
                // Clean up
                clientWriters.remove(writer);
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error closing socket: " + e.getMessage());
                }
            }
        }

        private void broadcast(String message) {
            for (PrintWriter writer : clientWriters.keySet()) {
                writer.println(message);
            }
        }
    }
}
