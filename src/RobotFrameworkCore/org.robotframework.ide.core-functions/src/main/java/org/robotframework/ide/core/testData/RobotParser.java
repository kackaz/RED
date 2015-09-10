/*
 * Copyright 2015 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.core.testData;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import org.robotframework.ide.core.testData.importer.ResourceImporter;
import org.robotframework.ide.core.testData.importer.VariablesFileImportReference;
import org.robotframework.ide.core.testData.importer.VariablesImporter;
import org.robotframework.ide.core.testData.model.RobotFileOutput;
import org.robotframework.ide.core.testData.model.RobotFileOutput.BuildMessage;
import org.robotframework.ide.core.testData.model.RobotFileOutput.Status;
import org.robotframework.ide.core.testData.model.RobotProjectHolder;
import org.robotframework.ide.core.testData.text.read.TxtRobotFileParser;


public class RobotParser {

    private boolean shouldEagerImport = false;
    private final RobotProjectHolder robotProject;

    private static final List<IRobotFileParser> availableFormatParsers = new LinkedList<>();
    static {
        availableFormatParsers.add(new TxtRobotFileParser());
    }


    public RobotParser(final RobotProjectHolder robotProject) {
        this.robotProject = robotProject;
    }


    /**
     * Should be used for unsaved editor content. Parsed output is not replacing
     * saved robot model in {@link RobotProjectHolder} object.
     * 
     * @param fileContent
     * @param fileOrDir
     * @return
     */
    public RobotFileOutput parseEditorContent(final String fileContent,
            final File fileOrDir) {
        RobotFileOutput robotFile = new RobotFileOutput();

        final IRobotFileParser parserToUse = getParser(fileOrDir);

        if (parserToUse != null) {
            ByteArrayInputStream bais = new ByteArrayInputStream(new byte[0]);
            if (fileContent != null && fileContent.length() > 0) {
                bais = new ByteArrayInputStream(fileContent.getBytes(Charset
                        .forName("UTF-8")));
            }

            parserToUse.parse(robotFile, bais, fileOrDir);
            importExternal(robotFile);
        } else {
            robotFile.addBuildMessage(BuildMessage.createErrorMessage(
                    "No parser found for file.", fileOrDir.getAbsolutePath()));
            robotFile.setStatus(Status.FAILED);
        }

        return robotFile;
    }


    public List<RobotFileOutput> parse(final File fileOrDir) {
        List<RobotFileOutput> output = new LinkedList<>();
        parse(fileOrDir, output);
        return output;
    }


    private void parse(final File fileOrDir, final List<RobotFileOutput> output) {
        if (fileOrDir != null) {
            boolean isDir = fileOrDir.isDirectory();
            if (isDir) {
                int currentOutputSize = output.size();
                File[] files = fileOrDir.listFiles();
                for (File f : files) {
                    parse(f, output);
                }

                if (currentOutputSize < output.size()) {
                    // is high level test suite
                    // TODO: place where in case of TH type we should put
                    // information
                }
            } else if (robotProject.shouldBeLoaded(fileOrDir)) {
                final IRobotFileParser parserToUse = getParser(fileOrDir);

                if (parserToUse != null) {
                    RobotFileOutput robotFile = new RobotFileOutput();
                    output.add(robotFile);

                    // do not change order !!! for performance reason is better
                    // to execute importing of variables before add to model,
                    // which replace previous object
                    parserToUse.parse(robotFile, fileOrDir);
                    importExternal(robotFile);
                    robotProject.addModelFile(robotFile);
                }
            }
        }
    }


    private void importExternal(final RobotFileOutput robotFile) {
        if (robotFile.getStatus() == Status.PASSED) {
            if (shouldEagerImport) {
                // eager get resources example
                final ResourceImporter resImporter = new ResourceImporter();
                resImporter.importResources(this, robotFile);
            }

            final VariablesImporter varImporter = new VariablesImporter();
            List<VariablesFileImportReference> varsImported = varImporter
                    .importVariables(robotProject.getRobotRuntime(),
                            robotProject, robotFile);
            robotFile.addVariablesReferenced(varsImported);
        }
    }


    private IRobotFileParser getParser(final File fileOrDir) {
        IRobotFileParser parserToUse = null;
        for (IRobotFileParser parser : availableFormatParsers) {
            if (parser.canParseFile(fileOrDir)) {
                parserToUse = parser;
                break;
            }
        }
        return parserToUse;
    }


    public void setEagerImport(final boolean shouldEagerImport) {
        this.shouldEagerImport = shouldEagerImport;
    }
}
