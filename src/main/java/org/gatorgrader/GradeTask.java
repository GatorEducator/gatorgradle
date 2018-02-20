package org.gatorgrader;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class GradeTask extends DefaultTask {
	private String message;
	private String recipient;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	@TaskAction
	public void sayGreeting() {
		System.out.printf("%s, %s!\n", getMessage(), getRecipient());
	}


}
