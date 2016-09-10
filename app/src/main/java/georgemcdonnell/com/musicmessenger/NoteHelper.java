package georgemcdonnell.com.musicmessenger;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by George on 23/11/2015.
 */
public class NoteHelper {

    private static final int MAX_NOTE_LENGTH = 4;
    public static final int QUAVER_DURATION = 1;
    public static final int B_FOUR_VALUE = 6;
    public static final String NOTE_PATTERN = "([1-8&&[^57]][A-G]#?b?[45] )*[1-8&&[^57]][A-G]#?b?[45]";
    public static final Pattern pattern
            = Pattern.compile(NOTE_PATTERN);

    public enum NoteType {
        NoteTypeQuaver, NoteTypeCrotchet, NoteTypeDottedCrotchet, NoteTypeMinim, NoteTypeDottedMinim, NoteTypeSemibreve
    }

    public static final Map<String, Integer> letterValues;
    static {
        Map<String, Integer> values = new HashMap<>();
        values.put("C", 0);
        values.put("D", 1);
        values.put("E", 2);
        values.put("F", 3);
        values.put("G", 4);
        values.put("A", 5);
        values.put("B", 6);
        letterValues = Collections.unmodifiableMap(values);
    }

    public static final Map<String, Double> frequencies;
    static {
        Map<String, Double> freqs = new HashMap<>();

        String[] noteNames = new String[] {"C4", "C#4", "Db4", "D4", "Eb4", "D#4", "E4", "F4", "F#4", "Gb4", "G4", "G#4", "Ab4", "A4", "A#4", "Bb4", "B4", "C5", "C#5", "Db5", "D5", "D#5", "Eb5", "E5", "F5", "F#5", "Gb5", "G5", "G#5", "Ab5", "A5"};
        Double[] noteFreqs = new Double[] {261.63, 277.18, 277.18, 293.66, 311.13, 311.13, 329.63, 349.23, 369.99, 369.99, 392.0, 415.3, 415.3, 440.0, 466.16, 466.16, 493.88, 523.25, 554.37, 554.37, 587.33, 622.25, 622.25, 659.26, 698.46, 739.99, 739.99, 783.99, 830.61, 830.61, 880.0};

        for (int i = 0; i < noteNames.length; i++) {
            String noteName = noteNames[i];
            Double noteFreq = noteFreqs[i];
            freqs.put(noteName, noteFreq);
        }

        frequencies = Collections.unmodifiableMap(freqs);
    }

    public ArrayList<Note> parseNotes(Context context, String notesSMS) {
        ArrayList<Note> notes = new ArrayList<>();
        String[] noteStrings = notesSMS.split(" ");

        for (String noteString : noteStrings) {
            int noteValue = Integer.valueOf(noteString.substring(0,1));
            int noteDuration = noteValue;
            if (noteValue <= 4) {
                noteValue--;
            } else {
                if (noteValue == 6) {
                    noteValue = 4;
                } else {
                    noteValue = 5;
                }
            }
            NoteType noteType = NoteType.values() [noteValue];
            String letter = noteString.substring(1, 2);
            int octave;
            boolean sharp = false;
            boolean flat = false;
            String freqIdentifier;
            if (noteString.length() == MAX_NOTE_LENGTH) { // contains accidental
                if (noteString.charAt(2) == '#') {
                    sharp = true;
                } else {
                    flat = true;
                }
                octave = Integer.valueOf(noteString.substring(3,4));
                freqIdentifier = noteString.substring(1,4);
            } else {
                octave = Integer.valueOf(noteString.substring(2,3));
                freqIdentifier = noteString.substring(1, 3);
            }

            int staveLevel = letterValues.get(letter);
            if (octave == 5) {
                staveLevel += 7;
            }

            notes.add(new Note(context, noteType, letter, octave, staveLevel, sharp, flat, freqIdentifier, noteDuration));
        }
        return notes;
    }
}
