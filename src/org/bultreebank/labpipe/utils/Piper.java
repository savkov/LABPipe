package org.bultreebank.labpipe.utils;

/**
 * <code>Piper</code> pipes a number of processes in the way this is done in by 
 * the | operator in a Linux shell. Note that in fact the static method {@link #pipe(java.lang.Process[]) }
 * does the piping using objects of this class.
 * 
 * @see <a href="http://blog.bensmann.com/piping-between-processes">http://blog.bensmann.com/piping-between-processes</a>
 * @author Ralf Bensmann
 */
public class Piper implements java.lang.Runnable {

    private java.io.InputStream input;
    private java.io.OutputStream output;

    /**
     * Constructs a <code>Runnable</code> object
     * 
     * @param   input   process <code>InputStream</code>
     * @param   output  process <code>OutputStream</code>
     * 
     */
    public Piper(java.io.InputStream input, java.io.OutputStream output) {
        this.input = input;
        this.output = output;
    }

    /**
     * Runs the <code>Process</code>
     */
    public void run() {
        try {
            // Create 512 bytes buffer
            byte[] b = new byte[512];
            int read = 1;
            // As long as data is read; -1 means EOF
            while (read > -1) {
                // Read bytes into buffer
                read = input.read(b, 0, b.length);
                if (read > -1) {
                    // Write bytes to output
                    output.write(b, 0, read);
                }
            }
        } catch (Exception e) {
            // Something happened while reading or writing streams; pipe is broken
            throw new RuntimeException("Broken pipe", e);
        } finally {
            try {
                input.close();
            } catch (Exception e) {
            }
            try {
                output.close();
            } catch (Exception e) {
            }
        }
    }
    
    /**
     * Pipes a list of <code>Process</code> objects together like a shell pipe.
     * 
     * @param   proc    list of processes
     * 
     * @return  InputStream - final output
     * 
     */
    public static java.io.InputStream pipe(java.lang.Process... proc) throws java.lang.InterruptedException {
        // Start Piper between all processes
        java.lang.Process p1;
        java.lang.Process p2;
        for (int i = 0; i < proc.length; i++) {
            p1 = proc[i];
            // If there's one more process
            if (i + 1 < proc.length) {
                p2 = proc[i + 1];
                // Start piper
                new Thread(new Piper(p1.getInputStream(), p2.getOutputStream())).start();
            }
        }
        java.lang.Process last = proc[proc.length - 1];
        // Wait for last process in chain; may throw InterruptedException
        last.waitFor();
        // Return its InputStream
        return last.getInputStream();
    }
    
}