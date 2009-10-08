package edu.cmu.cs.diamond.opendiamond;

import java.util.concurrent.Callable;

public interface ConnectionFunction {
    abstract Callable<MiniRPCReply> createCallable(Connection c);
}
