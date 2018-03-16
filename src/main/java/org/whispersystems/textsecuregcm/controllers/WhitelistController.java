package org.whispersystems.textsecuregcm.controllers;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.textsecuregcm.storage.*;
import org.whispersystems.textsecuregcm.util.Util;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v1/whitelist")
public class WhitelistController {

    private final Logger log = LoggerFactory.getLogger(WhitelistController.class);

    private WhitelistManager whitelistManager;
    private AccountsManager accountsManager;

    public WhitelistController(WhitelistManager whitelistManager, AccountsManager accountsManager) {
        this.whitelistManager = whitelistManager;
        this.accountsManager = accountsManager;
    }

    @POST
    @Timed
    @Path("/{number}/{type}")
    public Response setAccount(@PathParam("number") String number,
                               @PathParam("type") int type) {

        if (Util.isEmpty(number) || !Util.isValidNumber(number)) {
            throw new WebApplicationException(Response.status(404)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .entity("Non valid number: " + number + "\n")
                    .build());
        }

        if (!isValidType(type)) {
            throw new WebApplicationException(Response.status(404)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .entity("Non valid type: " + type + "\n")
                    .build());
        }

        if (whitelistManager.hasNumber(number)) {
            whitelistManager.update(number, type);
        } else {
            whitelistManager.insert(number, type);
        }

        Optional<Account> acc = accountsManager.get(number);
        if (acc.isPresent()) {
            Account account = acc.get();
            account.setWhitelisted(type);
            accountsManager.update(account);
        }

        return Response.ok().build();
    }

    @DELETE
    @Timed
    @Path("/{number}")
    public Response deleteAccount(@PathParam("number") String number) {

        if (Util.isEmpty(number) || !Util.isValidNumber(number)) {
            throw new WebApplicationException("Non valid number: " + number);
        }

        if (!whitelistManager.isInWhitelist(number)) {
            throw new WebApplicationException(Response.status(404)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .entity("Number doesn't exist!\n")
                    .build());
        }
        whitelistManager.update(number, Account.BLACKLISTED);

        Optional<Account> acc = accountsManager.get(number);
        if (acc.isPresent()) {
            Account account = acc.get();
            account.setWhitelisted(Account.BLACKLISTED);
            accountsManager.update(account);
        }
        return Response.ok().build();
    }

    private boolean isValidType(int type) {
        return type == 1 || type == 0;
    }
}