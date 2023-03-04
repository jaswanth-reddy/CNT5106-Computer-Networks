import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

public class Handshake extends Parameters
{

    private byte[] handShakeMsg;
    private int peerId;
    private int m;
    private String handShakeHdr;
    private String zeroBits;
    

    public byte[] fetch_HandShakeMsg() {
        return handShakeMsg;
    }

    public void init_HandShakeMsg(byte[] handShakeMsg) {
        this.handShakeMsg = handShakeMsg;
    }

    public int fetch_PeerID() {
        return peerId;
    }

    public void init_PeerID(int peerId) {
        this.peerId = peerId;
    }

    public String fetch_HandShakeHdr() {
        return handShakeHdr;
    }

    public void init_HandShakeHdr(String handShakeHdr) {
        this.handShakeHdr = handShakeHdr;
    }

    public String fetch_ZeroBits() {
        return zeroBits;
    }

    public void init_ZeroBits(String zeroBits) {
        this.zeroBits = zeroBits;
    }

    @Override
    public String toString() {
        return "Handshake { handShakeMsg=" + Arrays.toString(handShakeMsg) +'}';
    }

    

    public byte[] fetch_HSHeaderBytes() {
        return hSHeaderByt;
    }

    public void init_HSHeaderBytes(byte[] hSHeaderByt) {
        this.hSHeaderByt = hSHeaderByt;
    }

    private byte[] hSHeaderByt=new byte[32];
    Handshake()
    {

    }

    Handshake(int peerId)
    {
        this.handShakeMsg=new byte[32];
        this.peerId=peerId;
        this.handShakeHdr="P2PFILESHARINGPROJ"; 
        this.zeroBits="0000000000";
        this.m=0;
        this.hSHeaderByt=handShakeHdr.getBytes(StandardCharsets.UTF_8);
    }

    public void initiateHandShake()
    {
        byte[] hSHeaderArr=this.handShakeHdr.getBytes();
        byte[] zeroBitsByteArray=this.zeroBits.getBytes(StandardCharsets.UTF_8);
        String peer_ID=this.peerId+"";
        byte[] peer_IDArr=peer_ID.getBytes(StandardCharsets.UTF_8);
        int m=0;
        try
        {
            init_HSMessage(hSHeaderArr);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }
        try
        {
            init_HSMsgPd(zeroBitsByteArray);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }
        try
        {
            init_HSMsgPId(peer_IDArr);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
        }
        System.out.println("Hand Shake message generated is : "+ new String(this.handShakeMsg, StandardCharsets.UTF_8));
    }


    public void init_HSMessage(byte[] hSHeaderArr)
    {
        try
        {
            if(hSHeaderArr==null )
            {
                throw new Exception("Please define valid Hand Shake Header");
            }
            if(hSHeaderArr.length>18 )
            {
                throw new Exception(" Hand Shake Header length is greater than 18 bytes");
            }

            for (int i = 0; i < hSHeaderArr.length; i++)
            {
                this.handShakeMsg[m] = hSHeaderArr[i];
                m++;
            }

        }

        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    public void init_HSMsgPd(byte[] zeroBitsByteArray)
    {
        try
        {
            if (zeroBitsByteArray == null)
            {
                throw new Exception("Please define valid Zero bit padding");
            }
            if(zeroBitsByteArray.length>10)
            {
               throw new Exception("Zero bit padding length is greater than 10");
            }
            for (int i = 0; i < zeroBitsByteArray.length; i++)
            {
                this.handShakeMsg[m] = zeroBitsByteArray[i];
                m++;
            }
        }
         catch (Exception e)
        {
        e.printStackTrace();
        System.out.println(e.getMessage());
        }

    }


    public void init_HSMsgPId(byte[] peer_IDArr)
    {
        try
        {
            if (peer_IDArr == null)
            {
                throw new Exception("Please define valid PeerId");
            }
            if (peer_IDArr.length > 4)
            {
                throw new Exception("Zero bit padding length is greater than 10");
            }

            for (int i = 0; i < peer_IDArr.length; i++) {
                this.handShakeMsg[m] = peer_IDArr[i];
                m++;
            }
        }
         catch (Exception e)
        {
        e.printStackTrace();
        System.out.println(e.getMessage());
        }

    }
    public static Handshake byteToHandShake(byte[] b)
    {

        byte[] mheader;
        byte[] mpeerId;
        Handshake h;
        if(b.length!=32)
        {
            PeerToPeer.log.dataLog("INVALID length of HandShake Message");
            System.exit(0);
        }
        h=new Handshake();
        mheader=new byte[18];
        mpeerId=new byte[4];
        System.arraycopy(b,0,mheader,0,18);
        System.arraycopy(b,18+10,mpeerId,0,4);
        h.init_HSMessage(mheader);
        h.init_HSMsgPId(mpeerId);
        return h;
    }
    public  static byte[] handShakeToArray(Handshake handshake)
    {
        byte[] m=new byte[32];


            if(handshake.fetch_HandShakeHdr().length()>18||handshake.fetch_HandShakeHdr()==null||handshake.fetch_HandShakeHdr().length()==0)
            {
                PeerToPeer.log.dataLog("HandShake header not VALID");
                System.exit(0);
            }
            else
            {
                System.arraycopy(handshake.fetch_HandShakeHdr().getBytes(StandardCharsets.UTF_8),0,m,0,handshake.fetch_HandShakeHdr().length());
            }
        if(handshake.fetch_ZeroBits()==null || handshake.fetch_ZeroBits().isEmpty()||handshake.fetch_ZeroBits().length()>10)
        {
            PeerToPeer.log.dataLog("INVALID Zero bits");
            System.exit(0);
        }
        else
        {
            System.arraycopy(handshake.fetch_ZeroBits().getBytes(StandardCharsets.UTF_8),0,m,18,10);


        }
        if( (String.valueOf(handshake.fetch_PeerID())).length()>4)
        {
            PeerToPeer.log.dataLog("INVALID Peer bits");
            System.exit(0);
        }
        else
        {
           System.arraycopy(String.valueOf(handshake.fetch_PeerID()).getBytes(StandardCharsets.UTF_8),0,m,18+10,4);
        }
        return m;
    }

}
