import jakarta.enterprise.context.ApplicationScoped;
import model.VahRtfRequest;
import model.VahRtfResponse;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

@ApplicationScoped
public class VahRtfProcessor
{

   @Incoming("vah-rtf-requests")
   @Outgoing("vah-rtf-responses")
   public VahRtfResponse process(VahRtfRequest vahRtfRequest) throws InterruptedException
   {
      System.out.println("Received vah rtf request: " + vahRtfRequest.processId + " - " + vahRtfRequest.pnr);

      Thread.sleep(6000);

      return new VahRtfResponse(vahRtfRequest.processId, true);
   }

}
