package info.holliebaker.tagtalk;

import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Parcelable;
import android.speech.tts.TextToSpeech;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.Format;

public class NfcProcessor {
    private static final String TEXT_PLAIN = "text/plain";

    public class MessogeWriteException extends Exception {
        public MessogeWriteException(String message) {
            super(message);
        }

        public MessogeWriteException(Throwable cause) {
            super(cause);
        }
    }

    public class InvalidMessageException extends Exception {
        public InvalidMessageException(String message) {
            super(message);
        }
    }

    public NdefMessage[] getMessages(Intent intent) throws InvalidMessageException {
        Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

        if (rawMessages == null) {
            throw new InvalidMessageException(
                    "No messages found in EXTRA_NDEF_MESSAGES"
            );
        }

        NdefMessage[] messages = new NdefMessage[rawMessages.length];
        for (int i = 0; i < rawMessages.length; i++) {
            messages[i] = (NdefMessage) rawMessages[i];
        }

        return messages;
    }

    public String getMessagePlainText(NdefMessage message) throws InvalidMessageException, UnsupportedEncodingException {
        NdefRecord record = message.getRecords()[0];

        String mimeType = record.toMimeType();
        if (mimeType == null || !mimeType.equals(TEXT_PLAIN)) {
            throw new InvalidMessageException(
                    "Invalid mime type of " + (mimeType != null ? mimeType : "null") +
                            "expected " + TEXT_PLAIN
            );
        }

        byte[] payload = record.getPayload();
        if (payload.length == 0) {
            throw new InvalidMessageException(
                    "Record has empty payload"
            );
        }

        String encoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
        int languageCodeLength = payload[0] & 0063;

        return new String(
                payload,
                languageCodeLength + 1,
                payload.length - languageCodeLength - 1,
                encoding
        );
    }

    public void writeMessagePlainText(Tag tag, String text) throws MessogeWriteException {
        boolean written = false;
        String[] techList = tag.getTechList();

        for (String tech : techList) {
            if (tech.equals(Ndef.class.getName())) {
                NdefRecord[] records = new NdefRecord[1];
                records[0] = NdefRecord.createTextRecord("en", text);

                NdefMessage message = new NdefMessage(records);
                Ndef ndef = Ndef.get(tag);
                try {
                    ndef.connect();
                    ndef.writeNdefMessage(message);
                    ndef.close();

                    written = true;
                } catch (FormatException e) {
                    throw new MessogeWriteException((Throwable) e);
                } catch (IOException e) {
                    throw new MessogeWriteException((Throwable) e);
                }
            }
        }

        if (!written) {
            throw new MessogeWriteException(
                    "No messages have been written."
            );
        }
    }
}
