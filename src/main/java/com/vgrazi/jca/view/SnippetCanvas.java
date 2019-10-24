package com.vgrazi.jca.view;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.io.IOException;

@Component
public class SnippetCanvas extends JTextPane implements InitializingBean {
    private final HTMLDocument htmlDocument;
    @Value("${snippet-font-family}")
    private String fontFamily;

    @Value("${snippet-font-style}")
    private String fontStyle;

    @Value("${snippet-font-size}")
    private int fontSize;

    private StyleSheet styleSheet;

    private Element htmlElement;

    public SnippetCanvas() {
        HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
        styleSheet = new StyleSheet();
        htmlEditorKit.setStyleSheet(styleSheet);
        htmlDocument = (HTMLDocument) htmlEditorKit.createDefaultDocument();
        this.setEditorKit(htmlEditorKit);
        this.setDocument(htmlDocument);
        try {
            htmlElement = htmlDocument.getRootElements()[0];
            // set a skeleton div. This is where our snippet will go
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSnippet(String content) throws IOException, BadLocationException {
        Element divElement = htmlDocument.getElement("Contents");
        String divContent = String.format("<div>%s</div>", content);
        htmlDocument.setInnerHTML(divElement, divContent);
//        System.out.println("displaying " + divContent);
//        HtmlUtils.displayHtml(htmlDocument, null, 0);
        applyStyles();
    }

    public void removeContent() {
        System.out.println("Removing box");
        try {
            Element divElement = htmlDocument.getElement("Contents");
            htmlDocument.setInnerHTML(divElement, "<div></div>");
        } catch (BadLocationException | IOException e) {
            throw new IllegalArgumentException(e);
        }
//        HtmlUtils.displayHtml(htmlDocument, null, 0);
    }

    /**
     * rule is a selector followed by a bracket-enclosed style, eg
     * ".synchronized {background-color: yellow; color: green;}"
     * Important - after all style rules are added, call {@link #applyStyles()}
     */
    public void addStyleRule(String rule) {
        styleSheet.addRule(rule);
    }

    /**
     * To be called after adding content or style rules, to apply the styles
     */
    public void applyStyles() {
        Element sectionElem = htmlDocument.getRootElements()[0];

        int paraCount = sectionElem.getElementCount();
        for (int i = 0; i < paraCount; i++) {
            Element e = sectionElem.getElement(i);
            int rangeStart = e.getStartOffset();
            int rangeEnd = e.getEndOffset();
            htmlDocument.setParagraphAttributes(rangeStart, rangeEnd - rangeStart,
                    e.getAttributes(), true);
        }
    }

    @Override
    public void afterPropertiesSet() {
        try {
            htmlDocument.setInnerHTML(htmlElement, String.format("" +
                    "<html>" +
                    "  <header>" +
                    "    <style type=\"text/css\">" +
                    "       div { font-family:'%s'; font-size: %d pt; font-style:'%s'}" +
                    "     </style>" +
                    "  </header>" +
                    "  <body>" +
                    "    <div id=\"Contents\">" +
                    "    </div>" +
                    "  </body>" +
                    " </html>", fontFamily, fontSize, fontStyle)
            );

//            styleSheet = htmlEditorKit.getStyleSheet();
            // todo: change styles in the stylesheet to bold selected parts of code
//            HtmlUtils.displayHtml(htmlDocument, null, 0);
//            changer();
        } catch (BadLocationException | IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

//    int counter;
//
//    public void changer() {
//        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
//        scheduledExecutorService.scheduleAtFixedRate(this::change, 2000, 500, TimeUnit.MILLISECONDS);
//    }
//
//    // Diagnostic code: todo: remove
//    public void change() {
//        System.out.println("Counter:" + counter);
//        if (counter == 4) {
//            System.out.println("Replacing");
//            try {
//                setSnippet("<div id='Contents'>" +
//                        "<span class='synchronized'>replaced test 1</span><br>" +
//                        "<span class='synchronized'>replaced test 2</span><br>" +
//                        "</div>" +
//                        "");
////                HtmlUtils.displayHtml(htmlDocument, null, 0);
//            } catch (BadLocationException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        } else if (counter == 7) {
//            removeContent();
//        }
//
//        if (counter++ % 2 == 0) {
//            styleSheet.addRule(".synchronized {background-color: yellow; color: green;}");
//        } else {
//            styleSheet.addRule(".synchronized {background-color: red; color: white;}");
//        }
//        reapplyStyles();
//    }


}
