/*
 * Copyright 2014 Yaroslav Mytkalyk
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.docd.purefm.ui.dialogs;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import com.docd.purefm.Environment;
import com.docd.purefm.Extras;
import com.docd.purefm.R;
import com.docd.purefm.commandline.CommandLine;
import com.docd.purefm.commandline.CommandMount;
import com.docd.purefm.commandline.CommandStat;
import com.docd.purefm.commandline.ShellHolder;
import com.docd.purefm.file.GenericFile;
import com.docd.purefm.utils.StatFsCompat;
import com.docd.purefm.utils.ThemeUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.List;
import java.util.Set;

public final class PartitionInfoDialog extends DialogFragment {
    
    public static DialogFragment instantiate(final GenericFile file) {
        final Bundle args = new Bundle();
        args.putSerializable(Extras.EXTRA_FILE, file);
        
        final PartitionInfoDialog d = new PartitionInfoDialog();
        d.setArguments(args);
        return d;
    }
    
    private GenericFile file;
    private GetFSTypeTask task;
    
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        final Bundle args = this.getArguments();
        this.file = (GenericFile) args.getSerializable(Extras.EXTRA_FILE);
    }
    
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity = this.getActivity();
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setIcon(ThemeUtils.getDrawable(activity, R.attr.action_info));
        builder.setTitle(R.string.menu_partition);
        builder.setView(this.getInfoView(activity));
        builder.setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        
        return builder.create();
        
    }
    
    public View getInfoView(final Activity activity) {
        final View v = activity.getLayoutInflater().inflate(R.layout.dialog_partition_info, null);
        final TextView title = (TextView) v.findViewById(R.id.location);
        final String path = file.getAbsolutePath();
        title.setText(path);
        
        final TextView fs = (TextView) v.findViewById(R.id.filesystem);
        final View fileSystemRow = v.findViewById(R.id.filesystem_row);
        if (Environment.hasBusybox()) {
            this.task = new GetFSTypeTask(fs, v.findViewById(android.R.id.progress), fileSystemRow);
        } else {
            fileSystemRow.setVisibility(View.GONE);
        }

        final StatFsCompat stat = new StatFsCompat(path);
        final long valueTotal = stat.getTotalBytes();
        final long valueAvail = stat.getAvailableBytes();
        final long valueUsed = valueTotal - valueAvail;
        
        final TextView total = (TextView) v.findViewById(R.id.total);
        if (valueTotal != 0L) {
            total.setText(FileUtils.byteCountToDisplaySize(valueTotal));
        }
        
        final TextView block = (TextView) v.findViewById(R.id.block_size);
        final long blockSize = stat.getBlockSizeLong();
        if (blockSize != 0L) {
            block.setText(Long.toString(blockSize));
        }
        
        final TextView free = (TextView) v.findViewById(R.id.free);
        if (valueTotal != 0L) {
            free.setText(FileUtils.byteCountToDisplaySize(
                    stat.getFreeBytes()));
        }
        
        final TextView avail = (TextView) v.findViewById(R.id.available);
        if (valueTotal != 0L) {
            avail.setText(FileUtils.byteCountToDisplaySize(valueAvail));
        }
        
        final TextView used = (TextView) v.findViewById(R.id.used);
        if (valueTotal != 0L) {
            final StringBuilder usage = new StringBuilder();
            usage.append(FileUtils.byteCountToDisplaySize(valueUsed));
            usage.append(' ');
            usage.append('(');
            usage.append(valueUsed * 100L / valueTotal);
            usage.append('%');
            usage.append(')');
            used.setText(usage.toString());
        }
        
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (this.task != null && this.task.getStatus() != AsyncTask.Status.RUNNING) {
            this.task.execute(this.file);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (this.task != null && this.task.getStatus() == AsyncTask.Status.RUNNING) {
            this.task.cancel(false);
        }
    }

    private static final class GetFSTypeTask extends AsyncTask<GenericFile, Void, String> {
        private final TextView mFsTextView;
        private final View mFileSystemRow;
        private final View mFileSystemProgress;

        GetFSTypeTask(@NotNull final TextView fsTextView,
                      @NotNull final View fileSystemProgress,
                      @NotNull final View fileSystemRow) {
            this.mFsTextView = fsTextView;
            this.mFileSystemProgress = fileSystemProgress;
            this.mFileSystemRow = fileSystemRow;
        }

        @Override
        protected String doInBackground(final GenericFile... params) {
            final String path = params[0].getAbsolutePath();
            final Set<CommandMount.MountOutput> mounts = CommandMount.listMountpoints(path);
            if (!mounts.isEmpty()) {
                for (final CommandMount.MountOutput o : mounts) {
                    if (path.startsWith(o.mountPoint)) {
                        return o.fileSystem;
                    }
                }
            }

            final List<String> fsTypeResult = CommandLine.executeForResult(ShellHolder.getShell(),
                    new CommandStat(params[0].getAbsolutePath()));
            return fsTypeResult == null || fsTypeResult.isEmpty() ?
                    null : fsTypeResult.get(0);
        }

        @Override
        protected void onPostExecute(final String result) {
            if (result != null) {
                mFileSystemProgress.setVisibility(View.GONE);
                mFsTextView.setVisibility(View.VISIBLE);
                mFsTextView.setText(result);
            } else {
                mFileSystemRow.setVisibility(View.GONE);
            }
        }
    }
}
