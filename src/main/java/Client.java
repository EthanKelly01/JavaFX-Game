import java.net.*;
import java.io.*;
import java.util.HashMap;

public class Client {
    private Socket skt = null;
    private PrintWriter out = null;
    private BufferedReader in = null;
    private final static HashMap<String, String> config = new HashMap<>();

    public Client(String address, int port) {
        try {
            skt = new Socket(address, port);
            out = new PrintWriter(skt.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(skt.getInputStream()));
            update();
        } catch (IOException ignored) {};
    }

    private void update() {
        try {
            if (config.containsKey("Update")) out.println(config.get("Update"));
            String input = in.readLine();
            for (String x : input.split(Character.toString(input.charAt(0)))) if (x.indexOf(':') >= 0) config.put(x.substring(0, x.indexOf(':')), x.substring(x.indexOf(':') + 1));
        } catch (IOException ignored) {}
    }

    public void send() {
        //do something...
    }

    public void end() {
        try {
            if (skt != null) {
                in.close();
                if (config.containsKey("Disconnect")) out.println(config.get("Disconnect"));
                out.close();
                skt.close();
            }
        } catch (IOException ignored) {}
    }
}