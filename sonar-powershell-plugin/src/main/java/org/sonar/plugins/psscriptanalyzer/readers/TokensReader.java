package org.sonar.plugins.psscriptanalyzer.readers;


import java.io.File;
import java.io.FileInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.input.BOMInputStream;
import org.sonar.plugins.psscriptanalyzer.types.Token;
import org.sonar.plugins.psscriptanalyzer.types.Tokens;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TokensReader {


    public Tokens read(final File file) throws Throwable {
        final Tokens tokens = new Tokens();
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document doc = builder.parse(new FileInputStream(file));
        tokens.setComplexity(Integer.parseInt(doc.getDocumentElement().getAttribute("complexity")));
        final NodeList list = doc.getElementsByTagName("Token");
        for (int i = 0; i < list.getLength(); i++) {
            try {
                final Node node = list.item(i);
                final Token token = new Token();
                token.setText(getChildByName(node, "Text").getTextContent());
                token.setValue(getChildByName(node, "Value").getTextContent());
                token.setTokenFlags(getChildByName(node, "TokenFlags").getTextContent());
                token.setKind(getChildByName(node, "Kind").getTextContent());
                token.setcType(getChildByName(node, "CType").getTextContent());

                token.setStartLineNumber(Integer.parseInt(getChildByName(node, "StartLineNumber").getTextContent()));
                token.setEndLineNumber(Integer.parseInt(getChildByName(node, "EndLineNumber").getTextContent()));
                token.setStartOffset(Long.parseLong(getChildByName(node, "StartOffset").getTextContent()));
                token.setEndOffset(Long.parseLong(getChildByName(node, "EndOffset").getTextContent()));
                token.setStartColumnNumber(
                        Integer.parseInt(getChildByName(node, "StartColumnNumber").getTextContent()));
                token.setEndColumnNumber(Integer.parseInt(getChildByName(node, "EndColumnNumber").getTextContent()));
                tokens.getTokens().add(token);
            } catch (Exception e) {
            	System.err.println("Unexpected error reading results" + e.getMessage());	
            }
        }
        return tokens;
    }

    protected static final Node getChildByName(final Node root, final String name) throws Exception {
        final NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node child = children.item(i);
            if (name.equalsIgnoreCase(child.getNodeName())) {
                return child;
            }

        }
        throw new Exception("Child node with name " + name + " was not found");
    }
}