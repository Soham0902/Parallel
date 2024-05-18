import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class ParallelQuickSortServer {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new java.net.InetSocketAddress(8000), 0);
        server.createContext("/parallelquicksort", new ParallelQuickSortHandler());
        server.createContext("/sequentialquicksort", new SequentialQuickSortHandler());
        server.setExecutor(null); 
        server.start();
        System.out.println("Server started on port 8000.");
    }

    static class ParallelQuickSortHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                // Allow cross-origin requests
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

                int size = Integer.parseInt(exchange.getRequestURI().getQuery().split("=")[1]);
                int[] arr = generateRandomArray(size);
                long startTime = System.currentTimeMillis();
                parallelQuickSort(arr, 0, arr.length - 1);
                long endTime = System.currentTimeMillis();
                long sortingTime = endTime - startTime;
                String response = Long.toString(sortingTime); // Send sorting time as response
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }
    }

    static class SequentialQuickSortHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                // Allow cross-origin requests
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

                int size = Integer.parseInt(exchange.getRequestURI().getQuery().split("=")[1]);
                int[] arr = generateRandomArray(size);
                long startTime = System.currentTimeMillis();
                quickSort(arr, 0, arr.length - 1);
                long endTime = System.currentTimeMillis();
                long sortingTime = endTime - startTime;
                String response = Long.toString(sortingTime); // Send sorting time as response
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }
    }

    private static int[] generateRandomArray(int size) {
        int[] arr = new int[size];
        for (int i = 0; i < size; i++) {
            arr[i] = (int) (Math.random() * size * 10); // generating random numbers between 0 and size*10
        }
        return arr;
    }

    private static void parallelQuickSort(int[] arr, int low, int high) {
        if (high - low < 10000) {
            Arrays.sort(arr, low, high+1);
        } else {
            int mid = partition(arr, low, high);
            Thread leftThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    parallelQuickSort(arr, low, mid - 1);
                }
            });
            Thread rightThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    parallelQuickSort(arr, mid + 1, high);
                }
            });
            leftThread.start();
            rightThread.start();
            try {
                leftThread.join();
                rightThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void quickSort(int[] arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);
            quickSort(arr, low, pi - 1);
            quickSort(arr, pi + 1, high);
        }
    }

    private static int partition(int[] arr, int low, int high) {
        int pivot = arr[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (arr[j] < pivot) {
                i++;
                swap(arr, i, j);
            }
        }
        swap(arr, i + 1, high);
        return i + 1;
    }

    private static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}
