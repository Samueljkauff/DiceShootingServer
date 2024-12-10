import java.io.*;
import java.util.*;
import java.net.*;

public class Server {

    static ArrayList<ClientHandler> clientArray = new ArrayList<>();

    static int whosTurn = 0;

    public static void main(String[] args) {

        int playerId = 0;

        try (ServerSocket ss = new ServerSocket(7777)) {

            System.out.println("The Server has successfully started at port 7777");

            while (true) {
                Socket s = ss.accept();

                ClientHandler handler = new ClientHandler(s, playerId);
                Thread t = new Thread(handler);
                t.start();

                clientArray.add(handler);

                System.out.println("A new client connected.");

                playerId++;
                System.out.println("There is " + playerId + " client(s) connected");

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized static void broadCast(String s) {
        for (ClientHandler client : clientArray) {
            client.sendToClient(s);
        }

    }

    synchronized static void broadCastBesidesTurn(String s) {
        for (ClientHandler client : clientArray) {
            int i = 0;
            if (i != whosTurn) {
                client.sendToClient(s);
                i++;
            }

        }
    }

    synchronized static int getTurn() {
        return whosTurn;
    }

    synchronized static public void broadcastWinner() {
        int maximum = 0;
        List<Integer> winners = new ArrayList<>();
    
        // Find the maximum score
        for (ClientHandler client : clientArray) {
            if (client.getScore() > maximum) {
                maximum = client.getScore();
            }
        }
    
        // Find all players with the maximum score
        for (int i = 0; i < clientArray.size(); i++) {
            if (clientArray.get(i).getScore() == maximum) {
                winners.add(i);
            }
        }
    
        // Broadcast the winners
        if (winners.size() == 1) {
            broadCast("\n-------- Player_" + winners.get(0) + " WINS with a score of " + maximum + " --------");
        } else {
            broadCast("\n-------- It's a TIE! Winners are --------");
            for (int winner : winners) {
                broadCast("Player_" + winner + " with a score of " + maximum);
            }
        }
    }
    

    synchronized static void nextTurn() {
        whosTurn++;
        if (getTurn() >= clientArray.size()) {
            broadcastWinner();
            whosTurn = 0;
        }
    }

}

class ClientHandler implements Runnable {

    private Socket socket;
    private int playerId;
    private DataInputStream in;
    private DataOutputStream out;

    private int score;

    List<Integer> roll = new ArrayList<>();

    public ClientHandler(Socket socket, int playerId) {
        this.socket = socket;
        this.playerId = playerId;
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getScore() {
        return score;
    }

    public void setScore(int rollScore) {
        this.score = rollScore;
    }


    public void sendToClient(String s) {
        try {
            out.writeUTF(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        String clientResponse;

        try {
            out.writeUTF("You are Player_" + playerId);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                Server.broadCast("\n-------------------");
                synchronized(Server.class){
                    Server.broadCast("It is Player_" + Server.getTurn() + " turn.");
                }
                sendToClient("Type 'r' to roll.");
                Server.broadCast("Type 'exit' to if you want to leave.");
                Server.broadCast("-------------------");

                clientResponse = in.readUTF();

                Server.broadCast(clientResponse);
                switch (clientResponse.toLowerCase()) {
                    case "r":
                        synchronized (Server.class) {
                            if (Server.getTurn() != playerId) {
                                sendToClient("NOT YOUR TURN!");
                                continue;
                            }

                        }
                        Server.broadCast("Player_" + playerId + " decides to roll");
                        roll = Game.roll();
                        Server.broadCast("Player_" + playerId + " rolls a " + roll);

                        while (Game.rollScore(roll) == 0) {
                            sendToClient("Press Enter to reroll.");
                            in.readUTF();
                            roll = Game.roll();
                            Server.broadCast("Player_" + playerId + " rerolls a " + roll);
                        }

                        setScore(Game.rollScore(roll));
                        Server.broadCast("====================");
                        Server.broadCast("Player_" + playerId + " score is " + getScore());
                        Server.broadCast("====================");
                        synchronized (Server.class) {
                            Server.nextTurn();
                        }
                        break;

                    case "exit":
                        System.out.println("Client " + this.socket + " sends exit...");
                        System.out.println("Closing this connection.");
                        this.socket.close();
                        System.out.println("Connection closed");
                        break;

                    default:

                        break;
                }
                if (clientResponse.equals("Exit")) {
                    System.out.println("Client " + this.socket + " sends exit...");
                    System.out.println("Closing this connection.");
                    this.socket.close();
                    System.out.println("Connection closed");
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}