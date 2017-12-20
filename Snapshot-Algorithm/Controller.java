import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.lang.InterruptedException;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

public class Controller {

	private List<Bank.InitBranch.Branch> branches;
	private Map<String, Socket> branchToSocketMap;
	private Map<String, Bank.ReturnSnapshot> snapshotsMap;

	public List<Bank.InitBranch.Branch> getBranches() {
		return branches;
	}

	public Map<String, Socket> getBranchToSocketMap() {
		return branchToSocketMap;
	}

	public Map<String, Bank.ReturnSnapshot> getSnapshotsMap() {
		return snapshotsMap;
	}

	public Controller() {
		branchToSocketMap = new HashMap<String, Socket>();
		branches = new ArrayList<Bank.InitBranch.Branch>();
		snapshotsMap = new HashMap<String, Bank.ReturnSnapshot>();
	}

	public void sendInitBranchMsg(int bankAmount) throws IOException{
		Socket socket;
		for (Bank.InitBranch.Branch newBranch : this.getBranches()) {
			socket = new Socket(newBranch.getIp(), newBranch.getPort());
			this.getBranchToSocketMap().put(newBranch.getName(), socket);
			Bank.InitBranch.Builder initBranchBuilder = Bank.InitBranch.newBuilder();
			initBranchBuilder.setBalance(bankAmount / branches.size());
			for (Bank.InitBranch.Branch branchName : this.getBranches()) {
				Bank.InitBranch.Branch.Builder branch = Bank.InitBranch.Branch.newBuilder();
				branch.setName(branchName.getName());
				branch.setIp(branchName.getIp());
				branch.setPort(branchName.getPort());
				initBranchBuilder.addAllBranches(branch);
			}
			Bank.InitBranch initBranchMsg = initBranchBuilder.build();
			Bank.BranchMessage.Builder branchMsgBuilder = Bank.BranchMessage.newBuilder();
			Bank.BranchMessage branchMsg = branchMsgBuilder.setInitBranch(initBranchMsg).build();
			branchMsg.writeDelimitedTo(socket.getOutputStream());
			Thread receiver = new Thread(new Receiver(this, socket));
			receiver.start();
		}
	}

	public void printSnapshots(int snapshotId) {
		System.out.println("snapshot_id: " + snapshotId);
		for (int k = 0; k < this.getSnapshotsMap().size(); k++) {
			StringBuilder output = new StringBuilder();
			String branchName = this.getBranches().get(k).getName();
			Bank.ReturnSnapshot retMsg = this.getSnapshotsMap().get(branchName);
			output.append(branchName + ": " + retMsg.getLocalSnapshot().getBalance() + " ");
			for (int j = 0; j < retMsg.getLocalSnapshot().getChannelStateList().size(); j++) {
				if ( j != k) {
					output.append(branchName + "->branch" + (j+1) + ": " + retMsg.getLocalSnapshot().getChannelStateList().get(j) + " ");
				}
			}
			System.out.println(output.toString());
		}
	}

	public static void main(String[] args) {
		if (args.length < 2) {
			System.err.println("Usage: ./controller [Amount] [Branches File]");
			System.exit(1);
		}
		Controller controller = new Controller();
		int bankAmount = Integer.parseInt(args[0]);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(args[1])));
			String currentLine;
			while (null != (currentLine = reader.readLine())) {
				String[] branchAddress = currentLine.split("\\s+");
				Bank.InitBranch.Branch.Builder branchBuilder = Bank.InitBranch.Branch.newBuilder();
				Bank.InitBranch.Branch tempBranch = branchBuilder.setName(branchAddress[0])
					.setIp(branchAddress[1])
					.setPort(Integer.parseInt(branchAddress[2]))
					.build();
				controller.getBranches().add(tempBranch);
			}
			reader.close();
			controller.sendInitBranchMsg(bankAmount);

			//listen to branches
			int snapId = 1;
			Random randomNumber = new Random();
			while (true) {
				Thread.sleep(800);
				int targetBranchIndex = randomNumber.nextInt(controller.branches.size());
				Bank.InitSnapshot.Builder initSnapshotBuilder = Bank.InitSnapshot.newBuilder();
				Bank.InitSnapshot initSnapshotMsg = initSnapshotBuilder.setSnapshotId(snapId).build();
				Bank.BranchMessage.Builder msgBuilder = Bank.BranchMessage.newBuilder();
				Bank.BranchMessage msg = msgBuilder.setInitSnapshot(initSnapshotMsg).build();
				Socket socket = controller.getBranchToSocketMap().get(controller.branches.get(targetBranchIndex).getName());
				msg.writeDelimitedTo(socket.getOutputStream());

				Thread.sleep(6*1000);
				Bank.RetrieveSnapshot.Builder retrieveSnapBuilder = Bank.RetrieveSnapshot.newBuilder();
				Bank.RetrieveSnapshot retrieveSnap = retrieveSnapBuilder.setSnapshotId(snapId).build();
				Bank.BranchMessage.Builder mBuilder = Bank.BranchMessage.newBuilder();
				Bank.BranchMessage m = mBuilder.setRetrieveSnapshot(retrieveSnap).build();
				for (Socket soc : controller.getBranchToSocketMap().values()) {
					m.writeDelimitedTo(soc.getOutputStream());
				}
				snapId++;
			}
		} catch (IOException exception) {
			exception.printStackTrace();
			System.exit(1);
		} catch (InterruptedException exception) {
			exception.printStackTrace();
		}
	}
}
