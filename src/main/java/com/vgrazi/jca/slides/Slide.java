package com.vgrazi.jca.slides;

import com.vgrazi.jca.context.ThreadContext;
import com.vgrazi.jca.view.SnippetCanvas;
import com.vgrazi.jca.view.ThreadCanvas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * All slides extend this class
 */
public abstract class Slide {
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

    private Set<String> styleSelectors;

    @Autowired
    protected JLabel messages;

    @Autowired
    ApplicationContext applicationContext;

    private final Pattern REPLACE_WHITE = Pattern.compile("^(\\s*)(.*)", Pattern.MULTILINE);
    private final Pattern CLASS_LOCATOR = Pattern.compile("class=[\"|'](.+?)[\"|']", Pattern.MULTILINE);
    private String snippetFile;

    public abstract void run();

    /**
     * Sets all of the css style selectors, extracted from the snippet file
     */
    protected void setStyleSelectors(Set<String> styleSelectors) {
        this.styleSelectors = styleSelectors;
    }

    /**
     * Sets the specified selectorS as selected, and everything else unselected
     */
    protected void setCssSelected(String... selectorsArray) {
        Set<String> selectors = new HashSet<>(Arrays.asList(selectorsArray));
        styleSelectors.forEach(selector-> {
            if(selectors.contains(selector)) {
                threadContext.addStyleRule("." + selector + selectedFontColorStyle);
            }
            else {
                threadContext.addStyleRule("." + selector + deselectedFontColorStyle);
            }
        });
        snippetCanvas.applyStyles();
    }

    protected void setMessage(String message) {
        this.messages.setText(message);
    }

    /**
     * Sets the specified selector as selected, and everything else unselected
     */
    protected void setCssSelected(String selectedSelector) {
        styleSelectors.forEach(selector-> {
            if(selectedSelector.equals(selector)) {
                threadContext.addStyleRule("." + selector + selectedFontColorStyle);
            }
            else {
                threadContext.addStyleRule("." + selector + deselectedFontColorStyle);
            }
        });
        threadContext.addStyleRule(".keyword {color:blue}");

        snippetCanvas.applyStyles();
    }

    public void reset() {
        threadContext.reset();
        threadCanvas.hideMonolith(false);
        this.messages.setText("");
    }

    /**
     * Selects all of this slide's selectors, enabling all fonts
     */
    private void resetCss() {
        if (styleSelectors != null) {
            styleSelectors.forEach(selector-> threadContext.addStyleRule("." + selector + selectedFontColorStyle));
        }
        snippetCanvas.applyStyles();
    }

    public void setSnippetFile(String snippetFile) {
        try {
            Resource[] resources = applicationContext.getResources("classpath*:/" + snippetFile);
            for (Resource resource:resources) {
                Set<String> styleSelectors = null;
                if (resource.exists()) {
                    try {

                        styleSelectors = setSnippetResource(resource);
                    } catch (IOException | BadLocationException e) {
                        e.printStackTrace();
                    }
                    setStyleSelectors(styleSelectors);
                    resetCss();
                    break;
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
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
        while((line = reader.readLine()) != null) {
            builder.append(line).append("\n");
        }

        String snippetFileContent = builder.toString();
        Set<String> styles = extractStyleSelectors(snippetFileContent);
        // replace leading white space with &nbsp;
        prepareSnippet(snippetFileContent);

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

    /**
     * loads the snippet file into the canvas, replaces spaces with &nbsp; and ends all lines with <br/>\n
     */
    private void prepareSnippet(String snippetFileContent) throws IOException, BadLocationException {
        Matcher matcher = REPLACE_WHITE.matcher(snippetFileContent);
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            String blanks = matcher.group(1);
            String spaces = blanks.replaceAll(" ", "&nbsp;");
            builder.append(spaces).append(matcher.group(2)).append("<br/>\n");
            // would prefer to use <pre>$1</pre> but apparently this is not supported :(
//                snippetFileContent = matcher.replaceAll(spaces + "$1");
        }
        String snippet = builder.toString();
        // surround keywords with <span class='keyword'></span>.
//        snippet = snippet.replaceAll("(\\b(try|catch|finally|while|if|else|boolean|synchronized)\\b(?!['\"]))", "<span class='keyword'>$1</span>");
//        System.out.println(snippet);
        snippetCanvas.setSnippet(snippet);
    }


}
