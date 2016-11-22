import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ProxyListener {
    private static final int PORT = 8080;
    private static Socket connection;

    public static void main(String[] args) {
        try {
            ServerSocket proxySocket = new ServerSocket(PORT);
            System.out.println("listening on port: " + PORT);

            while (true) {
                connection = proxySocket.accept();
                ConnectionManager proxyThread = new ConnectionManager(connection);
                proxyThread.start();
            }

        } catch (IOException e) {
            System.out.println("Problem starting proxy" + e);
        }
    }
}
