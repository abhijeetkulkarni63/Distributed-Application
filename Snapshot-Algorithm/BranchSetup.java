import java.net.Socket;
import java.io.IOException;

public class BranchSetup implements Runnable{
	private Branch branch;

	public BranchSetup(Branch branchIn) {
		branch = branchIn;
	}

	@Override
	public void run() {
		try {
			Socket socket;
			for(Bank.InitBranch.Branch targetBranch : branch.getBranchesList()) {
				String targetBranchAddress = targetBranch.getIp() + ":" + targetBranch.getPort();
				if(!branch.getBranchToSocketMap().containsKey(targetBranchAddress)) {
					socket = new Socket(targetBranch.getIp(), targetBranch.getPort());
					branch.getBranchToSocketMap().put(targetBranchAddress, socket);
					//send intro msg to all branches
					Intro.IntroMessage.Builder introBuilder = Intro.IntroMessage.newBuilder();
					Intro.IntroMessage intro = introBuilder.setFromBranch("Hello!").build();
					intro.writeDelimitedTo(socket.getOutputStream());
					Thread receiver = new Thread(new Receiver(branch, socket));
					receiver.start();
				}
			}
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}
}