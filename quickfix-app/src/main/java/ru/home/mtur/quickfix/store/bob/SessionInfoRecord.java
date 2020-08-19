package ru.home.mtur.quickfix.store.bob;

import java.util.Objects;

public class SessionInfoRecord {
    private String sessionID;
    private long creationTime;

    public SessionInfoRecord(String sessionID) {
        this.sessionID = sessionID;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionInfoRecord that = (SessionInfoRecord) o;
        return creationTime == that.creationTime &&
                Objects.equals(sessionID, that.sessionID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionID, creationTime);
    }

    @Override
    public String toString() {
        return "SessionInfoRecord{" +
                "sessionID='" + sessionID + '\'' +
                ", creationTime=" + creationTime +
                '}';
    }
}
