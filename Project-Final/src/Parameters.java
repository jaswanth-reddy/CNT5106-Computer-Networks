public class Parameters {

    public static int numberOfPreferredNeighbors = 0;
    public static int unchokingInterval = 0;
    public static int optimisticUnchokingInterval = 0;
    public static int fileSize = 0;
    public static int pieceSize = 0;
    public static String fileName = "";


    public static int encodeBytetoInt(byte[] bArr, int os) {
        int intres = 0;
        int max=4, noOfBits=8;
        for (int i = 0; i < max; i++) {
            int s = (3 - i) * noOfBits;
            intres += (bArr[i + os] & 0x000000FF) << s;
        }
        return intres;
    }

    public static byte[] encodeIntToByte(int val) {
        byte[] byteArray = new byte[4];
        int i = 0, max=4, noOfBits=8;
        while (i < max) {
            int m = (byteArray.length - 1 - i) * noOfBits;
            byteArray[i] = (byte) ((val >>> m) & (0xFF));
            i++;
        }
        return byteArray;
    }
}
