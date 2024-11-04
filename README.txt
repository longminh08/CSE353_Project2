Long Nguyen

Frame Format Specification
Field	Size(bytes)	Range	Description
src	1		1-255	Source address of the frame (sender node ID)
dst	1		1-255	Destination address of the frame (receiver node ID)
ack	1		0-255	If 0: ACK frame. If >0: Size of data in bytes
priority    1   0-1 0 = normal, 1 = high priority
data	0-255		N/A	Contains the actual message data. Only present when ack > 0.

Feature Status/Description
Project Compiles and Builds without warnings or errors 		Complete
Switch class 							                    Complete
Switch has a frame buffer, and reads/writes appropriately 	Complete
Switch allows multiple connections 				            Complete
Switch floods frame when it doesn't know the destination 	Did it but doesn't show in output
Switch learns destinations, and doesn't forward packet to 	Complete
any port except the one required
Node class 							                        Complete
Nodes instantiate, and open connection to the switch 		Complete
Nodes open their input files, and send data to switch 		Complete
Nodes open their output files, and save data that they received Complete

Compile:
make
make run

Bugs:
The program doesn't exit properly. Priority implemented but behave weird.

Files:
Main.java: sets up the switch and nodes and controls a clean shutdown once every node is finished.
Switch.java: is responsible for maintaining a switching table, forwarding frames, and representing the central switch.
Node.java: defines nodes, establishes a connection with a switch, transmits and receives data, and records output.
Frame.java: the frame format for structured data communication between nodes is defined by .

Github: https://github.com/longminh08/CSE353_Project2