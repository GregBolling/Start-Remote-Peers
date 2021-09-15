// This code was taken from the Computer Network Fundamentals Canvas page
// It works on my Windows computer

/*
 *                     CEN5501C Project2
 * This is the program starting remote processes.
 * This program was only tested on CISE SunOS environment.
 * If you use another environment, for example, linux environment in CISE 
 * or other environments not in CISE, it is not guaranteed to work properly.
 * It is your responsibility to adapt this program to your running environment.
 */

import java.io.*;
import java.util.*;

/*
 * The StartRemotePeers class begins remote peer processes. 
 * It reads configuration file PeerInfo.cfg and starts remote peer processes.
 * You must modify this program a little bit if your peer processes are written in C or C++.
 * Please look at the lines below the comment saying IMPORTANT.
 */
public class StartRemotePeers {
	private int peerId;
	public boolean hasFile;
	public Vector<RemotePeerInfo> peerInfoVector;

	public StartRemotePeers(int peerId) {
		this.peerId = peerId;
	}

	public void getConfiguration() {
		String st;
		peerInfoVector = new Vector<RemotePeerInfo>();
		try {
			BufferedReader in = new BufferedReader(new FileReader("PeerInfo.cfg"));
			while ((st = in.readLine()) != null) {

				String[] tokens = st.split("\\s+");
				// System.out.println("tokens begin ----");
				// for (int x=0; x<tokens.length; x++) {
				// System.out.println(tokens[x]);
				// }
				// System.out.println("tokens end ----");
				if (Integer.parseInt(tokens[0]) == peerId && tokens[3].equals("1"))
					hasFile = true;

				peerInfoVector.addElement(new RemotePeerInfo(tokens[0], tokens[1], tokens[2], tokens[3]));

			}

			in.close();
		} catch (Exception ex) {
			System.out.println(ex.toString());
		}
	}

	public void Start() {
		// TODO Auto-generated method stub
		try {
			getConfiguration();

			// get current path
			String path = System.getProperty("user.dir");

			// start clients at remote hosts
			for (int i = 0; i < peerInfoVector.size(); i++) {
				RemotePeerInfo pInfo = (RemotePeerInfo) peerInfoVector.elementAt(i);

				System.out.println("Start remote peer " + pInfo.peerId + " at " + pInfo.peerAddress);

				Runtime.getRuntime()
						.exec("ssh " + pInfo.peerAddress + " cd " + path + "; java peerProcess " + pInfo.peerId);
			}
			System.out.println("Starting all remote peers has done.");

		} catch (Exception ex) {
			System.out.println(ex);
		}
	}

}
