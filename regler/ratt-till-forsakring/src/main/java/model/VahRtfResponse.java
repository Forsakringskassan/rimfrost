package model;

import java.util.UUID;

public class VahRtfResponse {

    public UUID processId;
    public boolean result;

    public VahRtfResponse() {}

    public VahRtfResponse(UUID processId, boolean result){
        this.processId = processId;
        this.result = result;
    }

}
