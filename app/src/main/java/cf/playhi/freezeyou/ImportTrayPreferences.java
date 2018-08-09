package cf.playhi.freezeyou;

import android.content.Context;

import net.grandcentrix.tray.TrayPreferences;
import net.grandcentrix.tray.core.SharedPreferencesImport;

public class ImportTrayPreferences extends TrayPreferences {

    ImportTrayPreferences(final Context context) {
        super(context, context.getPackageName(), 1);
    }

    @Override
    protected void onCreate(int initialVersion) {
        super.onCreate(initialVersion);

        importSharedPreferences();
    }

    private void importSharedPreferences() {

        // migrate sharedPreferences in here.
        final SharedPreferencesImport New_FreezeOnceQuit = new SharedPreferencesImport(getContext(),
                "New_FreezeOnceQuit", "pkgName", getContext().getString(R.string.sFreezeOnceQuit));
        migrate(New_FreezeOnceQuit);

        final SharedPreferencesImport New_AutoFreezeApplicationList = new SharedPreferencesImport(getContext(),
                "New_AutoFreezeApplicationList", "pkgName", getContext().getString(R.string.sAutoFreezeApplicationList));
        migrate(New_AutoFreezeApplicationList);

        final SharedPreferencesImport New_OneKeyUFApplicationList = new SharedPreferencesImport(getContext(),
                "New_OneKeyUFApplicationList", "pkgName", getContext().getString(R.string.sOneKeyUFApplicationList));
        migrate(New_OneKeyUFApplicationList);

    }
}
