package start;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

import com.google.gson.Gson;

public class Configer {
	private String ip;
	private int port;
	private String username;
	private String opReturnJsonPath;
	private String tomcatStartCommand;
	private int redisPost;
	private String redisHost;
	
	public void initial() throws IOException {

		Configer config = new Configer();
		
		Gson gson = new Gson();
		File configFile = new File("config.json");
		if(configFile.exists()) {
			FileInputStream fis = new FileInputStream(configFile);
			byte[] configJsonBytes = new byte[fis.available()];
			fis.read(configJsonBytes);
			
			String configJson = new String(configJsonBytes);
			config = gson.fromJson(configJson, Configer.class);
			
			if(config==null) {
				fis.close();
				return;
			}
			
			ip = config.getIp();
			port = config.getPort();
			opReturnJsonPath = config.getOpReturnJsonPath();
			username = config.getUsername();
			tomcatStartCommand = config.getTomcatStartCommand();
			redisPost = config.getRedisPost();
			redisHost = config.getRedisHost();
			
			fis.close();
		}
		return;
	}

	public void configHttp(Configer configer, Scanner sc, BufferedReader br) throws IOException {
		config(configer,sc, br,false) ;
	}
	public void configHttps(Configer configer, Scanner sc, BufferedReader br) throws IOException {
		config(configer,sc, br,true) ;
	}
	public void config(Configer configer, Scanner sc, BufferedReader br,boolean isHttps) throws IOException {
		
		Gson gson = new Gson();
		File configFile = new File("config.json");
		
		if(configFile.exists()) {
			FileInputStream fis = new FileInputStream(configFile);
			byte[] configJsonBytes = new byte[fis.available()];
			fis.read(configJsonBytes);
			
			String configJson = new String(configJsonBytes);
			configer = gson.fromJson(configJson, Configer.class);
			
			if(configer==null) {
				configer = new Configer();
			}
			fis.close();
		}
		
		FileOutputStream fos = new FileOutputStream(configFile);
		
		System.out.println("Input the IP of ES server:");

		while(true) {
			configer.setIp(br.readLine());
			if (configer.getIp().matches("((25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))"))break;
			System.out.println("It must be a IPaddress, like \"100.102.102.10\". Input again.");
		}
		
		System.out.println("Input the port of ES server:");
		
		while(true){
			if(!sc.hasNextInt()) {
				System.out.println("It must be a port. It's a integer between 0 and 655350. Input again.\"");
				sc.nextInt();
			}
			else {
				configer.setPort(sc.nextInt());
				if( configer.getPort()>0 && configer.getPort()<65535)break;
				System.out.println("It has to be between 0 and 655350. Input again.");
			}
		}
		
		if(isHttps) {
			System.out.println("Input the username of ES:");
			configer.setUsername(br.readLine());
		}
		
		File file;
		
		while(true) {
			System.out.println("Input the path of opreturn*.byte file ending with '/':");
			configer.setOpReturnJsonPath(br.readLine());
	        file = new File(configer.getOpReturnJsonPath());
	        if (!file.exists()) {
	        	System.out.println("\nPath doesn't exist.");
	        }else break;
		}
		
		fos.write(gson.toJson(configer).getBytes());
		fos.flush();
		fos.close();
		
		System.out.println("\nConfiged.");	
		sc.nextLine();
	}

	
	
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getOpReturnJsonPath() {
		return opReturnJsonPath;
	}

	public void setOpReturnJsonPath(String path) {
		this.opReturnJsonPath = path;
	}

	public String getTomcatStartCommand() {
		return tomcatStartCommand;
	}

	public void setTomcatStartCommand(String tomcatPath) {
		this.tomcatStartCommand = tomcatPath;
	}

	public String getRedisHost() {
		return redisHost;
	}

	public void setRedisHost(String redisHost) {
		this.redisHost = redisHost;
	}

	public int getRedisPost() {
		return redisPost;
	}

	public void setRedisPost(int redisPost) {
		this.redisPost = redisPost;
	}
}
