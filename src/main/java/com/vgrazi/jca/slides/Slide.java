package com.vgrazi.jca.slides;

import com.vgrazi.jca.context.ThreadContext;
import com.vgrazi.jca.util.UIUtils;
import com.vgrazi.jca.view.SnippetCanvas;
import com.vgrazi.jca.view.ThreadCanvas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.vgrazi.jca.util.Parsers.parseFont;

/**
 * All slides extend this class
 */
public abstract class Slide {
    private Font snippetFont;
    @Autowired
    protected ThreadContext threadContext;

    @Value("${selected-font-color-style}")
    protected String selectedFontColorStyle;

    @Value("${deselected-font-color-style}")
    protected String deselectedFontColorStyle;

    @Autowired
    protected ThreadCanvas threadCanvas;

    @Autowired
    protected SnippetCanvas snippetCanvas;

    @Autowired
    private UIUtils uiUtils;

    private Set<String> styleSelectors;

    @Autowired
    protected JLabel messages;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    private JLabel imageLabel;

    @Autowired
    JPanel cardPanel;

    private final Pattern REPLACE_WHITE = Pattern.compile("^(\\s*)(.*)", Pattern.MULTILINE);
    private final Pattern CLASS_LOCATOR = Pattern.compile("class=[\"|'](.+?)[\"|']", Pattern.MULTILINE);
    private int state;
    @Value("${HTML_DISABLED_COLOR}")
    private String htmlDisabledColor;
    private String snippetText;
    private static float snippetFontSize;
    private String snippetFile;

    private Logger logger = Logger.getLogger(getClass().getSimpleName());

    public abstract void run();

    protected void setMessage(String message) {
        setMessage(message, Color.white);
    }

    protected void setMessage(String message, Color foreground) {
        this.messages.setForeground(foreground);
        this.messages.setText(message);
    }

    /**
     * Sets the specified selector as selected, and everything else unselected
     */
    protected void setCssSelected(String selectedSelector) {
        // todo: Change to set state
    }

    public void reset() {
        threadContext.reset();
        threadCanvas.hideMonolith(false);
        this.messages.setText("");
        setMessage("     ");
        imageLabel.setIcon(null);
        setState(0);
        ((CardLayout) cardPanel.getLayout()).first(cardPanel);
    }

    public void setSnippetFile(String snippetFile) {
        // prevent resetting the snippet file, to eliminate flicker
        if (!snippetFile.equals(this.snippetFile) || snippetFontSize != snippetFont.getSize()) {
            this.snippetFile = snippetFile;
            Resource resource = new ClassPathResource("snippets/" + snippetFile);
            Set<String> styleSelectors = null;
            if (resource.exists()) {
                try {

                    styleSelectors = setSnippetResource(resource);
                } catch (IOException | BadLocationException e) {
                    e.printStackTrace();
                }
            }
            setState(0);
        }
    }

    /**
     * Reads the snippet from the specified filename, and adds a <br> to the end of every line, and replaces leading whitespace with &nbsp;
     * Returns a Set of the CSS class style selectors from the file
     * @throws IOException if IO Exception or bad HTML parsing
     */
    private Set<String> setSnippetResource(Resource resource) throws IOException, BadLocationException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
        String line;
        StringBuilder builder = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            builder.append(line).append("\n");
        }

        String snippetFileContent = builder.toString();
        Set<String> styles = extractStyleSelectors(snippetFileContent);
        this.snippetText = snippetFileContent;

        return styles;
    }

    /**
     * Finds all instances of class="xxx" or class='xxx'
     */
    private Set<String> extractStyleSelectors(String snippetFileContent) {
        Set<String> styles = new HashSet<>();
        Matcher matcher = CLASS_LOCATOR.matcher(snippetFileContent);
        while (matcher.find()) {
            styles.add(matcher.group(1));
        }
        return styles;
    }

    protected void setImage(String imageFile) {
        uiUtils.setImage(imageFile, imageLabel);
    }

    /**
     * Sets the state and redraws the snippet. State == -1 colors the entire snippet. State == 0 colors the constructor, etc
     *
     * @param state -1 colors the entire snippet. 0 colors the constructor, etc
     */
    public void setState(int state) {
        setMessage("");
        this.state = state;
        String snippet = getSnippet();
        snippet = applyState(state, snippet);
        getSnippetLabel().setText(snippet);
        // scroll the snippet scroll pane to the top left
        getSnippetLabel().setCaretPosition(0);
    }

    public void println(Object message) {
        logger.info(String.valueOf(message));
    }

    public void printf(String message, Object... params) {
        String formatted = String.format(message, params);
        println(formatted);
    }

    protected String getSnippet() {
        String snippetText = this.snippetText == null?"":this.snippetText;
        int fontSize = snippetFont.getSize();
        return "<html><head><style type=\"text/css\"> \n" +
                "pre{font-size:" + fontSize + ";}\n" +
                ".default { font-weight: bold}\n" +
                ".keyword { color: rgb(0,0,200); font-weight: bold; }\n" +
                ".highlight { color: rgb(0,0,0); background-color: yellow; font-weight: normal; }\n" +
                ".literal { color: rgb(0,0,255); font-weight: bold}\n" +
                ".comment { color: rgb(150,150,150);}\n" +
                ".unselected { color: rgb(128,128,128); }\n" +
                "</style> \n" +
                "</head>\n" +
                "<BODY BGCOLOR=\"#ffffff\" vertical-align='top'><p>\n" +
                "<pre>" + snippetText +
                "</pre></p></body>\n" +
                "</html>";
    }

    private SnippetCanvas getSnippetLabel() {
        snippetCanvas.setFont(snippetFont);
        return snippetCanvas;
    }

    private String applyState(int state, String snippet) {
//        println("ConcurrentExample.applyState " + state);
        if (snippet != null) {
            if (state == -1) {
                snippet = snippet.replaceAll("<state\\d:(#\\d\\d\\d\\d\\d\\d)>", "$1");
            } else {
                snippet = snippet.replaceAll("<state" + state + ":(#\\d\\d\\d\\d\\d\\d)>", "$1");
                snippet = snippet.replaceAll("<state\\d:(#\\d\\d\\d\\d\\d\\d)>", htmlDisabledColor);
            }
            // in order to change the size of the selected font, include a size css font style as follows: font-size:state2-size
            // the state number (in this example state2) corresponds to the state parameter
            if (state >= 0) {
                snippet = snippet.replaceAll(String.format("state%d-size", state), "24pt");
                snippet = snippet.replaceAll(String.format("state[~%d]-size", state), "21pt");
            }

            // for newer html output, intelliJ is spitting out css. The default css class is .s9
            // Look for <state2:s1> if state == 2 convert that to s1 else s9
//          println(snippet);
            snippet = snippet.replaceAll("<state" + state + "\\:(\\w*)>", "$1");
            snippet = snippet.replaceAll("<state\\d:(s\\w+)>", "s9");
            snippet = snippet.replaceAll("<state\\d:(\\w+)>", "unselected");
//          "<format state=3, class=\"keyword\"/>int </format>"

// we support comma separated ids, eg <0 keyword> or <0,12 default>
            snippet = snippet.replaceAll(String.format("<(\\d+,)*%d(,\\d+)*\\s+(\\w+)>", state), String.format("</span><span class=\"%s\">", "$3"));
            snippet = snippet.replaceAll("<(\\d+,)*\\d+(,\\d+)*\\s+(\\w+)>", "</span><span class=\"unselected\">");

        }
        return snippet;
    }

    @Value("${snippet-font}")
    private void setSnippetFont(String fontString) {
        this.snippetFont = parseFont(fontString);
        if(snippetFontSize == 0) {
            snippetFontSize = 18;
        }
        if(snippetFontSize != snippetFont.getSize()) {
            setSnippetFontSize(snippetFontSize);
        }
    }

    public void setSnippetFontSize(float size) {
        this.snippetFontSize = size;
        snippetFont = snippetFont.deriveFont(size);
        setState(state);
    }
}
