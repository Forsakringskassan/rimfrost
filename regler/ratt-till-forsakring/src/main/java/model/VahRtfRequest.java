package se.fk.github.rimfrost.regler.ratttillforsakring.model;

import java.util.UUID;

public class VahRtfRequest
{

   public UUID processId;
   public String pnr;

   public VahRtfRequest()
   {
   }

   public VahRtfRequest(UUID processId, String pnr)
   {
      this.processId = processId;
      this.pnr = pnr;
   }
}
