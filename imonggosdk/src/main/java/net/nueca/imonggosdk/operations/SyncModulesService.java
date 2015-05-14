package net.nueca.imonggosdk.operations;

import android.content.Intent;
import android.os.IBinder;

import net.nueca.imonggosdk.enums.Server;
import net.nueca.imonggosdk.enums.Table;
import net.nueca.imonggosdk.objects.User;

/**
 * Created by rhymart on 5/13/15.
 * imonggosdk (c)2015
 */
public class SyncModulesService extends ImonggoService {

    public static boolean isRequesting = true;

    private User user;
    private Server server;

    private int page = 1, responseCode = 200, count = 0;

    private boolean syncAllModules = true, syncSelectedModules = false,
            isSecured = false, initialSync = false, isActiveOnly = true;

    private Table[] tablesToSync;
    private int[] branches;
    private int branchIndex = 0;

    private Table tableSyncing;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
