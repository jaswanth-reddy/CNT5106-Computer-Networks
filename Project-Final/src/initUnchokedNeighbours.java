import java.util.Collections;
import java.util.Enumeration;
import java.util.TimerTask;
import java.util.Vector;

public class initUnchokedNeighbours extends TimerTask {
    public void run() {
        PeerToPeer.fetch_NxtPeerDetails();
        if (!PeerToPeer.unchokedNeighboursHashMapTable.isEmpty())
            PeerToPeer.unchokedNeighboursHashMapTable.clear();
        Enumeration<String> rmPIDs = Collections.enumeration(PeerToPeer.rmPeerHashMap.keySet());
        Vector<remPeerDetails> remotePeerVector = new Vector<>();
        while (rmPIDs.hasMoreElements()) {
            String key = rmPIDs.nextElement();
            remPeerDetails remotePeerInfo = PeerToPeer.rmPeerHashMap.get(key);
            if (remotePeerInfo.isChoked == 1
                    && !key.equals(PeerToPeer.peerId) 
                    && remotePeerInfo.isCompleted == 0
                    && remotePeerInfo.isHandShake == 1)
                remotePeerVector.add(remotePeerInfo);
        }

        if (remotePeerVector.size() > 0) {
            Collections.shuffle(remotePeerVector);
            remPeerDetails intialPeer = remotePeerVector.firstElement();
            PeerToPeer.rmPeerHashMap.get(intialPeer.peerId).isOptUnchokedNeighbor = 1;
            PeerToPeer.unchokedNeighboursHashMapTable.put(intialPeer.peerId, PeerToPeer.rmPeerHashMap.get(intialPeer.peerId));
            PeerToPeer.log.dataLog(PeerToPeer.peerId + " --> " + intialPeer.peerId + " MESSAGE: Has the Optimistically UnChoked neighbour from " + intialPeer.peerId);

            if (PeerToPeer.rmPeerHashMap.get(intialPeer.peerId).isChoked == 1) {
                PeerToPeer.rmPeerHashMap.get(intialPeer.peerId).isChoked = 0;
                PeerToPeer.sndReq2Unchoke(intialPeer.peerId, PeerToPeer.peerData.get(intialPeer.peerId));
                PeerToPeer.sndHaveMsg(intialPeer.peerId, PeerToPeer.peerData.get(intialPeer.peerId));
                PeerToPeer.rmPeerHashMap.get(intialPeer.peerId).state = 3;
            }
        }
    }
}