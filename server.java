package MyChat;

import java.io.*;
import java.net.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class Server {
    static ServerSocket serverSocket;
    static List<PrintStream> connectedClients = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        int defaultPort = 1025;

        hostServer(defaultPort);
    }

    // Starts the server on the specified port number
    private static void hostServer(int portNumber) throws Exception {
        serverSocket = new ServerSocket(portNumber);

        if (serverSocket != null) {
            displayConnectionDetails();
            handleClientConnection();
        } else {
            System.err.println("\n [Server Connection] Error Occurred \n");
        }
    }

    // Displays the details of the server connection
    private static void displayConnectionDetails() {
        System.out.println("\n     [ Local Server: Online (" + serverSocket.getLocalSocketAddress() + ") "
            + DateTimeFormatter.ofPattern("E, dd-MMM-yyyy/HH:mm:ss").format(LocalDateTime.now())
            + " (" + TimeZone.getDefault().getDisplayName() + ") ] ");
    }

    /**
     * Accepts client connections in a loop. For each client connection, it creates a new thread to handle the client's requests
     * Within the thread, it reads input from the client's input stream and sends output through the client's output stream
    */
    private static void handleClientConnection() throws IOException {
        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("\n[*] Client Connected: " + clientSocket.hashCode());

            Thread clientThread = new Thread(() -> {
                try {
                    BufferedReader inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintStream outputStream = new PrintStream(clientSocket.getOutputStream());

                    String clientName = getClientName(clientSocket, inputStream, outputStream);
                    processClientMessage(clientName, inputStream, outputStream);
                } catch (IOException | InterruptedException exc) {
                    exc.printStackTrace();
                }
            });

            clientThread.start();
        }
    }

    /**
     * Prompts the client to enter a name and performs validation
     * If a valid name is entered, it prints the client's name, the host-port details and sends a welcome message
    */
    private static String getClientName(Socket clientSocket, BufferedReader inputStream, PrintStream outputStream) throws IOException, InterruptedException {
        String clientName = null;

        while (true) {
            outputStream.print("Name: ");
            clientName = inputStream.readLine().trim();

            if (!clientName.isEmpty()) {
                System.out.println("User: " + clientName);
                System.out.println("Host-Port: " + clientSocket.getRemoteSocketAddress() + "\n");

                outputStream.println("\n          +--------------------------------------------+\n"
                    + "          | Welcome To MyChat (v1.0) [UID: " + clientSocket.hashCode() + "] |\n"
                    + "          +--------------------------------------------+\n");
                Thread.sleep(2000);
                break;
            } else {
                outputStream.println("\n [Server] Invalid Name \n");
            }
        }

        return clientName;
    }

    /**
     * If the client sends the message "Logout", it disconnects the client and notifies other clients
     * Otherwise, it broadcasts the message to all connected clients and indicates whether the message was delivered successfully
    */
    private static void processClientMessage(String clientName, BufferedReader inputStream, PrintStream outputStream) throws IOException, InterruptedException {
        synchronized (connectedClients) {
            connectedClients.add(outputStream);

            for (PrintStream clientOutput : connectedClients) {
                if (clientOutput != outputStream) {
                    clientOutput.println("\n\n [Server] " + clientName + " is Online "
                        + "(" + DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now()) + ")\n");
                }
            }
        }

        while (true) {
            outputStream.print("[" + clientName + "] ");
            String messageLine = inputStream.readLine();

            if (messageLine.equalsIgnoreCase("Logout")) {
                System.out.println("\n[*] Client Disconnected: " + clientName);
                
                outputStream.println("\n [Server Disconnected] ");
                outputStream.print("Session lasted for: ");
                break;
            } else {
                synchronized (connectedClients) {
                    System.out.println(" [" + DateTimeFormatter.ofPattern("dd/MMM/yyyy HH:mm:ss").format(LocalDateTime.now()) + "] "
                        + "[" + clientName + "] " + messageLine);

                    for (PrintStream clientOutput : connectedClients) {
                        if (clientOutput != outputStream) {
                            clientOutput.println("\n\n[" + clientName + "] " + messageLine);
                            clientOutput.println(" ".repeat(20) + "(Message Received)\n");
                        }
                    }

                    outputStream.println(" ".repeat(20) + "(Message Delivered)\n");
                    Thread.sleep(250);
                }
            }
        }

        synchronized (connectedClients) {
            for (PrintStream clientOutput : connectedClients) {
                if (clientOutput != outputStream) {
                    clientOutput.println("\n\n [Server] " + clientName + " is Offline "
                        + "(" + DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now()) + ")\n");
                }
            }

            connectedClients.remove(outputStream);
        }
    }
}