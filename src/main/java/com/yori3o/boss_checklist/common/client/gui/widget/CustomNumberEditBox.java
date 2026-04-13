package com.yori3o.boss_checklist.common.client.gui.widget;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.network.chat.Component;



public class CustomNumberEditBox extends EditBox {

    
    private boolean allowNegative = true;
    private boolean allowDecimal = true;

    private double step = 1.0;
    

    public CustomNumberEditBox(
            Font font, int x, int y, int width, int height, Component text, boolean allowNegative, boolean allowDecimal
    ) {
        super(font, x, y, width, height, text);
        this.allowNegative = allowNegative;
        this.allowDecimal = allowDecimal;
    }

    public CustomNumberEditBox(
            Font font, int x, int y, int width, int height, Component text
    ) {
        super(font, x, y, width, height, text);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (!this.canConsumeInput()) return false;

        char c = (char) event.codepoint();

        if (isAllowedChar(c)) {
            this.insertText(Character.toString(c));
            return true;
        }

        return false;
    }

    private boolean isAllowedChar(char c) {
        if (Character.isDigit(c)) return true;

        if (!allowDecimal && c == '.') return false;
        if (!allowNegative && c == '-') return false;

        return (allowDecimal && c == '.') || (allowNegative && c == '-');
    }

    @Override
    public void insertText(String input) {
        String filtered = filter(input);
        super.insertText(filtered);
    }

    private String filter(String text) {
        StringBuilder sb = new StringBuilder();

        for (char c : text.toCharArray()) {
            if (isAllowedChar(c)) {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    public void setAllowNegative(boolean value) {
        this.allowNegative = value;
    }

    public void setAllowDecimal(boolean value) {
        this.allowDecimal = value;
    }

    public double getDoubleValue() {
        try {
            return Double.parseDouble(this.getValue());
        } catch (Exception e) {
            return 0;
        }
    }

    public int getIntValue() {
        return (int) getDoubleValue();
    }

    public void setDoubleValue(double value) {
        this.setValue(String.valueOf(value));
    }

    public void setIntValue(int value) {
        this.setValue(String.valueOf(value));
    }

    @Override
    public void setValue(String value) {
        super.setValue(value);

        if (!allowDecimal) {
            try {
                int i = Integer.parseInt(this.getValue());
                super.setValue(String.valueOf(i));
            } catch (Exception ignored) {}
        }
    }

    public void onScroll(double amount) {
        if (!this.isFocused()) return;

        double value = getDoubleValue();

        if (amount > 0) value += getStep();
        else if (amount < 0) value -= getStep();

        if (!allowNegative) value = Math.max(0, value);

        setValue(format(value));
    }

    public double getStep() {
        if (Minecraft.getInstance().hasShiftDown()) {
            return step * 10;
        }
        return step;
    }

    public void setStep(double step) {
        this.step = step;
    }

    private String format(double value) {
        if (!allowDecimal) {
            return String.valueOf((int) value);
        }
        return String.valueOf(value);
    }

}