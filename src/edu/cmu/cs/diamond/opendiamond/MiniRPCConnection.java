package edu.cmu.cs.diamond.opendiamond;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

final class MiniRPCConnection {
    private static final int MINIRPC_HEADER_LENGTH = 16;

    final private AtomicInteger nextSequence = new AtomicInteger();

    final private SocketChannel channel;

    public MiniRPCConnection(SocketChannel channel) {
        this.channel = channel;
    }

    public void send(long sequence, int status, int cmd, ByteBuffer data)
            throws IOException {
        if ((sequence < 0) || (sequence > 0xFFFFFFFFL)) {
            throw new IllegalArgumentException(
                    "sequence must be between 0 and " + 0xFFFFFFFFL
                            + ", given: " + sequence);
        }

        // write header
        int datalen = data.remaining();
        ByteBuffer header = ByteBuffer.allocate(MINIRPC_HEADER_LENGTH);
        header.putInt((int) sequence).putInt(status).putInt(cmd)
                .putInt(datalen).flip();

        if ((channel.write(header) != MINIRPC_HEADER_LENGTH)) {
            throw new IOException("Can't write miniRPC header");
        }

        // write data
        if ((channel.write(data)) != datalen) {
            throw new IOException("Can't write miniRPC data");
        }
    }

    public void send(int status, int cmd, ByteBuffer data) throws IOException {
        send(nextSequence.getAndIncrement() & 0xFFFFFFFFL, status, cmd, data);
    }

    public void send(MiniRPCMessage msg) throws IOException {
        send(msg.getSequence(), msg.getStatus(), msg.getCmd(), msg.getData());
    }

    public MiniRPCMessage receive() throws IOException {
        ByteBuffer buf1 = ByteBuffer.allocate(MINIRPC_HEADER_LENGTH);
        if (channel.read(buf1) != MINIRPC_HEADER_LENGTH) {
            throw new IOException("Can't read header");
        }

        buf1.flip();

        long sequence = buf1.getInt() & 0xFFFFFFFFL;
        int status = buf1.getInt();
        int cmd = buf1.getInt();
        int datalen = buf1.getInt();

        ByteBuffer buf2 = ByteBuffer.allocate(datalen);
        if (channel.read(buf2) != datalen) {
            throw new IOException("Can't read data");
        }

        buf2.flip();

        return new MiniRPCMessage(sequence, status, cmd, buf2);
    }

    public void close() throws IOException {
        channel.close();
    }
}
