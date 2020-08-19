package ru.home.mtur.quickfix.store.bob;

import java.util.Objects;

public class SessionOutMsgRecord {
    private String sessionID;
    private long creationTime;
    private int outSeqNo;
    private String outMsg;

    public SessionOutMsgRecord(String sessionID, long creationTime, int outSeqNo) {
        this(sessionID, creationTime, outSeqNo, null);
    }

    public SessionOutMsgRecord(String sessionID, long creationTime, int outSeqNo, String outMsg) {
        this.sessionID = sessionID;
        this.creationTime = creationTime;
        this.outSeqNo = outSeqNo;
        this.outMsg = outMsg;
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public int getOutSeqNo() {
        return outSeqNo;
    }

    public void setOutSeqNo(int outSeqNo) {
        this.outSeqNo = outSeqNo;
    }

    public String getOutMsg() {
        return outMsg;
    }

    public void setOutMsg(String outMsg) {
        this.outMsg = outMsg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionOutMsgRecord that = (SessionOutMsgRecord) o;
        return creationTime == that.creationTime &&
                outSeqNo == that.outSeqNo &&
                Objects.equals(sessionID, that.sessionID) &&
                Objects.equals(outMsg, that.outMsg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionID, creationTime, outSeqNo, outMsg);
    }

    @Override
    public String toString() {
        return "SessionOutMsgRecord{" +
                "sessionID='" + sessionID + '\'' +
                ", creationTime=" + creationTime +
                ", outSeqNo=" + outSeqNo +
                ", outMsg='" + outMsg + '\'' +
                '}';
    }
}
