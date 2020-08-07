package ru.home.mtur.quickfix.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.field.MsgSeqNum;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class MsgHolder {
    final Logger log = LoggerFactory.getLogger(MsgHolder.class);

    public static AtomicLong nextMsgID = new AtomicLong();

    private long msgId = nextMsgID.getAndIncrement();
    private SessionID sessionID;
    private Message msg;

    public MsgHolder(SessionID sessionID, Message msg) {
        this.sessionID = sessionID;
        this.msg = msg;
    }

    public SessionID getSessionID() {
        return sessionID;
    }

    public void setSessionID(SessionID sessionID) {
        this.sessionID = sessionID;
    }

    public Message getMsg() {
        return msg;
    }

    public void setMsg(Message msg) {
        this.msg = msg;
    }

    public long getMsgId() {
        return msgId;
    }

    public int getMsgSeq() {
        try {
            return msg.getHeader().getInt(MsgSeqNum.FIELD);
        } catch (Exception e) {
            log.error("Unable to get MsgSeqNum from message: {}", msg, e);
            return -1;
        }
    }

    @Override
    public String toString() {
        return "MsgHolder{" +
                "msgId=" + msgId +
                ", sessionID=" + sessionID +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MsgHolder holder = (MsgHolder) o;
        return msgId == holder.msgId &&
                Objects.equals(sessionID, holder.sessionID) &&
                Objects.equals(msg, holder.msg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(msgId, sessionID, msg);
    }
}
