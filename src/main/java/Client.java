import java.net.*;
import java.io.*;
import java.util.HashMap;

//client network connection
public class Client {
    private Socket skt = null;
    private PrintWriter out = null;
    private BufferedReader in = null;
    private final static HashMap<String, String> config = new HashMap<>();
    private final Game game;

    public Client(Game game, String address, int port) {
        this.game = game;
        try {
            skt = new Socket(address, port);
            out = new PrintWriter(skt.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(skt.getInputStream()));
            update();
        } catch (IOException ignored) {}
    }

    //updates the client config for communicating with server
    private boolean update() {
        if (skt != null) {
            try {
                if (config.containsKey("Update")) out.println(config.get("Update"));
                String input = in.readLine();
                for (String x : input.split(Character.toString(input.charAt(0)))) if (x.indexOf(':') >= 0) config.put(x.substring(0, x.indexOf(':')), x.substring(x.indexOf(':') + 1));
                return true;
            } catch (IOException ignored) {}
        }
        return false;
    }

    //sends required information to server for one game loop cycle
    public void send(double x, double y) {
        //send position of window, then get position of host window and ball
        if (update()) {
            String output = config.get("FormatOut");
            output = output.replaceAll("x", Double.toString(x));
            output = output.replaceAll("y", Double.toString(y));
            out.println(output);

            try {
                String input = in.readLine();
                String[] nums = input.split("[,:]");
                game.updateClient(Double.parseDouble(nums[0]), Double.parseDouble(nums[1]), Double.parseDouble(nums[2]), Double.parseDouble(nums[3]));
            } catch (IOException ignored) {}
        }
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