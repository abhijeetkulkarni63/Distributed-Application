import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.net.Socket;
import java.lang.InterruptedException;

public class Branch {
	
	private List<Bank.InitBranch.Branch> branchesList;
	private String branchName;
	private int port;
	private ServerSocket serverSocket;
	private int balance;
	private Map<Integer, State> activeSnapshots;
	private Map<Integer, State> doneSnapshots;
	private Map<String, Socket> branchToSocketMap;
	private int branchIndex;
	private int initialBalance;

	public String getBranchName() {
		return branchName;
	}

	public int getPort() {
		return port;
	}

	public synchronized void setBalance(int balanceIn) {
		balance = balanceIn;
	}

	public synchronized int getBalance() {
		return balance;
	}

	public synchronized void addBalance(int amountIn) {
		balance = balance + amountIn;
	}

	public synchronized void deductBalance(int amountIn) {
		balance = balance - amountIn;
	}

	public ServerSocket getServerSocket() {
		return serverSocket;
	}

	public void setBranchesList(List<Bank.InitBranch.Branch> branchesListIn) {
		branchesList = branchesListIn;
	}

	public List<Bank.InitBranch.Branch> getBranchesList() {
		return branchesList;
	}

	public void putActiveSnapshots(int snapshotId, State stateIn) {
		activeSnapshots.put(snapshotId, stateIn);
	}

	public Map<Integer, State> getActiveSnapshots() {
		return activeSnapshots;
	}

	public Map<String, Socket> getBranchToSocketMap() {
		return branchToSocketMap;
	}

	public void setBranchIndex(int indexIn) {
		branchIndex = indexIn;
	}

	public int getBranchIndex() {
		return branchIndex;
	}

	public Map<Integer, State> getDoneSnapshots() {
		return doneSnapshots;
	}

	public void setInitialBalance(int balanceIn) {
		initialBalance = balanceIn;
	}

	public int getInitialBalance() {
		return initialBalance;
	}

	public Branch(String[] argsIn) {
		try {
			branchName = argsIn[0];
			port = Integer.parseInt(argsIn[1]);
			serverSocket = new ServerSocket(port);
			balance = -1;
			activeSnapshots = new HashMap<Integer, State>();
			branchToSocketMap = new HashMap<String, Socket>();
			branchIndex = 0;
			doneSnapshots = new HashMap<Integer, State>();
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

	public void initBranchesList() throws IOException{
		while (true) {
			Socket socket = this.serverSocket.accept();
			Bank.BranchMessage branchMsg = Bank.BranchMessage.parseDelimitedFrom(socket.getInputStream());
			if(branchMsg.hasInitBranch()) {
				if (!this.getBranchToSocketMap().containsKey("Controller")) {		
					this.getBranchToSocketMap().put("Controller", socket);
				}
				Bank.InitBranch initBranchMsg = branchMsg.getInitBranch();
				this.setBalance(initBranchMsg.getBalance());
				this.setInitialBalance(initBranchMsg.getBalance());
				this.setBranchesList(initBranchMsg.getAllBranchesList());
				//Spwan a receiver
				Thread receiver = new Thread(new Receiver(this, socket));
				receiver.start();
			}
			break;
		}
	}

	public void findBranchIndex() {
		for(Bank.InitBranch.Branch branch : this.getBranchesList()) {
			if(branch.getName().equals(this.getBranchName())) {
				break;
			} else {
				int currentIndex = this.getBranchIndex();
				this.setBranchIndex(++currentIndex);
			}
		}
	}

	public void setupBranchConnections() throws IOException{
		int currentBranchIndex = 0;
		for(Bank.InitBranch.Branch branch : this.getBranchesList()) {
			if(branch.getName().equals(this.getBranchName())) {
				break;
			} else {
				currentBranchIndex++;
			}
		}
		for(int i = currentBranchIndex + 1; i < this.getBranchesList().size(); i++) {
			Bank.InitBranch.Branch targetBranch = this.getBranchesList().get(i);
			Socket socket = new Socket(targetBranch.getIp(), targetBranch.getPort());
			this.getBranchToSocketMap().put(targetBranch.getName(), socket);
			Intro.IntroMessage.Builder introBuilder = Intro.IntroMessage.newBuilder();
			Intro.IntroMessage introMsg = introBuilder.setFromBranch(this.getBranchName()).build();
			introMsg.writeDelimitedTo(socket.getOutputStream());	
			Thread receiver = new Thread(new Receiver(this, socket));
			receiver.start();
		}
	}

	public static void main(String[] args) {
		try {
			Branch branch = new Branch(args);
			branch.initBranchesList();
			branch.findBranchIndex();
			//Thread setupThread = new Thread(new BranchSetup(branch));
			Thread receiverThread = new Thread(new SetupReceiver(branch));
			//setupThread.start();
			receiverThread.start();
			branch.setupBranchConnections();
			//start sending out random transfer messages
			Thread senderThread = new Thread(new Sender(branch));
			receiverThread.join();
			senderThread.start();
		} catch (IOException exception) {
			exception.printStackTrace();
		} catch (InterruptedException exception) {
			exception.printStackTrace();
		}
	}
}