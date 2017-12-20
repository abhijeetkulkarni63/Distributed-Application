import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.xml.bind.DatatypeConverter;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;

public class Client {

	public static void main(String[] args) {
		try{
			TTransport transport;
			if(args[0].contains("simple")) {
				transport = new TSocket(args[1], Integer.valueOf(args[2]));
				transport.open();
			} else {
				TSSLTransportParameters params = new TSSLTransportParameters();
		        params.setTrustStore("../../lib/java/test/.truststore", "thrift", "SunX509", "JKS");
		        transport = TSSLTransportFactory.getClientSocket(args[1], Integer.valueOf(args[2]), 0, params);
			}
			TProtocol protocol = new  TBinaryProtocol(transport);
		    FileStore.Client client = new FileStore.Client(protocol);
		    perform(client);
		    transport.close();
		} catch (TException exception) {
			exception.printStackTrace();
		}
	}

	private static void perform(FileStore.Client client) {
		try {
			/*System.out.println("Successor of 78a21c756c076df565e55979bef6d65d4146bb8f7830b0d017796ae1bf5527cd");
			System.out.println(client.findSucc("78a21c756c076df565e55979bef6d65d4146bb8f7830b0d017796ae1bf5527cd"));*/
			RFile rFile = new RFile();
			rFile.setContent("Je sue un garcion");
			RFileMetadata meta = new RFileMetadata();
			meta.setFilename("name.txt");
			meta.setVersion(0);
			meta.setOwner("Abhijeet");
			try {
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				meta.setContentHash(DatatypeConverter.printHexBinary(digest.digest((rFile.getContent()).getBytes()))
						.toLowerCase());
			} catch (NoSuchAlgorithmException exception) {
				exception.printStackTrace();
			}
			rFile.setMeta(meta);
			client.writeFile(rFile);

			rFile.setContent("Je sue una fille");
			meta.setFilename("name.txt");
			meta.setVersion(0);
			meta.setOwner("Vikram");
			try {
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				meta.setContentHash(DatatypeConverter.printHexBinary(digest.digest((rFile.getContent()).getBytes()))
						.toLowerCase());
			} catch (NoSuchAlgorithmException exception) {
				exception.printStackTrace();
			}
			rFile.setMeta(meta);
			client.writeFile(rFile);
			RFile newFile = client.readFile(rFile.getMeta().getFilename(), rFile.getMeta().getOwner());
			System.out.println(newFile.getContent());
		} catch (TException exception) {
			exception.printStackTrace();
		}
	}

}
