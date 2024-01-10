package fr.amr.utils;

public class Logger {

    public static final String FINE = "FINE";
    public static final String INFO = "INFO";
    public static final String WARNING = "WARNING";
    public static final String SEVERE = "SEVERE";

    private Logger(){
        super();
    }

    public static void log(String message){
        System.out.println(message);
    }

    public static void log(String level, String message){
        log("[" + level + "] - " + message);
    }

    public static void trace(String message){
        trace(INFO, message);
    }

    public static void trace(String level, String message){
        log(level, DateUtils.toString(DateUtils.now()) + " - " + message);
    }

    public static void fine(String message){
        trace(FINE, message);
    }

    public static void info(String message){
        trace(INFO, message);
    }

    public static void warn(String message){
        trace(WARNING, message);
    }

    public static void error(String message){
        trace(SEVERE, message);
    }

    public static void log(Throwable throwable){
        throwable.printStackTrace();
    }
}
