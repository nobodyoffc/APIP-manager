package Tools;

import java.math.BigInteger;
import java.util.Arrays;

import io.github.novacrypto.base58.Base58;


public class Address {
	
	
	public static boolean isValidFchAddr(String addr) {
		// TODO Auto-generated method stub
		
		byte[] addrBytes = Base58.base58Decode(addr);
		
		byte[] suffix = new byte[4];
		byte[] addrNaked = new byte[21];
		
		System.arraycopy(addrBytes,0,addrNaked,0,21);
		System.arraycopy(addrBytes,21,suffix,0,4);
		
		byte[] hash = Hash.Sha256x2(addrNaked);
		
		byte[] hash4 = new byte[4];
		System.arraycopy(hash, 0, hash4, 0, 4);

		if(addrNaked[0]==(byte)0x23 && Arrays.equals(suffix, hash4)) {
			return true;
		}
		return false;
	}
	public static String pubKeyToFchAddr(String pubKeyStr) {	
		byte[] b = Hash.Sha256(BytesTools.hexToByteArray(pubKeyStr));		
		byte[] h = Hash.Ripemd160(b);
		String address = hash160ToFCHAddr(h);
		return address;
	}

	public static String pubKeyToFchAddr(byte[] pubKeyBytes) {	
		byte[] b = Hash.Sha256(pubKeyBytes);		
		byte[] h = Hash.Ripemd160(b);
		String address = hash160ToFCHAddr(h);
		return address;
	}

	public static String compressPk65To33(String pk64_65) throws Exception{
		String publicKey = null;
		if (pk64_65.length()==130){
			publicKey = pk64_65.substring(2,pk64_65.length());
		}else if(pk64_65.length()==128){
			publicKey = pk64_65;
		}else{
			throw new Exception("public key is invalid");
		}
		String keyX = publicKey.substring(0,publicKey.length()/2);
		String keyY = publicKey.substring(publicKey.length()/2, publicKey.length());
		String y_d = keyY.substring(keyY.length()-1);
		String header;
		if ((Integer.parseInt(y_d, 16)&1)==0){
			header = "02";
		}else{
			header = "03";
		}
		String pk33 = header + keyX;
		return pk33;
	}
	public static String recoverPK33ToPK65(String PK33) {

		  BigInteger p = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F",16) ;   
		  BigInteger e = new BigInteger("3",16);
		  BigInteger one =new BigInteger("1",16);
		  BigInteger two =new BigInteger("2",16);
		  BigInteger four =new BigInteger("4",16);
		  BigInteger seven =new BigInteger("7",16);
		  String prefix = PK33.substring(0, 2);
 
		  if(prefix.equals("02")||prefix.equals("03")) {
			  BigInteger x = new BigInteger(PK33.substring(2),16);
	
			  BigInteger ySq = (x.modPow(e, p).add(seven)).mod(p);
			  BigInteger y = ySq.modPow(p.add(one).divide(four), p);
  
			  if (!(y.mod(two).equals(new BigInteger(prefix,16).mod(two)))) {
				  y=p.subtract(y);
			  }
			  
			  return "04"+PK33.substring(2)+BytesTools.bytesToHexStringBE(y.toByteArray());
			  }else return null;
		  }

	public static String hash160ToFCHAddr(String hash160Hex) {

		byte[] b =BytesTools.hexToByteArray(hash160Hex);
		
		byte[] d = {0x23};
		byte[] e = new byte[21];
		System.arraycopy(d, 0, e, 0, 1);
		System.arraycopy(b, 0, e, 1, 20);
		
 		byte[] c = Hash.Sha256x2(e);
 		byte[] f = new byte[4];
		System.arraycopy(c, 0, f, 0, 4);
 		byte[] addrRaw = BytesTools.bytesMerger(e, f);
 		
		return Base58.base58Encode(addrRaw);
	}
	public static String hash160ToFCHAddr(byte[] hash160Bytes) {
		
		byte[] prefixForFch = {0x23};
		byte[] hash160WithPrefix = new byte[21];
		System.arraycopy(prefixForFch, 0, hash160WithPrefix, 0, 1);
		System.arraycopy(hash160Bytes, 0, hash160WithPrefix, 1, 20);
		
		
 		byte[] hashWithPrefix = Hash.Sha256x2(hash160WithPrefix);
 		byte[] checkHash = new byte[4];
		System.arraycopy(hashWithPrefix, 0, checkHash, 0, 4);
 		byte[] addrRaw = BytesTools.bytesMerger(hash160WithPrefix, checkHash);
 		
		return Base58.base58Encode(addrRaw);
	}


}
