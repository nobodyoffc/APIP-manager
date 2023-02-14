package Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.GetResponse;
import redis.clients.jedis.Jedis;
import start.Start;


public class Managing {
	
	private static final String ServiceIndex = "service";

	public void menu(ElasticsearchClient esClient, Scanner sc,BufferedReader br, Jedis jedis) throws IOException {
		
		System.out.println(
				" Choice the operator: \n"	
				+"	1 Publish New Service\n"
				+"	2 Update Existed Service\n"
				+"	3 Stop Existed Service\n"
				+"	4 Recover Stoped Service\n"
				+"	5 Close Service Permanently\n"
				+"	0 Return"
				);	
		
		int choice = Start.choose(sc, 5);

		switch(choice) {
		case 1:
			publish(br, jedis);
			break;
		case 2:
			update(esClient,br, jedis);
			break;
		case 3:
			stop(esClient, br);
			break;
		case 4:
			recover(esClient, br);
			break;
		case 5:
			System.out.println("Do you really want to give up the service forever? y or n:");			
			String delete = sc.next();		
			if (delete.equals("y")) {
				close(esClient, br);
			}
			break;
		case 0:
			return;
		}

	}

	private void setting(Params params, Jedis jedis) {
		// TODO Auto-generated method stub

		HashMap<String,String> paramsMap = new HashMap<String,String>();
		if(params.getUrlHead()!=null)paramsMap.put("urlHead", params.getUrlHead());
		if(params.getCurrency()!=null)paramsMap.put("currency", params.getCurrency());
		if(params.getAccount()!=null)paramsMap.put("account", params.getAccount());
		if(params.getPricePerRequest()!=0)paramsMap.put("pricePerRequest", String.valueOf(params.getPricePerRequest()));
		if(params.getMinPayment()!=0)paramsMap.put("minPayment", String.valueOf(params.getMinPayment()));
		if(params.getSessonDays()!=0)paramsMap.put("sessonDays", String.valueOf(params.getSessonDays()));
		
		if(paramsMap.size()!=0) {
			jedis.hmset("params", paramsMap);
		}
	}

	private void publish(BufferedReader br, Jedis jedis) throws IOException {
		System.out.println("To publish a new service.");
		
		OpReturn opReturn = new OpReturn();
		
		Data data = new Data();
		
		Params params = new Params();
		
		data.setOp("publish");
		
		System.out.println("Input the English name of your service:");	
		data.setStdName(br.readLine());
		
		String ask = "Input the local names of your service, if you want. Press enter to end :";
		String[] localNames = inputStringArray(br,ask,0);
		if(localNames.length!=0) data.setLocalNames(localNames);

		System.out.println("Input the description of your service if you want.Press enter to ignore:");	
		String str = br.readLine();
		if(!str.equals(""))data.setDesc(str);
		
		String[] types = {"APIP","FEIP"};
		data.setTypes(types);
		
		ask = "Input the URLs of your service, if you want. Press enter to end :";
		String[] urls = inputStringArray(br,ask,0);
		if(urls.length!=0)data.setUrls(urls);
		
		System.out.println("Input the public key of the administrator for your service if you want. Press enter to ignore:");	
		while(true) {
			str = br.readLine();
			if("".equals(str)) {
				break;
			}else if(Tools.Address.isValidPubKey(str)) {
					data.setPubKeyAdmin(str);
					break;
			}else {
				System.out.println("\nThis isn't a public key. Input again: ");
				continue;
			}
		}
		
		ask = "Input the PIDs of the PIDs your service using, if you want. Press enter to end :";
		String[] protocols = inputStringArray(br,ask,64);
		if(protocols.length!=0)data.setProtocols(protocols);
		
		System.out.println("Input the head of the URL being requested for your service:");	
		str = br.readLine();
		if(!str.equals(""))params.setUrlHead(str);
		
		System.out.println("Input the currency you acceptting for your service, if you need. Press enter to ignore:");
		str = br.readLine();
		if(!str.equals(""))params.setCurrency(str);
		
		System.out.println("Input the account to recieve payments, if you need. Press enter to ignore:");
		str = br.readLine();
		if(!str.equals(""))params.setAccount(str);

		System.out.println("Input the price per request of your service, if you need. Press enter to ignore:");
		float flo = 0;
		while(true) {
			str = br.readLine();
			if(!("".equals(str))) {
				try {
					flo = Float.valueOf(str);
					params.setPricePerRequest(flo);
					break;
				}catch(Exception e) {
					System.out.println("It isn't a number. Input again:");
				}
			}else break;
		}
		
		System.out.println("Input the minimum amount of payment for your service, if you need. Press enter to ignore:");
		flo = 0;
		while(true) {
			str = br.readLine();
			if(!("".equals(str))) {
				try {
					flo = Float.valueOf(str);
					params.setMinPayment(flo);
					break;
				}catch(Exception e) {
					System.out.println("It isn't a number. Input again:");
				}
			}else break;
		}
		
		System.out.println("Input the expiring days of sesson key of your service, if you need. Press enter to ignore:");
		Integer num = 0;
		while(true) {
			str = br.readLine();
			if(!("".equals(str))) {
				try {
					num = Integer.valueOf(str);
					params.setSessonDays(num);
					break;
				}catch(Exception e) {
					System.out.println("It isn't a integer. Input again:");
				}
			}else break;
		}

		
		data.setParams(params);
		setting(params, jedis);
		opReturn.setData(data);
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		System.out.println("Check the JSON text below. Send it in a TX by the owner of the service to freecash blockchain:");
		System.out.println(gson.toJson(opReturn));
		System.out.println();
	}
	
	private String[] inputStringArray(BufferedReader br, String ask, int len) throws IOException {
		// TODO Auto-generated method stub
		
		System.out.println(ask);	
		ArrayList<String> itemList = new ArrayList<String>();
		while(true) {
			String item = br.readLine();
			if(item.equals(""))break;
			if(len>0) {
				if(item.length()!=len) {
					System.out.println("The length does not match.");
					continue;
				}
			}
			itemList.add(item);
			System.out.println("Input next item if you want or enter to end:");
		}
		if(itemList.isEmpty())return new String [0];
		
		String[] items = itemList.toArray(new String[itemList.size()]);
		
		return items;
	}
	
	private void update(ElasticsearchClient esClient, BufferedReader br, Jedis jedis) throws IOException {
		System.out.println("To update the service information.");
		
		System.out.println("Input the SID of your service:");	
		String sid;
		while(true) {
			sid = br.readLine();
			if(sid.length()==64) {
				break;
			}
			System.out.println("Illegal sid. Input again:");
		}
		String id = sid;
		GetResponse<Service> result = esClient.get(g->g.index(ServiceIndex).id(id), Service.class);
		
		if(!result.found()) {
			System.out.println("Service does not exist.");	
			return;
		}
		Service service = result.source();
		
		OpReturn opReturn = new OpReturn();
		
		Data data = new Data();
		
		data.setOp("update");
		data.setSid(sid);
		
		System.out.println("\nThe English name of your service: "+service.getStdName());	
		System.out.println("Input the English name of your service if you want to change it, . Press enter to keep it:");	
		String str = br.readLine();
		if(!str.equals("")) {
			data.setStdName(str);
		}else {
			data.setStdName(service.getStdName());
		}

		if(service.getLocalNames()!=null) {
			System.out.println("\nThe local names of your service: ");
			for(String item:service.getLocalNames()) {
				System.out.println(item);
			}
		}else {
			System.out.println("\nNo local names yet.");
		}
		String ask = "Input the local names of your service if you want to change it . Press enter to keep it or 'd' to delete it:";
		String[] localNames = inputStringArray(br,ask,0);
		if(localNames.length!=0) {
			data.setLocalNames(localNames);
		}else {
			if(service.getLocalNames()!=null) 
				data.setLocalNames(service.getLocalNames());
		}

		
		if(service.getDesc()!=null) {
			System.out.println("\nThe description of your service: "+service.getDesc());	
		}else {
			System.out.println("\nNo description yet.");
		}
		System.out.println("Input the description of your service if you want to change it . Press enter to keep it or 'd' to delete it:");	
		str = br.readLine();
		if(str.equals("d")) {
			data.setDesc(null);
		}else if(!str.equals("")) {
			data.setStdName(str);
		}else data.setStdName(service.getStdName());
		
		if(service.getUrls()!=null) {
			System.out.println("\nThe URLs of your service: ");	
			for(String item:service.getUrls()) {
				System.out.println(item);
			}
		}else {
			System.out.println("\nNo URLs yet.");
		}
		ask = "Input the URLs of your service if you want to change it . Press enter to keep it or 'd' to delete it:";
		String[] urls = inputStringArray(br,ask,0);
		if(urls.length!=0) {
			data.setUrls(urls);
		}else {
			if(service.getUrls()!=null)
				data.setUrls(service.getUrls());
		}
		
		if(service.getPubKeyAdmin()!=null) {
			System.out.println("\nThe pubulic Key of the administrator of your service: "+service.getPubKeyAdmin());	
		}else {
			System.out.println("\nNo pubulic Key of the administrator yet.");
		}
		System.out.println("Input the pubulic Key of the administrator of your service if you want to change it . Press enter to keep it or 'd' to delete it:");	
		
		while(true) {
			str = br.readLine();
			if("".equals(str)) {
				data.setPubKeyAdmin(service.getPubKeyAdmin());
				break;
			}else if(str.equals("d")) {
				data.setPubKeyAdmin(null);
				break;
			}else if(Tools.Address.isValidPubKey(str)) {
				data.setPubKeyAdmin(str);
				break;
			}else {
				System.out.println("\nThis isn't a public key. Input again: ");
				continue;
			}
		}
		
		if(service.getProtocols()!=null) {
			System.out.println("\nThe PIDs of your service: ");	
			for(String item:service.getProtocols()) {
				System.out.println(item);
			}
		}else {
			System.out.println("\nNo PIDs yet.");
		}
		ask = "Input the PIDs of your service if you want to change it . Press enter to keep it or 'd' to delete it:";
		String[] protocols = inputStringArray(br,ask,64);
		if(protocols.length!=0) {
			data.setProtocols(protocols);
		}else {
			if(service.getProtocols()!=null) 
				data.setProtocols(service.getProtocols());
		}
		
		Params params = service.getParams();
		
		if(params.getUrlHead()!=null) {
			System.out.println("\nThe head of the URL being requested for your service: "+params.getUrlHead());
		}else {
			System.out.println("\nNo head of the URL yet.");
		}
		System.out.println("Input the head of the URL being requested for your service if you want to change it . Press enter to keep it or 'd' to delete it:");
		str = br.readLine();
		if(str.equals("d")) {
			params.setUrlHead(null);
		}else if(!str.equals("")) {
			params.setUrlHead(str);
		}
		
		if(params.getCurrency()!=null) {
			System.out.println("\nThe currency you acceptting for your service: "+params.getCurrency());	
		}else {
			System.out.println("\nNo currency yet.");
		}
		System.out.println("Input the currency you acceptting for your service if you want to change it . Press enter to keep it or 'd' to delete it:");
		str = br.readLine();
		if(str.equals("d")) {
			params.setCurrency(null);
		}else if(!str.equals("")) {
			params.setCurrency(str);
		}
		
		if(params.getAccount()!=null) {
			System.out.println("\nThe account to recieve payments: "+params.getAccount());	
		}else {
			System.out.println("\nNo local names yet.");
		}
		System.out.println("Input the account to recieve payments if you want to change it . Press enter to keep it or 'd' to delete it:");
		str = br.readLine();
		if(str.equals("d")) {
			params.setAccount(null);
		}else if(!str.equals("")) {
			params.setAccount(str);
		}
		
		
		System.out.println("\nThe price per request of your service: "+params.getPricePerRequest());	
		System.out.println("Input the price per request of your service if you want to change it . Press enter to keep it:");
		float flo = 0;
		while(true) {
			str = br.readLine();
			if(!"".equals(str)) {
				try {
					flo = Float.valueOf(str);
					params.setPricePerRequest(flo);
					break;
				}catch(NumberFormatException e) {
					System.out.println("It isn't a number. Input again:");
				}
			}
		}

		
		System.out.println("\nThe minimum amount of payment for your service: "+params.getMinPayment());	
		System.out.println("Input the minimum amount of payment for your service if you want to change it. Press enter to keep it:");
		while(true) {
			str = br.readLine();
			if(!"".equals(str)) {
				try {
					flo = Float.valueOf(str);
					params.setMinPayment(flo);
					break;
				}catch(NumberFormatException e) {
					System.out.println("It isn't a number. Input again:");
				}
			}
		}
		
		System.out.println("\nThe expiring days of the sesson key of your service: "+params.getSessonDays());	
		System.out.println("Input the minimum amount of payment for your service if you want to change it. Press enter to keep it:");
		while(true) {
			str = br.readLine();
			if(!"".equals(str)) {
				try {
					Integer num = Integer.valueOf(str);
					params.setSessonDays(num);
					break;
				}catch(NumberFormatException e) {
					System.out.println("It isn't a integer. Input again:");
				}
			}
		}
		
		setting(params, jedis);
				
		data.setParams(params);
			
		opReturn.setData(data);
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		System.out.println("Check the JSON text below. Send it in a TX by the owner of the service to freecash blockchain:");
		System.out.println(gson.toJson(opReturn));
		System.out.println();
	}

	private void stop(ElasticsearchClient esClient,BufferedReader br) throws IOException {
		System.out.println("To stop the service.");
		
		System.out.println("Input the SID of your service:");	
		String sid = br.readLine();
		
		GetResponse<Service> result = esClient.get(g->g.index(ServiceIndex).id(sid), Service.class);
		
		if(!result.found()) {
			System.out.println("Service does not exist.");	
			return;
		}
		
		OpReturn opReturn = new OpReturn();
		
		Data data = new Data();
		
		data.setOp("stop");
		data.setSid(sid);
		
		opReturn.setData(data);
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		System.out.println("Check the JSON text below. Send it in a TX by the owner of the service to freecash blockchain:");
		System.out.println(gson.toJson(opReturn));
		System.out.println();
	}
	
	private void recover(ElasticsearchClient esClient,BufferedReader br) throws ElasticsearchException, IOException {
		System.out.println("To recover the service.");
		
		System.out.println("Input the SID of your service:");	
		String sid = br.readLine();
		
		GetResponse<Service> result = esClient.get(g->g.index(ServiceIndex).id(sid), Service.class);
		
		if(!result.found()) {
			System.out.println("Service does not exist.");	
			return;
		}
		
		OpReturn opReturn = new OpReturn();
		
		Data data = new Data();
		
		data.setOp("recover");
		data.setSid(sid);
		
		opReturn.setData(data);
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		System.out.println("Check the JSON text below. Send it in a TX by the owner of the service to freecash blockchain:");
		System.out.println(gson.toJson(opReturn));
		System.out.println();
	}
	
	private void close(ElasticsearchClient esClient,BufferedReader br) throws ElasticsearchException, IOException {
		System.out.println("To close the service.");
		
		System.out.println("Input the SID of your service:");	
		String sid = br.readLine();
		
		GetResponse<Service> result = esClient.get(g->g.index(ServiceIndex).id(sid), Service.class);
		
		if(!result.found()) {
			System.out.println("Service does not exist.");	
			return;
		}
		
		OpReturn opReturn = new OpReturn();
		
		Data data = new Data();
		
		data.setOp("close");
		data.setSid(sid);
		
		opReturn.setData(data);
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		System.out.println("Check the JSON text below. Send it in a TX by the owner of the service or its master to freecash blockchain:");
		System.out.println(gson.toJson(opReturn));
		System.out.println();
	}
}
