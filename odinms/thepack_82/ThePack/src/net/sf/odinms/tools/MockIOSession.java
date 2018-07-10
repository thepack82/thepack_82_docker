package net.sf.odinms.tools;

import java.net.SocketAddress;

import net.sf.odinms.net.MaplePacket;

import org.apache.mina.common.CloseFuture;
import org.apache.mina.common.IoFilterChain;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoService;
import org.apache.mina.common.IoServiceConfig;
import org.apache.mina.common.IoSessionConfig;
import org.apache.mina.common.TransportType;
import org.apache.mina.common.WriteFuture;
import org.apache.mina.common.IoFilter.WriteRequest;
import org.apache.mina.common.support.BaseIoSession;

public class MockIOSession extends BaseIoSession {

    @Override
    protected void updateTrafficMask() {
    }

    @Override
    public IoSessionConfig getConfig() {
        return null;
    }

    @Override
    public IoFilterChain getFilterChain() {
        return null;
    }

    @Override
    public IoHandler getHandler() {
        return null;
    }

    @Override
    public SocketAddress getLocalAddress() {
        return null;
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return null;
    }

    @Override
    public IoService getService() {
        return null;
    }

    @Override
    public SocketAddress getServiceAddress() {
        return null;
    }

    @Override
    public IoServiceConfig getServiceConfig() {
        return null;
    }

    @Override
    public TransportType getTransportType() {
        return null;
    }

    @Override
    public CloseFuture close() {
        return null;
    }

    @Override
    protected void close0() {
    }

    @Override
    public WriteFuture write(Object message, SocketAddress remoteAddress) {
        return null;
    }

    @Override
    public WriteFuture write(Object message) {
        if (message instanceof MaplePacket) {
            MaplePacket mp = (MaplePacket) message;
            if (mp.getOnSend() != null) {
                mp.getOnSend().run();
            }
        }
        return null;
    }

    @Override
    protected void write0(WriteRequest writeRequest) {
    }
}
