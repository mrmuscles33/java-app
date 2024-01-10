package fr.amr.filter;

import fr.amr.structure.Pair;

import java.util.ArrayList;
import java.util.List;

public class SecurityMgr {

    private SecurityMgr() {
        super();
    }

    public static List<Pair<String, String>> getAuthorizedMethods(String user) {
        // TODO : load authorized methods from database
        return new ArrayList<>();
    }
}
