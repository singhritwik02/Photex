package com.ritwik.photex;

public class TemplateModel {
    String LINK;
    Long TIMES_USED;
    String NAME;
    String STYLE_CODE = null;

    public String getSTYLE_CODE() {
        return STYLE_CODE;
    }

    public void setSTYLE_CODE(String STYLE_CODE) {
        this.STYLE_CODE = STYLE_CODE;
    }

    public TemplateModel(String LINK, Long TIMES_USED) {
        this.LINK = LINK;
        this.TIMES_USED = TIMES_USED;
    }

    public TemplateModel() {
    }

    public String getNAME() {
        return NAME;
    }

    public void setNAME(String NAME) {
        this.NAME = NAME;
    }

    public String getLINK() {
        return LINK;
    }

    public void setLINK(String LINK) {
        this.LINK = LINK;
    }

    public Long getTIMES_USED() {
        return TIMES_USED;
    }

    public void setTIMES_USED(Long TIMES_USED) {
        this.TIMES_USED = TIMES_USED;
    }
}
