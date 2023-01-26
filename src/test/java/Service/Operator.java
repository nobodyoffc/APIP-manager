package Service;

import java.util.Scanner;

public class Operator {
	
	public void menu(Scanner sc) {
		
		publish();
		stop();
		recover();
		update();
		close();
		
	}

	private void publish() {
		System.out.println("publish.");
	}
	
	private void stop() {
		System.out.println("stop.");
	}
	
	private void recover() {
		System.out.println("recover.");
	}
	
	private void update() {
		System.out.println("update.");
	}
	
	private void close() {
		System.out.println("close.");
	}
}
