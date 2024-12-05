import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class CatchServer {
    private final int port;
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private List<String> userInfo =  new ArrayList<String>();

    public CatchServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);

        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } finally {
            serverSocket.close();
        }
    }
    
   

    private class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private DataOutputStream output;

        public ClientHandler(Socket socket) throws IOException {
            this.clientSocket = socket;
            this.output = new DataOutputStream(socket.getOutputStream());
        }

        public void run() {
            try (DataInputStream input = new DataInputStream(clientSocket.getInputStream())) {
                String inputLine;
                send("Your client number:" + clients.size());
                for (ClientHandler client : clients) {
                	client.send(Integer.toString(clients.size()));
                }
                while ((inputLine = input.readUTF()) != null) {
                	if (inputLine.startsWith("DRAW:")){ // 그리기일때
	                    for (ClientHandler client : clients) {
	                        if (client != this) {
	                            client.send(inputLine);
	                        }
	                    }
                	}
                	else {
                		System.out.println(inputLine);
                   		
                	}
                }
            } catch (IOException e) {
                System.out.println("Client disconnected: " + e.getMessage());
            } finally {
                try {
                	int index = clients.indexOf(this);
                	System.out.println(index);
                    clients.remove(this);
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void send(String message) {
            try {
                output.writeUTF(message);
            } catch (IOException e) {
                System.out.println("Error sending message to client: " + e.getMessage());
                clients.remove(this);
                try {
                    clientSocket.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        int port = 30000; // Use your desired port
        try {
            new CatchServer(port).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
