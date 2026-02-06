package org.sonar.plugins.psscriptanalyzer.types;

public class Token {
    private String text;

    private String value;

    private String tokenFlags;

    private String kind;

    private String cType;

    private int startLineNumber;

    private int endLineNumber;

    private int startColumnNumber;

    private int endColumnNumber;

    private long startOffset;

    private long endOffset;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTokenFlags() {
        return tokenFlags;
    }

    public void setTokenFlags(String tokenFlags) {
        this.tokenFlags = tokenFlags;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getcType() {
        return cType;
    }

    public void setcType(String cType) {
        this.cType = cType;
    }

    public int getStartLineNumber() {
        return startLineNumber;
    }

    public void setStartLineNumber(int startLineNumber) {
        this.startLineNumber = startLineNumber;
    }

    public int getEndLineNumber() {
        return endLineNumber;
    }

    public void setEndLineNumber(int endLineNumber) {
        this.endLineNumber = endLineNumber;
    }

    public int getStartColumnNumber() {
        return startColumnNumber;
    }

    public void setStartColumnNumber(int startColumnNumber) {
        this.startColumnNumber = startColumnNumber;
    }

    public int getEndColumnNumber() {
        return endColumnNumber;
    }

    public void setEndColumnNumber(int endColumnNumber) {
        this.endColumnNumber = endColumnNumber;
    }

    public long getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(long startOffset) {
        this.startOffset = startOffset;
    }

    public long getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(long endOffset) {
        this.endOffset = endOffset;
    }
}
