import java.io.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Controller implements Runnable {
    private ServerSocket srvr = null;
    private static Game game = null;
    private final ArrayList<Player> threadpool = new ArrayList<>();
    private volatile boolean run = true;
    private Player opponent;

    private final static HashMap<String, String> config = new HashMap<>();

    public Controller(int port, Game game) {
        config.put("Delimiter", "`");
        config.put("Update", "update");
        config.put("Disconnect", "end");
        config.put("FormatOut", "x,y");
        config.put("FormatIn", "x1,y1:x2,y2");

        try {
            srvr = new ServerSocket(port);
            this.game = game;
        } catch (IOException ignored) {}
    }

    public void run() {
        if (srvr == null) return;
        while (run) try {
            Player temp = new Player(srvr.accept(), this);
            if (opponent == null) opponent = temp;
            temp.start();
            threadpool.add(temp);
        } catch (IOException ignored) {}
    }

    public void end() {
        try {
            run = false;
            for (Player x : threadpool) if (x != null) x.skt.close();
            if (srvr != null) srvr.close();
        } catch (IOException ignored) {}
    }

    private void removeThread(Player player) {
        threadpool.remove(player);
        if (player.equals(opponent)) {
            if (threadpool.size() > 0) opponent = threadpool.get(0);
            else opponent = null;
        }
    }

    public boolean getOpponent() { return (opponent != null); }

    private static class Player extends Thread {
        private final Socket skt;
        private final Controller srvr;

        public Player(Socket socket, Controller server) {
            skt = socket;
            srvr = server;
        }

        private String getConfig() {
            char delim = config.get("Delimiter").charAt(0);
            String output = "" + delim;
            for (String x : config.keySet()) output += x + ":" + config.get(x) + delim;
            return output;
        }

        public void run() {
            try {
                PrintWriter out = new PrintWriter(skt.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(skt.getInputStream()));

                out.println(getConfig()); //send new player config info

                String input;
                while (!this.isInterrupted() && (input = in.readLine()) != null) {
                    if (input.equals(config.get("Disconnect"))) break; //let client disconnect
                    else if (input.equals(config.get("Update"))) out.println(getConfig());
                    else if (this.equals(srvr.opponent)) { //player update loop
                        String[] nums = input.split(",");
                        game.updateHost(Double.parseDouble(nums[0]), Double.parseDouble(nums[1]));
                    }
                }
                in.close();
                out.close();
                skt.close();
            } catch (IOException ignored) {}
            srvr.removeThread(this);
        }
    }
}