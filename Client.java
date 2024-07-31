import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static final int PORT = 1234;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java Client <server address>");
            return;
        }

        String serverAddress = args[0];

        try (Socket socket = new Socket(serverAddress, PORT);
             OutputStream outputStream = socket.getOutputStream();
             PrintWriter writer = new PrintWriter(outputStream, true);
             InputStream inputStream = socket.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
             Scanner scanner = new Scanner(System.in)) {

            // Prompt for nickname
            System.out.print("Enter your nickname: ");
            String nickname = scanner.nextLine();
            writer.println(nickname);

            // Create a separate thread to handle incoming messages
            Thread messageListener = new Thread(() -> {
                try {
                    String message;
                    while ((message = reader.readLine()) != null) {
                        System.out.println("\n" + message);
                        System.out.print("You: ");
                    }
                } catch (IOException e) {
                    System.out.println("Connection closed.");
                }
            });
            messageListener.start();

            // Main loop to handle user input and send messages
            while (true) {
                System.out.print("You: ");
                String input = scanner.nextLine();
                writer.println(input);

                // Exit if the user sends "bye"
                if (input.equalsIgnoreCase("bye")) {
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        }
    }
}
