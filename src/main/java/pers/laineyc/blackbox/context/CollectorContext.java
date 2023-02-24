package pers.laineyc.blackbox.context;

public class CollectorContext {

    private CaseContext caseContext;

    private Boolean isDebug;

    public CollectorContext(CaseContext caseContext, Boolean isDebug) {
        this.caseContext = caseContext;
        this.isDebug = isDebug;
    }

    public Boolean getIsDebug() {
        return isDebug;
    }

    public CaseContext getCaseContext() {
        return caseContext;
    }

}
