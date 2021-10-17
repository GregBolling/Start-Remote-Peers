import java.util.Vector;

/*
 * This is the program starting remote processes.
 * This program was only tested on CISE SunOS environment.
 * If you use another environment, for example, linux environment in CISE 
 * or other environments not in CISE, it is not guaranteed to work properly.
 * It is your responsibility to adapt this program to your running environment.
 */

public class RemotePeerInfo {
	private int peerId;
	private String peerAddress;
	private int peerPort;
	private boolean hasFile;
	private Vector<Boolean> bitfield;
	private boolean choked;
	// this field means that the peer is interested in something from the running process
	private boolean interested;
	
	public RemotePeerInfo(String pId, String pAddress, String pPort, String hasFile) {
		this.peerId = Integer.parseInt(pId);
		this.peerAddress = pAddress;
		this.peerPort = Integer.parseInt(pPort);
		this.hasFile = "1".equals(hasFile);
		choked = false;
	}

	public int getPeerId() {
		return peerId;
	}

	public int getPeerPort() {
		return peerPort;
	}

	public boolean hasFile() {
		return hasFile;
	}

	public String getPeerAddress() {
		return peerAddress;
	}

	public void setBitfield(Vector<Boolean> bitfield) {
		this.bitfield = bitfield;
	}
	
	public Vector<Boolean> getBitfield() {
		return bitfield;
	}

	public boolean isChoked() {
		return choked;
	}

	public void setChoked(boolean choked) {
		this.choked = choked;
	}

	public boolean isInterested() {
		return interested;
	}

	public void setInterested(boolean interested) {
		this.interested = interested;
	}
}
