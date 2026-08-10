package com.vgrazi.jca.sprites;

import com.vgrazi.jca.util.ThreadUtils;

import java.awt.*;
import java.util.Map;

/**
 * Virtual thread sprite that renders a character orbiting around an oval track inside the monolith.
 * Extends OrbitalRunnerThreadSprite to share common orbital animation logic.
 */
public class VirtualRunnerThreadSprite<S> extends OrbitalRunnerThreadSprite<S> {
    public final String character;
    private final int horizontalShift;

    public VirtualRunnerThreadSprite(int horizontalShift, String character) {
        this(horizontalShift, character, false);
    }

    public VirtualRunnerThreadSprite(int horizontalShift, String character, boolean animateWithoutCarrier) {
        super(animateWithoutCarrier);
        this.horizontalShift = horizontalShift;
        this.character = character;
    }

    public VirtualRunnerThreadSprite(int horizontalShift, String character) {
        this.horizontalShift = horizontalShift;
        this.character = character;
    }

//    String lastCarrier;
    @Override
    protected Color getOvalColor() {
        return getThreadContext().getColor(this);
    }

    @Override
    public void render(Graphics2D graphics) {
        // Call parent to render the oval and other common elements
        super.render(graphics);
        // Note: renderCarrier is handled in drawHead for VirtualRunnerThreadSprite
        if(isInMonolith()){
            String carrier = ThreadUtils.getCarrier(this.getThread());
            if(carrier != null) {
                renderCarrier(graphics, rightBound, yPosition+height/2-5);
            }
        } else {
            renderCarrier(graphics, -100+getXPosition(), getYPosition());
        }
    }

    protected void drawHead(Graphics2D graphics, int capOffset, int offset, int yPos) {
        Graphics2D graphics1 = (Graphics2D) graphics.create();
        graphics1.setFont(CARRIER_FONT);
        if (isInMonolith()) {
            OrbitalPosition pos = calculateOrbitalPosition();
            graphics1.drawString(character, (int) Math.round(pos.x) + horizontalShift, (int) Math.round(pos.y) + 8);
        } else {
            int xpos = getXPosition() + getXOffset() - offset + capOffset + 5;
            graphics1.drawString(character, -100 + xpos + horizontalShift, yPos + 18);
        }

        graphics1.dispose();
    }

    private void renderCarrier(Graphics2D graphics, int xPosition, int yPosition) {
        Map.Entry<String, String> entry = ThreadUtils.getVirtualToCarrierMapping(getThread());
        String carrier = entry.getValue();

        if(carrier.contains("ForkJoinPool")) {
            Graphics graphics1 = graphics.create();
            Color carrierColor = getThreadContext().getCarrierColor(carrier, this);
            graphics1.setColor(carrierColor);
            int xPos = -100 + (isInMonolith() ? xPosition - arrowLength - 10 : xPosition - arrowLength - 30);
            graphics1.fill3DRect(xPos, yPosition + 3, arrowLength + 20, 12, true);
            graphics1.setFont(CARRIER_FONT);
            graphics1.drawString(carrier, lineEnd + getXOffset() + 25-100, yPosition + 3 + 12 + 2);

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
