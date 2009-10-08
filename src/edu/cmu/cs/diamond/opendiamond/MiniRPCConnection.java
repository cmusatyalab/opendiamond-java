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

    private void send(long sequence, int status, int cmd, ByteBuffer data)
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

    public void sendRequest(int cmd, ByteBuffer data) throws IOException {
        if (cmd <= 0) {
            throw new IllegalArgumentException(
                    "cmd must be positive for requests");
        }
        send(nextSequence.getAndIncrement() & 0xFFFFFFFFL,
                MiniRPCMessage.MINIRPC_PENDING, cmd, data);
    }

    public void sendMessage(int cmd, ByteBuffer data) throws IOException {
        if (cmd >= 0) {
            throw new IllegalArgumentException(
                    "cmd must be negative for messages");
        }

        send(nextSequence.getAndIncrement() & 0xFFFFFFFFL,
                MiniRPCMessage.MINIRPC_PENDING, cmd, data);
    }

    public void sendReply(MiniRPCMessage inReplyTo, ByteBuffer data)
            throws IOException {
        send(inReplyTo.getSequence(), MiniRPCMessage.MINIRPC_OK, inReplyTo
                .getCmd(), data);
    }

    public void sendReplyWithStatus(MiniRPCMessage inReplyTo, int status,
            ByteBuffer data) throws IOException {
        if ((status == MiniRPCMessage.MINIRPC_OK)
                || (status == MiniRPCMessage.MINIRPC_PENDING)) {
            throw new IllegalArgumentException(
                    "status cannot be MINIRPC_OK or MINIRPC_PENDING");
        }
        send(inReplyTo.getSequence(), status, inReplyTo.getCmd(), data);
    }

    public MiniRPCMessage receive() throws IOException {
        ByteBuffer buf1 = ByteBuffer.allocate(MINIRPC_HEADER_LENGTH);
        int bytesRead = 0;
        do {
            bytesRead += channel.read(buf1);
        } while (bytesRead != MINIRPC_HEADER_LENGTH);

        buf1.flip();

        long sequence = buf1.getInt() & 0xFFFFFFFFL;
        int status = buf1.getInt();
        int cmd = buf1.getInt();
        int datalen = buf1.getInt();

        ByteBuffer buf2 = readXDRData(datalen);
        buf2.flip();

        return new MiniRPCMessage(sequence, status, cmd, buf2);
    }

    private ByteBuffer readXDRData(int datalen) throws IOException {
        // get slack required
        int roundup = datalen;
        if ((roundup & 0x3) != 0) {
            // round up
            roundup = (roundup + 4) & (~3);
        }
        int slack = roundup - datalen;

        ByteBuffer buf2 = ByteBuffer.allocate(roundup);
        int bytesRead = 0;
        do {
            bytesRead += channel.read(buf2);
        } while (bytesRead != roundup);

        // reposition
        buf2.position(buf2.position() - slack);

        return buf2;
    }

    public void close() throws IOException {
        channel.close();
    }
}
