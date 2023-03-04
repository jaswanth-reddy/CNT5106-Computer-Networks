import java.io.BufferedReader;
import java.net.ServerSocket;
import java.io.*;
import java.util.*;

public class Initialize {

    Initialize() {

    }

    // fetch the details from the commonConfig.cfg file
    public void init_ConfigData(LogController log) throws IOException {
        String configs;
        BufferedReader b = null;
        try {
            // b=new BufferedReader(new FileReader(Parameters.COMMON_CONFIG_PATH));

            b = new BufferedReader(new FileReader("CommonConfig.cfg"));

            while ((configs = b.readLine()) != null) {
                String[] line = configs.split(" ");

                if (line[0].trim().equals("NumberOfPreferredNeighbors")) {
                    Parameters.numberOfPreferredNeighbors = Integer.parseInt(line[1]);
                }
                if (line[0].trim().equals("UnchokingInterval")) {
                    Parameters.unchokingInterval = Integer.parseInt(line[1]);
                }
                if (line[0].trim().equals("OptimisticUnchokingInterval")) {
                    Parameters.optimisticUnchokingInterval = Integer.parseInt(line[1]);
                }
                if (line[0].trim().equals("FileName")) {
                    Parameters.fileName = line[1];
                }
                if (line[0].trim().equals("FileSize")) {
                    Parameters.fileSize = Integer.parseInt(line[1]);
                }
                if (line[0].trim().equals("PieceSize")) {
                    Parameters.pieceSize = Integer.parseInt(line[1]);
                }
            }
        } catch (Exception ex) {
            log.dataLog(ex.getMessage());
        } finally {
            b.close();
        }
    }

    // fetch the details from the PeerInfo.cfg file
    // updates rmPeerHashMap
    public void init_PeerInfoDate(HashMap<String, remPeerDetails> rmPeerHashMap, LogController log)
            throws IOException {
        String configs;
        BufferedReader b = null;

        try {

            // b=new BufferedReader(new FileReader(Parameters.PEERS_PATH));
            b = new BufferedReader(new FileReader("PeerInfo.cfg"));
            while ((configs = b.readLine()) != null) {
                String[] line = configs.split(" ");
                rmPeerHashMap.put(line[0], new remPeerDetails(line[0], line[1], line[2], line[3].equals("1")));

            }
        } catch (Exception ex) {
            log.dataLog(ex.getMessage());
        } finally {
            b.close();
        }
    }

    // Fetches the preferredNeighboursInfo using the remotePeerInfo from
    // PeerConfifInfo.cfg
    public void set_PreferredNeighbours(String pId, HashMap<String, remPeerDetails> rmPeerHashMap,
            HashMap<String, remPeerDetails> preferredNeighbours) {
        for (Map.Entry<String, remPeerDetails> hm : rmPeerHashMap.entrySet()) {
            if (!hm.getKey().equals(pId)) {
                preferredNeighbours.put(hm.getKey(), hm.getValue());
            }
        }
    }

    // When the peer id is not available in the peerinfo.cfg, flag return false
    // customly writing the peerfile
    public void init_PeerFile(String peerId, LogController log) {
        try {
            File f = new File(peerId, Parameters.fileName);
            OutputStream fop = new FileOutputStream(f, true);
            byte intialByte = 0;
            int i = 0;
            while (i < Parameters.fileSize) {
                fop.write(intialByte);
                i++;
            }
            fop.close();

        } catch (Exception e) {
            log.dataLog("Error while creating intial dummy file for peer " + peerId);
        }
    }

    // create socket based on the peer info provided
    public void init_Socket(boolean flag, String peerId, Thread thread,
            HashMap<String, remPeerDetails> rmPeerHashMap, Initialize init_Config, int clientPort,
            Vector<Thread> pt, LogController log) {

        if (flag) {

            try {
                PeerToPeer.socket = new ServerSocket(clientPort);
                thread = new Thread(new Server(PeerToPeer.socket, peerId));
                thread.start();
            } catch (Exception ex) {
                log.dataLog(peerId + " peer is getting an exception while starting the thread");
                log.closeLog();
                System.exit(0);
            }
        } else {
            init_Config.init_PeerFile(peerId, log);

            try {

                for (Map.Entry<String, remPeerDetails> hm : rmPeerHashMap.entrySet()) {
                    remPeerDetails remotePeerInfo = hm.getValue();
                    if (Integer.parseInt(peerId) > Integer.parseInt(hm.getKey())) {
                        PeerModulator p = new PeerModulator(remotePeerInfo.fetch_PeerAddress(),
                                Integer.parseInt(remotePeerInfo.fetch_PeerPort()), 1, peerId);
                        Thread temp = new Thread(p);
                        pt.add(temp);
                        temp.start();
                    }

                }
            } catch (Exception e) {
                log.dataLog(peerId + " PeerModulator is getting an exception while starting the thread");
                log.closeLog();
                System.exit(0);
            }

            try {
                PeerToPeer.socket = new ServerSocket(clientPort);
                thread = new Thread(new Server(PeerToPeer.socket, peerId));
                thread.start();
            } catch (Exception ex) {
                log.dataLog(peerId + " peer is getting an exception while starting the thread");
                log.closeLog();
                System.exit(0);
            }

        }

    }
}
