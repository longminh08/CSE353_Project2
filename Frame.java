public class Frame {
    private byte src; // Source address of the frame (sender)
    private byte dst; // Destination address of the frame (receiver)
    private byte ack; // Size of the data being sent or an acknowledgment indicator
    private byte[] data; // Actual data being transmitted in the frame
    private boolean highPriority; // Priority flag

    // Setters
    public Frame(byte src, byte dst, byte ack, byte[] data, boolean highPriority) {
        this.src = src; 
        this.dst = dst; 
        this.ack = ack;  
        this.data = data;
        this.highPriority = highPriority;
    }

    // Convert frame to bytes for network transmission
    public byte[] toBytes() {
        int frameSize = 4;
        if (ack > 0) {
            frameSize += data.length; // Add the length of the data array
        }
        
        // Fill the header of the frame with source, destination, and size/ACK
        byte[] frameBytes = new byte[frameSize];
        frameBytes[0] = src;
        frameBytes[1] = dst;
        frameBytes[2] = ack;
        frameBytes[3] = (byte)(highPriority ? 1 : 0); // Store priority as byte
        
        // If ack indicates data is present, create the data array and copy data from bytes
        if (ack > 0) {
            System.arraycopy(data, 0, frameBytes, 4, data.length);
        }
        
        return frameBytes;
    }

    // Create frame from received bytes
    public static Frame fromBytes(byte[] bytes) {
        byte src = bytes[0];
        byte dst = bytes[1];
        byte ack = bytes[2];
        boolean highPriority = bytes[3] == 1;

        byte[] data = null;
        if (ack > 0) {
            data = new byte[ack];
            System.arraycopy(bytes, 4, data, 0, ack);
        }
        
        return new Frame(src, dst, ack, data, highPriority);
    }

    // Getters
    public byte getsrc() { return src; }
    public byte getdst() { return dst; }
    public byte getack() { return ack; }
    public byte[] getData() { return data; }
    public boolean isHighPriority() { return highPriority; }
}