import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class ConnectionManager extends Thread {
    private Socket clientConnection;
    private BufferedReader clientInput;

    public ConnectionManager(Socket connection) {
        this.clientConnection = connection;
    }

    public void run() {
        String reader;
        String[] parser = null;
        String url = null;
        StringBuilder sb = new StringBuilder();
        ArrayList<String> request = new ArrayList<String>();

        try {
            clientInput = new BufferedReader(new InputStreamReader(clientConnection.getInputStream()));
            reader = clientInput.readLine();
            if (reader != null) {
                parser = reader.split(" ");
                for (int i = 0; i < parser.length; i++) {
                    if (parser[i].startsWith("http:")) {
                        url = parser[i].replace("http", "https");
                    }
                }
            }


        } catch (IOException e) {
            System.out.println("Problem reading from client: " + e);
        }

        URL requestUrl = null;
        try {
            if (url != null) {
                requestUrl = new URL(url);
            }
        } catch (MalformedURLException e) {
            System.out.println("Incorrect URL: " + e);
        }


        HttpsURLConnection serverConnection = null;
        try {
            if (requestUrl != null) {

                URLConnection con = requestUrl.openConnection();
                con.setDoInput(true);
                serverConnection = (HttpsURLConnection) con;
                InputStream serverToClient = null;
                int response = serverConnection.getResponseCode();
                boolean redirect = false;

                if (response == serverConnection.HTTP_OK) {
                    serverToClient = serverConnection.getInputStream();
                } else if (response == serverConnection.HTTP_MOVED_TEMP || response == serverConnection.HTTP_MOVED_PERM ||
                        response == serverConnection.HTTP_SEE_OTHER){
                    System.out.println(response);
                    String redirectUrl = serverConnection.getHeaderField("Location");
                    redirectUrl = redirectUrl.replace("http", "https");
                    serverConnection = (HttpsURLConnection) new URL(redirectUrl).openConnection();
                    serverToClient = serverConnection.getInputStream();
                }


                OutputStream writeToClient = clientConnection.getOutputStream();



                byte[] buffer = new byte[1024];
                int bytesRead;

                if (serverToClient != null) {
                    while ((bytesRead = serverToClient.read(buffer)) != -1) {
                        writeToClient.write(buffer, 0, bytesRead);
                        writeToClient.flush();
                    }
                    serverToClient.close();
                }
                writeToClient.close();
                serverConnection.disconnect();
            }

            clientInput.close();
            clientConnection.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Problem reading from server to client: " + e);
        }



    }
}
