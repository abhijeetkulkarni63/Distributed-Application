import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.xml.bind.DatatypeConverter;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;


public class FileStoreHandler implements FileStore.Iface{
	
	private List<NodeID> fingerTable;
	private NodeID currentNode = new NodeID();
	private Map<String, RFile> fileStore;

	public FileStoreHandler(String ipIn, String portIn) {
		fileStore = new HashMap<String, RFile>();
		currentNode.ip = ipIn;
		currentNode.port = Integer.parseInt(portIn);
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			currentNode.id = DatatypeConverter.printHexBinary(digest.digest((ipIn + ":" + portIn).getBytes())).toLowerCase();
		} catch (NoSuchAlgorithmException exception) {
			exception.printStackTrace();
		}
	}

	@Override
	public void writeFile(RFile rFile) throws SystemException, TException {
		String fileOwner = rFile.getMeta().getOwner();
		String fileName = rFile.getMeta().getFilename();
		String fileId = null;
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			fileId = DatatypeConverter.printHexBinary(digest.digest((fileOwner + ":" + fileName).getBytes())).toLowerCase();
		} catch(NoSuchAlgorithmException exception) {
			exception.printStackTrace();
		}
		NodeID serverNode = findSucc(fileId);
		if((serverNode.id).compareTo(currentNode.id) == 0) {
			if(fileStore.containsKey(fileId)) {
				fileStore.get(fileId).setContent(rFile.getContent());
				fileStore.get(fileId).getMeta().setContentHash(rFile.getMeta().getContentHash());
				int version = fileStore.get(fileId).getMeta().getVersion();
				fileStore.get(fileId).getMeta().setVersion(++version);
			} else {
				fileStore.put(fileId, rFile);
			}
		} else {
			throw new SystemException();
		}
	}	

	@Override
	public RFile readFile(String filename, String owner) throws SystemException, TException {
		String fileId = null;
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			fileId = DatatypeConverter.printHexBinary(digest.digest((owner + ":" + filename).getBytes())).toLowerCase();
		} catch(NoSuchAlgorithmException exception) {
			exception.printStackTrace();
		}
		NodeID serverNode = findSucc(fileId);
		if((serverNode.id).compareTo(currentNode.id) == 0) {
			if(fileStore.containsKey(fileId)) {
				return fileStore.get(fileId);
			} else {
				throw new SystemException().setMessage("File Not Found");
			}
		} else {
			throw new SystemException();
		}
	}

	@Override
	public void setFingertable(List<NodeID> node_list) throws TException {
		fingerTable = node_list;
	}

	@Override
	public NodeID findSucc(String key) throws SystemException, TException {
		if (key.compareTo(currentNode.id) == 0) {
			return getNodeSucc();
		}
		NodeID predecessorNode = findPred(key);
		if(null == predecessorNode) {
			return currentNode;
		}
		NodeID successorNode = null;
		if((predecessorNode.id).compareTo(currentNode.id) == 0) {
			successorNode = getNodeSucc();
		} else {
			try {
				TTransport transport = new TSocket(predecessorNode.getIp(), Integer.valueOf(predecessorNode.getPort()));
				transport.open();
				TProtocol protocol = new TBinaryProtocol(transport);
				FileStore.Client client = new FileStore.Client(protocol);
				successorNode = client.getNodeSucc();
				transport.close();
			} catch (TTransportException exception) {
				exception.printStackTrace();
			}
		}
		return successorNode;
	}

	@Override
	public NodeID findPred(String key) throws SystemException, TException {
		NodeID successorNode = getNodeSucc();
		NodeID tempNode = currentNode;
		if(!checkInclusiveRange(key, tempNode.id, successorNode.id)) {
			int sizeOfFingerTable = fingerTable.size();
			for(int i = (sizeOfFingerTable-1); i > 0; i--) {
				if(checkExclusiveRange(fingerTable.get(i).id, tempNode.id, key)) {
					NodeID fingerTableId = fingerTable.get(i);
					try {
						TTransport transport = new TSocket(fingerTableId.getIp(), Integer.valueOf(fingerTableId.getPort()));
						transport.open();
						TProtocol protocol = new TBinaryProtocol(transport);
						FileStore.Client client = new FileStore.Client(protocol);
						tempNode = client.findPred(key);
						transport.close();
					} catch (TTransportException exception) {
						exception.printStackTrace();
					}
					break;
				}
			}
			if ((tempNode.id).compareTo(currentNode.id) == 0) {
				successorNode = getNodeSucc();
			} else {
				try {
					TTransport transport = new TSocket(tempNode.getIp(), Integer.valueOf(tempNode.getPort()));
					transport.open();
					TProtocol protocol = new TBinaryProtocol(transport);
					FileStore.Client client = new FileStore.Client(protocol);
					successorNode = client.getNodeSucc();
					transport.close();
				} catch (TTransportException exception) {
					exception.printStackTrace();
				}
			}
		}
		return tempNode;
	}

	@Override
	public NodeID getNodeSucc() throws SystemException, TException {
		return fingerTable.get(0);
	}

	private boolean checkExclusiveRange(String keyIn, String rangeValueOneIn, String rangeValueTwoIn) {
		if(rangeValueOneIn.compareTo(rangeValueTwoIn) < 0) {
			if(keyIn.compareTo(rangeValueOneIn) > 0) {
				if(keyIn.compareTo(rangeValueTwoIn) <= 0) {
					return true;
				}
				return false;
			}
		} else {
			if((keyIn.compareTo(rangeValueOneIn) > 0) || (keyIn.compareTo(rangeValueTwoIn) <= 0)) {
				return true;
			}
			return false;
		}
		return false;
	}
	
	private boolean checkInclusiveRange(String keyIn, String rangeValueOneIn, String rangeValueTwoIn) {
		if(rangeValueOneIn.compareTo(rangeValueTwoIn) < 0) {
			if(keyIn.compareTo(rangeValueOneIn) >= 0) {
				if(keyIn.compareTo(rangeValueTwoIn) < 0) {
					return true;
				}
				return false;
			}
		} else {
			if((keyIn.compareTo(rangeValueOneIn) >= 0) || (keyIn.compareTo(rangeValueTwoIn) < 0)) {
				return true;
			}
			return false;
		}
		return false;
	}
}
