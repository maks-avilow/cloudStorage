package serial;

import java.io.*;

public class TestSerialization {
    public static void main(String[] args) throws IOException, ClassNotFoundException {

        TransferObject object = new TransferObject("tag", "message");
        System.out.println(object);
        ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream("data/object.txt"));
        os.writeObject(object);

        ObjectInputStream is = new ObjectInputStream(new FileInputStream("data/object.txt"));
        TransferObject object1 = (TransferObject) is.readObject();
        System.out.println(object1);
    }
}
