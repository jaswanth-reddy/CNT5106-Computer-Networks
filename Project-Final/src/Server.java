import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable
{
    private final ServerSocket socket;
    private final String peerId;
    Socket remoteSocket;
    Thread thread;
    public Server(ServerSocket s,String peerId)
    {
        this.socket=s;
        this.peerId=peerId;
    }
    public void run()
    {
        while(true)
        {
            try
            {
                remoteSocket=socket.accept();
                thread=new Thread(new PeerModulator(this.peerId,remoteSocket,0));
                PeerToPeer.st.add(thread);
                thread.start();
            }
            catch (Exception ex)
            {
                PeerToPeer.log.dataLog(this.peerId+" an Exception when trying to establish a connection");
            }
        }
    }
}
