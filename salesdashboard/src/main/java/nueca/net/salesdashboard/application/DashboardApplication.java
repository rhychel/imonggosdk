package nueca.net.salesdashboard.application;


import nueca.net.salesdashboard.tools.FontsOverride;

public class DashboardApplication extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/AvenirNext-Regular.ttf");
    }
}
