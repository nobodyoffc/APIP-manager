package Tools;

import com.google.common.hash.Hashing;



public class Hash {
	
    private Hash() { }
	
	public static byte[] Sha256(byte[] b) {
		return Hashing.sha256().hashBytes(b).asBytes();
	}
	
	public static byte[] Sha256x2(byte[] b) {
		return Hashing.sha256().hashBytes(Hashing.sha256().hashBytes(b).asBytes()).asBytes();
	}
	
	
	public static String Sha256(String s) {
		return Hashing.sha256().hashBytes(s.getBytes()).toString();
	}
	
	public static String Sha256x2(String s) {
		return Hashing.sha256().hashBytes(Hashing.sha256().hashBytes(s.getBytes()).asBytes()).toString();
	}
	
	public static byte[] Ripemd160(byte[] b) {
		return Ripemd160.getHash(b);
	}

}

	
	
	

