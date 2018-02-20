package org.gatorgrader;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class GatorGraderPlugin implements Plugin<Project> {
	
	public void apply(Project project) {
		project.getTasks().create("grade", GradeTask.class, (task) -> {
			task.setMessage("Hello");
			task.setRecipient("World");
		});
	}

}
