package com.vgrazi.javaconcurrentanimated.study;

import java.awt.BorderLayout;
import java.awt.Container;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public class Main extends JFrame {
    private static Logger logger = Logger.getLogger("Main");
    private static void println(Object message) {
        logger.info(String.valueOf(message));
    }

    StyleSheet styleSheet = new StyleSheet();
    HTMLDocument htmlDocument;
    HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
    Element bodyElement;

    public static void main(String[] args) throws Exception {
        Main jTextPaneApp = new Main();
        jTextPaneApp.setVisible(true);
//        for (int i =0; i < 10; i++) {
//            Thread.currentThread().sleep(3000);
//            jTextPaneApp.change(i);
//        }
    }

    public Main() {
        setSize(400, 400);
        htmlEditorKit.setStyleSheet(styleSheet);
        htmlDocument = (HTMLDocument) htmlEditorKit.createDefaultDocument();
        JTextPane jTextPane = new JTextPane();
        jTextPane.setEditorKit(htmlEditorKit);
        jTextPane.setDocument(htmlDocument);

        try {
            Element htmlElement = htmlDocument.getRootElements()[0];
            htmlDocument.setInnerHTML(htmlElement, "" +
                    " <html>\n" +
                    "   <head>\n" +
                    "     <title>An example HTMLDocument</title>\n" +
                    "     <style type=\"text/css\">\n" +
                    "     </style>\n" +
                    "   </head>\n" +
                    "   <body>\n" +
                    "     <div id=\"main\">\n" +
                    "       <p>Paragraph 1-</p>\n" +
                    "       <p>Paragraph 2-</p>\n" +
                    "     </div>\n" +
                    "   </body>\n" +
                    " </html>" +
                    ""
            );
            bodyElement = htmlElement.getElement(0);

            Container contentPane = getContentPane();
            contentPane.add(jTextPane, BorderLayout.CENTER);
            super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reapplyStyles() {
        Element sectionElem = htmlDocument.getElement("main");
        if (sectionElem != null) {
            int paraCount = sectionElem.getElementCount();
            for (int i = 0; i < paraCount; i++) {
                Element e = sectionElem.getElement(i);
                int rangeStart = e.getStartOffset();
                int rangeEnd = e.getEndOffset();
                htmlDocument.setParagraphAttributes(rangeStart, rangeEnd - rangeStart,
                        e.getAttributes(), true);
            }
        }
    }

    boolean even;

    public void change(int i) throws Exception {
        println("Changing");
        Element div = htmlDocument.getElement("main");
        String htmlText;
        switch(i%3) {
            case 0:
                htmlText = "<div id='main'>hello</div>";
                break;
            case 1:
                htmlText = "<div id='main'>goodbye</div>";
                break;
            case 2:
            default:
                htmlText = "" +
                        "<div class='synchronized' id='main'>styledText" +
                        "</div>";
                break;
        }
        setSnippet(htmlText);
        if (even) {
            styleSheet.addRule(".synchronized {color: blue;}");
        } else {
            styleSheet.addRule(".synchronized {color: red;}");
        }
        reapplyStyles();
        even = !even;
    }

    private void setSnippet(String content) throws Exception {
        Element div = htmlDocument.getElement("main");
        htmlDocument.setInnerHTML(div, content);    }
}
