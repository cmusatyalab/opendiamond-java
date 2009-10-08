package edu.cmu.cs.diamond.opendiamond;

import java.util.concurrent.Callable;

public interface ConnectionFunction<T> {
    abstract Callable<T> createCallable(Connection c);
}
