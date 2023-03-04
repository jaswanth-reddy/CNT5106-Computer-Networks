
class MessagePiecePayLoad {
    public int contains_Piece, piece_Index;
    public String communicator_pId;
    public byte[] file_Piece;

    // Default constructor
    public MessagePiecePayLoad() 
    {
        // intialize the variables to values
        file_Piece = new byte[Parameters.pieceSize];
        piece_Index = -1;
        contains_Piece = 0;
        communicator_pId = null;
    }

    // fucntion to obtain the piece contents
    public int fetch_ContainsPiece() {
        return contains_Piece;
    }

    // function to initialize the piece contents
    public void init_ContainsPiece(int piece) {
        this.contains_Piece = piece;
    }

    // function to obtain the communicator peer id for logging purpose
    public String fetch_Communicator_pId() {
        return communicator_pId;
    }

    // fucntion to initialize the peer id which is sending the pieces of data
    public void init_Communicator_pId(String senderpId) {
        this.communicator_pId = senderpId;
    }

    // statuc block to class
    public static MessagePiecePayLoad convertToPiece(byte[] data) 
    {
        //Number of peers
        int arr_Size = 4;

        MessagePiecePayLoad msgPiecePayLoad = new MessagePiecePayLoad();
        byte[] data_arr = new byte[arr_Size];

        //copy the received piece of information
        System.arraycopy(data, 0, data_arr, 0, arr_Size);

        
        msgPiecePayLoad.piece_Index = Parameters.encodeBytetoInt(data_arr, 0);
        msgPiecePayLoad.file_Piece = new byte[data.length - arr_Size];
        System.arraycopy(data, arr_Size, msgPiecePayLoad.file_Piece, 0, data.length - arr_Size);
        return msgPiecePayLoad;
    }

}