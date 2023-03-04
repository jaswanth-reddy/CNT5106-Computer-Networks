import java.util.Comparator;
import java.util.Date;

public class remPeerDetails implements Comparator<remPeerDetails>
{
    public final boolean comparator = false;
    public boolean hasFile, isFirst;
    public String peerId, peerAddress, peerPort;
    public double streamRate = 0 ;
    public int peerPos, isFirstPeer, isInterested = 1,isPreferredNeighbor = 0, isOptUnchokedNeighbor = 0, isChoked = 1, state = -1, peerIndex, isCompleted = 0, isHandShake = 0;
    public Date sTime, fTime;
    
    public MessagePayLoad payloadData;


    // default constructor
    remPeerDetails(){}

    // function to fetch the PeerID info
    public String fetch_PeerId() {
        return peerId;
    }

    // function to initialize the PeerID info
    public void init_PeerId(String peerId) {
        this.peerId = peerId;
    }

    // function to fetch the PeerAddress info
    public String fetch_PeerAddress() {
        return peerAddress;
    }

    // function to initialize the PeerAddress info
    public void init_PeerAddress(String peerAddress) {
        this.peerAddress = peerAddress;
    }

    // function to fetch the PeerPort info
    public String fetch_PeerPort() {
        return peerPort;
    }

    // function to initialize the PeerPort info
    public void init_PeerPort(String peerPort) {
        this.peerPort = peerPort;
    }

    // function to check if there is a file
    public boolean check_FilePresent() {
        return hasFile;
    }

    // function to check if there is a file
    public void init_FilePresent(boolean hasFile) {
        this.hasFile = hasFile;
    }

    // function to obtain Positions of the peers
    public int fetch_PeerPosition() {
        return peerPos;
    }

    // function to obtain Positions of the peers
    public void init_PeerPosition(int peerPos) {
        this.peerPos = peerPos;
    }

    // constructor with parameters
    public remPeerDetails(String pId, String pAddress, String pPort,boolean hFile) 
    {
        peerId = pId;
        peerAddress = pAddress;
        peerPort = pPort;
        hasFile = hFile;
        payloadData = new MessagePayLoad();
    }

    // function to comapre remote peer details
    public int compareTo(remPeerDetails remotePeerInfo)
    {
        return Double.compare(this.streamRate,remotePeerInfo.streamRate);
    }

    public boolean checkIsNull(remPeerDetails peer){

        if(peer == null)
        {
            return true;
        }else return false;
    }

    // Abstarct method to comapre 2 peers
    public int compare(remPeerDetails peerX,remPeerDetails peerY)
    {
        if(checkIsNull(peerX) && checkIsNull(peerY))
            return 0;

        if(checkIsNull(peerX))
        {
            return 1;
        }

        if(checkIsNull(peerY))
        {
            return -1;
        }

        if(comparator)
        {
            return peerX.compareTo(peerY);
        }
        else
        {
            return peerY.compareTo(peerX);
        }
    }

}
