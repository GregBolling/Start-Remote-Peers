// This file uses a lot of logic from the sample file from Canvas

import java.net.*;
import java.io.*;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
//import java.math.BigInteger;
import java.nio.*;
//import java.util.*;
// idean of file output streams came from https://www.techiedelight.com/how-to-write-to-a-binary-file-in-java/
import java.io.IOException;
//import java.nio.channels.FileChannel;
//import java.io.FileOutputStream;

public class Server {
    private static final int sPort = 8000; // The server will be listening on this port number
    static private Vector<Integer> haveFile;
    static private ArrayList<Handler> handlers = new ArrayList<Handler>();

    private static peerProcess pp;

    public Server(peerProcess pp_) {
        pp = pp_;
    }

    public void startServer() throws Exception {
        ServerSocket listener = new ServerSocket(sPort);
        System.out.println("The server is running.");
        int clientNum = 1;

        // make list of peerIds that have the file
        haveFile = new Vector<Integer>();
        for (RemotePeerInfo rpi : pp.peerInfoVector) {
            if (rpi.hasFile()) {
                haveFile.addElement(rpi.getPeerId());
            }
        }

        try {
            while (true) {
                Handler h = new Handler(listener.accept(), clientNum);
                h.start();
                handlers.add(h);
                System.out.println("Client " + clientNum + " is connected!");
                clientNum++;
            }
        } finally {
            listener.close();
        }
    }

    /**
     * A handler thread class. Handlers are spawned from the listening loop and are
     * responsible for dealing with a single client's requests.
     */
    private static class Handler extends Thread {
        private byte[] message = new byte[50]; // message received from the client
        private Socket connection;
        private ObjectInputStream in; // stream read from the socket
        private ObjectOutputStream out; // stream write to the socket
        private int no; // The index number of the client
        int connectedFrom;

        public Handler(Socket connection, int no) {
            this.connection = connection;
            this.no = no;
        }

        private void runTimer() {
            // Every 5 seconds, recalculate the preferred neighbors
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                    try {
                        pp.calculatePreferredNeighbors();
                        /*
                         * for (int i = 0; i < pp.messagesToSend.size(); i++) {
                         * // send choke/unchoke messages
                         * sendMessageBB(pp.messagesToSend.get(i));
                         * }
                         */
                        // choke unchosen peers, unchoke chosen peers
                        int count = 0;
                        for (RemotePeerInfo rpi : pp.peerInfoVector) {
                            if (!pp.preferredNeighbors.contains(rpi.getPeerId())) {
                                pp.messagesToSend.add(Messages.createChokeMessage());
                                if (connectedFrom == rpi.getPeerId()) {
                                    rpi.setChoked(true);
                                    sendMessageBB(pp.messagesToSend.get(count));
                                }
                                count++;
                            } else {
                                pp.messagesToSend.add(Messages.createUnchokeMessage());
                                if (connectedFrom == rpi.getPeerId()) {
                                    rpi.setChoked(false);
                                    sendMessageBB(pp.messagesToSend.get(count));
                                }
                                count++;
                            }
                        }

                    } catch (Exception e) {
                    }
                }

            }, 0, pp.unchokingInterval* 1000);
        }

        private void serverLoop() throws ClassNotFoundException, IOException {

            // https://stackoverflow.com/ques1tions/2702980/java-loop-every-minute

            while (true) {
                while (in.available() <= 0) {
                }
                
                message = new byte[in.available()];

                in.read(message);

                ByteBuffer buff = ByteBuffer.wrap(message);

                connectedFrom = Messages.decodeMessage(buff, pp, -1);
                pp.logger.onConnectingFrom(connectedFrom);
                ByteBuffer messageToSend = Messages.createHandshakeMessage(pp.peerId);
                sendMessageBB(messageToSend);

                System.out.println("I am peer " + pp.getPeerId() + "(server) and I am connected to " + connectedFrom);

                // receive bitfield message

                while (in.available() <= 0) {
                }
                byte[] message2 = new byte[in.available()];

                in.read(message2);

                ByteBuffer buff2 = ByteBuffer.wrap(message2);

                int bitfieldRes = Messages.decodeMessage(buff2, pp, connectedFrom);

                ByteBuffer bitfieldMessage = Messages.createBitfieldMessage(pp.bitfield);
                sendMessageBB(bitfieldMessage);

                while (in.available() <= 0) {
                }
                message = new byte[in.available()];

                in.read(message);

                buff = ByteBuffer.wrap(message);

                int interestedRes = Messages.decodeMessage(buff, pp, connectedFrom);

                System.out.println("Peers interested in 1001: ");
                for (int i = 0; i < pp.interested.size(); i++) {
                    System.out.println(pp.interested.get(i));
                }

                // send interested/not interested
                for (int i = 0; i < pp.messagesToSend.size(); i++) {
                    sendMessageBB(pp.messagesToSend.get(i));
                }
                pp.messagesToSend.clear();
runTimer();
                while (in.available() <= 0) {
                }
                message = new byte[in.available()];

                in.read(message);

                buff = ByteBuffer.wrap(message);
                // receive request msg
                int reqRes = Messages.decodeMessage(buff, pp, connectedFrom);

                while (in.available() <= 0) {
                }
                message = new byte[in.available()];

                in.read(message);

                buff = ByteBuffer.wrap(message);

                int chokeRes = Messages.decodeMessage(buff, pp, connectedFrom);

                while (pp.pieceMessages.size() == 0) {
                }
                for (int i = 0; i < pp.pieceMessages.size(); i++) {
                    sendMessageBB(pp.pieceMessages.get(i));
                }
                while (true) {
                }

                /*
                 * if(handlers.size() >= 2)
                 * {
                 * 
                 * for(int i=0; i < handlers.size(); i++)
                 * {
                 * // start sending piece messages here
                 * // request piece from client
                 * // exclude server
                 * // coordinate piece distributuion between clients
                 * if(handlers.get(i).connectedFrom == connectedFrom)
                 * continue;
                 * messageToSend = Messages.createHandshakeMessage(connectedFrom);
                 * handlers.get(i).sendMessageBB(messageToSend);
                 * messageToSend =
                 * Messages.createHandshakeMessage(handlers.get(i).connectedFrom);
                 * sendMessageBB(messageToSend);
                 * 
                 * 
                 * 
                 * 
                 * }
                 * // choke and unchoke different processes
                 * }
                 */

            }
        }

        // try to handshake with processes that have the file
        // for (Integer i : haveFile) {
        // String messageToSend = createHandshakeMessage(peerId);
        // // new Handler(listener.accept(), peerId).sendMessage(messageToSend);
        // }

        public void run() {
            try {
                // initialize Input and Output streams
                out = new ObjectOutputStream(connection.getOutputStream());
                out.flush();
                in = new ObjectInputStream(connection.getInputStream());
                try {
                    serverLoop();
                } catch (ClassNotFoundException classnot) {
                    System.err.println("Data received in unknown format");
                }
            } catch (IOException ioException) {
                System.out.println("Disconnect with Client " + no);
            } finally {
                // Close connections
                try {
                    in.close();
                    out.close();
                    connection.close();
                } catch (IOException ioException) {
                    System.out.println("Disconnect with Client " + no);
                }
            }
        }

        // send a message to the output stream
        // public void sendMessage(String msg) {
        // try {
        // out.writeObject(msg);
        // out.flush();
        // System.out.println("Send message to Client " + no); // debug message
        // } catch (IOException ioException) {
        // ioException.printStackTrace();
        // }
        // }
        // send a message to the output stream
        public void sendMessageBB(ByteBuffer msg) {
            try {
                out.write(msg.array());
                out.flush();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}