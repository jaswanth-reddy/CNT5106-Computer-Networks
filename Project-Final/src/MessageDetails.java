import java.nio.charset.StandardCharsets;

public class MessageDetails {

    private byte[] msgArr;
    private byte[] msgLenArr;
    private byte[] plArr;
    private String msgLen;
    private String msgType;
    private int lenMsg = 1;

    MessageDetails() {

    }

    MessageDetails(int n) {
        try {
            if ((n == 0) || (n == 1) || (n == 2) || (n == 3)) {
                this.init_MsgType("" + n);
                this.plArr = null;
                this.lenMsg = 1;
                this.msgLen = this.lenMsg + "";
                this.msgLenArr = Parameters.encodeIntToByte(this.lenMsg);
            } else {
                System.out.println("Message seems to be invalid");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    MessageDetails(int n, byte[] arr) {
        try {
            if (arr == null) {
                if ((n == 0) || (n == 1) || (n == 2) || (n == 3)) {
                    this.lenMsg = 1;
                    this.msgLen = this.lenMsg + "";
                    this.msgLenArr = Parameters.encodeIntToByte(this.lenMsg);
                    this.plArr = null;
                } else {
                    System.out.println("Payload is empty");
                }

            } else {
                this.lenMsg = arr.length + 1;
                this.msgLen = this.lenMsg + "";
                this.msgLenArr = Parameters.encodeIntToByte(this.lenMsg);
                if (this.msgLenArr.length > 4) {
                    System.out.println("Length of message must be greater than size ");
                }
                this.plArr = arr;
            }
            this.init_MsgType("" + n);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public int fetch_Msg_Length() {
        return lenMsg;
    }

    public void init_Msg_Length(int lenMsg) {
        this.lenMsg = lenMsg;
    }

    public byte[] fetch_DataLenArr() {
        return msgLenArr;
    }

    public void init_DataLenArr(byte[] msgLenArr) {
        this.msgLenArr = msgLenArr;
    }

    public byte[] fetch_DataArr() {
        return msgArr;
    }

    public void init_DataArr(byte[] msgArr) {
        this.msgArr = msgArr;
    }

    public String fetch_MsgType() {
        return msgType;
    }

    public void init_MsgType(byte[] data) {
        this.msgType = new String(data, StandardCharsets.UTF_8);
        this.msgArr = data;
    }

    public void init_MsgType(String data) {
        this.msgType = data.trim();
        this.msgArr = this.msgType.getBytes(StandardCharsets.UTF_8);
    }

    public String fetch_MsgLen() {
        return msgLen;
    }

    public void init_MsgLen(byte[] b) {
        int l = Parameters.encodeBytetoInt(b, 0);
        this.msgLen = "" + l;
        this.msgLenArr = b;
        this.lenMsg = l;
    }

    public void init_MsgLen(String data) {
        this.lenMsg = Integer.parseInt(data);
        this.msgLen = data;
        this.msgLenArr = Parameters.encodeIntToByte(this.lenMsg);
    }

    public byte[] fetch_PLArr() {
        return plArr;
    }

    public void init_PLArr(byte[] plArr) {
        this.plArr = plArr;
    }

    public static byte[] transformMsgToB_Array(MessageDetails m) {
        byte[] dataByteArray;
        int dType;
        try {
            dType = Integer.parseInt(m.fetch_MsgType());
            if ((m.fetch_DataArr() == null) || ((dType < 0) || dType > 7) || (m.fetch_DataLenArr().length > 4)
                    || (m.fetch_DataLenArr() == null)) {
                throw new Exception("Message is Not Valid");
            }
            if (m.fetch_PLArr() == null) {
                dataByteArray = new byte[4 + 1];
                System.arraycopy(m.fetch_DataLenArr(), 0, dataByteArray, 0, m.fetch_DataLenArr().length);
                System.arraycopy(m.fetch_DataArr(), 0, dataByteArray, 4, 1);
            } else {
                dataByteArray = new byte[4 + 1 + m.fetch_PLArr().length];
                System.arraycopy(m.fetch_DataLenArr(), 0, dataByteArray, 0, m.fetch_DataLenArr().length);
                System.arraycopy(m.fetch_DataArr(), 0, dataByteArray, 4, 1);
                System.arraycopy(m.fetch_PLArr(), 0, dataByteArray, 4 + 1, m.fetch_PLArr().length);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            dataByteArray = null;
        }
        return dataByteArray;
    }

    public static MessageDetails convertByteArrayToData(byte[] b) {
        MessageDetails m = new MessageDetails();
        byte[] dl = new byte[4];
        byte[] dType = new byte[1];
        byte[] payload;
        int mlen;
        try {
            if (b.length < 4 + 1 || b == null) {
                throw new Exception("Message is not valid");
            }
            System.arraycopy(b, 0, dl, 0, 4);
            System.arraycopy(b, 4, dType, 0, 1);
            m.init_MsgLen(dl);
            m.init_MsgType(dType);
            mlen = Parameters.encodeBytetoInt(dl, 0);
            if (mlen > 1) {
                payload = new byte[mlen - 1];
                System.arraycopy(b, 4 + 1, payload, 0, b.length - 4 - 1);
                m.init_PLArr(payload);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());

        }
        return m;
    }
}
