package com.vgrazi.jca.sprites;

import com.vgrazi.jca.util.ThreadUtils;

import java.awt.*;
import java.util.Map;

/**
 * This is a regular thread sprite, except that when it is running (inside the monolith), it renders as a round rectangle
 * instead of as a thread
 */
public class VirtualRunnerThreadSprite<S> extends RunnerThreadSprite<S> {

    public static final Font CARRIER_FONT = new Font("Arial", Font.PLAIN, 24);

//    String lastCarrier;
    @Override
    public void render(Graphics2D graphics) {
// Render a bounding box around where the rounded circle should be.
//        Graphics2D graphicsDebug = (Graphics2D) graphics.create();
//        graphicsDebug.setStroke(new BasicStroke(1));
//        graphicsDebug.setColor(Color.yellow);
//        graphicsDebug.drawRect(leftBound, topBound, width, runnerEllipseHeight);

        Color color = getThreadContext().getColor(this);
        graphics.setColor(color);
        if(isInMonolith()){
            graphics.drawArc(-100+leftBound + getXOffset(), topBound, ellipseRadius * 2, ellipseRadius * 2, 90, 180);
            graphics.drawArc(-100+rightBound + getXOffset()- ellipseRadius * 2, topBound, ellipseRadius * 2, ellipseRadius * 2, 270, 180);
            graphics.drawLine(-100+lineStart + getXOffset(), topBound, -100 + lineEnd + getXOffset(), topBound);
            graphics.drawLine(-100+lineStart + getXOffset(), bottomBound, -100 + lineEnd + getXOffset(), bottomBound);
            renderCarrier(graphics, rightBound, yPosition+height/2-5);
            Graphics graphics1 = graphics.create();
            graphics1.setFont(new Font("Arial", Font.PLAIN, 24));
//                graphics.drawString(carrier, lineEnd + getXOffset()+15, bottomBound );
        }else{// && relativePosition != RelativePosition.At) {
            // render the runner thread before it enters the monolith
            int xPosition = getXPosition();
            int yPosition = getYPosition();
            graphics.drawLine(-100+xPosition - arrowLength + getXOffset(), yPosition, xPosition + getXOffset(), yPosition);
            renderCarrier(graphics, -100+xPosition, yPosition);
        }

        renderMessage(graphics);
        drawThreadCap(graphics);
        renderInterruptedFlag(graphics);
    }

    @Override
    protected void drawHead(Graphics2D graphics, int capOffset, int offset, int yPos) {
        Graphics graphics1 = graphics.create();
        graphics1.setFont(CARRIER_FONT);
        int xpos;
        if(isInMonolith()) {
            xpos = getXPosition() + getXOffset() - 8 - offset + capOffset;
            yPos = yPos + 10;
        }
        else {
            xpos = getXPosition() + getXOffset() - offset + capOffset + 5;
            yPos += 18;

        }
        graphics1.drawString("v", -100+xpos, yPos);
        graphics1.dispose();
    }

    private void renderCarrier(Graphics2D graphics, int xPosition, int yPosition) {
        Map.Entry<String, String> entry = ThreadUtils.getVirtualToCarrierMapping(getThread());
//        String virtual = entry.getKey();
        String carrier = entry.getValue();
        String virtual = entry.getKey();
        Graphics graphics1 = graphics.create();
        graphics1.setFont(CARRIER_FONT);
//            carrier = carrier.replaceAll("ForkJoinPool-\\d+-", "");
      if(carrier.contains("ForkJoinPool")) {
            Color carrierColor = getThreadContext().getCarrierColor(carrier, this);
            graphics1.setColor(carrierColor);
            int xPos = -100 + (isInMonolith() ? xPosition - arrowLength - 10 : xPosition - arrowLength - 30);
            graphics1.fill3DRect(xPos, yPosition + 3, arrowLength + 20, 12, true);
//            carrier = carrier.replaceAll("ForkJoinPool-\\d+-", "");
           graphics1.drawString(carrier, lineEnd + getXOffset() + 25-100, yPosition + 3 + 12 + 2-2);
        }
      // render virtual thread id
        graphics1.drawString(virtual, lineEnd + getXOffset() + 25-100, yPosition + 3 + 12 + 2 - 30+2);
        graphics1.dispose();
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
