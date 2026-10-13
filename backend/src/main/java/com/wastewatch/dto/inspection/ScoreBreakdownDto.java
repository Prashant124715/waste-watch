package com.wastewatch.dto.inspection;

public class ScoreBreakdownDto {

    private int wetWaste;
    private int dryWaste;
    private int sanitaryWaste;
    private int correctBins;
    private int labelledBins;
    private int noMixedWaste;

    public ScoreBreakdownDto() {
    }

    public ScoreBreakdownDto(int wetWaste, int dryWaste, int sanitaryWaste, int correctBins, int labelledBins, int noMixedWaste) {
        this.wetWaste = wetWaste;
        this.dryWaste = dryWaste;
        this.sanitaryWaste = sanitaryWaste;
        this.correctBins = correctBins;
        this.labelledBins = labelledBins;
        this.noMixedWaste = noMixedWaste;
    }

    public int getWetWaste() {
        return wetWaste;
    }

    public void setWetWaste(int wetWaste) {
        this.wetWaste = wetWaste;
    }

    public int getDryWaste() {
        return dryWaste;
    }

    public void setDryWaste(int dryWaste) {
        this.dryWaste = dryWaste;
    }

    public int getSanitaryWaste() {
        return sanitaryWaste;
    }

    public void setSanitaryWaste(int sanitaryWaste) {
        this.sanitaryWaste = sanitaryWaste;
    }

    public int getCorrectBins() {
        return correctBins;
    }

    public void setCorrectBins(int correctBins) {
        this.correctBins = correctBins;
    }

    public int getLabelledBins() {
        return labelledBins;
    }

    public void setLabelledBins(int labelledBins) {
        this.labelledBins = labelledBins;
    }

    public int getNoMixedWaste() {
        return noMixedWaste;
    }

    public void setNoMixedWaste(int noMixedWaste) {
        this.noMixedWaste = noMixedWaste;
    }
}
