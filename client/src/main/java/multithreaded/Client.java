package multithreaded;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.stream.IntStream;

public class Client {

    public static final int THREADS_COUNT = 10;
    public static final int PORT = 9997;

    public static void main(String[] args) {
        IntStream.range(0, THREADS_COUNT)
                .mapToObj(i -> new Thread(() -> {
                    try (Socket socket = new Socket("localhost", PORT);
                         BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
                        writer.write("select student where id >= 1 order by id limit 2 join subject on id = id\n");
                        writer.flush();
                        final String s = reader.readLine();
                        System.out.println(s);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }))
                .peek(Thread::run)
                .forEach(thread -> {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
    }
}