package vah;

import java.util.UUID;

import org.kie.kogito.internal.process.runtime.KogitoProcessContext;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class VahService {

    public VahRtfRequest startVahApplication(String pnr, KogitoProcessContext context) {
        var processId = UUID.fromString(context.getProcessInstance().getId());
        System.out.println(String.format("Started VAH application for pnr: %s with processId: %s", pnr, processId));
        return new VahRtfRequest(processId, pnr);
    }

    public boolean onVahRtfResponse(VahRtfResponse response) {
        System.out.println(String.format("VahRtfResponse for processId: %s finished with result: %s", response.processId, response.result));
        return response.result;
    }
}
