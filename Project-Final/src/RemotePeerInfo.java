import java.util.Comparator;
import java.util.Date;

public class RemotePeerInfo implements Comparator<RemotePeerInfo> {
    public final boolean comparator = false;
    public String peerId;
    public String peerAddress;
    public String peerPort;
    public int isFirstPeer;
    public double streamRate = 0;
    public int isInterested = 1;
    public int isPreferredNeighbor = 0;
    public int isOptUnchokedNeighbor = 0;
    public int isChoked = 1;
    public MessagePayLoad payloadData;
    public int state = -1;
    public int peerIndex;
    public int isCompleted = 0;
    public int isHandShake = 0;
    public Date sTime;
    public Date fTime;

    public RemotePeerInfo() {

    }

    public String fetch_PeerId() {
        return peerId;
    }

    public void init_PeerId(String peerId) {
        this.peerId = peerId;
    }

    public String fetch_PeerAddress() {
        return peerAddress;
    }

    public void init_PeerAddress(String peerAddress) {
        this.peerAddress = peerAddress;
    }

    public String fetch_PeerPort() {
        return peerPort;
    }

    public void init_PeerPort(String peerPort) {
        this.peerPort = peerPort;
    }

    public boolean check_FilePresent() {
        return hasFile;
    }

    public void init_FilePresent(boolean hasFile) {
        this.hasFile = hasFile;
    }

    public int fetch_PositionPeer() {
        return peerPos;
    }

    public void init_PeerPosition(int peerPos) {
        this.peerPos = peerPos;
    }

    public boolean hasFile;
    public int peerPos;
    public boolean isFirst;

    public RemotePeerInfo(String pId, String pAddress, String pPort, boolean hFile) {
        peerId = pId;
        peerAddress = pAddress;
        peerPort = pPort;
        hasFile = hFile;
        payloadData = new MessagePayLoad();
    }

    public int compareTo(RemotePeerInfo remotePeerInfo) {
        return Double.compare(this.streamRate, remotePeerInfo.streamRate);
    }

    public int compare(RemotePeerInfo peerX, RemotePeerInfo peerY) {
        if (peerX == null && peerY == null)
            return 0;

        if (peerX == null) {
            return 1;
        }

        if (peerY == null) {
            return -1;
        }

        if (comparator) {
            return peerX.compareTo(peerY);
        } else {
            return peerY.compareTo(peerX);
        }
    }

}