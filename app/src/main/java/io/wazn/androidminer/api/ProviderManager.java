package io.wazn.androidminer.api;

import java.util.ArrayList;
import io.wazn.androidminer.Config;

public final class ProviderManager {

    static private ArrayList<PoolItem> mPools = new ArrayList<PoolItem>();

    static public void add(PoolItem poolItem) {
        mPools.add(poolItem);
    }

    static public void add(String key, String pool,String port, int poolType, String poolUrl, String poolIP) {
        mPools.add(new PoolItem(key, pool, port, poolType, poolUrl, poolIP));
    }

    static public void add(String key, String pool, String port, int poolType, String poolUrl, String poolIP, String poolApi) {
        mPools.add(new PoolItem(key, pool, port, poolType, poolUrl, poolIP, poolApi, "",""));
    }

    static public PoolItem[] getPools() {
        return mPools.toArray(new PoolItem[mPools.size()]);
    }

    static public PoolItem getPoolById(int idx) {
        return mPools.get(idx);
    }

    static public PoolItem getPoolById(String idx) {
        int index = Integer.parseInt(idx);

        if (idx.equals("") || mPools.size() < index || mPools.size() == 0) {
            return null;
        }

        return mPools.get(index);
    }

    static final public ProviderData data = new ProviderData();

    static public PoolItem getSelectedPool() {
        if(request.mPoolItem != null) {
            return request.mPoolItem;
        }

        String sp = Config.read("selected_pool");
        if (sp.equals("")) {
            return null;
        }

        return getPoolById(sp);
    }
    static public void afterSave() {
        if(request.mPoolItem != null)  {
            return;
        }

        PoolItem pi = getSelectedPool();
        if(pi == null) {
            return;
        }

        //mPools.clear();
        request.mPoolItem = pi;
        data.isNew = true;
        request.start();
    }

    static final public ProviderRequest request = new ProviderRequest();

    static public void generate() {
        request.stop();
        request.mPoolItem = null;
        //mPools.clear();

        if(!mPools.isEmpty())
            return;

        // User Defined
        add("custom", "custom", "3333", 0, "custom", "");

        //  Wazn Official pool
        add(
                "Wazn Official pool",
                "pool.wazn.io",
                "4444",
                2, // CryptonoteNodeJS
                "https://pool.wazn.io",
                "157.90.229.215",
                "https://dig.wazn.io:14911/stats"
        );

        // Wazniya Wazn Pool
        add(
                "Wazniya Wazn Pool",
                "pool.wazniya.com",
                "3003",
                2, // CryptonoteNodeJS
                "https://pool.wazniya.com",
                "95.217.185.212",
                "https://dig.wazniya.com:15911/stats"
        );

        // Wazn IoT Proxy
        add(
                "Wazn IoT Proxy",
                "",
                "",
                3, // IoT Proxy
                "",
                "",
                ""
        );

    }
}
