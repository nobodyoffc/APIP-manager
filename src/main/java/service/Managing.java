package service;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.GetResponse;
import redis.clients.jedis.Jedis;
import start.Start;
import tools.Address;


public class Managing {
	
	private static final String ServiceIndex = "service";

	public void menu(ElasticsearchClient esClient, Scanner sc,BufferedReader br, Jedis jedis) throws IOException {
		
		System.out.println(
				"	-----------------------------\n"
				+"	Choose\n"
				+"	-----------------------------\n"
				+"	1 Find your service\n"
				+"	2 Publish New Service\n"
				+"	3 Update Existed Service\n"
				+"	4 Reload service to redis\n"
				+"	5 Stop Existed Service\n"
				+"	6 Recover Stoped Service\n"
				+"	7 Close Service Permanently\n"
				+"	0 Return\n"
				+"	-----------------------------"
				);	
		
		int choice = Start.choose(sc, 5);

		switch(choice) {
			case 1:
				getService(jedis,esClient,sc,br);
				break;
			case 2:
				publish(br, jedis);
				break;
			case 3:
				update(esClient,br, jedis);
				break;
			case 4:
				reloadService(esClient,jedis);
			case 5:
				stop(esClient, br);
				break;
			case 6:
				recover(esClient, br);
				break;
			case 7:
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

	private void reloadService(ElasticsearchClient esClient, Jedis jedis) throws IOException {
		Service service = new Gson().fromJson(jedis.get("service"), Service.class);
		Service finalService = service;
		GetResponse<Service> r = esClient.get(g -> g.index("service").id(finalService.getSid()), Service.class);
		String serviceJson = new Gson().toJson(r.source());
		jedis.set("service",serviceJson);
	}

	private Service getService(Jedis jedis, ElasticsearchClient esClient, Scanner sc, BufferedReader br) throws IOException {
		Gson gson = new Gson();

        System.out.println("Input the fch address of the owner:");
        String str = br.readLine();
        if(str.equals(""))return null;
        SearchResponse<Service> result = esClient.search(s -> s.index(ServiceIndex).query(q -> q.term(t -> t.field("owner").value(str))), Service.class);
        List<Hit<Service>> hitList = result.hits().hits();
        ArrayList<Service> serviceList = new ArrayList<Service>();
        for(Hit<Service> hit:hitList){
			Service s = hit.source();
			if(s.isClosed())continue;
            serviceList.add(s);
        }
        int size = serviceList.size();
        if(serviceList ==null || size==0){
            System.out.println("No service found under this owner.");
            return null;
        }
        Service service = new Service();
        for(int i = 0;i<size;i++){
            service = serviceList.get(i);
            System.out.println((i+1) +". service name: "+ service.getStdName()+"sid: "+service.getSid());
        }
        if(size==1){
            jedis.set("service",gson.toJson(service));
            System.out.println("Service has been wrote into redis. Press enter to continue...");
            br.readLine();
            return service;
        }

        int choice = Start.choose(sc, size);
        service= serviceList.get(choice-1);
        System.out.println(choice +". service name: "+ service.getStdName()+"sid: "+service.getSid());
        jedis.set("service",gson.toJson(service));
        System.out.println("Service has been wrote into redis. Press enter to continue...");
        br.readLine();
        return service;
	}

	private void publish(BufferedReader br, Jedis jedis) throws IOException {
		System.out.println("To publish a new service.");
		
		OpReturn opReturn = new OpReturn();
		
		Data data = new Data();
		
		Params params = new Params();

		Service service = new Service();
		
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
			}else if(Address.isValidPubKey(str)) {
					data.setPubKeyAdmin(str);
					break;
			}else {
				System.out.println("\nThis isn't a public key. Input again: ");
				continue;
			}
		}
		
		ask = "Input the PIDs of the PIDs your service using if you want. Press enter to end :";
		String[] protocols = inputStringArray(br,ask,64);
		if(protocols.length!=0)data.setProtocols(protocols);
		
		System.out.println("Input the head of the URL being requested for your service:");	
		str = br.readLine();
		if(!str.equals(""))params.setUrlHead(str);
		
		System.out.println("Input the currency you acceptting for your service, if you need. Press enter to ignore:");
		str = br.readLine();
		if(!str.equals(""))params.setCurrency(str);
		
		System.out.println("Input the account to recieve payments if you need. Press enter to ignore:");
		str = br.readLine();
		if(!str.equals(""))params.setAccount(str);

		System.out.println("Input the price per request of your service if you need. Press enter to ignore:");
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
					params.setSessionDays(num);
					break;
				}catch(Exception e) {
					System.out.println("It isn't a integer. Input again:");
				}
			}else break;
		}

		
		data.setParams(params);
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
			System.out.println("\nThe public Key of the administrator of your service: "+service.getPubKeyAdmin());
		}else {
			System.out.println("\nNo public Key of the administrator yet.");
		}
		System.out.println("Input the public Key of the administrator of your service if you want to change it . Press enter to keep it or 'd' to delete it:");
		
		while(true) {
			str = br.readLine();
			if("".equals(str)) {
				data.setPubKeyAdmin(service.getPubKeyAdmin());
				break;
			}else if(str.equals("d")) {
				data.setPubKeyAdmin(null);
				break;
			}else if(Address.isValidPubKey(str)) {
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
			System.out.println("\nThe currency you accepting for your service: "+params.getCurrency());
		}else {
			System.out.println("\nNo currency yet.");
		}
		System.out.println("Input the currency you accepting for your service if you want to change it . Press enter to keep it or 'd' to delete it:");
		str = br.readLine();
		if(str.equals("d")) {
			params.setCurrency(null);
		}else if(!str.equals("")) {
			params.setCurrency(str);
		}
		
		if(params.getAccount()!=null) {
			System.out.println("\nThe account to receive payments: "+params.getAccount());
		}else {
			System.out.println("\nNo local names yet.");
		}
		System.out.println("Input the account to receive payments if you want to change it . Press enter to keep it or 'd' to delete it:");
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
		
		System.out.println("\nThe expiring days of the session key of your service: "+params.getSessionDays());
		System.out.println("Input the minimum amount of payment for your service if you want to change it. Press enter to keep it:");
		while(true) {
			str = br.readLine();
			if(!"".equals(str)) {
				try {
					Integer num = Integer.valueOf(str);
					params.setSessionDays(num);
					break;
				}catch(NumberFormatException e) {
					System.out.println("It isn't a integer. Input again:");
				}
			}
		}
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
