package fk.rimfrost;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import se.fk.rimfrost.api.folkbokforing.jaxrsspec.controllers.generatedsource.DefaultApi;
import se.fk.rimfrost.api.folkbokforing.jaxrsspec.controllers.generatedsource.model.PopulationRegistrationSocialSecurityNrGet200Response;

@Path("/population_registration")
public class population_registration implements DefaultApi {
    @Override
    @GET
    @Path("/{social_security_nr}")
    @Produces(MediaType.APPLICATION_JSON)
    public PopulationRegistrationSocialSecurityNrGet200Response populationRegistrationSocialSecurityNrGet(@PathParam("social_security_nr") String social_security_nr) {

        var response = new PopulationRegistrationSocialSecurityNrGet200Response();
        String lastFour = social_security_nr.substring(social_security_nr.length() - 4);
        response.setResult(!lastFour.equals("9999"));
        return response;
      }
}

