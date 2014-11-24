package org.jenkinsci.plugins.beakerbuilder;

import hudson.util.VariableResolver;

public class XMLEscapingVariableResolver implements VariableResolver<String> {

    private final VariableResolver<String> inner;

    public XMLEscapingVariableResolver(VariableResolver<String> inner) {
        this.inner = inner;
    }

    public String resolve(String name) {
        String value = inner.resolve(name);
        return value
            .replaceAll("&", "&amp;")
            .replaceAll(">", "&gt;")
            .replaceAll("<", "&lt;")
            .replaceAll("\"", "&quot;")
            .replaceAll("'", "&apos;");
    }

}
