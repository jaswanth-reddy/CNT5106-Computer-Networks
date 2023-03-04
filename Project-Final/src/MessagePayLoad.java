import java.io.*;

// Class MessagePayLoad to capture info related to 
public class MessagePayLoad {

    public MessagePiecePayLoad[] msgpiece_Payload;
    public int bitSize;
    public static LogController l;

    // Initialize the Messager payload details as per the file to be transferred
    MessagePayLoad() {
        double d = (double) Parameters.fileSize / Parameters.pieceSize;
        this.bitSize = (int) Math.ceil(d);
        this.msgpiece_Payload = new MessagePiecePayLoad[this.bitSize];
        int i = 0;
        while (i < this.bitSize) {
            this.msgpiece_Payload[i++] = new MessagePiecePayLoad();
        }
    }

    // function to extract the data from the given byte array
    public static MessagePayLoad extractData(byte[] b) {
        MessagePayLoad msgPayload = new MessagePayLoad();
        int len_cnt = 0;

        while (len_cnt < b.length) {
            int index_cnt = 7;
            while (index_cnt >= 0) {
                // left shifting the index counter to read the other values
                int pack_cnt = 1 << index_cnt;
                int payload_Size = len_cnt * 8 + (8 - index_cnt - 1);

                // when the evaluated payload size is less than the given message payload size
                if (payload_Size < msgPayload.bitSize) {
                    if ((b[len_cnt] & (pack_cnt)) != 0) {
                        // setting the bit to 1 when found
                        msgPayload.msgpiece_Payload[payload_Size].contains_Piece = 1;
                    } else {
                        // setting the bit to zero
                        msgPayload.msgpiece_Payload[payload_Size].contains_Piece = 0;
                    }
                }
                index_cnt--;
            }
            len_cnt++;
        }
        return msgPayload;
    }

    // fucntion to validate if all the pieces are available or not
    public boolean containsEveryPiece() {
        // iterating over all the bitsizes and checking if any piece is missing
        for (int i = 0; i < this.bitSize; i++) {
            if (this.msgpiece_Payload[i].contains_Piece == 0) {
                // when found a missing piece then return false
                return false;
            }
        }
        return true;

    }

    // intialize the payload for the given peer id
    public void initPayLoad(String pId, boolean hasFile) {
        int size_cnt = 0;

        // if peer has the file requested
        if (hasFile) {
            // iterate over all the size count and update till it can fit in
            while (size_cnt < bitSize) {
                this.msgpiece_Payload[size_cnt].init_ContainsPiece(1);
                this.msgpiece_Payload[size_cnt].init_Communicator_pId(pId);
                size_cnt++;
            }
        }
        // whent the peer has no file with it
        else {
            // iterate over all the size count and update till it can fit in
            while (size_cnt < bitSize) {
                this.msgpiece_Payload[size_cnt].init_ContainsPiece(0);
                this.msgpiece_Payload[size_cnt].init_Communicator_pId(pId);
                size_cnt++;
            }
        }
    }




    public MessagePiecePayLoad[] getMsgpiece_Payload() {
        return msgpiece_Payload;
    }


        // function to transform the bits data byte array
    public byte[] transformData() {
        int arr_size = 0;
        int bcnt = 8;

        // count the number of bits which are not zero
        if (this.bitSize % bcnt != 0) {
            arr_size = arr_size + 1;
        }
        arr_size += this.bitSize / bcnt;

        // create a byte array with the size of the packets which has data
        byte[] byte_arr = new byte[arr_size];
        int val = 0;
        int arr_index = 0;
        int size_cnt = 1;

        while (size_cnt <= this.bitSize) {
            int t1 = this.msgpiece_Payload[size_cnt - 1].contains_Piece;

            // performing left shift operation
            val = val << 1;
            if (t1 == 1) {
                val++;
            }
            if (size_cnt % bcnt == 0) {
                byte_arr[arr_index] = (byte) val;
                arr_index++;
                val = 0;
            }
            size_cnt++;
        }

        size_cnt--;
        if (size_cnt % bcnt != 0) {
            int shift_cnt = this.bitSize - (this.bitSize / bcnt) * bcnt;
            val <<= (bcnt - shift_cnt);
            byte_arr[arr_index] = (byte) val;
        }

        return byte_arr;
    }



    // identify how many message pieces that are available
    public int avaliablePieces() {
        int msg_pieceCnt = 0;

        // iterating over all the sizes to count the msg pieces
        for (int i = 0; i < this.bitSize; i++) {
            // update the counter when found
            if (this.msgpiece_Payload[i].contains_Piece == 1) {
                msg_pieceCnt += 1;
            }
        }
        return msg_pieceCnt;
    }

    // Simultaneously update the payload information
    public synchronized void revisePayLoad(MessagePiecePayLoad msgpiece_Payload, String pId) {
        // update the info related message piece based on its availability
        if (PeerToPeer.currentDataPayLoad.msgpiece_Payload[msgpiece_Payload.piece_Index].contains_Piece == 1) {
            PeerToPeer.log.dataLog(pId + " This piece already exists");
        }

        // when the piece is not available and needs to update the information of piece
        else {
            try {
                byte[] writeData_arr;
                int mesgFile_offset = msgpiece_Payload.piece_Index * Parameters.pieceSize;

                // creating the file to write the contents recieved from the other peers
                File peerFileLoad = new File(PeerToPeer.peerId, Parameters.fileName);
                RandomAccessFile peerFileAccess = new RandomAccessFile(peerFileLoad, "rw");

                writeData_arr = msgpiece_Payload.file_Piece;

                // navigate to the offset of the access to file and push the contents to it
                peerFileAccess.seek(mesgFile_offset);
                peerFileAccess.write(writeData_arr);
                peerFileAccess.close();

                this.msgpiece_Payload[msgpiece_Payload.piece_Index].init_ContainsPiece(1);
                this.msgpiece_Payload[msgpiece_Payload.piece_Index].init_Communicator_pId(pId);
                PeerToPeer.log.dataLog(
                        PeerToPeer.peerId + " Peer has obtained the piece " + msgpiece_Payload.piece_Index + " from the Peer "
                                + pId
                                + ". Now it has " + PeerToPeer.currentDataPayLoad.avaliablePieces() + " pieces");

                if (PeerToPeer.currentDataPayLoad.containsEveryPiece()) {
                    // update the payload options of based on the condition that it has received all
                    // pieces of message
                    PeerToPeer.rmPeerHashMap.get(PeerToPeer.peerId).isInterested = 0;
                    PeerToPeer.rmPeerHashMap.get(PeerToPeer.peerId).isCompleted = 1;
                    PeerToPeer.rmPeerHashMap.get(PeerToPeer.peerId).isChoked = 0;

                    updatePeerConfig(PeerToPeer.peerId);
                    PeerToPeer.log.dataLog(PeerToPeer.peerId + " MESSAGE: Has finishes downloading the file...");
                    PeerToPeer.log.dataLog(PeerToPeer.peerId + " MESSAGE: Is now sending NOT INTERESTED MESSAGE");
                }
            } catch (Exception ex) {
                PeerToPeer.log.dataLog(ex.getMessage());
            }
        }
    }

    // Function to revise the configurations to the peer ID
    public void updatePeerConfig(String pId) {
        BufferedReader buffered_Read;
        BufferedWriter buffered_Write;

        String temp_PeerConfigLine = "";
        String PeerConfigLine;

        try {
            // reading the peer config file that need changes
            buffered_Read = new BufferedReader(new FileReader("PeerInfo.cfg"));

            while ((PeerConfigLine = buffered_Read.readLine()) != null) {
                // using the regex for splitting the line to parts and trimming
                String[] split_arr = PeerConfigLine.trim().split(" ");

                if (split_arr[0].equals(pId)) {
                    // upon receiving all the pieces then update the received flag to 1
                    split_arr[3] = "1";

                    // concatinate all the array items to make as is in peerinfo.cfg
                    PeerConfigLine = split_arr[0] + " " + split_arr[1] + " " + split_arr[2] + " " + split_arr[3];
                }
                temp_PeerConfigLine += PeerConfigLine + "\n";
            }
            buffered_Read.close();

            // write back to the same config file
            buffered_Write = new BufferedWriter(new FileWriter("PeerInfo.cfg"));
            buffered_Write.write(temp_PeerConfigLine);

            // close the stream sessions
            buffered_Write.close();

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }



    // function to init the PieceData
    public void init_DPiece(MessagePiecePayLoad[] dPiece) {
        this.msgpiece_Payload = dPiece;
    }

    // Function to check the payload data synchronously across all the pieces
    public synchronized boolean equate_dPayload(MessagePayLoad msgPayLoad) {
        // getting the Bit size
        int bitSize = msgPayLoad.fetch_BitSize();
        int i = 0;

        // iterate over all the bitsizes
        while (i < bitSize) {
            boolean msgHaspiece = msgPayLoad.getMsgpiece_Payload()[i].fetch_ContainsPiece() == 1;
            boolean curMsgHaspiece = this.getMsgpiece_Payload()[i].fetch_ContainsPiece() == 0;
            boolean equate_condition = msgHaspiece && curMsgHaspiece;

            if (equate_condition) {
                return true;
            }

            i = i + 1;
        }
        return false;
    }

    // Simultaneoulsy getting the bit field for the message payload given to the
    // node
    public synchronized int fetch_FstBitField(MessagePayLoad msgPayload) {
        // conditions for the bit size when greater than the given payload size
        if (this.fetch_BitSize() >= msgPayload.fetch_BitSize()) {
            int i = 0;
            while (i < msgPayload.fetch_BitSize()) {
                boolean msgPayloadPiece = msgPayload.getMsgpiece_Payload()[i].fetch_ContainsPiece() == 1;
                boolean curMsgPayload = this.getMsgpiece_Payload()[i].fetch_ContainsPiece() == 0;
                boolean condition = msgPayloadPiece && curMsgPayload;
                if (condition) {
                    return i;
                }
                i = i + 1;
            }
        }
        // conditions for the bit size when less than the given payload size
        else {
            int i = 0;
            while (i < this.fetch_BitSize()) {
                boolean msgPayloadPiece = msgPayload.getMsgpiece_Payload()[i].fetch_ContainsPiece() == 1;
                boolean curMsgPayload = this.getMsgpiece_Payload()[i].fetch_ContainsPiece() == 0;
                boolean condition = msgPayloadPiece && curMsgPayload;

                if (condition) {
                    return i;
                }
                i = i + 1;
            }
        }
        return -1;
    }

    // function to fetch the bit size
    public int fetch_BitSize() {
        return bitSize;
    }

    // function to init the bit size
    public void init_BitSize(int bitSize) {
        this.bitSize = bitSize;
    }

}
