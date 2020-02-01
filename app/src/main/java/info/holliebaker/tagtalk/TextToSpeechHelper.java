package info.holliebaker.tagtalk;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class TextToSpeechHelper implements TextToSpeech.OnInitListener {
    public class QueueFullException extends Exception {
    }

    private Queue<String> speechQueue;
    private boolean ready = false;
    private TextToSpeech textToSpeech;

    public TextToSpeechHelper(Context context, int queueCapacity) {
        speechQueue = new ArrayBlockingQueue<String>(queueCapacity);
        textToSpeech = new TextToSpeech(context, this);
    }

    public void speak(String text) throws QueueFullException {
        if (!speechQueue.add(text)) {
            throw new QueueFullException();
        }

        this.processQueue();
    }

    @Override
    public void onInit(int status) {
        if (status != TextToSpeech.SUCCESS) {
            Log.d(MainActivity.TAG, "Error initialising text to speech");

            return;
        }

        ready = true;

        this.processQueue();
    }

    private void processQueue() {
        if (!ready) {
            return;
        }

        while (!speechQueue.isEmpty()) {
            textToSpeech.speak(speechQueue.remove(), TextToSpeech.QUEUE_ADD, null);
        }
    }
}
