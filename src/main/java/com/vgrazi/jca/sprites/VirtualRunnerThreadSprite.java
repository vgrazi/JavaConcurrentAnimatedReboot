package com.vgrazi.jca.sprites;

import com.vgrazi.jca.util.ThreadUtils;
import com.vgrazi.jca.util.UIUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.util.Map;

/**
 * This is a regular thread sprite, except that when it is running (inside the monolith), it renders as a round rectangle
 * instead of as a thread
 */
public class VirtualRunnerThreadSprite<S> extends RunnerThreadSprite<S> {

    public static final Font CARRIER_FONT = new Font("Arial", Font.PLAIN, 24);
    @Autowired
    private UIUtils uiUtils;

//    String lastCarrier;
    @Override
    public void render(Graphics2D graphics) {
// Render a bounding box around where the rounded circle should be.
//        Graphics2D graphicsDebug = (Graphics2D) graphics.create();
//        graphicsDebug.setStroke(new BasicStroke(1));
//        graphicsDebug.setColor(Color.yellow);
//        graphicsDebug.drawRect(leftBound, topBound, width, runnerEllipseHeight);

        Color color = getThreadContext().getColor(this);
//        Color color = getColorByThreadState();
        graphics.setColor(color);
        if(isInMonolith()){
            graphics.drawArc(leftBound + getXOffset(), topBound, ellipseRadius * 2, ellipseRadius * 2, 90, 180);
            graphics.drawArc(rightBound + getXOffset()- ellipseRadius * 2, topBound, ellipseRadius * 2, ellipseRadius * 2, 270, 180);
            graphics.drawLine(lineStart + getXOffset(), topBound, lineEnd + getXOffset(), topBound);
            graphics.drawLine(lineStart + getXOffset(), bottomBound, lineEnd + getXOffset(), bottomBound);
            Map.Entry<String, String> entry = ThreadUtils.getVirtualToCarrierMapping(getThread());
            String carrier = "";
            if(entry != null) {
//                lastCarrier = entry.getValue().replaceAll("ForkJoinPool-\\d+-","");
                carrier = entry.getValue().replaceAll("ForkJoinPool-\\d+-","");
            }
//            else if(getThread().getState() != Thread.State.RUNNABLE){
//                carrier = "";
//            }
            if(carrier != null) {
                renderCarrier(graphics, rightBound, yPosition+height/2-5);
                Graphics graphics1 = graphics.create();
                graphics1.setFont(new Font("Arial", Font.PLAIN, 24));
//                graphics.drawString(carrier, lineEnd + getXOffset()+15, bottomBound );
            }
        }else{// && relativePosition != RelativePosition.At) {
            // render the runner thread before it enters the monolith
            int xPosition = getXPosition();
            int yPosition = getYPosition();
            graphics.drawLine(xPosition - arrowLength + getXOffset(), yPosition, xPosition + getXOffset(), yPosition);
            renderCarrier(graphics, xPosition, yPosition);
        }

        renderMessage(graphics);
        drawThreadCap(graphics);
        renderInterruptedFlag(graphics);
    }

    @Override
    protected void drawHead(Graphics2D graphics, int capOffset, int offset, int yPos) {
        Graphics graphics1 = graphics.create();
        graphics1.setFont(CARRIER_FONT);
        int xpos = 0;
        if(isInMonolith()) {
            xpos = getXPosition() + getXOffset() - 8 - offset + capOffset;
            yPos = yPos + 10;
        }
        else {
            xpos = getXPosition() + getXOffset() - offset + capOffset + 5;
            yPos += 18;

        }
        graphics1.drawString("V", xpos, yPos);
    }

    private void renderCarrier(Graphics2D graphics, int xPosition, int yPosition) {
        Map.Entry<String, String> entry = ThreadUtils.getVirtualToCarrierMapping(getThread());
//        String virtual = entry.getKey();
        String carrier = entry.getValue();

//            carrier = carrier.replaceAll("ForkJoinPool-\\d+-", "");
        if(!carrier.contains("ForkJoinPool")) {
            int debug = 0;
        } else {
            Graphics graphics1 = graphics.create();
            Color carrierColor = getThreadContext().getCarrierColor(carrier, this);
            graphics1.setColor(carrierColor);
            int xPos = isInMonolith() ? xPosition - arrowLength - 10 : xPosition - arrowLength - 30;
            graphics1.fill3DRect(xPos, yPosition + 3, arrowLength + 20, 12, true);
//            carrier = carrier.replaceAll("ForkJoinPool-\\d+-", "");
           graphics1.setFont(CARRIER_FONT);
           graphics.drawString(carrier, lineEnd + getXOffset() + 15, bottomBound);

           graphics1.dispose();
        }
    }


// leftBound     ___________________________________     rightBound
//    |       . |lineStart         ^        lineEnd|   .      |
//    |   .     |                  |               |      .   |
//    | .       |                  |               |        . |
//    |. _______|________________  | ______________|________ .|
//    |.        |                  |               |         .|
//    | .       |                  |               |        . |
//    |    .    |                  |               |      .   |
//    |        .| _________________V_______________|__ .      |

}
