import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ConnectionManager extends Thread {
    private Socket clientConnection;
    private BufferedReader clientInput;

    public ConnectionManager(Socket connection) {
        this.clientConnection = connection;
    }

    public void run() {
        String reader;
        String[] parser;
        String url = null;
        String requestType = null;

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
                requestType = parser[0];
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

        if (requestType.equals("GET")) {
            executeGet(requestUrl);
        } else if (requestType.equals("POST")) {
            executeGet(requestUrl);
        }


        HttpsURLConnection serverConnection = null;
        try {
            if (requestUrl != null) {

                URLConnection con = requestUrl.openConnection();
                con.setDoInput(true);
                serverConnection = (HttpsURLConnection) con;
                InputStream serverToClient = null;
                int response = serverConnection.getResponseCode();


                if (response == serverConnection.HTTP_OK) {
                    System.out.println(response);
                    serverToClient = serverConnection.getInputStream();
                } else if (response == serverConnection.HTTP_MOVED_TEMP || response == serverConnection.HTTP_MOVED_PERM ||
                        response == serverConnection.HTTP_SEE_OTHER){
                    System.out.println(response);
                    String redirectUrl = serverConnection.getHeaderField("Location");
                    HttpURLConnection redirectConnection = (HttpURLConnection) new URL(redirectUrl).openConnection();
                    redirectConnection.setDoInput(true);
                    serverToClient = redirectConnection.getInputStream();

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
                clientConnection.close();
            }

            clientInput.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Problem reading from server to client: " + e);
        }
    }

    public void executeGet(URL requestUrl) {

    }

    public void executePost(URL requestUrl) {

    }

}
