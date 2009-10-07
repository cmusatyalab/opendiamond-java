package edu.cmu.cs.diamond.opendiamond;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

class ConnectionAggregate {
    private static class BlastGetter implements Runnable {

        private final BlockingQueue<XDR_object> q;

        private final MiniRPCConnection blastConnection;

        public BlastGetter(MiniRPCConnection blastConnection,
                BlockingQueue<XDR_object> blastQueue) {
            this.blastConnection = blastConnection;
            this.q = blastQueue;
        }

        @Override
        public void run() {
            XDR_object obj;
            // block, waiting for blast channel object, then stick into queue
            try {
                while ((obj = getAndAcknowldgeBlastChannelObject()) != null) {
                    q.add(obj);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private XDR_object getAndAcknowldgeBlastChannelObject()
                throws IOException {
            MiniRPCMessage incoming = blastConnection.receive();
            XDR_object obj = new XDR_object(incoming.getData());

            // ack
            ByteBuffer data = ByteBuffer.allocate(4);
            data.putInt(1);
            data.flip();
            blastConnection.sendMessage(1, data);

            return obj;
        }

    }

    private final Set<Connection> connections = new HashSet<Connection>();

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final BlockingQueue<XDR_object> blastQueue = new ArrayBlockingQueue<XDR_object>(
            20);

    public CompletionService<MiniRPCReply> sendToAllControlChannels(int cmd,
            byte data[]) {
        CompletionService<MiniRPCReply> cs = new ExecutorCompletionService<MiniRPCReply>(
                executor);

        for (Connection c : connections) {
            MiniRPCConnection mc = c.getControlConnection();
            cs.submit(new RPC(mc, c.getHostname(), cmd, data));
        }

        return cs;
    }

    public void add(Connection connection) {
        connections.add(connection);

        // create task for getting blast messages
        executor.submit(new BlastGetter(connection.getDataConnection(),
                blastQueue));
    }

    public int size() {
        return connections.size();
    }

    public void close() {
        executor.shutdownNow();
        for (Connection c : connections) {
            c.close();
        }
    }

    public XDR_object getNextBlastChannelObject() throws InterruptedException {
        return blastQueue.take();
    }
}
