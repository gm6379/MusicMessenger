package georgemcdonnell.com.musicmessenger;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

public class NotesView extends View {

    private final int STEMMED_NOTE_HEIGHT = 110;
    private final int ACCIDENTAL_HEIGHT = 50;
    private final int ACCIDENTAL_WIDTH = 25;
    private final int SEMIBREVE_NOTE_HEIGHT = 40;

    private ArrayList<Note> notes;

    public NotesView(Context context) {
        super(context);
    }

    public NotesView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NotesView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setNotes(ArrayList<Note> notes) {
        this.notes = notes;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (notes != null) {
            int paddingTop = getPaddingTop();
            int paddingBottom = getPaddingBottom();

            double contentHeight = getHeight() - paddingTop - paddingBottom;

            Drawable sharp = null;
            Drawable flat = null;
            Drawable accidental = null;
            Resources resources = null;

            int left = 0;
            int right = 0;

            for (int i = 0; i < notes.size(); i++) {
                Note note = notes.get(i);

                Drawable noteImage = note.noteImage();
                int anchor = (int) (contentHeight - ((contentHeight / 13) * note.getStaveLevel()));

                if (note.isSharp()) {
                    if (resources == null) {
                        resources = getContext().getResources();
                    }
                    if (sharp == null) {
                        sharp = ContextCompat.getDrawable(getContext(), R.drawable.sharp);
                    }

                    accidental = sharp;
                } else if (note.isFlat()) {
                    if (resources == null) {
                        resources = getContext().getResources();
                    }
                    if (flat == null) {
                        flat = ContextCompat.getDrawable(getContext(), R.drawable.flat);
                    }

                    accidental = flat;
                }

                right += 50;

                if (note.isFlat() || note.isSharp()) {
                    if (note.isAboveMiddle()) {
                        accidental.setBounds(left, anchor - (ACCIDENTAL_HEIGHT - 5), right - ACCIDENTAL_WIDTH, anchor + 5);
                    } else {
                        accidental.setBounds(left, anchor - ACCIDENTAL_HEIGHT, right - ACCIDENTAL_WIDTH, anchor);
                    }
                    accidental.draw(canvas);
                    right += ACCIDENTAL_WIDTH;
                    left += ACCIDENTAL_WIDTH;
                }
                if (!note.isStemmedNote()) {
                    noteImage.setBounds(left, anchor - (SEMIBREVE_NOTE_HEIGHT - 10), right, anchor + 10);
                } else {
                    if (note.isAboveMiddle()) {
                        anchor -= 25;
                        noteImage.setBounds(left, anchor, right, anchor + STEMMED_NOTE_HEIGHT);
                    } else {
                        noteImage.setBounds(left, anchor - STEMMED_NOTE_HEIGHT, right, anchor);
                    }
                }

                noteImage.draw(canvas);

                left = right;
            }
        }
    }
}
