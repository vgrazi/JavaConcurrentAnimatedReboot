package com.vgrazi.javaconcurrentanimated.study;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StyleStudy extends JFrame {

    private StyleSheet styleSheet = new StyleSheet();
    private HTMLDocument htmlDocument;
    private final String blanks = "                                  "; // using Java 8 :(

    public static void main(String[] args) {
        StyleStudy stylePlay = new StyleStudy();
        stylePlay.setVisible(true);
    }

    public StyleStudy() {
        setSize(400, 400);
        HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
        htmlDocument = (HTMLDocument) htmlEditorKit.createDefaultDocument();
        JTextPane jTextPane = new JTextPane();
        jTextPane.setEditorKit(htmlEditorKit);
        jTextPane.setDocument(htmlDocument);

        try {
            Element htmlElement = htmlDocument.getRootElements()[0];
            htmlDocument.setInnerHTML(htmlElement, "" +
                    " <html>\n" +
                    "   <body>\n" +
                    "     <div id=\"Contents\">\n" +
                    "       <p>Some text</p>\n" +
                    "     </div>\n" +
                    "   </body>\n" +
                    " </html>\n" +
                    ""
            );

//            addInnerContent("" +
//                    "   <body>\n" +
//                    "     <div id=\"Contents\">\n" +
//                            "       Some text\n" +
//                    "     </div>\n" +
//                    "   </body>\n" +
//                            ""
//            );

            displayHtml(null, 0);
            htmlEditorKit.setStyleSheet(styleSheet);
            add(jTextPane, BorderLayout.CENTER);
            super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.schedule(() -> {
                try {
                    addInnerContent("<p><span class='style-1'>replaced test 1</span></p></div>");
//                    addOuterContent("" +
//                            "<div id=\"Contents\">" +
//                            "    <p class='style-1'>replaced test 1</p>" +
//                            "</div>");
                } catch (IOException | BadLocationException e) {
                    e.printStackTrace();
                }
            }, 2000, TimeUnit.MILLISECONDS);
            scheduler.schedule(() -> {
                try {
                    addInnerContent("<p><p><span class='style-1'>replaced test 2</span></p></p>");
//                    addOuterContent("" +
//                            "<div id=\"Contents\">" +
//                            "    <p class='style-1'>replaced test 1</p>" +
//                            "</div>");
                } catch (IOException | BadLocationException e) {
                    e.printStackTrace();
                }
            }, 4000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean flip;
    private void addInnerContent(String content) throws IOException, BadLocationException {
        Element divElement = htmlDocument.getElement("Contents");
        if(divElement == null) {
            content = "<div id='Contents'>" + content + "</div>";
            addOuterContent(content);
        }

        htmlDocument.setInnerHTML(divElement, content);
        styleSheet = htmlDocument.getStyleSheet();
        if (flip) {
            styleSheet.addRule(".style-1 {color: 'red'; align='left'}");
        }
        else {
            styleSheet.addRule(".style-1 {color: 'green'; align='left'}");
        }
        reapplyStyles();
        flip = !flip;
        displayHtml(null, 0);
    }

    private void addOuterContent(String content) throws IOException, BadLocationException {
        Element divElement = htmlDocument.getElement("Contents");

        htmlDocument.setOuterHTML(divElement, content);
        styleSheet.addRule(".style-1 {color: 'red'; align='left'}");
        reapplyStyles();
        displayHtml(null, 0);
    }

    private void reapplyStyles() {
        Element sectionElem = htmlDocument.getRootElements()[0];
        int paraCount = sectionElem.getElementCount();
        for (int i = 0; i < paraCount; i++) {
            Element e = sectionElem.getElement(i);
            int rangeStart = e.getStartOffset();
            int rangeEnd = e.getEndOffset();
            htmlDocument.setParagraphAttributes(rangeStart, rangeEnd - rangeStart, e.getAttributes(), true);
        }
    }

    private void displayHtml(Element node, int level) throws BadLocationException {
        String padding = blanks.substring(0, level * 2);
        if (node == null) {
            System.out.println("===============");
            node = htmlDocument.getRootElements()[0];

        }
        System.out.println("****" + padding + node.getName());
        if (node.isLeaf()) {
            System.out.println("***-" + padding + "   " + htmlDocument.getText(node.getStartOffset(), node.getEndOffset() - node.getStartOffset()));
        } else {
            int count = node.getElementCount();
            for (int i = 0; i < count; i++) {
                Element element = node.getElement(i);
                displayHtml(element, level + 1);
            }
        }
    }


}
