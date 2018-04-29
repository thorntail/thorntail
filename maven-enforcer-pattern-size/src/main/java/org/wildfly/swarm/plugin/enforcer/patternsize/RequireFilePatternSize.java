/**
 * Copyright 2015-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wildfly.swarm.plugin.enforcer.patternsize;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.enforcer.rule.api.EnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;

/**
 * @author Juan Gonzalez
 */
public class RequireFilePatternSize implements EnforcerRule {

    private RequiredFilePattern[] filePatterns;

    public RequiredFilePattern[] getRequiredFilePatterns() {
        return filePatterns;
    }

    public void setRequiredFilePatterns(RequiredFilePattern[] filePatterns) {
        this.filePatterns = filePatterns;
    }

    List<RequiredFilePatternFailure> checkFilePattern(final RequiredFilePattern requiredFilePattern) throws IOException {
        if (requiredFilePattern == null || requiredFilePattern.getDirectory() == null) {
            return null;
        }

        if (requiredFilePattern.getPattern() == null) {
            return null;
        }

        final Pattern pattern = Pattern.compile(requiredFilePattern.getPattern());

        String directory = requiredFilePattern.getDirectory();
        File directoryFile = new File(directory);

        final List<File> matchedFiles = new ArrayList<File>();

        if (Files.isDirectory(directoryFile.toPath())) {
            Files.walkFileTree(directoryFile.toPath(), EnumSet.of(FileVisitOption.FOLLOW_LINKS),
                               requiredFilePattern.isRecursive() ? Integer.MAX_VALUE : 1,
                               new SimpleFileVisitor<Path>() {
                                   @Override
                                   public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {
                                       Matcher matcher = pattern.matcher(filePath.getFileName().toString());
                                       if (matcher.matches()) {
                                           File file = filePath.toFile();
                                           matchedFiles.add(file);
                                       }

                                       return super.visitFile(filePath, attrs);
                                   }
                               });
        }

        List<RequiredFilePatternFailure> failures = new ArrayList<RequiredFilePatternFailure>();
        for (File matchedFile : matchedFiles) {
            long length = matchedFile.length();
            if (requiredFilePattern.getMinSize() != -1 && length < requiredFilePattern.getMinSize()) {
                failures.add(new RequiredFilePatternFailure(requiredFilePattern, matchedFile + " size(" + length + ") too small. Min. is " + requiredFilePattern.getMinSize()));
            } else if (requiredFilePattern.getMaxSize() != -1 && length > requiredFilePattern.getMaxSize()) {
                failures.add(new RequiredFilePatternFailure(requiredFilePattern, matchedFile + " size(" + length + ") too large. Max. is " + requiredFilePattern.getMaxSize()));
            }
        }

        return failures;
    }

    public void execute(EnforcerRuleHelper helper)
            throws EnforcerRuleException {

        RequiredFilePattern[] requiredFilePatterns = getRequiredFilePatterns();
        List<RequiredFilePatternFailure> failures = new ArrayList<RequiredFilePatternFailure>();

        if (requiredFilePatterns.length > 0) {
            for (RequiredFilePattern requiredFilePattern : requiredFilePatterns) {
                if (requiredFilePattern == null) {
                    failures.add(new RequiredFilePatternFailure(requiredFilePattern, "File pattern is empty"));
                }

                List<RequiredFilePatternFailure> failure = null;

                try {
                    failure = checkFilePattern(requiredFilePattern);
                } catch (IOException e) {
                    failures.add(new RequiredFilePatternFailure(requiredFilePattern, "Error while traversing files"));
                }

                if (failure != null && failure.size() > 0) {
                    failures.addAll(failure);
                }
            }
        } else {
            throw new EnforcerRuleException("The file pattern list is empty.");
        }

        if (!failures.isEmpty()) {
            String message = "Some files does not fullfill provided pattern rules:\n";
            StringBuilder buf = new StringBuilder();

            if (message != null) {
                buf.append(message + "\n");
            }

            for (RequiredFilePatternFailure fileFailure : failures) {
                if (fileFailure != null) {
                    buf.append("Failed pattern " + fileFailure.getPattern() + " in directory " + fileFailure.getDirectory() + "." + fileFailure.getMessage() + "\n");
                } else {
                    buf.append("(an empty file pattern or directory was given)\n");
                }
            }

            throw new EnforcerRuleException(buf.toString());
        }
    }

    @Override
    public boolean isCacheable() {
        return false;
    }

    @Override
    public boolean isResultValid(EnforcerRule cachedRule) {
        return false;
    }

    @Override
    public String getCacheId() {
        return null;
    }
}
