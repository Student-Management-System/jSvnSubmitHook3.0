public class MyMain {

    private String test;
    public static final int VALUE = 0;
    
    public static void main(String[] args) {
        boolean b = true;
        // provoke warning
        if (b == true) {
            System.out.println("");
        }
    }

}
