package com.vgrazi.jca.view;

import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.io.IOException;

@Component
public class SnippetCanvas extends JTextPane {
    private final HTMLDocument htmlDocument;

    public SnippetCanvas() {
        HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
        StyleSheet styleSheet = new StyleSheet();
        htmlEditorKit.setStyleSheet(styleSheet);
        htmlDocument = (HTMLDocument) htmlEditorKit.createDefaultDocument();
        this.setEditorKit(htmlEditorKit);
        this.setDocument(htmlDocument);
        try {
            Element htmlElement = htmlDocument.getRootElements()[0];
            // set a skeleton div. This is where our snippet will go
            htmlDocument.setInnerHTML(htmlElement, "" +
                    " <html>\n" +
                    "   <body>\n" +
                    "     <div id=\"Contents\">\n" +
                    "     </div>\n" +
                    "   </body>\n" +
                    " </html>" +
                    ""
            );

            styleSheet = htmlEditorKit.getStyleSheet();
            // todo: change styles in the stylesheet do bold selected parts of code
//            HtmlUtils.displayHtml(htmlDocument, null, 0);
//            changer();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeContent() {
        System.out.println("Removing box");
        try {
            Element divElement = htmlDocument.getElement("Contents");
            htmlDocument.setInnerHTML(divElement,"<div id='Contents'></div>");
        } catch (BadLocationException | IOException e) {
            throw new IllegalArgumentException(e);
        }
//        HtmlUtils.displayHtml(htmlDocument, null, 0);
    }


    private void reapplyStyles() {
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

//    int counter;
//    public void changer() {
//        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
//        scheduledExecutorService.scheduleAtFixedRate(this::change, 2000, 500, TimeUnit.MILLISECONDS);
//    }

    // Diagnostic code: todo: remove
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

    public void setSnippet(String content) throws IOException, BadLocationException {
        Element divElement = htmlDocument.getElement("Contents");
        htmlDocument.setInnerHTML(divElement, content);
        System.out.println("displaying " +content);
//        HtmlUtils.displayHtml(htmlDocument, null, 0);
        reapplyStyles();
    }


}
