package com.bokecc.vod.data;

public class DefinitionInfo {
    private String definitionText;
    private int definition;
    private boolean isSelected;

    public DefinitionInfo(String definitionText, int definition, boolean isSelected) {
        this.definitionText = definitionText;
        this.definition = definition;
        this.isSelected = isSelected;
    }

    public String getDefinitionText() {
        return definitionText;
    }

    public void setDefinitionText(String definitionText) {
        this.definitionText = definitionText;
    }

    public int getDefinition() {
        return definition;
    }

    public void setDefinition(int definition) {
        this.definition = definition;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
