import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class PeerModulator implements Runnable {
    private InputStream inpStream;
    private OutputStream outStream;
    public static final int activeSessionsCount = 1;
    private Socket soc = null;
    private int sessionMode;
    String remotepeerId, currentpeerId;

    public void run() {
        MessageProperties d = new MessageProperties();
        byte[] msgHSArray = new byte[32];
        byte[] bufferedMessage = new byte[5];
        byte[] dLen;
        byte[] dType;
        try {
            if (this.sessionMode != activeSessionsCount) {
                updatePeersData(msgHSArray);
                if (peerConn()) {
                    throw new Exception("Connection with " + this.currentpeerId + " failed");
                }
                PeerToPeer.log.dataLog(this.currentpeerId + " --> " + this.remotepeerId + " MESSAGE : Sending HandShake ");
                PeerToPeer.log.dataLog(this.currentpeerId + " --> " + this.remotepeerId + " MESSAGE : Establish TCP connection ");
                PeerToPeer.rmPeerHashMap.get(remotepeerId).state = 2;

            } else {
                if (peerConn()) {
                    throw new Exception("Connection with " + this.currentpeerId + " is failed");
                }
                updatePeersData(msgHSArray);
                PeerToPeer.log.dataLog(this.currentpeerId + " --> " + this.remotepeerId + " MESSAGE : Sending HandShake ");
                PeerToPeer.rmPeerHashMap.get(remotepeerId).state = 8;
                MessageDetails md = new MessageDetails(5, PeerToPeer.currentDataPayLoad.transformData());
                outStream.write(MessageDetails.transformMsgToB_Array(md));
            }
            x: while (true) {
                int handshakeBytes;
                if ((handshakeBytes = inpStream.read(bufferedMessage)) == -1) {
                    break x;
                }
                dLen = new byte[4];
                dType = new byte[1];
                System.arraycopy(bufferedMessage, 0, dLen, 0, 4);
                System.arraycopy(bufferedMessage, 4, dType, 0, 1);
                MessageDetails md = new MessageDetails();
                md.init_MsgLen(dLen);
                md.init_MsgType(dType);
                String s = "0 1 2 3";
                if (s.contains(md.fetch_MsgType())) {
                    d.m = md;
                } else {
                    int readBytes = 0;
                    int bytesToRead;
                    byte[] payloadMessage = new byte[md.fetch_Msg_Length() - 1];
                    while (readBytes < md.fetch_Msg_Length() - 1) {
                        bytesToRead = inpStream.read(payloadMessage, readBytes,
                                md.fetch_Msg_Length() - 1 - readBytes);
                        if (bytesToRead == -1) {
                            return;
                        }
                        readBytes += bytesToRead;
                    }
                    byte[] messageDataPayLoad = new byte[md.fetch_Msg_Length() + 4];
                    System.arraycopy(bufferedMessage, 0, messageDataPayLoad, 0, 5);
                    System.arraycopy(payloadMessage, 0, messageDataPayLoad, 5, payloadMessage.length);
                    d.m = MessageDetails.convertByteArrayToData(messageDataPayLoad);
                }
                d.peer_Id = this.remotepeerId;
                PeerToPeer.addToQueue(d);
            }
        } catch (Exception ex) {
            PeerToPeer.log.dataLog(ex.getMessage());
        }

    }

    public void updatePeersData(byte[] hsArr) throws IOException {
        x: while (1 == 1) {
            inpStream.read(hsArr);
            String s = new String(hsArr, StandardCharsets.UTF_8);
            if (s.substring(0, 18).equals("P2PFILESHARINGPROJ")) {
                remotepeerId = s.substring(s.length() - 4, s.length());
                PeerToPeer.log.dataLog(this.remotepeerId + " -> " + this.currentpeerId + " MESSAGE: Received a handshake message from " + this.remotepeerId);
                PeerToPeer.peerData.put(this.remotepeerId, this.soc);
                break x;
            }
        }

    }

    PeerModulator(String pId, Socket s, int sessionMode) {
        this.soc = s;
        this.sessionMode = sessionMode;
        this.currentpeerId = pId;
        try {
            inpStream = s.getInputStream();
            outStream = s.getOutputStream();
        } catch (IOException e) {
            PeerToPeer.log.dataLog(" MESSAGE: Error occurred while fetching data: " + this.currentpeerId);
        }
    }

    PeerModulator(String host, int port, int sessionMode, String pId) throws IOException {
        this.sessionMode = sessionMode;
        try {
            this.currentpeerId = pId;
            this.soc = new Socket(host, port);
        } catch (Exception ex) {
            PeerToPeer.log.dataLog(" MESSAGE: Error occurred opening a connection with peer " + pId);
        }
        try {
            inpStream = soc.getInputStream();
            outStream = soc.getOutputStream();
        } catch (IOException e) {
            PeerToPeer.log.dataLog(" MESSAGE: Error occurred while fetching data: " + this.currentpeerId);
        }
    }

    public boolean peerConn() {
        try {
            outStream.write(Handshake.handShakeToArray(new Handshake(Integer.parseInt(this.currentpeerId))));
        } catch (Exception ex) {
            PeerToPeer.log.dataLog(" MESSAGE: Sending HandShake Failed.");
            return true;
        }
        return false;
    }

}
