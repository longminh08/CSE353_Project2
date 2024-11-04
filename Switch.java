import java.net.*;
import java.io.*;
import java.util.concurrent.*;

public class Switch {
    private ServerSocket serverSocket;
    private ConcurrentHashMap<Byte, ClientHandler> clientMap;
    private PriorityBlockingQueue<Frame> frameQueue;
    private ExecutorService executor;
    private boolean isActive;

    public Switch(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        clientMap = new ConcurrentHashMap<>();
        // Initialize priority queue with custom comparator
        frameQueue = new PriorityBlockingQueue<>(11, (f1, f2) -> {
            if (f1.isHighPriority() && !f2.isHighPriority()) return -1;
            if (!f1.isHighPriority() && f2.isHighPriority()) return 1;
            return 0;
        });
        executor = Executors.newCachedThreadPool();
        isActive = true;
    }

    public void start() {
        // Start a thread to handle frame processing
        executor.submit(this::processFrames);

        // Keep accepting client connections
        while (isActive) {
            try {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                executor.submit(clientHandler);
            } catch (IOException e) {
                if (isActive) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void processFrames() {
        while (isActive) {
            try {
                // Use take() instead of poll() to wait for available frames
                Frame frame = frameQueue.take();
                byte destinationId = frame.getdst();
                ClientHandler destinationHandler = clientMap.get(destinationId);
                
                if (destinationHandler != null) {
                    destinationHandler.sendFrame(frame);
                } else {
                    floodFrame(frame);
                }
            } catch (InterruptedException e) {
                if (isActive) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void floodFrame(Frame frame) {
        byte sourceId = frame.getsrc();
        // Loop through all connected clients
        for (ClientHandler handler : clientMap.values()) {
            // Avoid sending to the source to prevent loops
            if (handler.getNodeId() != sourceId) {
                handler.sendFrame(frame);
            }
        }
    }

    private class ClientHandler implements Runnable {
        private DataInputStream input;
        private DataOutputStream output;
        private byte nodeId;

        public ClientHandler(Socket socket) throws IOException {
            this.input = new DataInputStream(socket.getInputStream());
            this.output = new DataOutputStream(socket.getOutputStream());
        }

        public byte getNodeId() { return nodeId; }

        public void sendFrame(Frame frame) {
            try {
                byte[] frameBytes = frame.toBytes();
                output.writeInt(frameBytes.length);
                output.write(frameBytes);
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                // The first message received specifies the node ID
                nodeId = input.readByte();
                clientMap.put(nodeId, this);

                while (isActive) {
                    int length = input.readInt();
                    byte[] frameBytes = new byte[length];
                    input.readFully(frameBytes);
                    Frame frame = Frame.fromBytes(frameBytes);
                    frameQueue.offer(frame);
                }
            } catch (IOException e) {
                if (isActive) {
                    e.printStackTrace();
                }
            } finally {
                clientMap.remove(nodeId);
            }
        }
    }

    public void shutdown() {
        isActive = false;
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
