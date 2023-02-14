package start;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import Service.Managing;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import esClient.StartClient;
import redis.clients.jedis.Jedis;


public class Start {

	private static final Logger log = LoggerFactory.getLogger(Start.class);
	private static StartClient startClient = new StartClient();

	public static void main(String[] args)throws Exception{

		log.info("Start.");
		System.out.println(" << APIP service >> pre version 2023.1.24\n"	);
		Scanner sc = new Scanner(System.in);
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		Configer configer = new Configer();
		Jedis jedis;

		boolean end = false;
		ElasticsearchClient esClient = null;
		boolean isRunning = false;
		while(!end) {
			configer.initial();

			System.out.println(
					"	-----------------------------\n"
					+ "	Menu\n"
					+ "	-----------------------------\n"
					+"	1 Create a Java HTTP Client\n"
					+"	2 Create a Java HTTPS Client\n"
					+"	3 Manage Service\n"
					+"	4 Find Users\n"
					+"	5 Run Service\n"
					+"	6 Stop Service\n"
					+"	0 Exit\n"
					+ "	-----------------------------"
					);

			int choice = choose(sc,6);

			switch(choice) {
			case 1: //Create HTTP client
				if(configer.getIp()==null || configer.getPort() == 0 || configer.getOpReturnJsonPath()==null) {
					configer.configHttp(configer,sc,br);
					configer.initial();
				}

				if(esClient!=null) {
					System.out.println("ES client already exists: "+esClient.toString());
					break;
				}

				esClient = creatHttpClient(configer);

				if(esClient != null) {
					System.out.println("Client has been created: "+esClient.toString());
					log.info("Client has been created:{} ",esClient.toString());
				}else {
					System.out.println("\n******!Create ES client failed!******\n");
				}
				break;
			case 2: //Create HTTPS client
				if(configer.getIp()==null || configer.getPort() == 0|| configer.getUsername()==null|| configer.getOpReturnJsonPath()==null) {
					configer.configHttps(configer,sc,br);
					configer.initial();
				}

				if(esClient!=null) {
					System.out.println("ES client already exists: "+esClient.toString());
					break;
				}

				esClient = creatHttpsClient(configer,sc);

				if(esClient != null) {
					System.out.println("Client has been created: "+esClient.toString());
					log.info("Client has been created:{} ",esClient.toString());
				}else {
					System.out.println("\n******!Create ES client failed!******\n");
				}
				break;
			case 3: //Manage service
				if(esClient==null) {
					System.out.println("Create a Java client for ES first.");
					break;
				}
				Managing serviceManager= new Managing();
				jedis = getJedis(configer, sc, br);
				serviceManager.menu(esClient, sc, br,jedis);
				break;

			case 4: //Find users
				jedis = getJedis(configer, sc, br);
				findUsers(br,jedis);
				break;

			case 5: //Run service
				if(esClient==null) {
					System.out.println("Create a Java client for ES first.");
					break;
				}

				if(isRunning) {
					System.out.println("Sevice has been running.");
					break;
				}
				runService(configer,br);
				isRunning = true;

				break;

			case 6: //Stop service
				execCmd(configer.getTomcatStartCommand() +"/shutdown.sh");
				isRunning = false;
				break;
			case 0:
				if(esClient!=null)startClient.shutdownClient();
				System.out.println("Exited, see you again.");
				end = true;
				break;
			default:
				break;
			}
		}
		startClient.shutdownClient();
		sc.close();
		br.close();
	}

    private static void runService(Configer configer, BufferedReader br) throws IOException {
		// TODO Auto-generated method stub
    	while(true) {
    		if(configer.getTomcatStartCommand()!=null){
    			File file = new File(configer.getTomcatStartCommand());
    			if(file.exists()) {
    	    	execCmd(configer.getTomcatStartCommand());

    			File configFile = new File("config.json");
				FileOutputStream fos = new FileOutputStream(configFile);
				Gson gson = new Gson();
    			fos.write(gson.toJson(configer).getBytes());
    			fos.flush();
    			fos.close();
    	    	return;
    			}
    		}
			System.out.println("Input the whole command to start up tomcat:");
			String comm = br.readLine();
	        configer.setTomcatStartCommand(comm);
		}
	}

	private static Jedis getJedis(Configer configer, Scanner sc, BufferedReader br) throws IOException {
		// TODO Auto-generated method stub
		Jedis jedis = new Jedis(configer.getRedisHost(), configer.getRedisPost());
		//jedis.auth("xxxx");
		int count = 0;

	       try {
				String ping = jedis.ping();
		        if (ping.equalsIgnoreCase("PONG")) {
		            System.out.println("Redis is ready.");
		            return jedis;
		        }else return null;
		    } catch (Exception e) {
			while(true) {
		    	if(count==3) {
		    		System.out.println("Failed to set redis.");
		    		return null;
		    	}
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

				while(true) {
					System.out.println("Input the host of Redis-server:");
					String host = br.readLine();
					if (host.matches("((25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))")) {
						configer.setRedisHost(host);
						break;
					}
				}

		    	System.out.println("Input the port of Redis server:");
				while(true){
					if(!sc.hasNextInt()) {
						System.out.println("It must be a port. It's a integer between 0 and 655350. Input again.\"");
						sc.nextInt();
					}
					else {
						int port = sc.nextInt();
						if( port<0 || port>65535) {
						System.out.println("It has to be between 0 and 655350. Input again.");
						}else {
							configer.setRedisPost(port);
							break;
						}
					}
				}

				jedis = new Jedis(configer.getRedisHost(), configer.getRedisPost());
				if(jedis.ping().equals("pong")) {
					FileOutputStream fos = new FileOutputStream(configFile);
					fos.write(gson.toJson(configer).getBytes());
					fos.flush();
					fos.close();
					return jedis;
				}
			    count++;
		    }
		}
	}

	@SuppressWarnings("deprecation")
	public static boolean execCmd(String command) {
        // String command = "cmd /c D:\\workSoft\\apache-tomcat-8.0.53-9000\\bin\\startup.bat";

        System.out.println(command);
        Process exec = null;
        BufferedReader in = null;
        try {
            exec = Runtime.getRuntime().exec(command);
            System.out.println(exec.isAlive());
            in = new BufferedReader(new InputStreamReader(exec.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
            return true;

        } catch (Exception e) {
            System.out.println("Something wrong.");
            return false;
        }
    }


	public static int choose(Scanner sc, int itemNum) throws IOException {
		System.out.println("\nInput the number to choose what you want to do:\n");
		int choice = 0;
		while(true) {
			while(!sc.hasNextInt()){
				System.out.println("\nInput one of the integers shown above.");
				sc.next();
			}
			choice = sc.nextInt();
		if(choice <= itemNum && choice>=0)break;
		System.out.println("\nInput one of the integers shown above.");
		}
		return choice;
	}

	private static ElasticsearchClient creatHttpClient(start.Configer configer) throws ElasticsearchException, IOException {
		// TODO Auto-generated method stub
		System.out.println("creatHttpClient");

		ElasticsearchClient esClient = null;
		try {
			esClient = startClient.getClientHttp(configer);
			System.out.println(esClient.info());
		} catch (ElasticsearchException | IOException e) {
			// TODO Auto-generated catch block
			log.info("Create esClient wrong",e);
			return null;
		}

		return esClient;
	}

	private static ElasticsearchClient creatHttpsClient(Configer configer,Scanner sc)  {
		// TODO Auto-generated method stub
		System.out.println("creatHttpsClient.");

		ElasticsearchClient esClient = null;
		try {
			esClient = startClient.getClientHttps(configer, sc);
			System.out.println(esClient.info());
		} catch (KeyManagementException | ElasticsearchException | NoSuchAlgorithmException | IOException e) {
			// TODO Auto-generated catch block
			log.info("Create esClient wrong",e);
			return null;
		}

		return esClient;
	}

	private static void findUsers(BufferedReader br,Jedis jedis) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("Input user id or a part of it. Press enter to list all:");
		String str = br.readLine();
		Set<String> result = new HashSet<> ();
		if("".equals(str)) {
			result =  jedis.keys("*");
			if(result.size()!=0) {
				for(String id:result) {
					String type = jedis.type(id);
					switch(type) {
					case "hash":
						Map<String, String> user = jedis.hgetAll(id);
						System.out.println(id+": " + user.toString());
						break;
					default:
						break;
					}
				}
			}else {
				System.out.println("No item found.");
			}
			jedis.close();
			return;
		}else {
			result =  jedis.keys("*"+str+"*");
			if(result.size()!=0) {
				for(String id:result) {
					Map<String, String> user = jedis.hgetAll(id);
					System.out.println(id+": " + user.toString());
				}
			}else {
				System.out.println("No item found.");
			}
			jedis.close();
			return;
		}
	}

}

