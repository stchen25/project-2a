

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
// =============================================================================



// =============================================================================
/**
 * @file   RandomNetworkLayer.java
 * @author Scott F. Kaplan (sfkaplan@cs.amherst.edu)
 * @date   April 2022
 *
 * A network layer that perform routing via random link selection.
 */
public class RandomNetworkLayer extends NetworkLayer {
// =============================================================================



    // =========================================================================
    // PUBLIC METHODS
    // =========================================================================



    // =========================================================================
    /**
     * Default constructor.  Set up the random number generator.
     */
    public RandomNetworkLayer () {

        random = new Random();

    } // RandomNetworkLayer ()
    // =========================================================================



    // =========================================================================
    /**
     * Create a single packet containing the given data, with header that marks
     * the source and destination hosts.
     *
     * @param destination The address to which this packet is sent.
     * @param data        The data to send.
     * @return the sequence of bytes that comprises the packet.
     */
    protected byte[] createPacket (int destination, byte[] data) {
        //buffer to store the packet
        Queue<Byte> packet = new LinkedList<Byte>();

        //first add the size of the data
        packet.add((byte) data.length);

        //convert source address to bytes and add it to packet
        byte[] source = intToBytes(getAddress());
        for (byte b : source){
            packet.add(b);
        }

        //convert destination address to bytes and add it to packet
        byte[] dest = intToBytes(destination);
        for (byte b : dest){
            packet.add(b);
        }

        //add data to packet
        for(int i=0; i<data.length; i++){
            packet.add(data[i]);
        }

        //empty queue into new byte array that will be our packet
        byte[] packetArray = new byte[packet.size()];
        int i = 0;
        while (!packet.isEmpty()){

            packetArray[i] = packet.remove();
            i++;
        }

        return packetArray;

    } // createPacket ()
    // =========================================================================



    // =========================================================================
    /**
     * Randomly choose the link through which to send a packet given its
     * destination.
     *
     * @param destination The address to which this packet is being sent.
     */
    protected DataLinkLayer route (int destination) {
        //create list of keys
        ArrayList<Integer> keys = new ArrayList<Integer>(dataLinkLayers.keySet());

        //randomly select a key and get its corresponding data link layer
        return dataLinkLayers.get(keys.get(random.nextInt(keys.size())));

    } // route ()
    // =========================================================================



    // =========================================================================
    /**
     * Examine a buffer to see if its data can be extracted as a packet; if so,
     * do it, and return the packet whole.
     *
     * @param buffer The receive-buffer to be examined.
     * @return the packet extracted packet if a whole one is present in the
     *         buffer; <code>null</code> otherwise.
     */
    protected byte[] extractPacket (Queue<Byte> buffer) {
        //if our packet has a size of bytesPerHeader - 1, then we are missing parts of the header
        //thus we have a damaged/invalid packet
        if(buffer.size() < bytesPerHeader - 1){
            return null;
        }

        //get length of data, which is the first byte in packet
        int length = (int)(buffer.peek());

        int size = buffer.size();

        //if size of our buffer matches with size of packet, we may extract the packet from the buffer
        if(size >= (bytesPerHeader + length)){
            byte[] packet = new byte[size];

            //empty contents of buffer into new byte array
            for(int i=0; i < size; i++){
                packet[i] = buffer.remove();
            }
            return packet;
        }


        return null;

    } // extractPacket ()
    // =========================================================================



    // =========================================================================
    /**
     * Given a received packet, process it.  If the destination for the packet
     * is this host, then deliver its data to the client layer.  If the
     * destination is another host, route and send the packet.
     *
     * @param packet The received packet to process.
     * @see   createPacket
     */
    protected void processPacket (byte[] packet) {

        //grab the destination component of the packet
        byte[] dest = new byte[4];
        copyFrom(dest, packet, destinationOffset);

        // find the address of the intended destination of this packet
        int destination = bytesToInt(dest);

        //if this host is the destination
        if(destination == address){

            //send data to client, removing the header
            byte[] data = new byte[packet.length - bytesPerHeader];
            copyFrom(data, packet, bytesPerHeader);
            client.receive(data);
            return;
        }
        //find a random data link layer
        DataLinkLayer newLink = route(destination);

        //send packet through new data link
        newLink.send(packet);

        return;

    } // processPacket ()
    // =========================================================================



    // =========================================================================
    // INSTANCE DATA MEMBERS

    /** The random source for selecting routes. */
    private Random random;
    // =========================================================================



    // =========================================================================
    // CLASS DATA MEMBERS

    /** The offset into the header for the length. */
    public static final int     lengthOffset      = 0;

    //since size of our length storage component of header is only 1 byte, we may set the source offset to 1
    /** The offset into the header for the source address. */
    public static final int     sourceOffset      = lengthOffset + 1;

    /** The offset into the header for the destination address. */
    public static final int     destinationOffset = sourceOffset + Integer.BYTES;

    /** How many total bytes per header. */
    public static final int     bytesPerHeader    = destinationOffset + Integer.BYTES;

    /** Whether to emit debugging information. */
    public static final boolean debug             = false;

    
    // =========================================================================



// =============================================================================
} // class RandomNetworkLayer
