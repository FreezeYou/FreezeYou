package cf.playhi.freezeyou.utils;

import android.content.Context;

import androidx.biometric.BiometricManager;

import net.grandcentrix.tray.AppPreferences;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

public class AuthenticationUtils {

    public static boolean isAuthenticationEnabled(Context context) {
        AppPreferences appPreferences = new AppPreferences(context);
        return appPreferences.getBoolean("enableAuthentication", false);
    }

    public static boolean isBiometricPromptPartAvailable(Context context) {
        BiometricManager biometricManager = BiometricManager.from(context);
        switch (biometricManager.canAuthenticate(
                BIOMETRIC_STRONG | BIOMETRIC_WEAK | DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                return true;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
            case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
            default:
                return false;
        }
    }
}
