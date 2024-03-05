package MyChat;

import java.io.*;
import java.net.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class Client {
    static Socket clientSocket;

    public static void main(String[] args) throws Exception {
        String defaultHost = "127.0.0.1";
        int defaultPort = 1025;

        connectToServer(defaultHost, defaultPort);
    }

    // Connects to the server using the specified host address and port number
    private static void connectToServer(String hostAddress, int portNumber) throws Exception {
        clientSocket = new Socket(hostAddress, portNumber);

        if (clientSocket != null) {
            displayConnectionDetails();
            handleServerResponse();
            sendClientMessage();
        } else {
            System.err.println("\n [Client Connection] Error Occurred \n");
        }
    }

    // Displays the details of the established connection
    private static void displayConnectionDetails() throws InterruptedException {
        System.out.println("\n     [ Secure Connection: " + clientSocket.getLocalSocketAddress() + " ] "
            + DateTimeFormatter.ofPattern("E, dd-MMM-yyyy/HH:mm:ss").format(LocalDateTime.now())
            + " (" + TimeZone.getDefault().getDisplayName() + ") ");
        Thread.sleep(2000);
    }

    /**
     * Handles the server's responses by creating a new thread
     * Within the thread, it reads input from the server's input stream and prints it to the console
    */
    private static void handleServerResponse() {
        Thread serverThread = new Thread(() -> {
            int responseLine;

            try {
                DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream());

                while ((responseLine = inputStream.read()) != -1) {
                    System.out.print((char) responseLine);
                        // if (responseLine.indexOf("Server Disconnected") != -1)
                        //     System.exit(0);
                }
            } catch (IOException exc) {
                exc.printStackTrace();
            }
        });

        serverThread.start();
    }

    /**
     * Reads client input from the command line and sends it to the server through the client socket's output stream
     * Repeats the process until the client enters a null value
    */
    private static void sendClientMessage() {
        String replyLine;

        try {
            BufferedReader clientInput = new BufferedReader(new InputStreamReader(System.in));
            PrintStream outputStream = new PrintStream(clientSocket.getOutputStream());

            while ((replyLine = clientInput.readLine()) != null) {
                outputStream.println(replyLine);
            }
        } catch (IOException exc) {
            exc.printStackTrace();
        }
    }
}