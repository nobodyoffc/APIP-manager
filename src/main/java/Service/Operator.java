package Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.GetResponse;
import start.Indices;
import start.Start;

public class Operator {
	
	public void menu(ElasticsearchClient esClient, Scanner sc,BufferedReader br) throws IOException {
		
		System.out.println(
				" Choice the operator: \n"	
				+"	1 Publish New Service\n"
				+"	2 Update Existed Service\n"
				+"	3 Stop Existed Service\n"
				+"	4 Recover Stoped Service\n"
				+"	5 Close Service Permanently\n"
				+"	0 Return"
				);	
		
		int choice = Start.choose(sc);

		switch(choice) {
		case 1:
			publish(br);
			break;
		case 2:
			update(esClient,br);
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

	private void publish(BufferedReader br) throws IOException {
		System.out.println("To publish a new service.");
		
		OpReturn opReturn = new OpReturn();
		
		Data data = new Data();
		
		Params params = new Params();
		
		data.setOp("publish");
		
		System.out.println("Input the English name of your service:");	
		data.setStdName(br.readLine());
		
		String ask = "Input the local names of your service, if you want. Input 'enter' to end :";
		String[] localNames = inputStringArray(br,ask);
		if(localNames.length!=0) data.setLocalNames(localNames);

		System.out.println("Input the description of your service if you want.Input 'enter' to ignore:");	
		String str = br.readLine();
		if(!str.equals(""))data.setDesc(str);
		
		String[] types = {"APIP","FEIP"};
		data.setTypes(types);
		
		ask = "Input the URLs of your service, if you want. Input 'enter' to end :";
		String[] urls = inputStringArray(br,ask);
		if(urls.length!=0)data.setUrls(urls);
		
		System.out.println("Input the public key of the administrator for your service if you want.Input 'enter' to ignore:");	
		str = br.readLine();
		if(!str.equals(""))data.setPubKeyAdmin(str);

		ask = "Input the PIDs of the PIDs your service using, if you want. Input 'enter' to end :";
		String[] protocols = inputStringArray(br,ask);
		if(protocols.length!=0)data.setProtocols(protocols);
		
		System.out.println("Input the head of the URL being requested for your service:");	
		str = br.readLine();
		if(!str.equals(""))params.setUrlHead(str);
		
		System.out.println("Input the currency you acceptting for your service, if you need. Input 'enter' to ignore:");
		str = br.readLine();
		if(!str.equals(""))params.setCurrency(str);
		
		System.out.println("Input the account to recieve payments, if you need. Input 'enter' to ignore:");
		str = br.readLine();
		if(!str.equals(""))params.setAccount(str);

		System.out.println("Input the price per request of your service, if you need. Input 'enter' to ignore:");
		str = br.readLine();
		if(!str.equals(""))params.setPricePerRequest(str);
		
		System.out.println("Input the minimum amount of payment for your service, if you need. Input 'enter' to ignore:");
		str = br.readLine();
		if(!str.equals(""))params.setMinPayment(str);
		
		data.setParams(params);
		
		opReturn.setData(data);
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		System.out.println("Check the JSON text below. Send it in a TX by the owner of the service to freecash blockchain:");
		System.out.println(gson.toJson(opReturn));
		System.out.println();
	}
	
	private String[] inputStringArray(BufferedReader br, String ask) throws IOException {
		// TODO Auto-generated method stub
		
		System.out.println(ask);	
		ArrayList<String> itemList = new ArrayList<String>();
		while(true) {
			String item = br.readLine();
			if(item.equals(""))break;
			itemList.add(item);
			System.out.println("Input next item if you want or input 'enter' to end:");
		}
		if(itemList.isEmpty())return new String [0];
		
		String[] items = itemList.toArray(new String[itemList.size()]);
		
		return items;
	}
	
	private void update(ElasticsearchClient esClient, BufferedReader br) throws IOException {
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
		GetResponse<Service> result = esClient.get(g->g.index(Indices.ServiceIndex).id(id), Service.class);
		
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
		System.out.println("Input the English name of your service if you want to change it, 'enter' to keep it:");	
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
		String ask = "Input the local names of your service if you want to change it , 'enter' to keep it or 'd' to delete it:";
		String[] localNames = inputStringArray(br,ask);
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
		System.out.println("Input the description of your service if you want to change it , 'enter' to keep it or 'd' to delete it:");	
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
		ask = "Input the URLs of your service if you want to change it , 'enter' to keep it or 'd' to delete it:";
		String[] urls = inputStringArray(br,ask);
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
		System.out.println("Input the pubulic Key of the administrator of your service if you want to change it , 'enter' to keep it or 'd' to delete it:");	
		str = br.readLine();
		if(str.equals("d")) {
			data.setPubKeyAdmin(null);
		}else if(!str.equals("")) {
			data.setPubKeyAdmin(str);
		}else {
			data.setPubKeyAdmin(service.getPubKeyAdmin());
		}

		if(service.getProtocols()!=null) {
			System.out.println("\nThe PIDs of your service: ");	
			for(String item:service.getProtocols()) {
				System.out.println(item);
			}
		}else {
			System.out.println("\nNo PIDs yet.");
		}
		ask = "Input the PIDs of your service if you want to change it , 'enter' to keep it or 'd' to delete it:";
		String[] protocols = inputStringArray(br,ask);
		if(protocols.length!=0) {
			data.setProtocols(protocols);
		}else {
			if(service.getProtocols()!=null) 
				data.setProtocols(service.getProtocols());
		}
		
		Params params = service.getParams();
		Tools.ParseTools.gsonPrint(params);
		
		if(params.getUrlHead()!=null) {
			System.out.println("\nThe head of the URL being requested for your service: "+params.getUrlHead());
		}else {
			System.out.println("\nNo head of the URL yet.");
		}
		System.out.println("Input the head of the URL being requested for your service if you want to change it , 'enter' to keep it or 'd' to delete it:");
		str = br.readLine();
		if(str.equals("d")) {
			params.setUrlHead(null);
		}else if(!str.equals("")) {
			params.setUrlHead(str);
		}
		
		Tools.ParseTools.gsonPrint(params);
		
		if(params.getCurrency()!=null) {
			System.out.println("\nThe currency you acceptting for your service: "+params.getCurrency());	
		}else {
			System.out.println("\nNo currency yet.");
		}
		System.out.println("Input the currency you acceptting for your service if you want to change it , 'enter' to keep it or 'd' to delete it:");
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
		System.out.println("Input the account to recieve payments if you want to change it , 'enter' to keep it or 'd' to delete it:");
		str = br.readLine();
		if(str.equals("d")) {
			params.setAccount(null);
		}else if(!str.equals("")) {
			params.setAccount(str);
		}
		
		if(params.getPricePerRequest()!=null) {
			System.out.println("\nThe price per request of your service: "+params.getPricePerRequest());	
		}else {
			System.out.println("\nNo price per request yet.");
		}
		System.out.println("Input the price per request of your service if you want to change it , 'enter' to keep it or 'd' to delete it:");
		str = br.readLine();
		if(str.equals("d")) {
			params.setPricePerRequest(null);
		}else if(!str.equals("")) {
			params.setPricePerRequest(str);
		}
		
		if(params.getMinPayment()!=null) {
			System.out.println("\nThe minimum amount of payment for your service: "+params.getMinPayment());	
		}else {
			System.out.println("\nNo minimum amount of payment yet.");
		}
		System.out.println("Input the minimum amount of payment for your service if you want to change it , 'enter' to keep it or 'd' to delete it:");
		str = br.readLine();
		if(str.equals("d")) {
			params.setMinPayment(null);
		}else if(!str.equals("")) {
			params.setMinPayment(str);
		}
		
		Tools.ParseTools.gsonPrint(params);
		
		data.setParams(params);
		
		Tools.ParseTools.gsonPrint(data);
		
		opReturn.setData(data);
		
		Tools.ParseTools.gsonPrint(opReturn);
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		System.out.println("Check the JSON text below. Send it in a TX by the owner of the service to freecash blockchain:");
		System.out.println(gson.toJson(opReturn));
		System.out.println();
	}

	private void stop(ElasticsearchClient esClient,BufferedReader br) throws IOException {
		System.out.println("To stop the service.");
		
		System.out.println("Input the SID of your service:");	
		String sid = br.readLine();
		
		GetResponse<Service> result = esClient.get(g->g.index(Indices.ServiceIndex).id(sid), Service.class);
		
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
		
		GetResponse<Service> result = esClient.get(g->g.index(Indices.ServiceIndex).id(sid), Service.class);
		
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
		
		GetResponse<Service> result = esClient.get(g->g.index(Indices.ServiceIndex).id(sid), Service.class);
		
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
