import java.net.*;
import java.io.*;
import java.util.concurrent.*;

public class Node implements Runnable {
    private byte id;
    private Socket connection;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private String inputFileName;
    private String outputFileName;
    private BlockingQueue<Frame> ackQueue;
    private boolean isActive;

    public Node(byte id, String switchAddress, int switchPort) {
        this.id = id;
        this.inputFileName = "node" + id + ".txt";
        this.outputFileName = "node" + id + "output.txt";
        this.ackQueue = new LinkedBlockingQueue<>();
        this.isActive = true;

        initiateConnection(switchAddress, switchPort);
    }

    private void initiateConnection(String address, int port) {
        while (isActive) {
            try {
                connection = new Socket(address, port);
                inputStream = new DataInputStream(connection.getInputStream());
                outputStream = new DataOutputStream(connection.getOutputStream());

                outputStream.writeByte(id);
                outputStream.flush();
                break;
            } catch (IOException e) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    @Override
    public void run() {
        Thread receiverThread = new Thread(this::processIncomingFrames);
        receiverThread.start();

        try (BufferedReader fileReader = new BufferedReader(new FileReader(inputFileName))) {
            String line;
            while ((line = fileReader.readLine()) != null && isActive) {
                try {
                    // Format: destID:priority:message
                    String[] elements = line.split(":", 3);
                    if (elements.length != 3) {
                        System.err.println("Node " + id + ": Invalid format in line: " + line);
                        continue;
                    }

                    // Parse destination node ID
                    byte destinationId;
                    try {
                        destinationId = Byte.parseByte(elements[0].trim());
                    } catch (NumberFormatException e) {
                        System.err.println("Node " + id + ": Invalid destination ID: " + elements[0]);
                        continue;
                    }

                    // Parse priority flag
                    boolean isPriority = elements[1].trim().equalsIgnoreCase("high");

                    // Get message content
                    String messageContent = elements[2].trim();
                    byte[] messageData = messageContent.getBytes();

                    // Create and send frame with priority
                    Frame dataFrame = new Frame(id, destinationId, (byte) messageData.length, messageData, isPriority);
                    sendFrame(dataFrame);

                    // Wait for acknowledgment
                    Frame ack = ackQueue.take();
                    if (ack.getack() != 0 || ack.getsrc() != destinationId) {
                        System.err.println("Node " + id + ": Received invalid ACK");
                    }

                } catch (Exception e) {
                    System.err.println("Node " + id + ": Error processing line: " + line);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Node " + id + ": Error reading input file");
            e.printStackTrace();
        }

        isActive = false;
        try {
            receiverThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void processIncomingFrames() {
        try {
            while (isActive) {
                int frameSize = inputStream.readInt();
                byte[] frameData = new byte[frameSize];
                inputStream.readFully(frameData);
                Frame receivedFrame = Frame.fromBytes(frameData);

                if (receivedFrame.getdst() == id) {
                    if (receivedFrame.getack() > 0) {
                        // Data frame received
                        appendToOutputFile(receivedFrame);
                        // Send acknowledgment with same priority as received frame
                        Frame ackFrame = new Frame(id, receivedFrame.getsrc(), (byte) 0, null, receivedFrame.isHighPriority());
                        sendFrame(ackFrame);
                    } else {
                        // Add acknowledgment frame to queue
                        ackQueue.offer(receivedFrame);
                    }
                }
            }
        } catch (IOException e) {
            if (isActive) {
                System.err.println("Node " + id + ": Error receiving frames");
                e.printStackTrace();
            }
        }
    }

    private void sendFrame(Frame frame) throws IOException {
        byte[] frameBytes = frame.toBytes();
        outputStream.writeInt(frameBytes.length);
        outputStream.write(frameBytes);
        outputStream.flush();
    }

    private void appendToOutputFile(Frame frame) throws IOException {
        try (FileWriter writer = new FileWriter(outputFileName, true)) {
            String priority = frame.isHighPriority() ? "[HIGH] " : "[NORMAL] ";
            writer.write(frame.getsrc() + ": " + priority + new String(frame.getData()) + "\n");
        }
    }

    public void shutdown() {
        isActive = false;
        try {
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}