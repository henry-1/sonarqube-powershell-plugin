package org.sonar.plugins.psscriptanalyzer.readers;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.Optional;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.sonar.plugins.psscriptanalyzer.types.PsaRule;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PsaRuleReader {
	
	public final PsaRule read(final Path path) {		
		
		try {
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder builder = factory.newDocumentBuilder();
	        final Document doc = builder.parse(new FileInputStream(path.toFile()));
	        final Node node = doc.getElementsByTagName("rule").item(0);	        
            
            String ruleKey = getChildByName(node, "key").getTextContent();            
            String description = getChildByName(node,"description").getTextContent();
            String whyIsThisAnIssue = getChildByName(node, "whyIsThisAnIssue").getTextContent();
            
            PsaRule rule = new PsaRule(ruleKey, description, whyIsThisAnIssue);
                  
            // optional attributes, also set in the constructor with default values
            getChildText(node, "ruleType").ifPresent(rule::setRuleType);
            getChildText(node, "severity").ifPresent(rule::setSeverity); 
            getChildText(node, "remediationFunction").ifPresent(rule::setRemediationFunction);
            getChildText(node, "debtRemediationFunctionLinearOffset").ifPresent(rule::setDebRemediationFunctionLinearOffset);
            getChildText(node, "debtRemediationFunctionCoefficient").ifPresent(rule::setDebtRemediationFunctionCoefficient);
             
            return rule;
        
        } catch (Exception e) {
        	System.err.println("[PSA-Plugin] Unexpected error reading rules definitions" + e.getMessage());        	
        }
        
        return null;        
	}
	
	protected final static Optional<String> getChildText(Node root, String name) {
		Node child;
		try {
			child = getChildByName(root, name);
		} catch (Exception e) {
			return null;
		}
	    return Optional.ofNullable(child == null ? null : child.getTextContent());
	}
	
	protected final static Node getChildByName(final Node root, final String name) throws Exception {
        final NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node child = children.item(i);
            if (name.equalsIgnoreCase(child.getNodeName())) {
            	return child;
            }
        }
        return null;
    }
}
