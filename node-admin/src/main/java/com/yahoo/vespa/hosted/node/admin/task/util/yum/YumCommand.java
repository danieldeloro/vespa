// Copyright 2020 Oath Inc. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
package com.yahoo.vespa.hosted.node.admin.task.util.yum;

import com.yahoo.vespa.hosted.node.admin.component.TaskContext;
import com.yahoo.vespa.hosted.node.admin.task.util.process.CommandLine;
import com.yahoo.vespa.hosted.node.admin.task.util.process.Terminal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author freva
 */
public abstract class YumCommand<T extends YumCommand<T>> {

    private List<String> enabledRepos = List.of();

    /** Enables the given repos for this command */
    public T enableRepo(String... repo) {
        enabledRepos = List.of(repo);
        return getThis();
    }

    protected abstract T getThis(); // Hack to get around unchecked cast warning

    protected void addParametersToCommandLine(CommandLine commandLine) {
        commandLine.add("--assumeyes");
        enabledRepos.forEach(repo -> commandLine.add("--enablerepo=" + repo));
    }

    public abstract boolean converge(TaskContext context);


    public static class GenericYumCommand extends YumCommand<GenericYumCommand> {
        private static final Pattern UNKNOWN_PACKAGE_PATTERN = Pattern.compile("(?dm)^No package ([^ ]+) available\\.$");

        private final Terminal terminal;
        private final String yumCommand;
        private final Pattern commandOutputNoopPattern;
        private final List<YumPackageName> packages;
        private final List<String> options = new ArrayList<>();

        GenericYumCommand(Terminal terminal, String yumCommand, List<YumPackageName> packages, Pattern commandOutputNoopPattern) {
            this.terminal = terminal;
            this.yumCommand = yumCommand;
            this.packages = packages;
            this.commandOutputNoopPattern = commandOutputNoopPattern;

            switch (yumCommand) {
                case "install": {
                    if (packages.size() > 1) options.add("skip_missing_names_on_install=False");
                    break;
                }
                case "upgrade": {
                    if (packages.size() > 1) options.add("skip_missing_names_on_update=False");
                    break;
                }
                case "remove": break;
                default: throw new IllegalArgumentException("Unknown yum command: " + yumCommand);
            }

            if (packages.isEmpty() && ! "upgrade".equals(yumCommand)) {
                throw new IllegalArgumentException("No packages specified");
            }
        }

        @Override
        protected void addParametersToCommandLine(CommandLine commandLine) {
            super.addParametersToCommandLine(commandLine);
            options.forEach(option -> commandLine.add("--setopt", option));
        }

        @Override
        public boolean converge(TaskContext context) {
            if (packages.isEmpty() && ! "upgrade".equals(yumCommand)) {
                throw new IllegalArgumentException("No packages specified");
            }

            CommandLine commandLine = terminal.newCommandLine(context);
            commandLine.add("yum", yumCommand);
            addParametersToCommandLine(commandLine);
            commandLine.add(packages.stream().map(YumPackageName::toName).collect(Collectors.toList()));

            // There's no way to figure out whether a yum command would have been a no-op.
            // Therefore, run the command and parse the output to decide.
            boolean modifiedSystem = commandLine
                    .executeSilently()
                    .mapOutput(this::mapOutput);

            if (modifiedSystem) {
                commandLine.recordSilentExecutionAsSystemModification();
            }

            return modifiedSystem;
        }

        private boolean mapOutput(String output) {
            Matcher unknownPackageMatcher = UNKNOWN_PACKAGE_PATTERN.matcher(output);
            if (unknownPackageMatcher.find()) {
                throw new IllegalArgumentException("Unknown package: " + unknownPackageMatcher.group(1));
            }

            return !commandOutputNoopPattern.matcher(output).find();
        }

        protected GenericYumCommand getThis() { return this; }
    }


    public static class InstallFixedYumCommand extends YumCommand<InstallFixedYumCommand> {
        // Note: "(?dm)" makes newline be \n (only), and enables multiline mode where ^$ match lines with find()
        private static final Pattern CHECKING_FOR_UPDATE_PATTERN =
                Pattern.compile("(?dm)^Package matching [^ ]+ already installed\\. Checking for update\\.$");
        private static final Pattern NOTHING_TO_DO_PATTERN = Pattern.compile("(?dm)^Nothing to do$");

        private final Terminal terminal;
        private final YumPackageName yumPackage;

        InstallFixedYumCommand(Terminal terminal, YumPackageName yumPackage) {
            this.terminal = terminal;
            this.yumPackage = yumPackage;
        }

        @Override
        public boolean converge(TaskContext context) {
            String targetVersionLockName = yumPackage.toVersionLockName();

            boolean alreadyLocked = terminal
                    .newCommandLine(context)
                    .add("yum", "--quiet", "versionlock", "list")
                    .executeSilently()
                    .getOutputLinesStream()
                    .map(YumPackageName::parseString)
                    .filter(Optional::isPresent) // removes garbage first lines, even with --quiet
                    .map(Optional::get)
                    .anyMatch(packageName -> {
                        // Ignore lines for other packages
                        if (packageName.getName().equals(yumPackage.getName())) {
                            // If existing lock doesn't exactly match the full package name,
                            // it means it's locked to another version and we must remove that lock.
                            String versionLockName = packageName.toVersionLockName();
                            if (versionLockName.equals(targetVersionLockName)) {
                                return true;
                            } else {
                                terminal.newCommandLine(context)
                                        .add("yum", "versionlock", "delete", versionLockName)
                                        .execute();
                            }
                        }

                        return false;
                    });

            boolean modified = false;

            if (!alreadyLocked) {
                CommandLine commandLine = terminal.newCommandLine(context).add("yum", "versionlock", "add");
                // If the targetVersionLockName refers to a package in a by-default-disabled repo,
                // we must enable the repo unless targetVersionLockName is already installed.
                // The other versionlock commands (list, delete) does not require --enablerepo.
                addParametersToCommandLine(commandLine);
                commandLine.add(targetVersionLockName).execute();
                modified = true;
            }

            // The following 3 things may happen with yum install:
            //  1. The package is installed or upgraded to the target version, in case we'd return
            //     true from converge()
            //  2. The package is already installed at target version, in case
            //     "Nothing to do" is printed in the last line and we may return false from converge()
            //  3. The package is already installed but at a later version than the target version,
            //     in case the last 2 lines of the output is:
            //       - "Package matching yakl-client-0.10-654.el7.x86_64 already installed. Checking for update."
            //       - "Nothing to do"
            //     And in case we need to downgrade and return true from converge()

            var installCommand = terminal.newCommandLine(context).add("yum", "install");
            addParametersToCommandLine(installCommand);
            installCommand.add(yumPackage.toName());

            String output = installCommand.executeSilently().getUntrimmedOutput();

            if (NOTHING_TO_DO_PATTERN.matcher(output).find()) {
                if (CHECKING_FOR_UPDATE_PATTERN.matcher(output).find()) {
                    // case 3.
                    var upgradeCommand = terminal.newCommandLine(context).add("yum", "downgrade");
                    addParametersToCommandLine(upgradeCommand);
                    upgradeCommand.add(yumPackage.toName()).execute();
                    modified = true;
                } else {
                    // case 2.
                }
            } else {
                // case 1.
                installCommand.recordSilentExecutionAsSystemModification();
                modified = true;
            }

            return modified;
        }

        protected InstallFixedYumCommand getThis() { return this; }
    }
}
