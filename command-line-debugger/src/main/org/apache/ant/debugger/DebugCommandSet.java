package org.apache.ant.debugger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.tools.ant.Project;

/**
 * Handles all debug support functionality.
 */
public class DebugCommandSet {

	protected Project project;

	protected Map commandSupport = new HashMap();

	public void setProject(Project project) {
		this.project = project;
	}

	/**
	 * Initialize all debug commands here - preferrably read from a properties
	 * file.
	 */
	public void init(Map commands) {
		if (commands != null)
			commandSupport.putAll(commands);
		Properties props = new Properties();
		try {
			InputStream is = DebugCommandSet.class.getClassLoader()
					.getResourceAsStream(
							"org/apache/ant/debugger/debug-support.properties");
			props.load(is);
			Enumeration en = props.keys();
			while (en.hasMoreElements()) {
				String key = (String) en.nextElement();
				String className = props.getProperty(key);
				Class commandClass;
				try {
					commandClass = Class.forName(className);
					Object command = commandClass.newInstance();
					if (command instanceof DebugSupport)
						commandSupport.put(key, command);
					else
						project
								.log(
										"Command Class: "
												+ className
												+ " does not implement DebugSupport. Ignoring.",
										Project.MSG_WARN);
				} catch (ClassNotFoundException e) {
					project.log("Could not find class: " + className,
							Project.MSG_WARN);
				} catch (InstantiationException e) {
					project.log("Could not instantiate class: " + className,
							Project.MSG_WARN);
				} catch (IllegalAccessException e) {
					project.log("Illegal Access while instantiating class: "
							+ className, Project.MSG_WARN);
				}
			}
		} catch (IOException ioe) {
			// if the resource could not be located, then initialize with what
			// is known
			throw new RuntimeException(ioe);
		}
	}

	public void handleCommand(String command) {
		if (command == null || command.trim().length() == 0) {
			project.log("Invalid command. Use /? for more information.");
		}
		command = command.trim();
		if (command.equals("/?")) {
			printUsage();
			return;
		}

		String[] tokens = command.split(" ");
		DebugSupport selected = (DebugSupport) commandSupport.get(tokens[0]);
		if (selected != null) {
			selected.execute(project, tokens);
		} else {
			printUsage();
		}
	}

	protected void printUsage() {
		// log all help stuff here
		project
				.log("You may use one of the following commands: locate, inspect, return");
		project
				.log("Type the command followed by /? for more information. Eg. inspect /?");
	}

}
