import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        // Parse number of nodes from the command-line arguments
        int numberOfNodes = Integer.parseInt(args[0]);

        // Initialize the network switch
        Switch networkSwitch = null;
        try {
            networkSwitch = new Switch(191104);
            Thread switchThread = new Thread(networkSwitch::start);
            switchThread.start();

            // Initialize and start each node
            Thread[] threadsForNodes = new Thread[numberOfNodes];
            Node[] nodesArray = new Node[numberOfNodes];

            for (int i = 0; i < numberOfNodes; i++) {
                nodesArray[i] = new Node((byte)(i + 1), "localhost", 191104);
                threadsForNodes[i] = new Thread(nodesArray[i]);
                threadsForNodes[i].start();
            }

            // Wait for all nodes to finish execution
            for (Thread nodeThread : threadsForNodes) {
                nodeThread.join();
            }

            // Shutdown each node and the network switch
            for (Node node : nodesArray) {
                node.shutdown();
            }
            networkSwitch.shutdown();
            switchThread.join();

        } catch (IOException | InterruptedException exception) {
            exception.printStackTrace();
        }
    }
}
