package com.skyemarine.sartrek;

import net.rim.device.api.ui.*;
import net.rim.device.api.util.*;

/**
 * This class attempts to simplify the brain-dead method RIM provides for formatting
 * text in a RichTextField control. It takes as input a text string with embedded HTML-style
 * syntax. This includes only the following:
 * 
 *      <b>bold this section</b>
 *      <i>italicise this section</i>
 *      <u>underline this section</u>
 */
class RichTextFormatter {    
    // Attributes are defined with a bit mask so they can easily be combined
    private static final int ATTR_PLAIN = 0x0;
    private static final int ATTR_BOLD = 0x1;
    private static final int ATTR_ITALIC = 0x2;
    private static final int ATTR_UNDERLINED = 0x4;
    
    // Fonts are defined as an enumeration because they can't easily be combined
    private static final byte FONT_PLAIN = 0;
    private static final byte FONT_BOLD = 1;
    private static final byte FONT_ITALIC = 2;
    private static final byte FONT_UNDERLINED = 3;
    private static final byte FONT_BOLD_ITALIC = 4;
    private static final byte FONT_BOLD_UNDERLINED = 5;
    private static final byte FONT_ITALIC_UNDERLINED = 6;
    private static final byte FONT_BOLD_ITALIC_UNDERLINED = 7;

    
    /**
     * Generate a TextFormat object that can be used to update the text
     * in a RichTextField control.
     */
    public static TextFormat format(String rich) {
        boolean inBold = false;
        boolean inItalic = false;
        boolean inUnderlined = false;
        IntVector offsets = new IntVector();
        IntVector attributes = new IntVector();
        String text = "";
        
        if (rich == null)
            rich = "";
            
        offsets.addElement(0);
        attributes.addElement(ATTR_PLAIN);
      
        for (int i = 0; i < rich.length(); ) {
            int addAttr = ATTR_PLAIN;
            int remAttr = ATTR_PLAIN;
            
            if (!inBold && rich.length() >= i + 3 && rich.substring(i, i + 3).equals("<b>")) {
                addAttr = ATTR_BOLD;
                inBold = true;
                i += 3;
            }
            else if (inBold && rich.length() >= i + 4 && rich.substring(i, i + 4).equals("</b>")) {
                remAttr = ATTR_BOLD;
                inBold = false;
                i += 4;
            }
            else if (!inItalic && rich.length() >= i + 3 && rich.substring(i, i + 3).equals("<i>")) {
                addAttr = ATTR_ITALIC;
                inItalic = true;
                i += 3;
            }
            else if (inItalic && rich.length() >= i + 4 && rich.substring(i, i + 4).equals("</i>")) {
                remAttr = ATTR_ITALIC;
                inItalic = false;
                i += 4;
            }
            else if (!inUnderlined && rich.length() >= i + 3 && rich.substring(i, i + 3).equals("<u>")) {
                addAttr = ATTR_UNDERLINED;
                inUnderlined = true;
                i += 3;
            }
            else if (inUnderlined && rich.length() >= i + 4 && rich.substring(i, i + 4).equals("</u>")) {
                remAttr = ATTR_UNDERLINED;
                inUnderlined = false;
                i += 4;
            }

            if (addAttr != ATTR_PLAIN || remAttr != ATTR_PLAIN) {
                int attr = attributes.lastElement();
                attr |= addAttr;
                attr ^= remAttr;               

                // Check for multiple entries at the same offset and merge them into one
                if (offsets.lastElement() == text.length()) {
                    attributes.setElementAt(attr, attributes.size() - 1);
                }
                else {             
                    offsets.addElement(text.length());
                    attributes.addElement(attr);
                }
            }
            else
                text += rich.charAt(i++);
        }
        if (offsets.lastElement() == text.length() && attributes.size() > 0)
            attributes.removeElementAt(attributes.size() - 1);
        else
            offsets.addElement(text.length());

        // Convert vectors to arrays
        TextFormat format = new TextFormat();
        format.text = text;
        format.offsets = new int[offsets.size()];
        for (int i = 0; i < offsets.size(); i++)
            format.offsets[i] = offsets.elementAt(i);
        format.attributes = new byte[attributes.size()];
        for (int i = 0; i < attributes.size(); i++) {
            switch (attributes.elementAt(i)) {
                case ATTR_PLAIN: format.attributes[i] = FONT_PLAIN; break;
                case ATTR_BOLD: format.attributes[i] = FONT_BOLD; break;
                case ATTR_ITALIC: format.attributes[i] = FONT_ITALIC; break;
                case ATTR_UNDERLINED: format.attributes[i] = FONT_UNDERLINED; break;
                case ATTR_BOLD | ATTR_ITALIC: format.attributes[i] = FONT_BOLD_ITALIC; break;
                case ATTR_BOLD | ATTR_UNDERLINED: format.attributes[i] = FONT_BOLD_UNDERLINED; break;
                case ATTR_ITALIC | ATTR_UNDERLINED: format.attributes[i] = FONT_ITALIC_UNDERLINED; break;
                case ATTR_BOLD | ATTR_ITALIC | ATTR_UNDERLINED: format.attributes[i] = FONT_BOLD_ITALIC_UNDERLINED; break;
            }
        }
        
        return format;
    }
    
    /**
     * A container for the arcane format fields required by RichTextField.
     */
    public static class TextFormat {
        String text;
        int[] offsets;
        byte[] attributes;
        Font fonts[];
        
        // Pre-initialize the fonts array
        {
            fonts = new Font[] {                
                Font.getDefault().derive(Font.PLAIN),
                Font.getDefault().derive(Font.BOLD),
                Font.getDefault().derive(Font.ITALIC),
                Font.getDefault().derive(Font.UNDERLINED),
                Font.getDefault().derive(Font.BOLD | Font.ITALIC),
                Font.getDefault().derive(Font.BOLD | Font.UNDERLINED),
                Font.getDefault().derive(Font.ITALIC | Font.UNDERLINED),
                Font.getDefault().derive(Font.BOLD | Font.ITALIC | Font.UNDERLINED),
                /*
                Font.getDefault().derive(Font.BOLD, Font.getDefault().getHeight() + 2),
                Font.getDefault().derive(Font.ITALIC, Font.getDefault().getHeight() + 2),
                Font.getDefault().derive(Font.UNDERLINED, Font.getDefault().getHeight() + 2),
                Font.getDefault().derive(Font.BOLD | Font.ITALIC, Font.getDefault().getHeight() + 2),
                Font.getDefault().derive(Font.BOLD | Font.UNDERLINED, Font.getDefault().getHeight() + 2),
                Font.getDefault().derive(Font.ITALIC | Font.UNDERLINED, Font.getDefault().getHeight() + 2),
                Font.getDefault().derive(Font.BOLD | Font.ITALIC | Font.UNDERLINED, Font.getDefault().getHeight() + 2),

                MobileNavigator.BaseFont.derive(Font.PLAIN),
                MobileNavigator.BaseFont.derive(Font.BOLD, Font.getDefault().getHeight() + 2),
                MobileNavigator.BaseFont.derive(Font.ITALIC, 0),
                MobileNavigator.BaseFont.derive(Font.UNDERLINED, 0),
                MobileNavigator.BaseFont.derive(Font.BOLD | Font.ITALIC, 0),
                MobileNavigator.BaseFont.derive(Font.BOLD | Font.UNDERLINED, 0),
                MobileNavigator.BaseFont.derive(Font.ITALIC | Font.UNDERLINED, 0),
                MobileNavigator.BaseFont.derive(Font.BOLD | Font.ITALIC | Font.UNDERLINED, 0),
                */
            };
        }
    }
} 
