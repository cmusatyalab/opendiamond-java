package edu.cmu.cs.diamond.opendiamond;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

class ConnectionSet {
    private final Set<Connection> connections;

    private final BlockingQueue<BlastChannelObject> blastQueue = new ArrayBlockingQueue<BlastChannelObject>(
            20);

    private final ExecutorService executor;

    ConnectionSet(ExecutorService executor, Set<Connection> connections) {
        this.executor = executor;
        this.connections = new HashSet<Connection>(connections);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                CompletionService<Object> blastTasks = new ExecutorCompletionService<Object>(
                        ConnectionSet.this.executor);

                int blastCount = 0;
                for (Connection c : ConnectionSet.this.connections) {
                    // create tasks for getting blast messages
                    blastTasks.submit(new BlastGetter(c, c.getHostname(),
                            blastQueue));
                    blastCount++;
                }

                // wait for things to finish
                try {
                    for (int i = 0; i < blastCount; i++) {
                        blastTasks.take().get();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof IOException) {
                        IOException e2 = (IOException) cause;

                        // inject into blast queue
                        try {
                            blastQueue.put(new BlastChannelObject(null, null,
                                    e2));
                        } catch (InterruptedException e1) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }

                // all tasks done, inject final object and close
                try {
                    addNoMoreResultsToBlastQueue();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                close();
            }
        });
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

    public void addNoMoreResultsToBlastQueue() throws InterruptedException {
        blastQueue.put(BlastChannelObject.NO_MORE_RESULTS);
    }
}
