package start;


import java.io.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import service.Managing;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
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
		Jedis jedis = null;

		boolean end = false;
		ElasticsearchClient esClient = null;
		boolean isRunning = false;
		while(!end) {
			
			configer.initial();

			System.out.println(
					"	-----------------------------\n"
					+ "	Menu\n"
					+ "	-----------------------------\n"
					+"	1 Manage Service\n"
					+"	2 Find Users\n"
					+"	3 Run Service\n"
					+"	4 Stop Service\n"
					+"	5 Recreate Order Index\n"
					+"	6 Config ES\n"
					+"	0 Exit\n"
					+ "	-----------------------------"
					);

			int choice = choose(sc,6);

			switch(choice) {
				case 1: //Manage service
					if(esClient==null) {
						esClient = startClient.createEsClient(configer, sc, br,esClient);
					}
					Managing serviceManager= new Managing();
					if(jedis==null)
						jedis = getJedis(configer, sc, br);
					serviceManager.menu(esClient, sc, br,jedis);
					break;

				case 2: //Find users
					if(jedis==null)
						jedis = getJedis(configer, sc, br);
					findUsers(br,jedis);
					break;

				case 3: //Run service
					if(esClient==null) {
						System.out.println("Create a Java client for ES first.");
						break;
					}
					if(isRunning) {
						System.out.println("Sevice has been running.");
						break;
					}
					runService(configer,jedis,br);
					isRunning = true;

					break;

				case 4: //Stop service
					execCmd(configer.getTomcatStartCommand() +"/shutdown.sh");
					isRunning = false;
					break;
				case 5:
					if(esClient==null) {
						esClient = startClient.createEsClient(configer, sc, br,esClient);
					}
					recreateOrderIndex(esClient);
					break;
				case 6:
					new Configer().configEs(sc,br);
					break;
				case 0:
					if(esClient!=null)startClient.shutdownClient();
					if(jedis!=null)jedis.close();
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

	private static void recreateOrderIndex(ElasticsearchClient esClient) throws InterruptedException {

		if(esClient==null) {
			System.out.println("Create a Java client for ES first.");
			return;
		}
		try {
			DeleteIndexResponse req = esClient.indices().delete(c -> c.index("order"));

			if(req.acknowledged()) {
				log.info("Index order was deleted.");
			}
		}catch(ElasticsearchException | IOException e) {
			log.info("Deleting index order failed.",e);
		}

		TimeUnit.SECONDS.sleep(2);

		String orderJsonStr = "{\"mappings\":{\"properties\":{\"id\":{\"type\":\"keyword\"},\"fromAddr\":{\"type\":\"wildcard\"},\"toAddr\":{\"type\":\"wildcard\"},\"amount\":{\"type\":\"long\"},\"time\":{\"type\":\"long\"},\"txid\":{\"type\":\"keyword\"},\"txIndex\":{\"type\":\"long\"},\"height\":{\"type\":\"long\"}}}}";
		InputStream orderJsonStrIs = new ByteArrayInputStream(orderJsonStr.getBytes());
		try {
			CreateIndexResponse req = esClient.indices().create(c -> c.index("order").withJson(orderJsonStrIs));
			orderJsonStrIs.close();
			System.out.println(req.toString());
			if(req.acknowledged()) {
				log.info("Index order was created.");
			}else {
				log.info("Creating index order failed.");
				return;
			}
		}catch(ElasticsearchException | IOException e) {
			log.info("Creating index order failed.",e);
			return;
		}
	}


	private static void runService(Configer configer, Jedis jedis,BufferedReader br) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("Input the sid of your service. Press enter if you have set it in redis:");
		String str = br.readLine();
		if(!str.equals("")) {
			jedis.set("sid",str);
		}

		if(configer.getTomcatStartCommand()==null){
			
			configer.configTomcatStartCommand(br);
			
			configer.initial();
			
	    	execCmd(configer.getTomcatStartCommand());

    	    return;
		}else {
			execCmd(configer.getTomcatStartCommand());
		}
	}

	public static Jedis getJedis(Configer configer, Scanner sc, BufferedReader br) throws IOException {
		// TODO Auto-generated method stub
		
		if(configer.getRedisPort() ==0 || configer.getRedisHost()==null) configer.configRedis(sc, br);	
		
		Jedis jedis = new Jedis(configer.getRedisHost(), configer.getRedisPort());
		//jedis.auth("xxxx");
		
		int count = 0;

		while(true) {
	       try {
				String ping = jedis.ping();
		        if (ping.equals("PONG")) {
		            System.out.println("Redis is ready.");
		            return jedis;
		        }else {
		        	System.out.println("Failed to startup redis.");
		        }
		    } catch (Exception e) {
		    	e.printStackTrace();
		    }
		    count++;
	    	if(count==3) {
	    		System.out.println("Check your redis server.");
	    		return null;
	    	}
	    	configer.configRedis(sc, br);
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

	public static void findUsers(BufferedReader br,Jedis jedis) throws IOException {
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

