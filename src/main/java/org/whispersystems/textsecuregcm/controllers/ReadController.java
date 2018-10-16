package org.whispersystems.textsecuregcm.controllers;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import io.dropwizard.auth.Auth;
import org.whispersystems.textsecuregcm.push.NotPushRegisteredException;
import org.whispersystems.textsecuregcm.push.ReceiptSender;
import org.whispersystems.textsecuregcm.push.TransientPushFailureException;
import org.whispersystems.textsecuregcm.storage.Account;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/v1/read")
public class ReadController {

    private final ReceiptSender receiptSender;

    public ReadController(ReceiptSender receiptSender) {
        this.receiptSender = receiptSender;
    }

    @Timed
    @PUT
    @Path("/{destination}/{messageId}")
    public void sendDeliveryRead(@Auth                     Account source,
                                 @PathParam("destination") String destination,
                                 @PathParam("messageId")   long messageId,
                                 @QueryParam("when")       Optional<Long> when,
                                 @QueryParam("relay")      Optional<String> relay)
            throws IOException
    {
        try {
            receiptSender.sendRead(source, destination, messageId, when, relay);
        } catch (NoSuchUserException | NotPushRegisteredException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        } catch (TransientPushFailureException e) {
            throw new IOException(e);
        } catch (IOException e) {
            throw new WebApplicationException(Response.status(500).build());
        }
    }

}
