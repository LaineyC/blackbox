package pers.laineyc.blackbox.tool;

import pers.laineyc.blackbox.model.Case;

public final class CaseTool {

    public static String buildUri(Case case_){
        return buildUri(case_.getName());
    }

    public static String buildUri(String... items){
        StringBuilder builder = new StringBuilder();
        for(int i = 0, n = items.length; i < n; i++) {
            builder.append(items[i]);
            if (i != n - 1) {
                builder.append(":");
            }
        }
        return builder.toString();
    }

}
