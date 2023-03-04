import java.io.*;
import java.net.Socket;
import java.util.*;

// part of PeerToPeer 
public class MessageActions {
    RandomAccessFile rf;

    // fetch the message details from the given peerID
    public boolean fetch_PayLoadDataFromPeer(String pId, MessageDetails msd_Details) {
        MessagePayLoad msg_Payload = MessagePayLoad.extractData(msd_Details.fetch_PLArr());
        PeerToPeer.rmPeerHashMap.get(pId).payloadData = msg_Payload;

        return PeerToPeer.currentDataPayLoad.equate_dPayload(msg_Payload);
    }

    // function used to display the console output and write to output stream based
    // on Meassage type
    public void push_InterestedMsgType(String pId, Socket socket) {
        // Console log the information obtained from the peers
        PeerToPeer.log.dataLog(PeerToPeer.peerId + " --> " + pId + " MESSAGE: Sent a REQUEST message to Peer " + pId);
        PeerToPeer.log.dataLog(PeerToPeer.peerId + " --> " + pId + " MESSAGE: Sent a INTERESTED message to Peer " + pId);

        // write back to the socket and console output
        push_Output(MessageDetails.transformMsgToB_Array(new MessageDetails(2)), socket);
    }

    // function used to display the console output and write to output stream based
    // on Meassage type
    public void push_NotInterestedMessage(String pId, Socket socket) {
        // Console log the information obtained from the peers
        PeerToPeer.log.dataLog(PeerToPeer.peerId + " --> " + pId + " MESSAGE: Sent a NOT INTERESTED message to Peer " + pId);

        // write back to the socket and console output
        push_Output(MessageDetails.transformMsgToB_Array(new MessageDetails(3)), socket);
    }

    // function used to display the console output and write to output stream based
    // on Meassage type
    public void push_BitFieldMessage(String pId, Socket socket) {
        // Console log the information obtained from the peers
        PeerToPeer.log.dataLog(PeerToPeer.peerId + " --> " + pId + " MESSAGE: Sent a BITFIELD message to Peer " + pId);

        // write back to the socket and console output
        push_Output(MessageDetails.transformMsgToB_Array(new MessageDetails(+5, PeerToPeer.currentDataPayLoad.transformData())),
                socket);
    }

    // function used to display the console output and write to output stream based
    // on Meassage type
    public void push_ChokeMessage(String pId, Socket socket) {
        // Console log the information obtained from the peers
        PeerToPeer.log.dataLog(PeerToPeer.peerId + " --> " + pId + " MESSAGE: Sent a CHOKE message to Peer " + pId);

        // write back to the socket and console output
        push_Output(MessageDetails.transformMsgToB_Array(new MessageDetails(0)), socket);
    }

    // function used to display the console output and write to output stream based
    // on Meassage type
    public void push_UnChokeMessage(String pId, Socket socket) {
        // Console log the information obtained from the peers
        PeerToPeer.log.dataLog(PeerToPeer.peerId + " --> " + pId + " MESSAGE: Sent a UNCHOKE message to Peer " + pId);

        // write back to the socket and console output
        push_Output(MessageDetails.transformMsgToB_Array(new MessageDetails(1)), socket);
    }

    // function to write the data to the peer data file
    public void msgBytes_Transfer(Socket socket, MessageDetails requestMessage, String pId) {
        // Parameters to read the Data and write to the peerfile Data file
        byte[] byte_arr = requestMessage.fetch_PLArr();
        int piece_Index = Parameters.encodeBytetoInt(byte_arr, 0);
        byte[] byte_ToRead = new byte[Parameters.pieceSize];
        int readBytes = 0;

        // File to be pushed to the data
        File msg_PeerFile = new File(PeerToPeer.peerId, Parameters.fileName);

        // Console log the information obtained from the peers
        PeerToPeer.log.dataLog(PeerToPeer.peerId + " --> " + pId + " MESSAGE: Sending PIECE " + piece_Index + " to Peer " + pId);

        try {
            // push the contents to the created file
            rf = new RandomAccessFile(msg_PeerFile, "r");
            rf.seek((long) piece_Index * Parameters.pieceSize);
            readBytes = rf.read(byte_ToRead, 0, Parameters.pieceSize);
        } catch (Exception ex) {
            PeerToPeer.log.dataLog(PeerToPeer.peerId + " has error in reading the file: " + ex.toString());
        }

        byte[] buffbytes = new byte[readBytes + 4];
        System.arraycopy(byte_arr, 0, buffbytes, 0, 4);
        System.arraycopy(byte_ToRead, 0, buffbytes, 4, readBytes);

        // write back to the socket and console output
        push_Output(MessageDetails.transformMsgToB_Array(new MessageDetails(7, buffbytes)), socket);

        try {
            rf.close();
        } catch (Exception ignored) {
        }
    }

    // function to send the data to the peer data file
    public void push_Request(int pNo, Socket socket) {
        // byte array for copying the data
        byte[] byte_arr = new byte[4];

        // writing the contents to 0
        for (int i = 0; i < 4; i++)
            byte_arr[i] = 0;

        // copy the contents
        byte[] piece_arr = Parameters.encodeIntToByte(pNo);
        System.arraycopy(piece_arr, 0, byte_arr, 0, piece_arr.length);

        // write back to the socket and console output
        push_Output(MessageDetails.transformMsgToB_Array(new MessageDetails(6, byte_arr)), socket);
    }

    // write back to the socket and console output
    public void push_Output(byte[] encodedBitField, Socket socket) {
        // Use the Output stream to write the data to the socket of the peer
        try {
            OutputStream op = socket.getOutputStream();
            op.write(encodedBitField);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());

        }
    }

    // function to send HAVE message the data to the peer data file
    public void push_HaveMessage(String pId, Socket socket) {
        // Console log the information obtained from the peers
        PeerToPeer.log.dataLog(PeerToPeer.peerId + " sent a HAVE message to Peer " + pId);

        // write back to the socket and console output
        push_Output(MessageDetails.transformMsgToB_Array(new MessageDetails(4, PeerToPeer.currentDataPayLoad.transformData())),
                socket);
    }

}
