package org.sonar.plugins.psscriptanalyzer.types;

import java.util.LinkedList;
import java.util.List;

public class Tokens {

    private int complexity;

    private final List<Token> tokens = new LinkedList<>();

    public List<Token> getTokens() {
        return tokens;
    }

    public void setComplexity(int complexity) {
        this.complexity = complexity;
    }

    public int getComplexity() {
        return complexity;
    }
}