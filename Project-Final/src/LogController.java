import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LogController {
    static FileOutputStream fos;
    static OutputStreamWriter osWriter;

    LogController(String fName) throws Exception {
        File dir = new File("logs");
        dir.mkdir();
        File logs = new File("logs", fName);
        fos = new FileOutputStream("logs//" + fName);
        osWriter = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
    }

    public void printLog(String s) {
        try {
            osWriter.write(s + "\n");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    public void dataLog(String message) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat d = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        printLog(d.format(calendar.getTime()) + " Peer " + message);
        System.out.println(d.format(calendar.getTime()) + " Peer " + message);
    }

    public void closeLog() {
        try {
            osWriter.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

}
