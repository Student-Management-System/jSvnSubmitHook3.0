public class MyMain {

    public static final int VALUE = 0;
    private String test;

    public static void main(String[] args) {
        boolean b = true;
        // provoke warning
        if (b == true) {
            System.out.println("");
        }
    }

}
