package start;

import java.io.BufferedReader;
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
		Scanner sc = new Scanner(System.in);
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		Configer configer = new Configer();
		
		boolean end = false;
		ElasticsearchClient esClient = null;
		boolean isRunning = false;
		while(!end) {
			configer.initial();
			
			System.out.println(
					" << APIP service >> pre version 2023.1.24\n\n"	
					+"	1 Create a Java HTTP Client\n"
					+"	2 Create a Java HTTPS Client\n"
					+"	3 Manage Service\n"
					+"	4 Find Users\n"
					+"	5 Run Service\n"
					+"	0 exit"
					);	
			
			int choice = choose(sc,5);

			switch(choice) {
			case 1: //Create HTTP client
				if(configer.getIp()==null || configer.getPort() == 0 || configer.getPath()==null) {
					configer.configHttp(sc,br);
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
				if(configer.getIp()==null || configer.getPort() == 0|| configer.getUsername()==null|| configer.getPath()==null) {
					configer.configHttps(sc,br);
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
				
				serviceManager.menu(esClient, sc, br);
				
				break;
				
			case 4: //Find users
				
				
				findUsers(br);
				
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
				
				Runnable runService = new RunService();
				
				Thread serveThread = new Thread(runService);
				serveThread.start();
				isRunning = true;
				
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
	
	public static int choose(Scanner sc,int num) throws IOException {
		System.out.println("\nInput the number you want to do:\n");
		int choice = 0;
		while(true) {
			while(!sc.hasNextInt()){
				System.out.println("\nInput one of the integers shown above.");
				sc.next();
			}
			choice = sc.nextInt();
		if(choice <= num && choice>=0)break;
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
	
	private static void findUsers(BufferedReader br) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("Input user id or a part of it. Press enter to list all:");
		String str = br.readLine();
		Jedis jedis = new Jedis();
		Set<String> result = new HashSet<String> ();
		if("".equals(str)) {
			result =  jedis.keys("*");
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

