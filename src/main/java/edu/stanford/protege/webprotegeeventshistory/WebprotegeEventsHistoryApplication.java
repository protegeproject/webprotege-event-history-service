package edu.stanford.protege.webprotegeeventshistory;

import edu.stanford.protege.webprotege.ipc.WebProtegeIpcApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(WebProtegeIpcApplication.class)
public class WebprotegeEventsHistoryApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebprotegeEventsHistoryApplication.class, args);
	}

}
