package multiThreadedHttpServer.threadMgmt;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import multiThreadedHttpServer.util.ServerUtil;

public class HttpServer implements Runnable {

	@Override
	public void run() {
		int freePortNumber = ServerUtil.getFreePortNumber();
		try (ServerSocket server = new ServerSocket(freePortNumber)) {
			System.out.println("Host Name: " + server.getInetAddress().getLocalHost().getHostName());
			System.out.println("Port Number: " + freePortNumber);
			for (;;) {
				Socket socket = server.accept();
				Thread workerThread = new Worker(socket);
				workerThread.start();
			}
		} catch (IOException exception) {
			System.err.println(exception.getStackTrace());
		}
	}
}
