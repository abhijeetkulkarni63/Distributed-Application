package multiThreadedHttpServer.driver;

import multiThreadedHttpServer.threadMgmt.HttpServer;

public class Driver {

	public static void main(String[] args) {
		try{
			Thread serverThread = new Thread(new HttpServer());
			serverThread.start();
			serverThread.join();
		} catch(InterruptedException exception) {
			System.err.println(exception.getStackTrace());
		}
	}
}