package com.docd.purefm.dialogs;

import java.util.List;

import com.docd.purefm.Extras;
import com.docd.purefm.R;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.tasks.DeleteTask;
import com.docd.purefm.text.style.DashSpan;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.view.ActionMode;
import android.widget.TextView;

public final class DeleteFilesDialog extends DialogFragment {
    
    public static DialogFragment instantiate(ActionMode mode, List<GenericFile> files) {
        final GenericFile[] extraFiles = new GenericFile[files.size()];
        files.toArray(extraFiles);
        final Bundle extras = new Bundle();
        extras.putSerializable(Extras.EXTRA_FILE, extraFiles);
        
        final DeleteFilesDialog dialog = new DeleteFilesDialog();
        dialog.setArguments(extras);
        dialog.mode = mode;
        
        return dialog;
    }
    
    private ActionMode mode;
    private GenericFile[] files;
    
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        final Bundle extras = this.getArguments();
        final Object[] o = (Object[]) extras.getSerializable(Extras.EXTRA_FILE);
        this.files = new GenericFile[o.length];
        for (int i = 0; i < o.length; i++) {
            this.files[i] = (GenericFile) o[i];
        }
    }
    
    @Override
    public Dialog onCreateDialog(Bundle state) {
        final Activity a = getActivity();
        final AlertDialog.Builder b = new AlertDialog.Builder(a);
        b.setTitle(R.string.dialog_delete_title);
        
        final TextView content = (TextView) a.getLayoutInflater()
                .inflate(R.layout.dialog_delete, null);
        content.setMovementMethod(new ScrollingMovementMethod());
        
        final StringBuilder fileList = new StringBuilder();
        for (int i = 0; i < this.files.length; i++) {
            fileList.append(this.files[i].getName());
            if (i != this.files.length - 1) {
                fileList.append('\n');
            }
        }
        final SpannableString ss = new SpannableString(fileList.toString());
        ss.setSpan(new DashSpan(), 0, ss.length(), 0);
        content.setText(ss);
        b.setView(content);
        
        b.setPositiveButton(R.string.menu_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mode != null) {
                    mode.finish();
                }
                dialog.dismiss();
                final DeleteTask task = new DeleteTask(getActivity());
                task.execute(files);
            }
        });
        b.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return b.create();
    }
}
