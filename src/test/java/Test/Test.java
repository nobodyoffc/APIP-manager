package Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import service.Service;
import redis.clients.jedis.Jedis;

public class Test {

	public static void main(String[] args) throws ClassNotFoundException {
		// TODO Auto-generated method stub
		//getFiled();
		//jedisTest();
		//stringToNum();
		
		Map<String, String> user = new HashMap<String, String> ();
		
		user.put("1", "a");
		user.put("2", "b");
		
		System.out.println(user.toString());
	}

	private static void getFiled() throws ClassNotFoundException {
		// TODO Auto-generated method stub
		
		Class<?> c = Class.forName("Service");
		
		Field[] f = c.getFields();
		
		for(Field fi:f) {
			System.out.println(fi);
		}
	}

	private static void stringToNum() {
		// TODO Auto-generated method stub
		String str = "0.01s";
		
		Float flo = Float.valueOf(str);
		
		System.out.println(flo);
	}

	private static void jedisTest() {
		// TODO Auto-generated method stub
		Jedis jedis = new Jedis();
		
		jedis.set("feip", "cid");
		
		System.out.println("feip:"+ jedis.get("feip"));
		
		Service service = new Service();
		
		service.setSid("fjasd");
		service.setStdName("test");
		
		HashMap<String, String> map = new HashMap <String,String>();
		
		map.put("cid", "carmx");
		map.put("addr", "ffffarmx");
		
		jedis.hmset("service",map);
		
		Set<String> keys = jedis.keys("*");
		for(String key:keys) {
			System.out.println(key);
		}
		
		jedis.close();
	}
	
	
}
