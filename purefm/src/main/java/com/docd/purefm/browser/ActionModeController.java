package com.docd.purefm.browser;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.DialogFragment;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.Toast;

import com.docd.purefm.R;
import com.docd.purefm.activities.BrowserActivity;
import com.docd.purefm.adapters.BrowserBaseAdapter;
import com.docd.purefm.dialogs.DeleteFilesDialog;
import com.docd.purefm.dialogs.FilePropertiesDialog;
import com.docd.purefm.dialogs.RenameFileDialog;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.utils.ClipBoard;

public final class ActionModeController {

    private final MultiChoiceModeListener multiChoiceListener;
    
    private final Activity activity;
    private AbsListView list;

    private ActionMode mode;

    public ActionModeController(final Activity activity) {
        this.activity = activity;
        this.multiChoiceListener = new MultiChoiceListener();
    }
    
    protected void finishActionMode() {
        if (this.mode != null) {
            this.mode.finish();
        }
    }
    
    public void setListView(AbsListView list) {
        if (this.mode != null) {
            this.mode.finish();
        }
        this.list = list;
        this.list.setMultiChoiceModeListener(this.multiChoiceListener);
    }

    private final class MultiChoiceListener implements MultiChoiceModeListener {

        final String selected = activity
                .getString(R.string._selected);

        @Override
        public boolean onPrepareActionMode(ActionMode mode,
                Menu menu) {
            menu.clear();
            activity.getMenuInflater().inflate(
                    R.menu.browser_contextual, menu);
            if (list.getCheckedItemCount() > 1) {
                menu.removeItem(android.R.id.edit);
                menu.removeItem(R.id.properties);
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            ActionModeController.this.mode = null;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            ActionModeController.this.mode = mode;
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode,
                MenuItem item) {
            final SparseBooleanArray items = list
                    .getCheckedItemPositions();
            switch (item.getItemId()) {
            case android.R.id.edit:
                for (int i = 0; i < items.size(); i++) {
                    final int key = items.keyAt(i);
                    if (items.get(key)) {
                        final DialogFragment rename = RenameFileDialog
                                .instantiate(mode, (GenericFile) list
                                        .getAdapter().getItem(key));
                        rename.show(activity.getFragmentManager(),
                                BrowserActivity.TAG_DIALOG);
                        return true;
                    }
                }
                return false;
                                
            case R.id.properties:
                for (int i = 0; i < items.size(); i++) {
                    final int key = items.keyAt(i);
                    if (items.get(key)) {
                        final DialogFragment prop = FilePropertiesDialog
                                .instantiate((GenericFile) list
                                            .getAdapter().getItem(key));
                        mode.finish();
                        prop.show(activity.getFragmentManager(),
                                BrowserActivity.TAG_DIALOG);
                        break;
                    }
                }
                return true;

            case R.id.menu_delete:
                final List<GenericFile> files1 = new LinkedList<GenericFile>();
                final BrowserBaseAdapter adapter = (BrowserBaseAdapter) list.getAdapter();
                for (int i = 0; i < items.size(); i++) {
                    final int key = items.keyAt(i);
                    if (items.get(key)) {
                        files1.add(adapter.getItem(key));
                    }
                }
                final DialogFragment dialog = DeleteFilesDialog.instantiate(mode, files1);
                dialog.show(activity.getFragmentManager(), BrowserActivity.TAG_DIALOG);
                return true;

            case android.R.id.cut:
                final GenericFile[] files = new GenericFile[list
                        .getCheckedItemCount()];
                int index = -1;
                for (int i = 0; i < items.size(); i++) {
                    final int key = items.keyAt(i);
                    if (items.get(key)) {
                        files[++index] = (GenericFile) list.getAdapter()
                                .getItem(key);
                    }
                }
                ClipBoard.cut(files);
                Toast.makeText(
                        activity,
                        activity.getString(R.string.cut_)
                                + (index + 1)
                                + activity
                                        .getString(R.string._files),
                        Toast.LENGTH_SHORT).show();
                mode.finish();
                return true;

            case android.R.id.copy:
                final GenericFile[] files2 = new GenericFile[list
                        .getCheckedItemCount()];
                int index1 = -1;
                for (int i = 0; i < items.size(); i++) {
                    final int key = items.keyAt(i);
                    if (items.get(key)) {
                        files2[++index1] = (GenericFile) list.getAdapter()
                                .getItem(key);
                    }
                }
                ClipBoard.copy(files2);
                Toast.makeText(
                        activity,
                        activity.getString(R.string.copied_)
                                + (index1 + 1)
                                + activity
                                        .getString(R.string._files),
                        Toast.LENGTH_SHORT).show();
                mode.finish();
                return true;
                
            case R.id.select_all:
                for (int i = 0; i < list.getCount(); i++) {
                    list.setItemChecked(i, true);
                }
                return true;

            default:
                return false;
            }
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode,
                int position, long id, boolean checked) {
            mode.setTitle(list.getCheckedItemCount() + selected);
            mode.invalidate();
        }
    };
}
