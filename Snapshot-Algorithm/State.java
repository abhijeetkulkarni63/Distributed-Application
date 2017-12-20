import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class State {
	private Branch branch;
	private int localState;
	/*private Map<String, Integer> branchChannelState;
	private Map<String, Boolean> isRecording;*/
	private List<Integer> channelState;
	private List<Boolean> isRecording;
	private int numMarker;

	public State(int balanceIn, Branch branchIn) {
		branch = branchIn;
		localState = balanceIn;
		channelState = new ArrayList<Integer>(branch.getBranchesList().size());
		for(int i = 0; i < branch.getBranchesList().size(); i++) {
			channelState.add(0);
		}
		isRecording = new ArrayList<Boolean>(branch.getBranchesList().size());
		for(int i = 0; i < branch.getBranchesList().size(); i++) {
			isRecording.add(false);
		}
		numMarker = 0;
	}

	public void setLocalState(int localStateIn) {
		localState = localStateIn;
	}

	public int getLocalState() {
		return localState;
	}

	public List<Integer> getChannelState() {
		return channelState;
	}

	public List<Boolean> getIsRecording() {
		return isRecording;
	}

	public int getNumMarker() {
		return numMarker;
	}

	public void setNumMarker(int numMarkerIn) {
		numMarker = numMarkerIn;
	}

	public String toString() {
		return "State[localState:" + localState + ", ChannelState:" + channelState +"]";
	}
}
