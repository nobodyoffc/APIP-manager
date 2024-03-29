package startAPIP;

import api.Constant;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import menu.Menu;
import order.OrderOpReturn;
import order.OrderOpReturnData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import servers.NewEsClient;
import service.Managing;
import service.Service;
import startFEIP.StartFEIP;

import java.io.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class StartAPIP {

	private static final Logger log = LoggerFactory.getLogger(StartFEIP.class);
	private static NewEsClient newEsClient = new NewEsClient();

	public static void main(String[] args)throws Exception{

		log.info("Start.");
		System.out.println(" << APIP service >> pre version 2023.1.24\n"	);
		Scanner sc = new Scanner(System.in);
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		Jedis jedis = null;

		boolean end = false;
		ElasticsearchClient esClient = null;
		boolean isRunning = false;
		while(!end) {

			ConfigAPIP configAPIP = ConfigAPIP.getClassInstanceFromFile(ConfigAPIP.class);
			if (configAPIP == null || configAPIP.getEsIp() == null) {
				configAPIP = new ConfigAPIP();
				configAPIP.setEs(br);
			}
			configAPIP.setConfigerToFile();
			esClient = newEsClient.checkEsClient(esClient, configAPIP, br);
			if (esClient == null) {
				newEsClient.shutdownClient();
				return;
			}

			Menu menu = new Menu();

			ArrayList<String> menuItemList = new ArrayList<>();
			menuItemList.add("Manage Service");
			menuItemList.add("Run Service");
			menuItemList.add("Stop Service");
			menuItemList.add("Recreate Order Index");
			menuItemList.add("Set nPrice of APIs");
			menuItemList.add("Find Users");
			menuItemList.add("How to buy this service?");
			menuItemList.add("config");

			menu.add(menuItemList);

			menu.show();
			int choice = menu.choose(sc);

			switch(choice) {
				case 1: //Manage service
					Managing serviceManager= new Managing();
					if(jedis==null)
						jedis = getJedis(configAPIP, br);
					serviceManager.menu(esClient, sc, br,jedis);
					break;

				case 2: //Run service
					if(isRunning) {
						System.out.println("Sevice has been running.");
						break;
					}
					runService(configAPIP, jedis, br);
					isRunning = true;

					break;

				case 3: //Stop service
					execCmd(configAPIP.getTomcatStartCommand() + "/shutdown.sh");
					isRunning = false;
					break;
				case 4:
					recreateOrderIndex(esClient);
					break;

				case 5:
					if (jedis == null)
						jedis = getJedis(configAPIP, br);
					setNPrices(jedis, br);
					break;

				case 6: //Find users
					if (jedis == null)
						jedis = getJedis(configAPIP, br);
					findUsers(br);
					break;
				case 7:
					System.out.println("Anyone can send a freecash TX with following json in Op_Return to buy your service:" +
							"\n--------");
					String sidStr = new Jedis().get("service");
					Gson gson = new GsonBuilder().setPrettyPrinting().create();
					Service service = gson.fromJson(sidStr, Service.class);
					System.out.println(gson.toJson(getOrder(service.getSid()))+
							"\n--------" +
							"\nMake sure the 'sid' is your service id. " +
							"\nAny key to continue...");
					br.readLine();
					break;
				case 8:
					configAPIP.config(br);
					break;
				case 0:
					if (esClient != null) newEsClient.shutdownClient();
					if(jedis!=null)jedis.close();
					System.out.println("Exited, see you again.");
					end = true;
					break;
				default:
					break;
			}
		}
		newEsClient.shutdownClient();
		sc.close();
		br.close();
	}

	private static void setNPrices(Jedis jedis, BufferedReader br) throws IOException {
		Map<Integer, String> apiMap = loadAPIs();
		showAllAPIs(apiMap);
		while (true) {
			System.out.println("Input:" +
					"\n\t'a' to set all nPrices," +
					"\n\t'one' to set all nPrices by 1," +
					"\n\t'zero' to set all nPrices by 0," +
					"\n\tan integer to set the corresponding API," +
					"\n\tor 'q' to quit. ");
			String str = br.readLine();
			if ("".equals(str)) str = br.readLine();
			if (str.equals("q")) return;
			if (str.equals("a")) {
				setAllNPrices(apiMap, jedis, br);
				System.out.println("Done.");
				return;
			}
			if (str.equals("one")) {
				for (int i = 0; i < apiMap.size(); i++) {
					jedis.hset(RedisKeys.NPrice, apiMap.get(i + 1), "1");
				}
				System.out.println("Done.");
				return;
			}
			if (str.equals("zero")) {
				for (int i = 0; i < apiMap.size(); i++) {
					jedis.hset(RedisKeys.NPrice, apiMap.get(i + 1), "0");
				}
				System.out.println("Done.");
				return;
			}
			try {
				int i = Integer.parseInt(str);
				if (i > apiMap.size()) {
					System.out.println("The integer should be no bigger than " + apiMap.size());
					continue;
				} else {
					setNPrice(i, apiMap, jedis, br);
					System.out.println("Done.");
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Wrong input.");
			}
		}
	}

	private static void setAllNPrices(Map<Integer, String> apiMap, Jedis jedis, BufferedReader br) throws IOException {
		for (int i : apiMap.keySet()) {
			setNPrice(i, apiMap, jedis, br);
		}
	}

	private static void setNPrice(int i, Map<Integer, String> apiMap, Jedis jedis, BufferedReader br) throws IOException {
		String apiName = apiMap.get(i);
		while (true) {
			System.out.println("Input the multiple number of API " + apiName + ":");
			String str = br.readLine();
			try {
				int n = Integer.parseInt(str);
				jedis.hset(RedisKeys.NPrice, apiName, String.valueOf(n));
				return;
			} catch (Exception e) {
				System.out.println("Wong input.");
				return;
			}
		}
	}

	private static void showAllAPIs(Map<Integer, String> apiMap) {
		System.out.println("API list:");
		for (int i = 1; i <= apiMap.size(); i++) {
			System.out.println(i + ". " + apiMap.get(i));
		}
	}

	private static Map<Integer, String> loadAPIs() {

		ArrayList<String> apiList = Constant.apiList;

		Map<Integer, String> apiMap = new HashMap<Integer, String>();
		for (int i = 0; i < apiList.size(); i++) {
			apiMap.put(i + 1, apiList.get(i));
		}
		return apiMap;
	}

	public static ElasticsearchClient checkEsClient(ElasticsearchClient esClient, ConfigAPIP configAPIP, BufferedReader br) throws IOException, KeyManagementException, NoSuchAlgorithmException {
		if (esClient == null) {
			if (configAPIP.getEsUsername() == null) {
				return newEsClient.getClientHttp(configAPIP.getEsIp(), configAPIP.getEsPort());
			} else {
				System.out.println("Input the password of " + configAPIP.getEsUsername() + ":");
				String password = br.readLine();
				return newEsClient.getClientHttps(configAPIP.getEsIp(), configAPIP.getEsPort(), configAPIP.getEsUsername(), password);
			}
		}
		return esClient;
	}

	private static OrderOpReturn getOrder(String sid){
		OrderOpReturn orderOpReturn = new OrderOpReturn();
		OrderOpReturnData data = new OrderOpReturnData();
		data.setOp("buy");
		data.setSid(sid);
		orderOpReturn.setData(data);
		orderOpReturn.setType("APIP");
		orderOpReturn.setSn("1");
		orderOpReturn.setPid("");
		orderOpReturn.setName("OpenAPI");
		orderOpReturn.setVer("1");
		return orderOpReturn;
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

	private static void runService(ConfigAPIP configAPIP, Jedis jedis, BufferedReader br) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("Input the sid of your service. Press enter if you have set it in redis:");
		String str = br.readLine();
		if(!str.equals("")) {
			jedis.set("sid",str);
		}

		System.out.println("Would you like to provide unconfirmed TXs data? Input 'y' to provide, or press enter to ignore:");
		String str1 = br.readLine();
		if (str1.equals("y")) {
			jedis.set(RedisKeys.ScanMempool, "true");
		}

		if (configAPIP.getTomcatStartCommand() == null) {

			configAPIP.setTomcatStartCommand(br);

			configAPIP.setConfigerToFile();

			execCmd(configAPIP.getTomcatStartCommand());

    	    return;
		}else {
			execCmd(configAPIP.getTomcatStartCommand());
		}
	}

	public static Jedis getJedis(ConfigAPIP configAPIP, BufferedReader br) throws IOException {
		// TODO Auto-generated method stub

		if (configAPIP.getRedisPort() == 0 || configAPIP.getRedisIp() == null) configAPIP.setRedisIp(br);

		Jedis jedis = new Jedis(configAPIP.getRedisIp(), configAPIP.getRedisPort());
		//jedis.auth("xxxx");

		int count = 0;

		while(true) {
	       try {
				String ping = jedis.ping();
		        if (ping.equals("PONG")) {
		            System.out.println("Redis is ready.");
					jedis.set("esIp", configAPIP.getEsIp());
					jedis.set("esPort", String.valueOf(configAPIP.getEsPort()));
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
			configAPIP.setRedisIp(br);
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

	public static void findUsers(BufferedReader br) throws IOException {
		System.out.println("Input user's fch address or session name. Press enter to list all users:");
		String str = br.readLine();

		Jedis jedis0Common = new Jedis();
		Jedis jedis1Session = new Jedis();
		jedis1Session.select(1);

		if("".equals(str)){
			Set<String> addrSet = jedis0Common.hkeys(RedisKeys.AddrSessionName);
			for(String addr: addrSet){
				UserAPIP user = getUser(addr,jedis0Common,jedis1Session);
				System.out.println(user.toString());
			}
		}else{
			if(jedis0Common.hget(RedisKeys.AddrSessionName,str)!=null){
				UserAPIP user = getUser(str, jedis0Common, jedis1Session);
				System.out.println(user.toString());
			}else if(jedis1Session.hgetAll(str)!=null){
				UserAPIP user = getUser(jedis1Session.hget(str,"addr"), jedis0Common, jedis1Session);
				System.out.println(user.toString());
			}
		}

		br.readLine();
	}

	private static UserAPIP getUser(String addr, Jedis jedis0Common, Jedis jedis1Session) {
		UserAPIP user = new UserAPIP();
		user.setAddress(addr);
		user.setBalance(jedis0Common.hget(RedisKeys.Balance,addr));
		String sessionName = jedis0Common.hget(RedisKeys.AddrSessionName,addr);
		user.setSessionName(sessionName);
		user.setSessionKey(jedis1Session.hget(sessionName,"sessionKey"));

		long timestamp = System.currentTimeMillis() + jedis1Session.expireTime(sessionName); // example timestamp in milliseconds
		Date date = new Date(timestamp); // create a new date object from the timestamp

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // define the date format
		String formattedDate = sdf.format(date); // format the date object to a string

		user.setExpireAt(formattedDate);

		return user;
	}


}

