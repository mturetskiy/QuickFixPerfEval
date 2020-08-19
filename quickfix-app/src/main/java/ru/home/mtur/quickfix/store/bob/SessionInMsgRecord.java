package ru.home.mtur.quickfix.store.bob;

import java.util.Objects;

public class SessionInMsgRecord {
    private String sessionID;
    private long creationTime;
    private int inSeqNo;
    private String inMsg;

    public SessionInMsgRecord(String sessionID, long creationTime, int inSeqNo) {
        this(sessionID, creationTime, inSeqNo, null);
    }

    public SessionInMsgRecord(String sessionID, long creationTime, int inSeqNo, String inMsg) {
        this.sessionID = sessionID;
        this.creationTime = creationTime;
        this.inSeqNo = inSeqNo;
        this.inMsg = inMsg;
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

    public int getInSeqNo() {
        return inSeqNo;
    }

    public void setInSeqNo(int inSeqNo) {
        this.inSeqNo = inSeqNo;
    }

    public String getInMsg() {
        return inMsg;
    }

    public void setInMsg(String inMsg) {
        this.inMsg = inMsg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionInMsgRecord that = (SessionInMsgRecord) o;
        return creationTime == that.creationTime &&
                inSeqNo == that.inSeqNo &&
                Objects.equals(sessionID, that.sessionID) &&
                Objects.equals(inMsg, that.inMsg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionID, creationTime, inSeqNo, inMsg);
    }

    @Override
    public String toString() {
        return "SessionInMsgRecord{" +
                "sessionID='" + sessionID + '\'' +
                ", creationTime=" + creationTime +
                ", inSeqNo=" + inSeqNo +
                ", inMsg='" + inMsg + '\'' +
                '}';
    }
}
