package edu.cmu.cs.diamond.opendiamond;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

class ConnectionSet {
    private static class BlastGetter implements Runnable {

        private final BlockingQueue<BlastChannelObject> q;

        private final Connection connection;

        private final String hostname;

        public BlastGetter(Connection connection, String hostname,
                BlockingQueue<BlastChannelObject> blastQueue) {
            this.connection = connection;
            this.hostname = hostname;
            this.q = blastQueue;
        }

        private XDR_object getAndAcknowldgeBlastChannelObject()
                throws IOException {
            System.out.println(hostname + ": waiting for blast object");
            MiniRPCMessage incoming = connection.receiveBlast();
            XDR_object obj = new XDR_object(incoming.getData());
            System.out.println(hostname + ":   blast object done");

            // ack
            System.out.println(hostname + ": sending credit");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(baos);
            out.writeInt(1); // 1 credit

            connection.sendMessageBlast(1, baos.toByteArray());
            System.out.println(hostname + ":   credit done");

            return obj;
        }

        @Override
        public void run() {
            XDR_object obj;
            // block, waiting for blast channel object, then stick into queue
            try {
                while ((obj = getAndAcknowldgeBlastChannelObject()) != null) {
                    q.put(new BlastChannelObject(obj, hostname));
                }
            } catch (IOException e) {
                // bye bye
                e.printStackTrace();
            } catch (InterruptedException e) {
                // bye bye
                e.printStackTrace();
            }
        }
    }

    private final Set<Connection> connections;

    private final BlockingQueue<BlastChannelObject> blastQueue = new ArrayBlockingQueue<BlastChannelObject>(
            20);

    private final ExecutorService executor;

    ConnectionSet(ExecutorService executor, Set<Connection> connections) {
        this.executor = executor;
        this.connections = new HashSet<Connection>(connections);

        for (Connection c : connections) {
            // create task for getting blast messages
            executor.execute(new BlastGetter(c, c.getHostname(), blastQueue));
        }
    }

    public void close() {
        for (Connection c : connections) {
            c.close();
        }
    }

    public BlastChannelObject getNextBlastChannelObject()
            throws InterruptedException {
        return blastQueue.take();
    }

    public <T> CompletionService<T> runOnAllServers(ConnectionFunction<T> cf) {
        CompletionService<T> cs = new ExecutorCompletionService<T>(executor);

        for (Connection c : connections) {
            cs.submit(cf.createCallable(c));
        }

        return cs;
    }

    public CompletionService<MiniRPCReply> sendToAllControlChannels(
            final int cmd, final byte[] data) {
        return runOnAllServers(new ConnectionFunction<MiniRPCReply>() {
            @Override
            public Callable<MiniRPCReply> createCallable(Connection c) {
                return new RPC(c, c.getHostname(), cmd, data);
            }
        });
    }

    public int size() {
        return connections.size();
    }
}
