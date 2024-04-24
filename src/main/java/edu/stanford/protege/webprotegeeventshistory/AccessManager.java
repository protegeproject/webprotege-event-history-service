package edu.stanford.protege.webprotegeeventshistory;


import edu.stanford.protege.webprotege.authorization.*;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotegeeventshistory.dto.BuiltInAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutionException;

@Component
public class AccessManager {

    private final static Logger LOGGER = LoggerFactory.getLogger(AccessManager.class);

    private final CommandExecutor<GetAuthorizationStatusRequest, GetAuthorizationStatusResponse> getAuthorizationStatusExecutor;

    public AccessManager(CommandExecutor<GetAuthorizationStatusRequest, GetAuthorizationStatusResponse> getAuthorizationStatusExecutor) {
        this.getAuthorizationStatusExecutor = getAuthorizationStatusExecutor;
    }

    public boolean hasPermission(@Nonnull Subject subject,
                                 @Nonnull Resource resource,
                                 @Nonnull BuiltInAction builtInAction,
                                 ExecutionContext executionContext) {
        try {
            GetAuthorizationStatusResponse response = getAuthorizationStatusExecutor.execute(new GetAuthorizationStatusRequest(resource, subject, builtInAction.getActionId()),
                            executionContext)
                    .get();
            return response.authorizationStatus().equals(AuthorizationStatus.AUTHORIZED);

        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Error when getting authorization status", e);
            return false;
        }

    }



}
