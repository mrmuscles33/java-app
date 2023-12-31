package fr.amr.servlet.test;

import fr.amr.filter.JSONRequest;
import fr.amr.filter.JSONResponse;
import fr.amr.utils.Logger;

public class TestServlet {

    private TestServlet() {
        super();
    }

    public void toInit(JSONRequest request, JSONResponse response) {
        Logger.log("TestServlet.toInit");
    }
}
