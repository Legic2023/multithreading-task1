import java.util.*;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        List<Future<Integer>> threads = new ArrayList<>();
        String[] texts = new String[25];

        // т.к. на моем процессоре не так много ядер, создадим пул из 6 процессов
        final ExecutorService threadPool = Executors.newFixedThreadPool(6);

        // заполняем массив строк
        for (int i = 0; i < texts.length; i++) {
            texts[i] = generateText("aab", 30_000);
        }

        long startTs = System.currentTimeMillis(); // start time
        for (String text : texts) {

            // описываем действие для потока в Callable
            Callable<Integer> callable = () -> {
                int maxSize = 0;
                for (int i = 0; i < text.length(); i++) {
                    for (int j = 0; j < text.length(); j++) {
                        if (i >= j) {
                            continue;
                        }
                        boolean bFound = false;
                        for (int k = i; k < j; k++) {
                            if (text.charAt(k) == 'b') {
                                bFound = true;
                                break;
                            }
                        }
                        if (!bFound && maxSize < j - i) {
                            maxSize = j - i;
                        }
                    }
                }
                System.out.println(text.substring(0, 100) + " -> " + maxSize);

                return maxSize;
            };

            // передаем Callable в Pool
            Future<Integer> intFuture = threadPool.submit(callable);
            threads.add(intFuture); // добавляем Future в List
        }

        // Считываем значения и находим максимальное
        int maxRange = 0;
        for (Future<Integer> intFuture : threads) {
            if (intFuture.get() > maxRange) {
                maxRange = intFuture.get();
            };
        }

        threadPool.shutdown(); // закрываем Pool
        long endTs = System.currentTimeMillis(); // end time

        System.out.printf("Максимальный интервал значений: %d%n", maxRange);
        System.out.println("Time: " + (endTs - startTs) + "ms");
    }

    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }
}