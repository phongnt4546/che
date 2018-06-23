/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.stack;

import static org.eclipse.che.commons.lang.NameGenerator.generate;
import static org.eclipse.che.selenium.core.constant.TestProjectExplorerContextMenuConstants.ContextMenuCommandGoals.RUN;
import static org.eclipse.che.selenium.pageobject.dashboard.NewWorkspace.Stack.GO;

import com.google.inject.Inject;
import java.util.ArrayList;
import org.eclipse.che.selenium.core.client.TestWorkspaceServiceClient;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.pageobject.dashboard.Dashboard;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Skoryk Serhii */
public class CreateWorkspaceFromGoStackTest {
  private static final String WORKSPACE_NAME = generate("workspace", 4);
  private static final String DESKTOP_GO_SIMPLE_PROJECT = "desktop-go-simple";
  private static final String WEB_GO_SIMPLE_PROJECT = "web-go-simple";

  ArrayList<String> projects = new ArrayList<>();

  @Inject private Dashboard dashboard;
  @Inject private StackHelper stackHelper;
  @Inject private DefaultTestUser defaultTestUser;
  @Inject private TestWorkspaceServiceClient workspaceServiceClient;

  @BeforeClass
  public void setUp() {
    projects.add(DESKTOP_GO_SIMPLE_PROJECT);
    projects.add(WEB_GO_SIMPLE_PROJECT);

    dashboard.open();
  }

  @AfterClass
  public void tearDown() throws Exception {
    workspaceServiceClient.delete(WORKSPACE_NAME, defaultTestUser.getName());
  }

  @Test
  public void checkWorkspaceCreationFromGoStack() {
    String currentWindow;

    stackHelper.createWorkspaceWithProjectsFromStack(GO, WORKSPACE_NAME, projects);

    currentWindow = stackHelper.switchToIdeAndWaitWorkspaceIsReadyToUse();

    stackHelper.waitProjectInitialization(DESKTOP_GO_SIMPLE_PROJECT);
    stackHelper.waitProjectInitialization(WEB_GO_SIMPLE_PROJECT);

    stackHelper.startCommandAndCheckResult(
        DESKTOP_GO_SIMPLE_PROJECT,
        RUN,
        "desktop-go-simple:run",
        "Hello, world. Sqrt(2) = 1.4142135623730951");

    stackHelper.startCommandAndCheckResult(WEB_GO_SIMPLE_PROJECT, RUN, "run", "listening on");
    stackHelper.startCommandAndCheckApp(currentWindow, "//pre[contains(text(),'Hello there')]");
    stackHelper.closeProcessTabWithAskDialog("run");

    stackHelper.startCommandAndCheckResult(
        WEB_GO_SIMPLE_PROJECT, RUN, "web-go-simple:run", "listening on");
    stackHelper.startCommandAndCheckApp(currentWindow, "//pre[contains(text(),'Hello there')]");
  }
}