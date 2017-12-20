import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;
import org.apache.thrift.transport.TTransportException;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Server {

	public static FileStoreHandler handler;
	@SuppressWarnings("rawtypes")
	public static FileStore.Processor processor;
	public static int port;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args) {
		try {
			handler = new FileStoreHandler(InetAddress.getLocalHost().getHostAddress(), args[0]);
			processor = new FileStore.Processor(handler);
			port = Integer.valueOf(args[0]);
			Runnable simple = new Runnable() {
				public void run() {
					simple(processor);
				}
			};
			new Thread(simple).start();
		} catch(RuntimeException | UnknownHostException exception) {
			exception.printStackTrace();
		}
	}

	protected static void simple(@SuppressWarnings("rawtypes") FileStore.Processor processor) {
		try {
			TServerTransport serverTransport = new TServerSocket(port);
			TServer server = new TSimpleServer(new Args(serverTransport).processor(processor));
			System.out.println("Starting Simple Server...");
			server.serve();
		} catch (TTransportException e) {
			e.printStackTrace();
		}
	}
}
