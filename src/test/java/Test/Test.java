package Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Test {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println("Input:");
		
		String str = br.readLine();
		
		System.out.println("Result: "+str);
		
		if(str.equals(" "))System.out.println("\" \"");
		if(str.equals(""))System.out.println("\"\"");
		if(str.equals("\n"))System.out.println("\\n");
	}

}
