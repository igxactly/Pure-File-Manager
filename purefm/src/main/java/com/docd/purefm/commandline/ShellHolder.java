package com.docd.purefm.commandline;

import android.util.Log;

import com.stericson.RootTools.execution.Shell;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * ShellHolder holds shared Shell instance
 */
public final class ShellHolder {

    private static int commandId;

    public static int getNextCommandId() {
        return commandId++;
    }

    private ShellHolder() {}

    private static Shell shell;

    public static synchronized void releaseShell() {
        if (shell != null) {
            try {
                shell.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            shell = null;
        }
    }

    /**
     * The shell is set by BrowserActivity and is released when BrowserActivity is destroyed
     *
     * @return shell shared Shell instance
     */
    @NotNull
    public static synchronized Shell getShell() {
        if (shell == null) {
            try {
                shell = ShellFactory.getShell();
            } catch (IOException e) {
                Log.w("getShell() error:", e);
            }
        }
        return shell;
    }
}