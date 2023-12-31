package fr.amr.filter;

import fr.amr.structure.Pair;

import java.util.List;

public class SecurityMgr {

    private SecurityMgr() {
        super();
    }

    public static List<Pair> getAuthorizedMethods(String user) {
        return (List<Pair>) Context.get("authorizedMethods");
    }
}
