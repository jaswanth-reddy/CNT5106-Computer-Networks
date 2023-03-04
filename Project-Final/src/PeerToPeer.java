import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class PeerToPeer {

    // variables to perform peer2peer communication
    public static HashMap<String, remPeerDetails> rmPeerHashMap = new HashMap<>();
    public static HashMap<String, remPeerDetails> preferredNeighbours = new HashMap<>();

    // Logger to keep traack of actions
    static LogController log;
    public static volatile Timer timer;
    public static MessagePayLoad currentDataPayLoad = null;

    // Port with which peer is listening
    static int clientPort;
    public static ServerSocket socket = null;
    public static Thread thread;
    public static String peerId;
    public static boolean finishedFlag = false;
    public static Queue<MessageProperties> queue = new LinkedList<>();

    // Threads to work on the individual pieces of the data
    public static Vector<Thread> st = new Vector<>();
    public static Thread mp;
    public static Vector<Thread> pt = new Vector<>();
    public static HashMap<String, Socket> peerData = new HashMap<>();
    public static volatile Hashtable<String, remPeerDetails> preferredNeighboursHashMapTable = new Hashtable<>();
    public static volatile Hashtable<String, remPeerDetails> unchokedNeighboursHashMapTable = new Hashtable<>();

    public static void main(String args[]) throws Exception {

        // Enter the PeerID to work on
        peerId = args[0]; // 1001, 1002, 1003, 1004
        // Logger to store the actions for peer
        log = new LogController("Peer_" + peerId + ".log");
        boolean flag = false;

        // Class to initialize the peer config.
        Initialize init_Config;

        try {

            log.dataLog(peerId + " is started");

            // initialize object to configure the peer details
            init_Config = new Initialize();

            // fetch the details from the commonConfig.cfg file
            init_Config.init_ConfigData(log);

            // fetch the details from the PeerInfo.cfg file
            // updates rmPeerHashMap
            init_Config.init_PeerInfoDate(rmPeerHashMap, log);

            // Fetches the preferredNeighboursInfo using the remotePeerInfo from
            // PeerConfifInfo.cfg
            init_Config.set_PreferredNeighbours(peerId, rmPeerHashMap, preferredNeighbours);

            // Fetch the port number for the designated client
            // init_Config.init_Port(rmPeerHashMap, peerId, clientPort, flag);
            x: for (Map.Entry<String, remPeerDetails> hm : rmPeerHashMap.entrySet()) {

                remPeerDetails r = hm.getValue();
                if (r.peerId.equals(peerId)) {
                    clientPort = Integer.parseInt(r.peerPort);

                    if (r.hasFile) {
                        flag = true;
                        break x;
                    }
                }
            }

            currentDataPayLoad = new MessagePayLoad();
            currentDataPayLoad.initPayLoad(peerId, flag);

            // Create a thread associated to the peer ID
            Thread t = new Thread(new DataModulator(peerId));
            t.start();

            // create a socket based on the port details of the peer ID
            init_Config.init_Socket(flag, peerId, thread, rmPeerHashMap, init_Config, clientPort, pt, log);

            // creating the preferred neighbours
            init_PreferredNeighbours();
            // creating the unchoked neighbours
            initUnchokedNeighbours();

            Thread cThread = thread;
            Thread mp = t;

            while (true) {
                finishedFlag = isFinished();

                // checking if the file is transferred to peers or not
                if (finishedFlag) {
                    log.dataLog("All peers have completed downloading the file.");

                    terminatePreferredNeighbors();
                    terminateUnchokedNeighbors();

                    try {
                        Thread.currentThread();
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {
                    }

                    // if (cThread.isAlive())
                    // cThread.interrupt();

                    // if (mp.isAlive())
                    // mp.interrupt();

                    // for (Thread thread : pt)
                    // if (thread.isAlive())
                    // thread.interrupt();

                    // for (Thread thread : st)
                    // if (thread.isAlive())
                    // thread.interrupt();

                    break;
                }
                // Else make the thread wait for 5 seconds until it gets a request form the
                // peers
                else {
                    try {
                        Thread.currentThread();
                        Thread.sleep(5000);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        } catch (Exception exception) {
            log.dataLog(String.format(peerId + " is ending with error : " + exception.getMessage()));
        } finally {
            log.dataLog(String.format(peerId + " Peer is terimating."));
            log.closeLog();
            System.exit(0);
        }
    }

    public static synchronized MessageProperties removeDataFromQueue() {
        MessageProperties dp = null;
        if (queue.isEmpty()) {
        } else {
            dp = queue.remove();
        }
        return dp;
    }

    public static synchronized void addToQueue(MessageProperties dp) {
        queue.add(dp);
    }

    public static void sndReq2Unchoke(String remotePeerID, Socket socket) {
        log.dataLog(peerId + " --> " + remotePeerID + " MESSAGE: Sending UNCHOKE message to Peer");

        displayResult(MessageDetails.transformMsgToB_Array(new MessageDetails(1)), socket);
    }

    public static void sndHaveMsg(String remotePeerID, Socket socket) {
        byte[] b = PeerToPeer.currentDataPayLoad.transformData();
        log.dataLog(peerId + " --> " + remotePeerID + " MESSAGE: Sending HAVE message to Peer");

        displayResult(MessageDetails.transformMsgToB_Array(new MessageDetails(4, b)), socket);
    }

    public static void fetch_NxtPeerDetails() {
        try {
            String pInfo;

            BufferedReader br = new BufferedReader(new FileReader("PeerInfo.cfg"));

            while ((pInfo = br.readLine()) != null) {
                String[] p = pInfo.trim().split(" ");
                String peerID = p[0];

                if (Integer.parseInt(p[3]) == 1) {
                    rmPeerHashMap.get(peerID).isCompleted = 1;
                    rmPeerHashMap.get(peerID).isInterested = 0;
                    rmPeerHashMap.get(peerID).isChoked = 0;
                }
            }
            br.close();
        } catch (Exception exception) {
            log.dataLog(peerId + "" + exception.toString());
        }
    }

    public static synchronized boolean isFinished() {
        String peerDetail;
        int Count = 1;

        try {
            // BufferedReader bufferedReader = new BufferedReader(new
            // FileReader(Parameters.PEERS_PATH));
            BufferedReader bufferedReader = new BufferedReader(new FileReader("PeerInfo.cfg"));
            while ((peerDetail = bufferedReader.readLine()) != null) {
                Count = Count * Integer.parseInt(peerDetail.trim().split(" ")[3]);
            }
            bufferedReader.close();
            return Count != 0;
        } catch (Exception e) {
            log.dataLog(e.toString());
            return false;
        }
    }

    public static void initUnchokedNeighbours() {
        timer = new Timer();
        timer.schedule(new initUnchokedNeighbours(),
                0, Parameters.optimisticUnchokingInterval * 1000L);
    }

    public static void terminateUnchokedNeighbors() {
        timer.cancel();
    }

    public static void init_PreferredNeighbours() {
        timer = new Timer();
        timer.schedule(new InitPreferredNeighbours(),
                0, Parameters.unchokingInterval * 1000L);
    }

    public static void terminatePreferredNeighbors() {
        timer.cancel();
    }

    private static void displayResult(byte[] b, Socket socket) {
        try {
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(b);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}