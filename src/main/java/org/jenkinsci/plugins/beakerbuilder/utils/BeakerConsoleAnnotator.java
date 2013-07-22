package org.jenkinsci.plugins.beakerbuilder.utils;

import hudson.console.LineTransformationOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Console annotator which annotates Beaker messages using {@link BeakerConsoleNote}. Annotated message has to start
 * with <i>[Beaker]</i> prefix.
 * 
 * @see BeakerConsoleNote
 * @see LineTransformationOutputStream
 * @author vjuranek
 * 
 */
public class BeakerConsoleAnnotator extends LineTransformationOutputStream {

    private final OutputStream out;

    public BeakerConsoleAnnotator(OutputStream out) {
        this.out = out;
    }

    @Override
    protected void eol(byte[] b, int len) throws IOException {
        String line = Charset.defaultCharset().decode(ByteBuffer.wrap(b, 0, len)).toString();
        if (line.startsWith("[Beaker]"))
            new BeakerConsoleNote().encodeTo(out);
        out.write(b, 0, len);
    }

    @Override
    public void close() throws IOException {
        super.close();
        out.close();
    }

}