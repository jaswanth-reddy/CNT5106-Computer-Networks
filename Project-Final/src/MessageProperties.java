public class MessageProperties {
    public MessageDetails fetch_M() {
        return m;
    }

    public void init_M(MessageDetails m) {
        this.m = m;
    }

    public String fetch_peerID() {
        return peer_Id;
    }

    public void init_peerID(String peer_Id) {
        this.peer_Id = peer_Id;
    }

    MessageDetails m;
    String peer_Id;

    MessageProperties() {
        m = new MessageDetails();
        peer_Id = null;
    }

}