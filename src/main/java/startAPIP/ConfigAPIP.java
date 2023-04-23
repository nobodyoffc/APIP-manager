package startAPIP;

import servers.ConfigBase;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

public class ConfigAPIP extends ConfigBase {

    private String tomcatStartCommand;
    private int redisPort = 6379;
    private String redisIp = "127.0.0.1";
    private String rpcIp = "127.0.0.1";
    private String rpcAllowIp = "127.0.0.1";
    private int rpcPort = 8332;
    private String rpcUsername = "user";
    private String avatarBasePath = "avatar/elements";
    private String avatarPngPath = "avatar/png";

    @Override
    public void config(BufferedReader br) throws IOException {
        printConfiger();
        setEs(br);
        setTomcatStartCommand(br);
        setRedisIp(br);
        setRedisPort(br);
        setRpcIp(br);
        setRpcAllowIp(br);
        setRpcPort(br);
        setRpcUsername(br);
        setAvatarBasePath(br);
        setAvatarPngPath(br);
    }

    public String getRpcUsername() {
        return rpcUsername;
    }

    public void setRpcUsername(BufferedReader br) throws IOException {

        String name;

        System.out.println("Input the name of freecash RPC. Input 's' to skip:");
        name = br.readLine();
        if ("s".equals(name)) return;
        setRpcName(name);
        System.out.println("\nThe name of freecash RPC was set.");
    }

    public void setRpcName(String rpcName) {
        this.rpcUsername = rpcName;
    }

    public String getTomcatStartCommand() {
        return tomcatStartCommand;
    }

    public void setTomcatStartCommand(BufferedReader br) throws IOException {

        String comm;

        System.out.println("Input the whole command for startting tomcat server. Input 's' to skip:");
        comm = br.readLine();
        if ("s".equals(comm)) return;
        setTomcatStartCommand(comm);
        System.out.println("\nThe tomcat server command was set.");
    }

    public void setTomcatStartCommand(String tomcatStartCommand) {
        this.tomcatStartCommand = tomcatStartCommand;
    }

    public int getRedisPort() {
        return redisPort;
    }

    public void setRedisPort(BufferedReader br) throws IOException {
        if (this.redisPort != 0)
            System.out.println("Port of redis server is: " + this.redisPort);

        System.out.println("Input the port of redis server. Press 's' to skip:");

        while (true) {
            String str = br.readLine();
            if ("s".equals(str)) return;
            int i = 0;
            try {
                i = Integer.parseInt(str);
            } catch (Exception e) {
                System.out.println("It must be a port. It's a integer between 0 and 655350. Input again.\"");
            }
            if (i > 0 && i < 65535) {
                this.redisPort = i;
                return;
            }
            System.out.println("It has to be between 0 and 655350. Input again.");
        }
    }

    public void setRedisPort(int redisPort) {
        this.redisPort = redisPort;
    }

    public String getRedisIp() {
        return redisIp;
    }

    public void setRedisIp(BufferedReader br) throws IOException {
        if (this.redisIp != null)
            System.out.println("IP of Redis is: " + this.redisIp);

        System.out.println("Input the IP of Redis server. Input 's' to skip:");

        while (true) {
            String str = br.readLine();
            if ("s".equals(str)) return;
            if (str.matches("((25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))")) {
                redisIp = str;
                return;
            }
            System.out.println("It must be a IP address, like \"100.102.102.10\". Input again.");
        }
    }

    public void setRedisIp(String redisIp) {
        this.redisIp = redisIp;
    }

    public String getRpcIp() {
        return rpcIp;
    }

    private void setRpcIp(BufferedReader br) throws IOException {
        if (this.rpcIp != null)
            System.out.println("IP of freecash RPC is: " + this.rpcIp);

        System.out.println("Input the IP of freecash RPC server. Press 's' to skip:");

        while (true) {
            String str = br.readLine();
            if ("s".equals(str)) return;
            if (str.matches("((25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))")) {
                this.rpcIp = str;
                System.out.println("The freecash RPC IP was set.");
                break;
            }
            System.out.println("It must be a IP address, like \"100.102.102.10\". Input again.");
        }
    }
    private void setRpcAllowIp(BufferedReader br) throws IOException {
        if (this.rpcIp != null)
            System.out.println("The allowed IP of freecash RPC is: " + this.rpcIp);

        System.out.println("Input the allowed requesting IP. Press 's' to skip:");

        while (true) {
            String str = br.readLine();
            if ("s".equals(str)) return;
            if (str.matches("((25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))")) {
                this.rpcAllowIp = str;
                System.out.println("The allowed requesting IP was set.");
                break;
            }
            System.out.println("It must be a IP address, like \"100.102.102.10\". Input again.");
        }
    }

    public void setRpcIp(String rpcIp) {
        this.rpcIp = rpcIp;
    }

    public int getRpcPort() {
        return rpcPort;
    }

    public void setRpcPort(BufferedReader br) throws IOException {
        if (this.rpcPort != 0)
            System.out.println("Port of freecash RPC is: " + this.rpcPort);

        System.out.println("Input the port of freecash RPC. Press 's' to skip:");

        while (true) {
            String str = br.readLine();
            if ("s".equals(str)) return;
            int i = 0;
            try {
                i = Integer.parseInt(str);
            } catch (Exception e) {
                System.out.println("It must be a port. It's a integer between 0 and 655350. Input again.\"");
            }
            if (i > 0 && i < 65535) {
                this.rpcPort = i;
                return;
            }
            System.out.println("It has to be between 0 and 655350. Input again.");
        }
    }

    public void setRpcPort(int rpcPort) {
        this.rpcPort = rpcPort;
    }

    public String getAvatarBasePath() {
        return avatarBasePath;
    }

    private void setAvatarBasePath(BufferedReader br) throws IOException {

        while (true) {
            if (this.avatarBasePath != null)
                System.out.println("The base path of avatar making is: " + this.avatarBasePath);
            System.out.println("Input the base path of avatar making. Press 's' to skip:");
            String str1 = br.readLine();
            if ("s".equals(str1)) return;
            if (!str1.endsWith("/")) str1 = str1 + "/";
            File file = new File(str1);
            if (file.exists()) {
                this.avatarBasePath = str1;
                System.out.println("\nThe base path of avatar making was set.");
                return;
            } else {
                System.out.println("\nPath doesn't exist. ");
            }
        }
    }

    public void setAvatarBasePath(String avatarBasePath) {
        this.avatarBasePath = avatarBasePath;
    }

    public String getAvatarPngPath() {
        return avatarPngPath;
    }

    private void setAvatarPngPath(BufferedReader br) throws IOException {

        while (true) {
            if (this.avatarPngPath != null)
                System.out.println("The path for generated png files is: " + this.avatarPngPath);
            System.out.println("Input the path for generated png files. Press 's' to skip:");
            String str1 = br.readLine();
            if ("s".equals(str1)) return;
            if (!str1.endsWith("/")) str1 = str1 + "/";
            File file = new File(str1);
            if (!file.exists()) {
                System.out.println("\nPath doesn't exist. ");
                continue;
            } else {
                this.avatarPngPath = str1;
                System.out.println("\nThe path for generated png files was set.");
                return;
            }
        }
    }
    public String getRpcAllowIp() {
        return rpcAllowIp;
    }

    public void setRpcAllowIp(String rpcAllowIp) {
        this.rpcAllowIp = rpcAllowIp;
    }

    public void setAvatarPngPath(String avatarPngPath) {
        this.avatarPngPath = avatarPngPath;
    }
}
