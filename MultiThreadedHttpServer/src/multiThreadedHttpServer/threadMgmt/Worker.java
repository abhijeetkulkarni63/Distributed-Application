package multiThreadedHttpServer.threadMgmt;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import multiThreadedHttpServer.Server.HttpRequest;
import multiThreadedHttpServer.Server.HttpResponse;
import multiThreadedHttpServer.util.RFC7321FormattedDate;
import multiThreadedHttpServer.util.Status;

public class Worker extends Thread {

	private Socket socket;

	private static ConcurrentMap<String, Integer> resourceAccessLog = new ConcurrentHashMap<String, Integer>();
	private static ConcurrentMap<String, RFC7321FormattedDate> resourceModificationLog = new ConcurrentHashMap<String, RFC7321FormattedDate>();

	public Worker(Socket socketIn) {
		socket = socketIn;
	}

	@Override
	public void run() {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream())) {
			String request = reader.readLine();
			HttpRequest httpRequest = new HttpRequest(request);
			if (httpRequest.validateRequest()) {
				// 200 OK
				HttpResponse response = new HttpResponse();
				response.setStatus(new Status(200, "OK"));
				response.setFileResource(request);
				response.setProtocol(request);
				response.buildHeader(response, resourceModificationLog);
				dataOut.write(response.generateResponseHeader().getBytes());
				response.generateResourceResponse(dataOut);
				synchronized(this){
					if (resourceModificationLog.containsKey(response.getFileResource().toString())) {
						resourceModificationLog.replace(response.getFileResource().toString(),
								new RFC7321FormattedDate(Calendar.getInstance()));
					} else {
						resourceModificationLog.put(response.getFileResource().toString(),
								new RFC7321FormattedDate(Calendar.getInstance()));
					}
					if (resourceAccessLog.containsKey(response.getFileResource().toString())) {
						resourceAccessLog.replace(response.getFileResource().toString(),
								resourceAccessLog.get(response.getFileResource().toString()) + 1);
					} else {
						resourceAccessLog.put(response.getFileResource().toString(), 1);
					}
				}
				System.out.println(response.getFileResource().getName() + "|"
						+ socket.getInetAddress().getHostAddress() + "|" + socket.getPort() + "|"
						+ resourceAccessLog.get(response.getFileResource().toString()));
			} else {
				// 404 Page Not Found
				HttpResponse response = new HttpResponse();
				response.setStatus(new Status(404, "Not Found"));
				response.setProtocol(request);
				response.setFileResource(request);
				response.buildHeader(response, resourceModificationLog);
				dataOut.write(response.generateResponseHeader().getBytes());
			}
		} catch (IOException exception) {
			System.err.println(exception.getStackTrace());
		}
	}
}
