package edu.stanford.protege.webprotegeeventshistory;

import edu.stanford.protege.webprotege.authorization.BasicCapability;
import edu.stanford.protege.webprotege.authorization.Capability;
import edu.stanford.protege.webprotege.authorization.ProjectResource;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.AuthorizedCommandHandler;
import edu.stanford.protege.webprotege.ipc.CommandExecutionException;
import edu.stanford.protege.webprotegeeventshistory.dto.ProjectEventsQueryRequest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class GetLatestProjectEventsCommandHandlerTest {

    private GetLatestProjectEventsCommandHandler handler;

    private ProjectId projectId;

    @Before
    public void setUp() {
        // The contract methods under test never touch the service, so no stub is needed.
        handler = new GetLatestProjectEventsCommandHandler(null);
        projectId = ProjectId.generate();
    }

    @Test
    public void GIVEN_handler_THEN_isAuthorizedCommandHandler() {
        assertTrue(handler instanceof AuthorizedCommandHandler);
    }

    @Test
    public void GIVEN_request_WHEN_getTargetResource_THEN_returnsProjectResourceForRequestProject() {
        ProjectEventsQueryRequest request = new ProjectEventsQueryRequest();
        request.projectId = projectId;

        assertEquals(ProjectResource.forProject(projectId), handler.getTargetResource(request));
    }

    @Test
    public void GIVEN_nullProjectId_WHEN_getTargetResource_THEN_throwsBadRequest() {
        ProjectEventsQueryRequest request = new ProjectEventsQueryRequest();
        request.projectId = null;

        CommandExecutionException exception =
                assertThrows(CommandExecutionException.class, () -> handler.getTargetResource(request));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    public void GIVEN_handler_WHEN_getRequiredCapabilities_THEN_equalsViewProject() {
        assertEquals(List.of((Capability) new BasicCapability("ViewProject")), handler.getRequiredCapabilities());
    }
}
