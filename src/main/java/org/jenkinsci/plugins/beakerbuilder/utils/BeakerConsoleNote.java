package org.jenkinsci.plugins.beakerbuilder.utils;

import hudson.Extension;
import hudson.MarkupText;
import hudson.console.ConsoleAnnotationDescriptor;
import hudson.console.ConsoleAnnotator;
import hudson.console.ConsoleNote;
import hudson.model.Run;

/**
 * Annotator which currently adds only color highlighting. There are two message categories: error, staring with
 * <i>ERROR</i> prefix, and info, which starts with <i>INFO</i> prefix.
 * 
 * @author vjuranek
 * 
 */
public class BeakerConsoleNote extends ConsoleNote<Run<?, ?>> {

    @Override
    public ConsoleAnnotator<Run<?, ?>> annotate(Run<?, ?> context, MarkupText text, int charPos) {
        if (text.getText().contains("ERROR"))
            text.addMarkup(0, text.length(), "<span style=\"font-weight: bold; color:red\">", "</span>");
        if (text.getText().contains("INFO"))
            text.addMarkup(0, text.length(), "<span style=\"color:#993300\">", "</span>");
        return null;
    }

    @Extension
    public static final class DescriptorImpl extends ConsoleAnnotationDescriptor {
        public String getDisplayName() {
            return "Beaker Console Note";
        }
    }

    private static final long serialVersionUID = 1L;
}
