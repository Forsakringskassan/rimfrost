package fk.rimfrost;

import jakarta.ws.rs.GET;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import se.fk.rimfrost.api.folkbokforing.jaxrsspec.controllers.generatedsource.PopulationRegistrationControllerApi;
import se.fk.rimfrost.api.folkbokforing.jaxrsspec.controllers.generatedsource.model.PopulationRegistrationSocialSecurityNrGet200Response;

@ApplicationScoped
@Path("/population_registration/{social_security_nr}")
public class PopulationRegistration implements PopulationRegistrationControllerApi
{
   public PopulationRegistrationSocialSecurityNrGet200Response populationRegistrationSocialSecurityNrGet(
         String social_security_nr)
   {
      var response = new PopulationRegistrationSocialSecurityNrGet200Response();
      String lastFour = social_security_nr.substring(social_security_nr.length() - 4);
      response.setResult(!lastFour.equals("9999"));
      return response;
   }
}
