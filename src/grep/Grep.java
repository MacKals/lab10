package grep;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/** Search web pages for lines matching a pattern. */
public class Grep {
    public static void main(String[] args) throws Exception {
        
        final int CONSUMERS = 10;
        
        // substring to search for
        String substring = "EECE 210";
        
        // URLs to search
        String[] urls = new String[] {
                "http://eece210.ece.ubc.ca/",
                "http://github.com/EECE-210/lab2",
                "http://github.com/EECE-210/mp1",
        };
        
        // list for accumulating matching lines
        List<Text> matches = Collections.synchronizedList(new ArrayList<Text>());
        
        // queue for sending lines from producers to consumers
        BlockingQueue<Line> queue = new LinkedBlockingQueue<Line>();
        
        Thread[] producers = new Thread[urls.length]; // one producer per URL
        Thread[] consumers = new Thread[CONSUMERS];
        
        for (int ii = 0; ii < consumers.length; ii++) { // start Consumers
            Thread consumer = consumers[ii] = new Thread(new Consumer(substring, queue, matches));
            consumer.start();
        }
        
        for (int ii = 0; ii < urls.length; ii++) { // start Producers
            Thread producer = producers[ii] = new Thread(new Producer(urls[ii], queue));
            producer.start();
        }
        
        for (Thread producer : producers) { // wait for Producers to stop
            producer.join();
        }
        
        // stop Consumers
        // ...
        // ...
        
        for (Thread consumer : consumers) { // wait for Consumers to stop
            consumer.join();
        }
        
        for (Text match : matches) {
            System.out.println(match);
        }
        System.out.println(matches.size() + " lines matched");
    }
}

class Producer implements Runnable {
    
    private final String url;
    private final BlockingQueue<Line> queue;
    
    Producer(String url, BlockingQueue<Line> queue) {
        this.url = url;
        this.queue = queue;
    }

    public void run() {
        
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
            String line;
            int lineNumber = 0;
            while ((line = in.readLine()) != null) { 
                queue.put( new Text(url, ++lineNumber, line));
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
}

class Consumer implements Runnable {
    
    private final String substring;
    private final BlockingQueue<Line> queue;
    private final List<Text> matches;
    
    Consumer(String substring, BlockingQueue<Line> queue, List<Text> matches) {
        this.substring = substring;
        this.queue = queue;
        this.matches = matches;
    }

    public void run() {
        //take lines from producers off the queue and add matches to the list
               
        try {
            
            Text line = (Text) queue.take();
            if (line.toString().contains(substring)) {
                matches.add(line);
            }
            
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

interface Line {
    /** @return the filename. */
    public String filename();
    /** @return the line number. */
    public int lineNumber();
    /** @return the text on the line. */
    public String text();
}

class Text implements Line {
    private final String filename;
    private final int lineNumber;
    private final String text;
    
    public Text(String filename, int lineNumber, String text) {
        this.filename = filename;
        this.lineNumber = lineNumber;
        this.text = text;
    }
    
    public String filename() {
        return filename;
    }
    
    public int lineNumber() {
        return lineNumber;
    }
    
    public String text() {
        return text;
    }
    
    @Override public String toString() {
        return filename + ":" + lineNumber + ":" + text;
    }
}
