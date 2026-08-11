package com.busgo.booking.dto;

import java.util.List;

public class HoldRequest {
    private String tripId;
    private String userId;
    private List<String> seatCodes;

    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public List<String> getSeatCodes() { return seatCodes; }
    public void setSeatCodes(List<String> seatCodes) { this.seatCodes = seatCodes; }
}
