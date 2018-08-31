package eu.davidea.samples.flexibleadapter.dialogs;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import eu.davidea.samples.flexibleadapter.R;

public class MessageDialog extends DialogFragment {

    public static final String TAG = MessageDialog.class.getSimpleName();
    public static final String ARG_ICON = "icon";
    public static final String ARG_TITLE = "title";
    public static final String ARG_MESSAGE = "message";

    public MessageDialog() {
    }

    /**
     * Use from Activities.
     *
     * @param icon    dialog icon
     * @param title   dialog title
     * @param message dialog message
     * @return a new instance of MessageDialog
     */
    public static MessageDialog newInstance(int icon, String title, String message) {
        return newInstance(icon, title, message, null);
    }

    /**
     * Use from Activities.
     *
     * @param icon     dialog icon
     * @param title    dialog title
     * @param message  dialog message
     * @param fragment target fragment
     * @return a new instance of MessageDialog
     */
    public static MessageDialog newInstance(int icon, String title, String message, Fragment fragment) {
        MessageDialog confirmDialog = new MessageDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_ICON, icon);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        confirmDialog.setArguments(args);
        if (fragment != null) confirmDialog.setTargetFragment(fragment, 0);
        return confirmDialog;
    }

    @SuppressLint("InflateParams")
    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        View dialogView = LayoutInflater.from(getActivity())
                                        .inflate(R.layout.dialog_message, null);

        TextView messageView = dialogView.findViewById(R.id.message);
        messageView.setMovementMethod(LinkMovementMethod.getInstance());
        messageView.setText(Html.fromHtml(getArguments().getString(ARG_MESSAGE)));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppTheme_AlertDialog);
        builder.setTitle(getArguments().getString(ARG_TITLE))
               .setIcon(getArguments().getInt(ARG_ICON))
               .setView(dialogView)
               .setPositiveButton(R.string.OK, new OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       dialog.dismiss();
                   }
               });

        return builder.create();
    }

}