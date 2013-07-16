import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Wrapper for a client socket and its Input- and OutputStreams (which can be accessed over a BufferedReader and a
 * PrintWriter).
 * 
 * @author Thomas Bornschlegel
 * 
 */
public class ClientObject {

    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;
    private int clientId = -1;

    public ClientObject(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    public BufferedReader getInputFromClient() {
        return in;
    }

    public PrintWriter getOutputToServer() {
        return out;
    }

    public Socket getSocket() {
        return socket;
    }

    /**
     * @return the client id or -1 if it has not been set
     * */
    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

}