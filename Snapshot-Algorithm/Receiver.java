import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Map;

public class Receiver implements Runnable {
	private Branch branch;
	private Controller controller;
	private Socket socket;

	public Receiver(Branch branchIn, Socket socketIn) {
		branch = branchIn;
		socket = socketIn;
	}

	public Receiver(Controller controllerIn, Socket socketIn) {
		controller = controllerIn;
		socket = socketIn;
	}

	public Branch getBranch() {
		return branch;
	}

	public Socket getSocket() {
		return socket;
	}

	private void processMessage(Bank.BranchMessage messageIn) throws IOException{
		if(messageIn.hasInitBranch()) {
			//code to handle initBranch message
			Bank.InitBranch initBranchMsg = messageIn.getInitBranch();
			branch.setBalance(initBranchMsg.getBalance());
			branch.setBranchesList(initBranchMsg.getAllBranchesList());
		}

		if(messageIn.hasInitSnapshot()) {
			//code to handle initSnapshot message
			//System.out.println("Init Snapshot Message Received");
			Bank.InitSnapshot initSnapshotMsg = messageIn.getInitSnapshot();
			//1. Record own state
			State state = new State(branch.getBalance(), branch);
			int snapshotId = initSnapshotMsg.getSnapshotId();
			for (int i = 0; i < branch.getBranchesList().size(); i++) {
				if (i != branch.getBranchIndex()) {
					state.getIsRecording().set(i, true);
				}
			}
			branch.putActiveSnapshots(snapshotId, state);
			//2. Send Marker message to all other branches (include snapshot_id)
			Bank.Marker.Builder markerBuilder = Bank.Marker.newBuilder();
			Bank.Marker marker = markerBuilder.setSnapshotId(snapshotId).build();
			Bank.BranchMessage.Builder branchMsgBuilder = Bank.BranchMessage.newBuilder();
			Bank.BranchMessage branchMsg = branchMsgBuilder.setMarker(marker).build();
			for (Bank.InitBranch.Branch otherBranch : branch.getBranchesList()) {
				if (!(otherBranch.getName().equals(branch.getBranchName()))) {
					try {
						Socket socket = branch.getBranchToSocketMap().get(otherBranch.getName());
						branchMsg.writeDelimitedTo(socket.getOutputStream());
						//System.out.println("Sending first marker to " + otherBranch.getName());
					} catch (IOException exception) {
						exception.printStackTrace();
					}
				}
			}
		}

		if (messageIn.hasMarker()) {
			//code to handle marker message
			//1. Check if the the marker is received for the first time.
			String incomingBranch = null;
			for (Map.Entry<String, Socket> entry : branch.getBranchToSocketMap().entrySet()) {
				if (Objects.equals(this.socket, entry.getValue())) {
					incomingBranch = entry.getKey();
				}
			}
			int incomingBranchIndex = 0;
			for (int i = 0; i < branch.getBranchesList().size(); i++) {
				if((incomingBranch).equals(branch.getBranchesList().get(i).getName())) {
					incomingBranchIndex = i;
				}
			}
			Bank.Marker markerMsg = messageIn.getMarker();
			if (!branch.getActiveSnapshots().containsKey(markerMsg.getSnapshotId())) {
			//true:
			//	1.1 Record own state
				//System.out.println("First Marker Received from " + incomingBranch);
				State state = new State(branch.getBalance(), branch);
				state.setNumMarker(state.getNumMarker() + 1);
				int snapshotId = markerMsg.getSnapshotId();
			//	1.2 Record state of incoming channel from sender as empty
				state.getChannelState().set(incomingBranchIndex, 0);
			//state.putChannelState(incomingBranch, 0);
				branch.putActiveSnapshots(snapshotId, state);
			//	1.3 Start recording on other incoming channels
				for (int i = 0; i < branch.getBranchesList().size(); i++) {
					if ((incomingBranchIndex != i) && (i != branch.getBranchIndex())) {
						state.getIsRecording().set(i, true);
						//System.out.println("Recording " + branch.getBranchesList().get(i).getName());
					}
				}
			//	1.4 send Marker message to other branches
				Bank.Marker.Builder markerBuilder = Bank.Marker.newBuilder();
				Bank.Marker marker = markerBuilder.setSnapshotId(markerMsg.getSnapshotId()).build();
				Bank.BranchMessage.Builder branchMsgBuilder = Bank.BranchMessage.newBuilder();
				Bank.BranchMessage branchMsg = branchMsgBuilder.setMarker(marker).build();
				for(Bank.InitBranch.Branch targetBranch : branch.getBranchesList()) {
					if(!targetBranch.getName().equals(branch.getBranchName())) {
						try {
							Socket socket = branch.getBranchToSocketMap().get(targetBranch.getName());
							branchMsg.writeDelimitedTo(socket.getOutputStream());
							//System.out.println("Sending Marker to " + targetBranch.getName());
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			} else {
				//false:
				State state = branch.getActiveSnapshots().get(markerMsg.getSnapshotId());
				//System.out.println("Duplicate Marker");
				state.getIsRecording().set(incomingBranchIndex, false);
				state.setNumMarker(state.getNumMarker() + 1);
				if (state.getNumMarker() == (branch.getBranchesList().size() - 1)) {
					branch.getDoneSnapshots().put(markerMsg.getSnapshotId(), state);
					//System.out.println("SNAP COMPLETE");
					//System.out.println("DoneSnapshots: " + branch.getDoneSnapshots().toString());
				}
			}
		}

		if (messageIn.hasRetrieveSnapshot()) {
			//code to handle retrieveSnapshot message in return set returnSnapshot message
			//System.out.println("Received RetrieveSnapshot");
			Bank.RetrieveSnapshot snapMsg = messageIn.getRetrieveSnapshot();
			while (!branch.getDoneSnapshots().containsKey(snapMsg.getSnapshotId())) {

			}
			//System.out.println("Preparing ReturnSnapshot");
			State state = branch.getDoneSnapshots().get(snapMsg.getSnapshotId());
			Bank.ReturnSnapshot.LocalSnapshot.Builder localSnapBuilder = Bank.ReturnSnapshot.LocalSnapshot.newBuilder();
			localSnapBuilder.setSnapshotId(snapMsg.getSnapshotId());
			localSnapBuilder.setBalance(state.getLocalState());
			for (int channelState : state.getChannelState()) {
				localSnapBuilder.addChannelState(channelState);
			}
			Bank.ReturnSnapshot.LocalSnapshot localSnap = localSnapBuilder.build();
			Bank.ReturnSnapshot.Builder retSnapBuilder = Bank.ReturnSnapshot.newBuilder();
			Bank.ReturnSnapshot retSnap = retSnapBuilder.setLocalSnapshot(localSnap).build();
			Bank.BranchMessage.Builder msgBuilder = Bank.BranchMessage.newBuilder();
			Bank.BranchMessage msg = msgBuilder.setReturnSnapshot(retSnap).build();
			msg.writeDelimitedTo(socket.getOutputStream());
		}

		if (messageIn.hasReturnSnapshot()) {
			Bank.ReturnSnapshot retMsg = messageIn.getReturnSnapshot();
			String incomingBranch = null;
			for (Map.Entry<String, Socket> entry : controller.getBranchToSocketMap().entrySet()) {
				if (Objects.equals(this.socket, entry.getValue())) {
					incomingBranch = entry.getKey();
				}
			}
			controller.getSnapshotsMap().put(incomingBranch, retMsg);
			if (controller.getSnapshotsMap().size() == controller.getBranches().size()) {
				controller.printSnapshots(retMsg.getLocalSnapshot().getSnapshotId());
				controller.getSnapshotsMap().clear();
			}
		}

		if (messageIn.hasTransfer()) {
			//code to handle transfer message
			Bank.Transfer transferMsg = messageIn.getTransfer();
			String incomingBranch = null;
			for (Map.Entry<String, Socket> entry : branch.getBranchToSocketMap().entrySet()) {
					if (Objects.equals(this.socket, entry.getValue())) {
						incomingBranch = entry.getKey();
					}
			}
			int incomingBranchIndex = 0;
			for (int i = 0; i < branch.getBranchesList().size(); i++) {
				if((incomingBranch).equals(branch.getBranchesList().get(i).getName())) {
					incomingBranchIndex = i;
				}
			}
			for (State state : branch.getActiveSnapshots().values()) {
				if (state.getIsRecording().get(incomingBranchIndex) == true) {
					int currentState = state.getChannelState().get(incomingBranchIndex);
					state.getChannelState().set(incomingBranchIndex, currentState + transferMsg.getMoney());
				}
			}
			branch.addBalance(transferMsg.getMoney());
		}
	}

	public void run() {
		while (true) { 
			try {
				Bank.BranchMessage branchMsg = Bank.BranchMessage.parseDelimitedFrom(this.getSocket().getInputStream());
				if(null != branchMsg) {
					this.processMessage(branchMsg);
				}
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}
	}
}
