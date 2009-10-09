package edu.cmu.cs.diamond.opendiamond;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;

class RPC implements Callable<MiniRPCReply> {
    final public static int DIAMOND_SUCCESS = 0;

    final public static int DIAMOND_FAILURE = 500;

    final public static int DIAMOND_FCACHEMISS = 501;

    final public static int DIAMOND_NOSTATSAVAIL = 502;

    final public static int DIAMOND_NOMEM = 503;

    final public static int DIAMOND_COOKIE_EXPIRED = 504;

    final public static int MAX_FILTER_NAME = 128;

    final public static int MAX_FILTERS = 64;

    final private Connection connection;

    final private int cmd;

    final private ByteBuffer data;

    final private String hostname;

    public RPC(Connection connection, String hostname, int cmd, ByteBuffer data) {
        this.connection = connection;
        this.hostname = hostname;
        this.cmd = cmd;
        this.data = data;
    }

    @Override
    public MiniRPCReply call() throws Exception {
        return doRPC();
    }

    public MiniRPCReply doRPC() throws IOException {
        connection.sendControlRequest(cmd, data);
        MiniRPCReply reply = new MiniRPCReply(connection.receiveControl(),
                hostname);

        System.out.println(reply);

        return reply;
    }

    public static String statusToString(int status) {
        switch (status) {
        case DIAMOND_SUCCESS:
            return "DIAMOND_SUCCESS";

        case DIAMOND_FAILURE:
            return "DIAMOND_FAILURE";

        case DIAMOND_FCACHEMISS:
            return "DIAMOND_FCACHEMISS";

        case DIAMOND_NOSTATSAVAIL:
            return "DIAMOND_NOSTATSAVAIL";

        case DIAMOND_NOMEM:
            return "DIAMOND_NOMEM";

        case DIAMOND_COOKIE_EXPIRED:
            return "DIAMOND_COOKIE_EXPIRED";

        default:
            return MiniRPCMessage.statusToString(status);
        }
    }
}