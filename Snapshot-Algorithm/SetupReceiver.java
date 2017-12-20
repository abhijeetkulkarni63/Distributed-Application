import java.net.ServerSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.io.IOException;

public class SetupReceiver implements Runnable {
	private Branch branch;

	public SetupReceiver(Branch branchIn) {
		branch = branchIn;
	}

	@Override
	public void run() {
		try {
			if (branch.getBranchIndex() != 0) {
				while (branch.getBranchToSocketMap().size() != branch.getBranchesList().size()) {
					Socket socket = branch.getServerSocket().accept();
					Intro.IntroMessage introMsg = Intro.IntroMessage.parseDelimitedFrom(socket.getInputStream());	
					branch.getBranchToSocketMap().put(introMsg.getFromBranch(), socket);	
					Thread receiver = new Thread(new Receiver(branch, socket));
					receiver.start();
				}
			}
 		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}
}