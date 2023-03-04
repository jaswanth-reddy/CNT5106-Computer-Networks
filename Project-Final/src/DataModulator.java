import java.io.*;
import java.net.Socket;
import java.util.*;

public class DataModulator implements Runnable {
    private static String pId = null;
    RandomAccessFile rf;
    MessageActions msg_Actions = new MessageActions();

    public DataModulator(String pId) {
        DataModulator.pId = pId;
    }

    public void run() {

        // Getter and setter functions to manipulate and fetch the message info
        MessageDetails msg_Details;
        // Getter and setter functions for peer details of the message
        MessageProperties msg_Prop;

        // varibale to store the kind of messages obtained from the communicated peer
        String msg_Category;
        String cur_PeerId;

        while (true) {
            msg_Prop = PeerToPeer.removeDataFromQueue();

            // when the received message dowsnot have any property details
            while (msg_Prop == null) {
                // making the thread wait to fetch the details
                Thread.currentThread();
                try {
                    Thread.sleep(500);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                msg_Prop = PeerToPeer.removeDataFromQueue();
            }

            // Fetch the info related to the message and peer info
            msg_Details = msg_Prop.fetch_M();
            msg_Category = msg_Details.fetch_MsgType();
            cur_PeerId = msg_Prop.fetch_peerID();

            int state = PeerToPeer.rmPeerHashMap.get(cur_PeerId).state;

            // when HAVE message category is received
            if (msg_Category.equals("" + 4) && state != 14) {
                PeerToPeer.log.dataLog(PeerToPeer.peerId + " --> " + cur_PeerId + " MESSAGE: Received 'HAVE' message from Peer "
                        + cur_PeerId);

                // Given the HAVE message, we try to fetch the message details from the given
                // peerID
                if (msg_Actions.fetch_PayLoadDataFromPeer(cur_PeerId, msg_Details)) {
                    // Upon positive response from the peer, we send out the INTERESTED message
                    msg_Actions.push_InterestedMsgType(cur_PeerId, PeerToPeer.peerData.get(cur_PeerId));

                    // update the state of the message info
                    PeerToPeer.rmPeerHashMap.get(cur_PeerId).state = 9;
                }
                // when the peer has no message details
                else {
                    msg_Actions.push_NotInterestedMessage(cur_PeerId, PeerToPeer.peerData.get(cur_PeerId));

                    // update the state of the message info
                    PeerToPeer.rmPeerHashMap.get(cur_PeerId).state = 13;
                }
            } else {
                switch (state) {
                    case 2: // State: BitField
                        state_BitField(msg_Category, cur_PeerId);
                        break;

                    case 3: // State: Interested
                        state_interested(msg_Category, cur_PeerId);
                        break;

                    case 4: // State: request
                        state_request(msg_Category, cur_PeerId, msg_Details);
                        break;

                    case 8: // State: bitfield
                        state_bitfield(msg_Category, cur_PeerId, msg_Details);
                        break;

                    case 9: // State: Choke
                        state_Choke(msg_Category, cur_PeerId);
                        break;

                    case 11: // State: choke
                        state_choke(msg_Category, cur_PeerId, msg_Details);
                        break;

                    case 14:// unchoke
                        state_unChoke(msg_Category, cur_PeerId, msg_Details);
                        break;
                }
            }

        }
    }

    // function is called when the state is pointed to unchoke: case 14
    public void state_unChoke(String msg_Category, String cur_PeerId, MessageDetails msg_Details) {
        // checking the message category for 1
        if (msg_Category.equals("" + 1)) {
            // Console log the information obtained from the peers
            PeerToPeer.log.dataLog(PeerToPeer.peerId + " got CHOKED by Peer " + cur_PeerId);

            // trying to sleep the thread for 6 seconds
            try {
                Thread.sleep(6000);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            // Console log the information obtained from the peers
            PeerToPeer.log.dataLog(PeerToPeer.peerId + " got UNCHOKED by Peer " + cur_PeerId);
            PeerToPeer.rmPeerHashMap.get(cur_PeerId).state = 14;
        }
        // checking the message category for 4
        else if (msg_Category.equals("" + 4)) {
            // Given the HAVE message, we try to fetch the message details from the given
            // peerID
            if (msg_Actions.fetch_PayLoadDataFromPeer(cur_PeerId, msg_Details)) {
                // Upon positive response from the peer, we send out the INTERESTED message
                msg_Actions.push_InterestedMsgType(cur_PeerId, PeerToPeer.peerData.get(cur_PeerId));
                // update the state of the message info
                PeerToPeer.rmPeerHashMap.get(cur_PeerId).state = 9;
            } else {
                // function used to display the console output and write to output stream based
                // on Meassage type
                msg_Actions.push_NotInterestedMessage(cur_PeerId, PeerToPeer.peerData.get(cur_PeerId));
                // update the state of the message info
                PeerToPeer.rmPeerHashMap.get(cur_PeerId).state = 13;
            }
        }
    }

    // function is called when the state is pointed to choke: case 11
    public void state_choke(String msg_Category, String cur_PeerId, MessageDetails msg_Details) {
        // checking the message category for 0
        if (msg_Category.equals("" + 0)) {
            // Console log the information obtained from the peers
            PeerToPeer.log.dataLog(PeerToPeer.peerId + " got CHOKED by Peer " + cur_PeerId);
            PeerToPeer.rmPeerHashMap.get(cur_PeerId).state = 14;
        }
        // checking the message category for 7
        else if (msg_Category.equals("" + 7)) {
            // get the payload for the given message
            byte[] payload_arr = msg_Details.fetch_PLArr();

            // Obtain the start and the diff time
            PeerToPeer.rmPeerHashMap.get(cur_PeerId).fTime = new Date();
            long differnce = PeerToPeer.rmPeerHashMap.get(cur_PeerId).fTime.getTime()
                    - PeerToPeer.rmPeerHashMap.get(cur_PeerId).sTime.getTime();

            // fetch the stream rate of the data
            PeerToPeer.rmPeerHashMap.get(cur_PeerId).streamRate = ((double) (payload_arr.length + 5) / (double) differnce)
                    * 100;
            MessagePiecePayLoad p = MessagePiecePayLoad.convertToPiece(payload_arr);
            PeerToPeer.currentDataPayLoad.revisePayLoad(p, "" + cur_PeerId);
            int request_index = PeerToPeer.currentDataPayLoad.fetch_FstBitField(PeerToPeer.rmPeerHashMap.get(cur_PeerId).payloadData);

            if (request_index != -1) {
                // function to send the data to the peer data file
                msg_Actions.push_Request(request_index, PeerToPeer.peerData.get(cur_PeerId));

                // update the state of the message info and time
                PeerToPeer.rmPeerHashMap.get(cur_PeerId).state = 11;
                PeerToPeer.rmPeerHashMap.get(cur_PeerId).sTime = new Date();
            } else
                PeerToPeer.rmPeerHashMap.get(cur_PeerId).state = 13; // // update the state of the message info
            PeerToPeer.fetch_NxtPeerDetails();
            ;

            // Enumerate over the keys of the remote peer Hashmap
            Enumeration<String> keys = Collections.enumeration(PeerToPeer.rmPeerHashMap.keySet());

            // send the message over the peerID
            while (keys.hasMoreElements()) {
                String nextElement = keys.nextElement();
                remPeerDetails r = PeerToPeer.rmPeerHashMap.get(nextElement);

                if (nextElement.equals(PeerToPeer.peerId))
                    continue;
                if (r.isCompleted == 0 && r.isChoked == 0 && r.isHandShake == 1) {
                    msg_Actions.push_HaveMessage(nextElement, PeerToPeer.peerData.get(nextElement));

                    // update the state of the message info
                    PeerToPeer.rmPeerHashMap.get(nextElement).state = 3;
                }
            }
        }
    }

    // function is called when the state is pointed to Choke: case 9
    public void state_Choke(String msg_Category, String cur_PeerId) {
        // checking the message category for 6
        if (msg_Category.equals("" + 0)) {
            // Console log the information obtained from the peers
            PeerToPeer.log.dataLog(PeerToPeer.peerId + " --> " + cur_PeerId + " MESSAGE: Got CHOKED by Peer " + cur_PeerId);

            // update the state of the message info
            PeerToPeer.rmPeerHashMap.get(cur_PeerId).state = 14;
        }

        // checking the message category for 6
        else if (msg_Category.equals("" + 1)) {
            // Console log the information obtained from the peers
            PeerToPeer.log.dataLog(PeerToPeer.peerId + " got CHOKED by Peer " + cur_PeerId);

            // sleep the thread fopr 5 seconds
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Console log the information obtained from the peers
            PeerToPeer.log.dataLog(PeerToPeer.peerId + " got UNCHOKED by Peer " + cur_PeerId);

            //
            int obtain_Mismatch = PeerToPeer.currentDataPayLoad
                    .fetch_FstBitField(PeerToPeer.rmPeerHashMap.get(cur_PeerId).payloadData);

            // if there is no mismatch
            if (obtain_Mismatch != -1) {
                // function to send the data to the peer data file
                msg_Actions.push_Request(obtain_Mismatch, PeerToPeer.peerData.get(cur_PeerId));

                // update the state of the message info
                PeerToPeer.rmPeerHashMap.get(cur_PeerId).state = 11;
                PeerToPeer.rmPeerHashMap.get(cur_PeerId).sTime = new Date();
            } else
                PeerToPeer.rmPeerHashMap.get(cur_PeerId).state = 13; // update the state of the message info
        }
    }

    // function is called when the state is pointed to bITFIELD: case 8
    public void state_bitfield(String msg_Category, String cur_PeerId, MessageDetails msg_Details) {
        // checking the message category for 6
        if (msg_Category.equals("" + 5)) {
            // Given the HAVE message, we try to fetch the message details from the given
            // peerID
            if (msg_Actions.fetch_PayLoadDataFromPeer(cur_PeerId, msg_Details)) {
                // Upon positive response from the peer, we send out the INTERESTED message
                msg_Actions.push_InterestedMsgType(cur_PeerId, PeerToPeer.peerData.get(cur_PeerId));

                // update the state of the message info
                PeerToPeer.rmPeerHashMap.get(cur_PeerId).state = 9;
            } else { // function used to display the console output and write to output stream based
                     // on Meassage type
                msg_Actions.push_NotInterestedMessage(cur_PeerId, PeerToPeer.peerData.get(cur_PeerId));

                // update the state of the message info
                PeerToPeer.rmPeerHashMap.get(cur_PeerId).state = 13;
            }
        }
    }

    // function is called when the state is pointed to REQUEST : case 4
    public void state_request(String msg_Category, String cur_PeerId, MessageDetails msg_Details) {
        // checking the message category for 6
        if (msg_Category.equals("" + 6)) {
            // function to write the data to the peer data file
            msg_Actions.msgBytes_Transfer(PeerToPeer.peerData.get(cur_PeerId), msg_Details, cur_PeerId);

            // If the peerID is any of the preferred Neighbours or the unchoked Neighbours
            // thern we choke the peerID
            if (!PeerToPeer.preferredNeighboursHashMapTable.containsKey(cur_PeerId)
                    && !PeerToPeer.unchokedNeighboursHashMapTable.containsKey(cur_PeerId)) {
                // function to choke the identified peerID
                msg_Actions.push_ChokeMessage(cur_PeerId, PeerToPeer.peerData.get(cur_PeerId));

                // update the state of the message info
                PeerToPeer.rmPeerHashMap.get(cur_PeerId).isChoked = 1;
                PeerToPeer.rmPeerHashMap.get(cur_PeerId).state = 6;
            }
        }
    }

    // function is called when the state is pointed to Interested : case 3
    public void state_interested(String msg_Category, String cur_PeerId) {
        // checking the message category for 2
        if (msg_Category.equals("" + 2)) {
            // Console log the information obtained from the peers
            PeerToPeer.log.dataLog(PeerToPeer.peerId + " --> " + cur_PeerId + " MESSAGE: Got REQUEST message to Peer " + cur_PeerId);
            PeerToPeer.log.dataLog(
                    PeerToPeer.peerId + " --> " + cur_PeerId + " MESSAGE: Got INTERESTED message from Peer " + cur_PeerId);

            // update the state of the message info
            PeerToPeer.rmPeerHashMap.get(cur_PeerId).isInterested = 1;
            PeerToPeer.rmPeerHashMap.get(cur_PeerId).isHandShake = 1;

            // If the peerID is any of the preferred Neighbours or the unchoked Neighbours
            // thern we choke the peerID
            if (!PeerToPeer.preferredNeighboursHashMapTable.containsKey(cur_PeerId)
                    && !PeerToPeer.unchokedNeighboursHashMapTable.containsKey(cur_PeerId)) {
                // function to choke the identified peerID
                msg_Actions.push_ChokeMessage(cur_PeerId, PeerToPeer.peerData.get(cur_PeerId));

                // update the state of the message info
                PeerToPeer.rmPeerHashMap.get(cur_PeerId).isChoked = 1;
                PeerToPeer.rmPeerHashMap.get(cur_PeerId).state = 6;
            }
            // if the peer is not found
            else { // update the state of the message info
                PeerToPeer.rmPeerHashMap.get(cur_PeerId).isChoked = 0;

                // then we try to unchoke the peerID
                msg_Actions.push_UnChokeMessage(cur_PeerId, PeerToPeer.peerData.get(cur_PeerId));

                // update the state of the message info
                PeerToPeer.rmPeerHashMap.get(cur_PeerId).state = 4;
            }
        }
        // checking the message category for 3
        else if (msg_Category.equals("" + 3)) {
            // Console log the information obtained from the peers
            PeerToPeer.log.dataLog(
                    PeerToPeer.peerId + " --> " + cur_PeerId + " MESSAGE: Got NOT INTERESTED message from Peer " + cur_PeerId);

            // update the state of the message info
            PeerToPeer.rmPeerHashMap.get(cur_PeerId).isInterested = 0;
            PeerToPeer.rmPeerHashMap.get(cur_PeerId).state = 5;
            PeerToPeer.rmPeerHashMap.get(cur_PeerId).isHandShake = 1;
        }
    }

    // function is called when the state is pointed to BITFIELD: case 2
    public void state_BitField(String msg_Category, String cur_PeerId) {
        // checking the message category for 5
        if (msg_Category.equals("" + 5)) {
            // Console log the information obtained from the peers
            PeerToPeer.log.dataLog(
                    PeerToPeer.peerId + " --> " + cur_PeerId + " MESSAGE: Got BITFIELD message from Peer " + cur_PeerId);

            // Sending the Bitfield message to the peerID
            msg_Actions.push_BitFieldMessage(cur_PeerId, PeerToPeer.peerData.get(cur_PeerId));
            // update the state of the message info
            PeerToPeer.rmPeerHashMap.get(cur_PeerId).state = 3;
        }
    }

}
