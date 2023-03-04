import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.TimerTask;
//function to set preferred neighbours depending on the handshake message 
public class InitPreferredNeighbours extends TimerTask {
    public void run() {
        PeerToPeer.fetch_NxtPeerDetails();
        Enumeration<String> rmPIDs = Collections.enumeration(PeerToPeer.rmPeerHashMap.keySet());
        int intrestedPeers = 0;
        StringBuilder sb = new StringBuilder();
        while (rmPIDs.hasMoreElements()) {
            String remotePeerId = rmPIDs.nextElement();
            remPeerDetails remPeer = PeerToPeer.rmPeerHashMap.get(remotePeerId);
            if (remPeer.isCompleted == 0 && remPeer.isHandShake == 1)
                intrestedPeers++;
            if (remotePeerId.equals(PeerToPeer.peerId))
                continue;
            else if (remPeer.isCompleted == 1) {
                try {
                    PeerToPeer.preferredNeighbours.remove(remotePeerId);
                } catch (Exception ignored) {
                }
            }
        }
        if (intrestedPeers > 7) {

            if (!PeerToPeer.preferredNeighbours.isEmpty())
                PeerToPeer.preferredNeighbours.clear();

            List<remPeerDetails> remPeersArray = new ArrayList<>(PeerToPeer.rmPeerHashMap.values());
            remPeersArray.sort(new remPeerDetails());
            int cnt = 0;

            for (remPeerDetails remotePeerInfo : remPeersArray) {
                if (cnt > Parameters.numberOfPreferredNeighbors - 1)
                    break;

                if (remotePeerInfo.isHandShake == 1 && !remotePeerInfo.peerId.equals(PeerToPeer.peerId)
                        && PeerToPeer.rmPeerHashMap.get(remotePeerInfo.peerId).isCompleted == 0) {
                    PeerToPeer.rmPeerHashMap.get(remotePeerInfo.peerId).isPreferredNeighbor = 1;
                    PeerToPeer.preferredNeighbours.put(remotePeerInfo.peerId,
                            PeerToPeer.rmPeerHashMap.get(remotePeerInfo.peerId));
                    cnt++;
                    sb.append(remotePeerInfo.peerId).append(", ");

                    if (PeerToPeer.rmPeerHashMap.get(remotePeerInfo.peerId).isChoked == 1) {
                        PeerToPeer.sndReq2Unchoke(remotePeerInfo.peerId, PeerToPeer.peerData.get(remotePeerInfo.peerId));
                        PeerToPeer.rmPeerHashMap.get(remotePeerInfo.peerId).isChoked = 0;
                        PeerToPeer.sndHaveMsg(remotePeerInfo.peerId, PeerToPeer.peerData.get(remotePeerInfo.peerId));
                        PeerToPeer.rmPeerHashMap.get(remotePeerInfo.peerId).state = 3;
                    }
                }
            }
        } else {
            rmPIDs = Collections.enumeration(PeerToPeer.rmPeerHashMap.keySet());
            while (rmPIDs.hasMoreElements()) {
                String nxt_Peer = rmPIDs.nextElement();
                remPeerDetails remPeer = PeerToPeer.rmPeerHashMap.get(nxt_Peer);
                if (nxt_Peer.equals(PeerToPeer.peerId))
                    continue;

                if (remPeer.isCompleted == 0 && remPeer.isHandShake == 1) {
                      if (remPeer.isChoked == 1) {
                        PeerToPeer.sndReq2Unchoke(nxt_Peer, PeerToPeer.peerData.get(nxt_Peer));
                        PeerToPeer.rmPeerHashMap.get(nxt_Peer).isChoked = 0;
                        PeerToPeer.sndHaveMsg(nxt_Peer, PeerToPeer.peerData.get(nxt_Peer));
                        PeerToPeer.rmPeerHashMap.get(nxt_Peer).state = 3;
                    }
                    if (!PeerToPeer.preferredNeighbours.containsKey(nxt_Peer)) {
                        sb.append(nxt_Peer).append(", ");
                        PeerToPeer.preferredNeighbours.put(nxt_Peer, PeerToPeer.rmPeerHashMap.get(nxt_Peer));
                        PeerToPeer.rmPeerHashMap.get(nxt_Peer).isPreferredNeighbor = 1;
                    }
                  
                }
            }
        }
        if (!sb.toString().equals(""))
            PeerToPeer.log.dataLog(PeerToPeer.peerId + " selected preferred neighbors : " + sb);
    }
}