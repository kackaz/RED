/*
 * Copyright 2017 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.launch;

import static org.robotframework.ide.eclipse.main.plugin.RedPlugin.newCoreException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.rf.ide.core.execution.server.AgentConnectionServer;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;
import org.robotframework.ide.eclipse.main.plugin.RedPreferences;
import org.robotframework.ide.eclipse.main.plugin.model.RobotProject;

import com.google.common.collect.Range;
import com.google.common.primitives.Ints;

public abstract class AbstractRobotLaunchConfiguration implements IRobotLaunchConfiguration {

    private static final String PROJECT_NAME_ATTRIBUTE = "Project name";

    private static final String REMOTE_AGENT = "Remote agent";

    protected static final String REMOTE_HOST_ATTRIBUTE = "Remote host";

    protected static final String REMOTE_PORT_ATTRIBUTE = "Remote port";

    protected static final String REMOTE_TIMEOUT_ATTRIBUTE = "Remote timeout";

    protected final ILaunchConfiguration configuration;

    protected AbstractRobotLaunchConfiguration(final ILaunchConfiguration config) {
        this.configuration = config;
    }

    @Override
    public String getName() {
        return configuration.getName();
    }

    @Override
    public String getTypeName() {
        try {
            return configuration.getType().getName();
        } catch (final CoreException e) {
            return null;
        }
    }

    @Override
    public String getProjectName() throws CoreException {
        return configuration.getAttribute(PROJECT_NAME_ATTRIBUTE, "");
    }

    @Override
    public void setProjectName(final String projectName) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(PROJECT_NAME_ATTRIBUTE, projectName);
    }

    @Override
    public RobotProject getRobotProject() throws CoreException {
        final IProject project = getProject();
        return RedPlugin.getModelManager().getModel().createRobotProject(project);
    }

    protected IProject getProject() throws CoreException {
        final String projectName = getProjectName();
        if (projectName.isEmpty()) {
            return null;
        }
        final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        if (!project.exists()) {
            throw newCoreException("Project '" + projectName + "' cannot be found in workspace");
        }
        return project;
    }

    @Override
    public boolean isRemoteAgent() throws CoreException {
        return Boolean.valueOf(configuration.getAttribute(REMOTE_AGENT, "false"));
    }

    @Override
    public String getAgentConnectionHost() throws CoreException {
        if (isRemoteAgent()) {
            final String host = getAgentConnectionHostValue();
            if (host.isEmpty()) {
                throw newCoreException("Server IP cannot be empty");
            }
            return host;
        }
        return AgentConnectionServer.DEFAULT_CLIENT_HOST;
    }

    @Override
    public int getAgentConnectionPort() throws CoreException {
        if (isRemoteAgent()) {
            final String port = getAgentConnectionPortValue();
            final Integer portAsInt = Ints.tryParse(port);
            if (portAsInt == null || !Range.closed(1, MAX_PORT).contains(portAsInt)) {
                throw newCoreException(
                        String.format("Server port '%s' must be an Integer between 1 and %,d", port, MAX_PORT));
            }
            if (portAsInt < 0) {
                throw newCoreException("Unable to find free port");
            }
            return portAsInt;
        }
        return AgentConnectionServer.findFreePort();
    }

    @Override
    public int getAgentConnectionTimeout() throws CoreException {
        if (isRemoteAgent()) {
            final String timeout = getAgentConnectionTimeoutValue();
            final Integer timeoutAsInt = Ints.tryParse(timeout);
            if (timeoutAsInt == null || !Range.closed(1, MAX_TIMEOUT).contains(timeoutAsInt)) {
                throw newCoreException(String.format("Connection timeout '%s' must be an Integer between 1 and %,d",
                        timeout, MAX_TIMEOUT));
            }
            return timeoutAsInt;
        }
        return AgentConnectionServer.DEFAULT_CLIENT_CONNECTION_TIMEOUT;
    }

    @Override
    public String getAgentConnectionHostValue() throws CoreException {
        return configuration.getAttribute(REMOTE_HOST_ATTRIBUTE, "");
    }

    @Override
    public String getAgentConnectionPortValue() throws CoreException {
        return configuration.getAttribute(REMOTE_PORT_ATTRIBUTE, "");
    }

    @Override
    public String getAgentConnectionTimeoutValue() throws CoreException {
        return configuration.getAttribute(REMOTE_TIMEOUT_ATTRIBUTE, "");
    }

    @Override
    public void setRemoteAgentValue(final String customRemote) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(REMOTE_AGENT, customRemote);
    }

    @Override
    public void setAgentConnectionHostValue(final String host) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(REMOTE_HOST_ATTRIBUTE, host);
    }

    @Override
    public void setAgentConnectionPortValue(final String port) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(REMOTE_PORT_ATTRIBUTE, port);
    }

    @Override
    public void setAgentConnectionTimeoutValue(final String timeout) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(REMOTE_TIMEOUT_ATTRIBUTE, timeout);
    }

    @Override
    public void fillDefaults() throws CoreException {
        final RedPreferences preferences = RedPlugin.getDefault().getPreferences();
        setRemoteAgentValue(String.valueOf(false));
        setAgentConnectionHostValue(preferences.getLaunchRemoteHost());
        setAgentConnectionPortValue(preferences.getLaunchRemotePort());
        setAgentConnectionTimeoutValue(preferences.getLaunchRemoteTimeout());
        setProjectName("");
        setProcessFactory(LaunchConfigurationsWrappers.FACTORY_ID);
    }

    private void setProcessFactory(final String id) throws CoreException {
        final ILaunchConfigurationWorkingCopy launchCopy = asWorkingCopy();
        launchCopy.setAttribute(DebugPlugin.ATTR_PROCESS_FACTORY_ID, id);
    }

    public ILaunchConfigurationWorkingCopy asWorkingCopy() throws CoreException {
        return configuration instanceof ILaunchConfigurationWorkingCopy
                ? (ILaunchConfigurationWorkingCopy) configuration : configuration.getWorkingCopy();
    }

}
