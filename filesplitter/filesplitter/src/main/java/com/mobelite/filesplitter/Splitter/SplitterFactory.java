package com.mobelite.filesplitter.Splitter;

public final class SplitterFactory {
    public static SplitterImpl createInstance() {
        return new SplitterImpl();
    }
}
