package com.docd.purefm.file;

import com.docd.purefm.Environment;
import com.docd.purefm.settings.Settings;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public final class FileFactory {
    private FileFactory() {}


    @NotNull
    public static GenericFile newFile(String path) {
        return Settings.useCommandLine && Environment.hasBusybox() ?
                CommandLineFile.fromFile(new File(path)) :
                new JavaFile(path);
    }

    @NotNull
    public static GenericFile newFile(File path) {
        return Settings.useCommandLine && Environment.hasBusybox() ?
                CommandLineFile.fromFile(path) :
                new JavaFile(path);
    }

    @NotNull
    public static GenericFile newFile(File file, String name) {
        return Settings.useCommandLine && Environment.hasBusybox() ?
                CommandLineFile.fromFile(new File(file, name)) :
                new JavaFile(file, name);
    }
}