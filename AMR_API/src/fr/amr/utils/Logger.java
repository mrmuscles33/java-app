package fr.amr.utils;

public class Logger {

    private Logger(){
        super();
    }

    public static void log(String message){
        System.out.println(message);
    }

    public static void log(Throwable throwable){
        throwable.printStackTrace();
    }
}
