package edu.cmu.cs.diamond.opendiamond;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.*;

class ConnectionSet {
    private static class BlastGetter implements Runnable {

        private final BlockingQueue<BlastChannelObject> q;

        private final Connection connection;

        public BlastGetter(Connection connection,
                BlockingQueue<BlastChannelObject> blastQueue) {
            this.connection = connection;
            this.q = blastQueue;
        }

        @Override
        public void run() {
            XDR_object obj;
            // block, waiting for blast channel object, then stick into queue
            try {
                while ((obj = getAndAcknowldgeBlastChannelObject()) != null) {
                    try {
                        q.put(new BlastChannelObject(obj, connection));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private XDR_object getAndAcknowldgeBlastChannelObject()
                throws IOException {
            MiniRPCConnection blastConnection = connection.getDataConnection();

            System.out.println(connection.getHostname()
                    + ": waiting for blast object");
            MiniRPCMessage incoming = blastConnection.receive();
            XDR_object obj = new XDR_object(incoming.getData());
            System.out.println(connection.getHostname()
                    + ":   blast object done");

            // ack
            System.out.println(connection.getHostname() + ": sending credit");
            ByteBuffer data = ByteBuffer.allocate(4);
            data.putInt(1);
            data.flip();
            blastConnection.sendMessage(1, data);
            System.out.println(connection.getHostname() + ":   credit done");

            return obj;
        }

    }

    private final ConcurrentMap<String, Connection> connections = new ConcurrentHashMap<String, Connection>();

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final BlockingQueue<BlastChannelObject> blastQueue = new ArrayBlockingQueue<BlastChannelObject>(
            20);

    public CompletionService<MiniRPCReply> sendToAllControlChannels(
            final int cmd, final ByteBuffer data) {
        return runOnAllServers(new ConnectionFunction() {
            @Override
            public Callable<MiniRPCReply> createCallable(Connection c) {
                MiniRPCConnection mc = c.getControlConnection();
                return new RPC(mc, c.getHostname(), cmd, data
                        .asReadOnlyBuffer());
            }
        });
    }

    public CompletionService<MiniRPCReply> runOnAllServers(ConnectionFunction cf) {
        CompletionService<MiniRPCReply> cs = new ExecutorCompletionService<MiniRPCReply>(
                executor);

        for (Entry<String, Connection> entry : connections.entrySet()) {
            Connection c = entry.getValue();
            cs.submit(cf.createCallable(c));
        }

        return cs;
    }

    public void setConnectionsFromCookies(Map<String, Cookie> cookieMap)
            throws IOException {
        Set<String> currentHosts = connections.keySet();
        // System.out.println("existing: " + currentHosts);

        // first, identify dead connections and close them
        Set<String> passedInHosts = cookieMap.keySet();
        Set<String> deadHosts = new HashSet<String>(currentHosts);

        deadHosts.removeAll(passedInHosts);

        // System.out.println("removing: " + deadHosts);

        for (String d : deadHosts) {
            final Connection c = connections.remove(d);

            executor.submit(new Runnable() {
                @Override
                public void run() {
                    c.close();
                }
            });
        }

        // next, create new connections
        Set<String> newHosts = new HashSet<String>(passedInHosts);

        // System.out.println("adding: " + newHosts);

        CompletionService<Object> connectionCreator = new ExecutorCompletionService<Object>(
                executor);
        for (final String host : newHosts) {
            connectionCreator.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    add(host, new Connection(host));

                    return null;
                }
            });
        }

        checkAllFutures(newHosts.size(), connectionCreator);

        // finally, do the RPCs
        for (Map.Entry<String, Cookie> e : cookieMap.entrySet()) {
            final String hostname = e.getKey();
            final Cookie c = e.getValue();

            // look up hostname in connection map
            final Connection conn = connections.get(hostname);

            connectionCreator.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    // clear scope
                    checkStatus(new RPC(conn.getControlConnection(), hostname,
                            4, ByteBuffer.allocate(0)).doRPC());

                    // define scope
                    ByteBuffer data = XDREncoders.encodeString(c.getCookie());
                    checkStatus(new RPC(conn.getControlConnection(), hostname,
                            24, data).doRPC());

                    return null;
                }
            });
        }

        checkAllFutures(cookieMap.size(), connectionCreator);
    }

    public static void checkStatus(MiniRPCReply reply) throws IOException {
        int status = reply.getMessage().getStatus();
        if (status != MiniRPCMessage.MINIRPC_OK) {
            // TODO case on other statuses
            throw new IOException("bad status on RPC from "
                    + reply.getHostname() + ": " + RPC.statusToString(status));
        }
    }

    private void checkAllFutures(int size,
            CompletionService<Object> connectionCreator) throws IOException {
        for (int i = 0; i < size; i++) {
            try {
                connectionCreator.take().get();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            } catch (ExecutionException e1) {
                Throwable cause = e1.getCause();
                if (cause instanceof IOException) {
                    throw (IOException) cause;
                }
                e1.printStackTrace();
            }
        }
    }

    private void add(String hostname, Connection connection) {
        connections.put(hostname, connection);

        // create task for getting blast messages
        executor.execute(new BlastGetter(connection, blastQueue));
    }

    public int size() {
        return connections.size();
    }

    public void clear() {
        executor.shutdownNow();
        for (Connection c : connections.values()) {
            c.close();
        }
        connections.clear();
    }

    public BlastChannelObject getNextBlastChannelObject()
            throws InterruptedException {
        return blastQueue.take();
    }
}
