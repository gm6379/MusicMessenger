package georgemcdonnell.com.musicmessenger;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import georgemcdonnell.com.musicmessenger.NoteHelper.NoteType;


/**
 * Created by George on 22/11/2015.
 */
public class Note {

    // example valid note 2D4
    // example valid note 8F#4

    private Context context;
    private NoteType type;
    private String letter;
    private int octave;
    private boolean sharp;
    private boolean flat;
    private int staveLevel;
    private boolean aboveMiddle;
    private boolean stemmedNote;
    private double frequency;
    private int duration;
    private Drawable image;

    public Note(Context context, NoteType type, String letter, int octave, int staveLevel, boolean sharp, boolean flat, String frequencyIdentifier, int duration) {
        this.context = context;
        this.type = type;
        this.letter = letter;
        this.octave = octave;
        this.sharp = sharp;
        this.flat = flat;
        this.staveLevel = staveLevel;
        this.aboveMiddle = aboveMiddle();
        this.image = noteImage();
        this.stemmedNote = hasStem();
        this.frequency = frequency(frequencyIdentifier);
        this.duration = duration * NoteHelper.QUAVER_DURATION;
    }

    public Drawable noteImage() {
        Resources res = context.getResources();
        String identifier;
        if (!aboveMiddle || type.ordinal() == 5) { // semibreve is the exception to the rule
            identifier = "note_" + String.valueOf(type.ordinal());
        } else {
            identifier = "note_" + String.valueOf(type.ordinal()) + "_1";
        }
        int resourceId = res.getIdentifier(identifier, "drawable",
                context.getPackageName());
        return ContextCompat.getDrawable(context, resourceId);
    }

    public boolean isAboveMiddle() {
        return aboveMiddle;
    }

    public int getStaveLevel() {
        return staveLevel;
    }

    public double getFrequency() {
        return frequency;
    }

    public boolean isSharp() {
        return sharp;
    }

    public boolean isFlat() {
        return flat;
    }

    public boolean isStemmedNote() {
        return stemmedNote;
    }

    public int getDuration() {
        return duration;
    }

    private boolean aboveMiddle() {
        if (staveLevel >= NoteHelper.B_FOUR_VALUE) {
            return true;
        } else {
            return false;
        }
    }

    private boolean hasStem() {
        if (this.type.equals(NoteType.NoteTypeSemibreve)) {
            return false;
        } else {
            return true;
        }
    }

    private double frequency(String identifier) {
        return NoteHelper.frequencies.get(identifier);
    }
}
