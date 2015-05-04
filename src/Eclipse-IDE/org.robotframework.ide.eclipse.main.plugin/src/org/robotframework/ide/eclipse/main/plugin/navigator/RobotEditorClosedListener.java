package org.robotframework.ide.eclipse.main.plugin.navigator;

import javax.inject.Inject;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.robotframework.ide.eclipse.main.plugin.RobotElementChange;
import org.robotframework.ide.eclipse.main.plugin.RobotModelEvents;
import org.robotframework.ide.eclipse.main.plugin.RobotSuiteFile;
import org.robotframework.ide.eclipse.main.plugin.tableeditor.RobotFormEditor;

public class RobotEditorClosedListener implements IPartListener {

    @Inject
    private IEventBroker broker;

    @Override
    public void partActivated(final IWorkbenchPart part) {
        // nothing to do
    }

    @Override
    public void partBroughtToTop(final IWorkbenchPart part) {
        // nothing to do
    }

    @Override
    public void partClosed(final IWorkbenchPart part) {
        // the robot editor was closed and maybe changes were not
        // saved, so we need to once again read the file in order to
        // properly refresh navigator content of this file
        if (part instanceof RobotFormEditor) {
            final RobotFormEditor robotEditor = (RobotFormEditor) part;
            final RobotSuiteFile suiteModel = robotEditor.provideSuiteModel();
            
            if (suiteModel.exists()) {
                suiteModel.refreshOnFileChange();

                broker.post(RobotModelEvents.EXTERNAL_MODEL_CHANGE, RobotElementChange.createChangedElement(suiteModel));
            }
        }
    }

    @Override
    public void partDeactivated(final IWorkbenchPart part) {
        // nothing to do
    }

    @Override
    public void partOpened(final IWorkbenchPart part) {
        // nothing to do
    }

}
