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
import java.awt.*;
import java.io.IOException;

@Component
public class SnippetCanvas extends JEditorPane {
    @Value("${snippet-font-family}")
    private String fontFamily;

    @Value("${snippet-font-style}")
    private String fontStyle;

    private StyleSheet styleSheet;

    private Element htmlElement;

    public SnippetCanvas() {
        setContentType("text/html");
        setOpaque(true);
        setBackground(Color.white);
    }

    /**
     * Sets the font size in px for the "outer" dimv
     */
    public void setFontSize(int fontSize) {
//        styleSheet.addRule(String.format(".outer{font-style:\"bold\";font-size:%d px;}", fontSize));
//        applyStyles();
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
//        println("Counter:" + counter);
//        if (counter == 4) {
//            println("Replacing");
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
