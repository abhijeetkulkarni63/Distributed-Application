package multiThreadedHttpServer.util;

import java.io.IOException;
import java.net.ServerSocket;

public class ServerUtil {

	final private static int MIN_PORT_NUMBER = 1024;
	final private static int MAX_PORT_NUMBER = 65535;

	public static int getFreePortNumber() {
		ServerSocket socket;
		for (int portNumber = MIN_PORT_NUMBER; portNumber <= MAX_PORT_NUMBER; portNumber++) {
			try {
				socket = new ServerSocket(portNumber);
				socket.close();
				return portNumber;
			} catch (IOException exception) {

			}
		}
		System.err.println("Server is experiencing heavy traffic. Please try after some time.");
		System.exit(-1);
		return -1;
	}
}
