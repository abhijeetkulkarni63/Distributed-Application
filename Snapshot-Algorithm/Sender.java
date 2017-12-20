import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.lang.InterruptedException;

public class Sender implements Runnable {
	private Branch branch;

	public Sender(Branch branchIn) {
		branch = branchIn;
	}

	public void run() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException exception) {
			exception.printStackTrace();
		}
		Socket socket;
		Random randomNumber = new Random();
		while(true) {
			try {
				Thread.sleep(10);
				int targetBranchIndex = randomNumber.nextInt(branch.getBranchesList().size());
				Bank.Transfer.Builder transferBuilder = Bank.Transfer.newBuilder();
				int moneyPercent = randomNumber.nextInt(5) + 1;
				int transferAmt = ((branch.getInitialBalance() * moneyPercent) / 100);
				if ((branch.getBalance() - transferAmt) > 0) {
					Bank.Transfer transferMsg = transferBuilder.setMoney(transferAmt).build();
					Bank.InitBranch.Branch targetBranch = branch.getBranchesList().get(targetBranchIndex);
					Bank.BranchMessage.Builder msgBuilder = Bank.BranchMessage.newBuilder();
					Bank.BranchMessage message = msgBuilder.setTransfer(transferMsg).build();
					if(!(targetBranch.getName().equals(branch.getBranchName()))) {	
						branch.deductBalance(transferAmt);
						socket = branch.getBranchToSocketMap().get(targetBranch.getName());
						message.writeDelimitedTo(socket.getOutputStream());
						//System.out.println("Sending " + transferAmt + "to " + targetBranch.getName());
					}
				}
			} catch(IOException exception) {
				exception.printStackTrace();
			} catch (InterruptedException exception) {
				exception.printStackTrace();
			}
		}
	}
}
