package esClient;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import start.Configer;

public class StartClient{
	
	ElasticsearchClient esClient = null;
	RestClient restClient = null;
	RestClientTransport transport = null;
	
	static final Logger log = LoggerFactory.getLogger(StartClient.class);

	public ElasticsearchClient getClientHttp(start.Configer configer) throws ElasticsearchException, IOException {
		
		System.out.println("Creating a client...");
		
		try {
		
		// Create a client without authentication check
		restClient = RestClient.builder(
				new HttpHost(configer.getIp(), configer.getPort()))
				.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
		        	@Override
					public RequestConfig.Builder customizeRequestConfig(
							RequestConfig.Builder requestConfigBuilder) {
						return requestConfigBuilder.setConnectTimeout(5000 * 1000) // 连接超时（默认为1秒）
								.setSocketTimeout(6000 * 1000);// 套接字超时（默认为30秒）//更改客户端的超时限制默认30秒现在改为100*1000分钟
					}
		        })
				.build();

		// Create the transport with a Jackson mapper
		transport = new RestClientTransport(
				restClient, new JacksonJsonpMapper());

		// And create the API client
		esClient = new ElasticsearchClient(transport);
		
		return esClient;
		
		}catch(Exception e) {
			log.error("The elasticsearch server may need a authorization. Try \"2 Create aclient in HTTPS net.\". Error info: {}",e);
		return null;
		}
	}
	

	public ElasticsearchClient getClientHttps(Configer configer,Scanner sc) throws ElasticsearchException, IOException, NoSuchAlgorithmException, KeyManagementException{
		
		System.out.println("Input the password of user '"+configer.getUsername()+"':");
		
		String password = sc.next();
		
		System.out.println("Creating a client with authentication...");
		
	    final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
	    
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(configer.getUsername(), password));
        
		restClient = RestClient.builder(new HttpHost(configer.getIp(), configer.getPort(),"https"))
				.setHttpClientConfigCallback(h ->h.setDefaultCredentialsProvider(credentialsProvider))
				.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
		        	@Override
					public RequestConfig.Builder customizeRequestConfig(
							RequestConfig.Builder requestConfigBuilder) {
						return requestConfigBuilder.setConnectTimeout(5000 * 1000) // 连接超时（默认为1秒）
								.setSocketTimeout(6000 * 1000);// 套接字超时（默认为30秒）//更改客户端的超时限制默认30秒现在改为100*1000分钟
					}
		        })
				.build();
        
		// Create the transport with a Jackson mapper
		transport = new RestClientTransport(
				restClient, new JacksonJsonpMapper());

		// And create the API client
		esClient = new ElasticsearchClient(transport);
		
		return esClient;
	}


	public void shutdownClient() throws IOException {

			if(this.transport!=null)this.transport.close();
			if(this.restClient!=null)this.restClient.close();
			if(this.esClient!=null)this.esClient.shutdown();
			log.info("Client has been closed:{} ");
	}

//	public ElasticsearchClient getEsClient() {
//		return esClient;
//	}

}
