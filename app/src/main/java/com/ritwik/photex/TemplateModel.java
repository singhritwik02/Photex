package com.ritwik.photex;

public class TemplateModel {
    String LINK;
    Long TIMES_USED;
    String NAME;

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
