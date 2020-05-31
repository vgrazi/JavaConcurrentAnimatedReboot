package com.vgrazi.jca.util;

import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import java.util.logging.Logger;

public class HtmlUtils {
    private final static String blanks = "                                  "; // using Java 8 :(

    private static Logger logger = Logger.getLogger("HtmlUtils");
    private static void println(String message) {
        logger.info(message);
    }
    /**
     * Displays the html structure of the supplied node.
     * To start, pass in an HTMLDocument, null, 0
     */
    public static void displayHtml(HTMLDocument htmlDocument, Element node, int level) {
        try {
            String padding = blanks.substring(0, level * 2);
            if (node == null) {
                println("===============");
                node = htmlDocument.getRootElements()[0];

            }
            println("****" + padding + node.getName());
            if (node.isLeaf()) {
                println("***-" + padding + "   " + htmlDocument.getText(node.getStartOffset(), node.getEndOffset() - node.getStartOffset()));
            } else {
                int count = node.getElementCount();
                for (int i = 0; i < count; i++) {
                    Element element = node.getElement(i);
                    displayHtml(htmlDocument, element, level + 1);
                }
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}
