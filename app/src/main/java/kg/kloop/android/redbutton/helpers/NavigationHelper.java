package kg.kloop.android.redbutton.helpers;

/**
 * Created by alexwalker on 16.10.2017.
 */

public class NavigationHelper {

    private static int selectedItemId;

    public static void setSelectedItemId(int selectedItemId) {
        NavigationHelper.selectedItemId = selectedItemId;
    }
    public static int getSelectedItemId(){
        return selectedItemId;
    }
}
