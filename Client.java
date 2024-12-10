
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {

        Communication com = new Communication();
        Thread t = new Thread(com);
        t.start();

        Scanner scan = new Scanner(System.in);
        String input;

        while (true) {
            input = scan.nextLine();
            com.sendToServer(input);
        }
    }
}

class Communication implements Runnable {

    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    public Communication() {
        try {
            socket = new Socket("localhost", 7777);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendToServer(String s) {
        try {
            out.writeUTF(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (in.available() == 0) {
                    continue;
                }
                String message = in.readUTF();
                System.out.println(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
